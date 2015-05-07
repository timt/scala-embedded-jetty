package io.shaka.jetty

object JettyRunner extends App {

  private val jetty: EmbeddedJetty = EmbeddedJetty.jetty.start()

  sys.addShutdownHook {
    jetty.stop()
  }
}
