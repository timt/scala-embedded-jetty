package io.shaka.jetty

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import io.shaka.http.Http.HttpHandler
import io.shaka.http.HttpHeader.httpHeader
import io.shaka.http.Method.method
import io.shaka.http._

object Handlers {

  def adaptToJettyHandler(handler: HttpHandler): org.eclipse.jetty.server.handler.AbstractHandler =
    new org.eclipse.jetty.server.handler.AbstractHandler {
      override def handle(target: String, baseRequest: org.eclipse.jetty.server.Request, servletRequest: HttpServletRequest, servletResponse: HttpServletResponse): Unit = {
        val response = handler(adaptFromHttpServletRequest(servletRequest))
        adaptToHttpServletResponse(servletResponse, response)
        baseRequest.setHandled(true)
      }

      private def adaptFromHttpServletRequest(servletRequest: HttpServletRequest): Request = {
        import scala.collection.JavaConverters._
        Request(
          method(servletRequest.getMethod),
          servletRequest.getRequestURI,
          Headers(servletRequest.getHeaderNames.asScala.toList.flatMap(name => servletRequest.getHeader(name).split(",").toList.map(httpHeader(name) -> _.trim))),
          optionalEntity(Stream.continually(servletRequest.getInputStream.read).takeWhile(-1 !=).map(_.toByte).toArray)
        )
      }

      private def optionalEntity(bytes: Array[Byte]) = if (bytes.length > 0) Some(Entity(bytes)) else None

      private def adaptToHttpServletResponse(servletResponse: HttpServletResponse, response: Response): Unit = {
        servletResponse.setStatus(response.status.code)
        response.headers.headers.groupBy(_._1).foreach {
          case (key, values) => servletResponse.addHeader(key.name, values.map(_._2).mkString(","))
        }
        response.entity.foreach(entity => {
          val out = servletResponse.getOutputStream
          out.write(entity.content)
          out.close()
        })
      }

    }

}
