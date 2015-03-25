package io.shaka.jetty

import java.io.File

import io.shaka.jetty.EmbeddedJetty.ToLog
import org.eclipse.jetty.server._
import org.eclipse.jetty.server.handler.{ContextHandler, ContextHandlerCollection, RequestLogHandler}
import org.eclipse.jetty.webapp.WebAppContext

object EmbeddedJetty {
  type ToLog = String => Unit
  private val doNothingLog: ToLog = _ => ()

  def jetty: EmbeddedJetty = jetty(JettyConfiguration())

  def jetty(port: Int): EmbeddedJetty = jetty(JettyConfiguration(port = port))

  def jetty(config: JettyConfiguration, log: ToLog = doNothingLog): EmbeddedJetty = new EmbeddedJetty(config, log)
}

class EmbeddedJetty private(config: JettyConfiguration, otherLog: ToLog) {
  private val server: Server = new Server()
  private val build: JettyComponentBuilder = JettyComponentBuilder(config, otherLog)
  private val handlers: ContextHandlerCollection = build.handlers

  private val httpConnector: ServerConnector = build.httpConnector(server)
  server.setConnectors(Array(httpConnector))
  server.setHandler(handlers)
  server.setStopAtShutdown(true)

  lazy val port = httpConnector.getLocalPort

  def start(): EmbeddedJetty = {
    val startedAt = System.nanoTime()
    try {
      server.start()
      while (server.isStarting) Thread.`yield`()
    } catch {
      case ex: Exception ⇒
        log(s"EMBEDDED JETTY >>> problem starting jetty! $ex")
        ex.printStackTrace()
        System.exit(100)
    }
    val elapsedTime = BigDecimal((System.nanoTime() - startedAt) / 1000000.0).formatted("%.2f")
    log(s"EMBEDDED JETTY >>> server started on port $port in $elapsedTime milli seconds")
    this
  }

  def stop(): EmbeddedJetty = {
    server.stop()
    this
  }

  def addJettyHandler(path: String, handler: Handler): EmbeddedJetty = {
    val contextHander = new ContextHandler(path)
    contextHander.setHandler(handler)
    handlers.addHandler(contextHander)
    if(server.isStarted) contextHander.start()
    this
  }

  private def log: ToLog = (message) => {
    otherLog(message)
    println(message)
  }
}

case class JettyComponentBuilder(config: JettyConfiguration, log: ToLog) {

  def httpConnector(server: Server) = {
    val httpConfiguration = new HttpConfiguration()
    httpConfiguration.setOutputBufferSize(config.outputBufferSize)
    val connector = new ServerConnector(server, new HttpConnectionFactory(httpConfiguration))
    connector.setPort(config.port)
    connector.setIdleTimeout(config.idleTimeout)
    connector
  }

  def webAppHandler: WebAppContext = {
    val warPath = config.webappLocation
    log(s"EMBEDDED JETTY >>> running webapp from $warPath")
    val webAppContext = new WebAppContext()
    webAppContext.setContextPath(config.context)
    webAppContext.setTempDirectory(new File(config.tempDirectory))
    log("EMBEDDED JETTY >>> using temp directory: " + webAppContext.getTempDirectory)
    webAppContext.setWar(warPath)
    webAppContext
  }

  def loggingHandler: RequestLogHandler = {
    val requestLog = new NCSARequestLog()
    new File(config.logsDirectory).mkdirs()
    requestLog.setFilename("logs/yyyy_mm_dd-request.log")
    requestLog.setFilenameDateFormat("yyyy_MM_dd")
    requestLog.setRetainDays(30)
    requestLog.setAppend(true)
    requestLog.setExtended(true)
    requestLog.setLogCookies(false)
    requestLog.setLogTimeZone("GMT")
    val requestLogHandler = new RequestLogHandler()
    requestLogHandler.setRequestLog(requestLog)
    requestLogHandler
  }

  def handlers = {
    val handlers = new ContextHandlerCollection()
    handlers.addHandler(loggingHandler)
    handlers.addHandler(webAppHandler)
    handlers
  }

}
