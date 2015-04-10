package io.shaka.jetty

import JettyConfigurationDefaults._

case class JettyConfiguration(
                               port: Int = defaultPort,
                               logsDirectory: String = defaultLogsDirectory,
                               outputBufferSize: Int = defaultOutputBufferSize,
                               idleTimeout: Int = defaultIdleTimeout,
                               contexts: Traversable[ContextConfiguration] = defaultContextConfiguration)

object JettyConfigurationDefaults {
  val defaultPort = 0
  val defaultLogsDirectory: String = "./logs"
  val defaultOutputBufferSize = 5000000
  val defaultIdleTimeout = 30000
  def defaultContextConfiguration = Seq(ContextConfiguration(webappLocation = ContextConfiguration.classpathResource("webapp")))
}