package io.shaka.jetty

import io.shaka.http.Http.http
import io.shaka.http.Request.GET
import io.shaka.http.Status.OK
import io.shaka.jetty.EmbeddedJetty.ToLog
import org.eclipse.jetty.server.{AbstractNCSARequestLog, RequestLog}
import org.scalatest.FunSuite

import scala.collection.mutable

class EmbeddedJettyLoggingSpec extends FunSuite {

  test("Override the logging") {
    val logMessages = mutable.ListBuffer[String]()

    val logSpy: ToLog = (message) ⇒ logMessages += message

    val jetty = EmbeddedJetty.jetty(JettyConfiguration(), logSpy)

    try {
      jetty.start()
      assert(logMessages.exists(m ⇒ m.startsWith("EMBEDDED JETTY")))
    } finally jetty.stop()
  }

  test("Override the request logging") {
    val logMessages = mutable.ListBuffer[String]()

    val requestLogSpy: RequestLog = new AbstractNCSARequestLog {
      override def isEnabled = true

      override def write(s: String) = logMessages += s
    }

    val jetty = EmbeddedJetty.jetty(JettyConfiguration(), EmbeddedJetty.printlnLog, requestLogSpy)

    try {
      jetty.start()
      val response = http(GET(s"http://localhost:${jetty.port}"))
      assert(response.status === OK)
      assert(logMessages.exists(m ⇒ m.contains(""""GET / HTTP/1.1" 200""")))
    } finally jetty.stop()
  }
}
