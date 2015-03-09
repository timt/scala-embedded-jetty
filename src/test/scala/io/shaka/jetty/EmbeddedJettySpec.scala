package io.shaka.jetty

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import io.shaka.http.Http.http
import io.shaka.http.Request.GET
import io.shaka.http.Status.OK
import io.shaka.jetty.EmbeddedJetty.jetty
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import org.scalatest.FunSuite

class EmbeddedJettySpec extends FunSuite {
  jettyTest("can server content from 'webapp' folder on classpath") { jetty =>
    val response = http(GET(s"http://localhost:${jetty.port}"))
    assert(response.status === OK)
  }

  jettyTest("can add another context handler"){ jetty =>
    jetty.addHandler("/bob", new AbstractHandler() {
      override def handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse) = {
        response.setContentType("text/html; charset=utf-8")
        response.setStatus(HttpServletResponse.SC_OK)
        val out = response.getWriter()
        out.println("<h1>Hello World</h1>")
        baseRequest.setHandled(true)
      }
    })
    val response = http(GET(s"http://localhost:${jetty.port}/bob/"))
    assert(response.status === OK)
  }

  def jettyTest(testName: String)(block: (EmbeddedJetty) => Unit): Unit ={
    test(testName){
      val server = jetty.start()
      block(server)
      server.stop()
    }
  }
}
