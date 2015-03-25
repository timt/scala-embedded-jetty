package io.shaka.jetty

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import org.eclipse.jetty.http.HttpStatus.OK_200
import org.eclipse.jetty.server.handler.AbstractHandler

object Handlers {
  type Headers = Map[String, List[String]]
  type Entity = Option[Array[Byte]]

  case class Request(method: String, url: String, headers: Headers = Map.empty, entity: Entity = None)

  case class Response(status: Int = OK_200, headers: Headers = Map.empty, entity: Entity = None)

  type Handler = (Request) => Response
  
  def adaptToJettyHandler(handler: Handler) =
    new AbstractHandler {
      private def adaptFromHttpServletRequest(servletRequest: HttpServletRequest): Request = {
        Request(
          servletRequest.getMethod,
          servletRequest.getRequestURI)
      }

      override def handle(target: String, baseRequest: org.eclipse.jetty.server.Request, servletRequest: HttpServletRequest, servletResponse: HttpServletResponse): Unit = {
        val response = handler(adaptFromHttpServletRequest(servletRequest))
        adaptToHttpServletResponse(servletResponse, response)
        baseRequest.setHandled(true)
      }

      def adaptToHttpServletResponse(servletResponse: HttpServletResponse, response: Handlers.Response): Unit = {
        servletResponse.setStatus(response.status)
        response.headers.foreach { case (key, values) => servletResponse.addHeader(key, values.mkString(","))}
        response.entity.foreach(bytes => {
          val out = servletResponse.getOutputStream
          out.write(bytes)
          out.close()
        })
      }
    }
  
}
