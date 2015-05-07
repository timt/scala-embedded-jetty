package io.shaka.jetty

import io.shaka.jetty.EmbeddedJetty.ToLog
import org.scalatest.FunSuite

import scala.collection.mutable

class EmbeddedJettyLoggingSpec extends FunSuite {

  test("Override the logging") {
    val logMessages = mutable.MutableList[String]()

    val logSpy: ToLog = (message) ⇒ logMessages += message

    val jetty = EmbeddedJetty.jetty(JettyConfiguration(), logSpy)

    try {
      jetty.start()
      assert(logMessages.exists(m ⇒ m.startsWith("EMBEDDED JETTY")))
    } finally jetty.stop()
  }
}
