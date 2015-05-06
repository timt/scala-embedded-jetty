package io.shaka.jetty

import io.shaka.jetty.ContextConfiguration.{defaultContext, defaultTempDirectory}
import org.eclipse.jetty.webapp.WebAppContext

case class ContextConfiguration(
                                tempDirectory: String = defaultTempDirectory,
                                context: String = defaultContext,
                                webappLocation: String)

object ContextConfiguration {
  val defaultTempDirectory: String = "./tmp"
  val defaultContext: String = "/"

  def classpathResource(name: String): String =
    Option(new WebAppContext().getClass.getClassLoader.getResource(name)).map(_.toExternalForm).getOrElse(throw new RuntimeException(s"$name folder not found on classpath!"))
}
