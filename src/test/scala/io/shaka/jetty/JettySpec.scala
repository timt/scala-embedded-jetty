package io.shaka.jetty

import io.shaka.jetty.EmbeddedJetty.jetty
import org.scalatest.FunSuite

trait JettySpec extends FunSuite {
  def jettyTest(testName: String)(block: (EmbeddedJetty) => Unit): Unit ={
    test(testName){
      val server = jetty.start()
      block(server)
      server.stop()
    }
  }
}
