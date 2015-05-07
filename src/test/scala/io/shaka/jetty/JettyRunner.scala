package io.shaka.jetty

import io.shaka.http.Response.respond

object JettyRunner extends App {

  private val jetty: EmbeddedJetty = EmbeddedJetty.jetty

  jetty.addHandler("/bob", (request) => {
    respond("<h1>Hello Bob</h1>")
  })

  jetty.start()

  sys.addShutdownHook {
    jetty.stop()
  }
}
