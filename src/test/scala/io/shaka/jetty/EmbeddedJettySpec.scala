package io.shaka.jetty

import io.shaka.http.Http.http
import io.shaka.http.Request.GET
import io.shaka.http.Status.OK
import io.shaka.jetty.EmbeddedJetty.jetty
import org.scalatest.FunSuite

class EmbeddedJettySpec extends FunSuite {
  test("can server content from 'webapp' folder on classpath") {
    val server = jetty.start()
    val response = http(GET(s"http://localhost:${server.port}"))
    assert(response.status === OK)
    server.stop()
  }


}
