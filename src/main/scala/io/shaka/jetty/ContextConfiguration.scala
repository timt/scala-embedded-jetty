package io.shaka.jetty

import org.eclipse.jetty.webapp.WebAppContext
import ContextConfiguration._

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
