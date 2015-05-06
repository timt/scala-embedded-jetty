package io.shaka.jetty

import io.shaka.jetty.JettyConfigurationDefaults.{defaultContextConfiguration, defaultIdleTimeout, defaultLogsDirectory, defaultOutputBufferSize, someFreePort}

case class JettyConfiguration(
                               port: Int = someFreePort,
                               logsDirectory: String = defaultLogsDirectory,
                               outputBufferSize: Int = defaultOutputBufferSize,
                               idleTimeout: Int = defaultIdleTimeout,
                               contexts: Traversable[ContextConfiguration] = defaultContextConfiguration)

object JettyConfigurationDefaults {
  val someFreePort = 0
  val defaultLogsDirectory: String = "./logs"
  val defaultOutputBufferSize = 5000000
  val defaultIdleTimeout = 30000
  def defaultContextConfiguration = Seq(ContextConfiguration(webappLocation = ContextConfiguration.classpathResource("webapp")))
}