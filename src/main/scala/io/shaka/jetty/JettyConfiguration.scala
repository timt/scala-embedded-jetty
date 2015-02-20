package io.shaka.jetty

import JettyConfigurationDefaults.{defaultLogsDirectory, defaultContext, defaultOutputBufferSize, defaultIdleTimout, defaultTempDirectory, defaultWebappLocation}
import org.eclipse.jetty.webapp.WebAppContext

case class JettyConfiguration(
                               port: Int = 0,
                               tempDirectory: String = defaultTempDirectory,
                               logsDirectory: String = defaultLogsDirectory,
                               context: String = defaultContext,
                               webappLocation: String = defaultWebappLocation,
                               outputBufferSize: Int = defaultOutputBufferSize,
                               idleTimeout: Int = defaultIdleTimout)

object JettyConfigurationDefaults {
  val defaultTempDirectory: String = "./tmp"
  val defaultLogsDirectory: String = "./logs"
  val defaultContext: String = "/"
  val defaultWebappLocation: String = new WebAppContext().getClass.getClassLoader.getResource("webapp").toExternalForm
  val defaultOutputBufferSize = 5000000
  val defaultIdleTimout = 30000
}