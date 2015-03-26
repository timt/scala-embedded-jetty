package io.shaka.jetty

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import org.eclipse.jetty.http.HttpStatus.OK_200

object Handlers {
  type Headers = Map[String, List[String]]
  type Entity = Option[Array[Byte]]

  case class Request(method: String, url: String, headers: Headers = Map.empty, entity: Entity = None)

  case class Response(status: Int = OK_200, headers: Headers = Map.empty, entity: Entity = None) {
    def entityAsString = new String(entity.get)
  }

  object Response {
    val okResponse = Response(status = OK_200)
  }

  type Handler = (Request) => Response

  def adaptToJettyHandler(handler: Handler): org.eclipse.jetty.server.handler.AbstractHandler =
    new org.eclipse.jetty.server.handler.AbstractHandler {
      override def handle(target: String, baseRequest: org.eclipse.jetty.server.Request, servletRequest: HttpServletRequest, servletResponse: HttpServletResponse): Unit = {
        val response = handler(adaptFromHttpServletRequest(servletRequest))
        adaptToHttpServletResponse(servletResponse, response)
        baseRequest.setHandled(true)
      }

      private def adaptFromHttpServletRequest(servletRequest: HttpServletRequest): Request = {
        import scala.collection.JavaConverters._
        Request(
          servletRequest.getMethod,
          servletRequest.getRequestURI,
          servletRequest.getHeaderNames.asScala.toList.map(name => name -> servletRequest.getHeader(name).split(",").toList.map(_.trim)).toMap,
          someBytes(Stream.continually(servletRequest.getInputStream.read).takeWhile(-1 !=).map(_.toByte).toArray)
        )
      }

      private def someBytes(bytes: Array[Byte]) = if (bytes.length > 0) Some(bytes) else None

      private def adaptToHttpServletResponse(servletResponse: HttpServletResponse, response: Handlers.Response): Unit = {
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
