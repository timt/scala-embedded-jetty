package io.shaka.jetty

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

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
    def markHandled() = jettyBaseRequest.setHandled(true)
    def contentType(contentType: ContentType) = servletResponse.setContentType(contentType.value)
    def status(status: Status) = servletResponse.setStatus(status.code)
    def entity(entity: Entity):Unit = servletResponse.getOutputStream.write(entity.content)
    def entity(entity: String):Unit = this.entity(Entity(entity))

  }
}