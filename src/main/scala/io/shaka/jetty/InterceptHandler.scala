package io.shaka.jetty

import javax.servlet.http.{Cookie, HttpServletResponse, HttpServletRequest}

import io.shaka.http.{Entity, Status, ContentType}
import io.shaka.jetty.InterceptHandler.{RequestResponse, Interceptor}
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.HandlerWrapper
import org.eclipse.jetty.server.Handler

class InterceptHandler(underlying: Handler) extends HandlerWrapper {
  setHandler(underlying)
  private var intercept: Interceptor = InterceptHandler.passThroughInterceptor

  override def handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse): Unit = {
    intercept(RequestResponse(target, baseRequest, request, response))
    super.handle(target, baseRequest, request, response)
  }

  def withIntercept(intercept: Interceptor): Unit = {
    this.intercept = intercept
  }
}

object InterceptHandler {
  type Interceptor = (RequestResponse) => Unit
  val passThroughInterceptor: Interceptor = _ => ()
  case class RequestResponse(target: String, jettyBaseRequest: Request, servletRequest: HttpServletRequest, servletResponse: HttpServletResponse){
    def markHandled() = { jettyBaseRequest.setHandled(true); this }
    def contentType(contentType: ContentType) = { servletResponse.setContentType(contentType.value); this }
    def status(status: Status) = { servletResponse.setStatus(status.code); this }
    def entity(entity: Entity):RequestResponse = { servletResponse.getOutputStream.write(entity.content); this }
    def entity(entity: String):RequestResponse = this.entity(Entity(entity))
    def getRequestCookie(name: String): Option[String] = servletRequest.getCookies.find(_.getName == name).map(_.getValue)
    def cookie(name: String, value: String): RequestResponse = { servletResponse.addCookie(new Cookie(name, value)); this }
  }
}