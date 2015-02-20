package io.shaka.jetty

import EmbeddedJetty.ToLog
import java.io.File
import org.eclipse.jetty.server.handler.{HandlerCollection, RequestLogHandler}
import org.eclipse.jetty.server.{HttpConfiguration, HttpConnectionFactory, NCSARequestLog, Server, ServerConnector}
import org.eclipse.jetty.webapp.WebAppContext

object EmbeddedJetty{
  type ToLog = String => Unit
  private val doNothingLog: ToLog = _ => ()
  def jetty: EmbeddedJetty = jetty(JettyConfiguration())
  def jetty(port: Int): EmbeddedJetty = jetty(JettyConfiguration(port = port))
  def jetty(config: JettyConfiguration, log: ToLog = doNothingLog): EmbeddedJetty = new EmbeddedJetty(config, log)
}

class EmbeddedJetty private(config: JettyConfiguration, otherLog: ToLog) {
  private val server = new Server()
  private val connector = createConnector
  server.setConnectors(Array(connector))
  server.setHandler(createHandlers)
  server.setStopAtShutdown(true)

  lazy val port = connector.getLocalPort

  def start() = {
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

  def stop() = {
    server.stop()
    this
  }


  private def createConnector = {
    val httpConfiguration = new HttpConfiguration()
    httpConfiguration.setOutputBufferSize(config.outputBufferSize)
    val connector = new ServerConnector(server, new HttpConnectionFactory(httpConfiguration))
    connector.setPort(config.port)
    connector.setIdleTimeout(config.idleTimeout)
    connector
  }


  private def createHandlers = {
    val context = {
      val warPath = new WebAppContext().getClass.getClassLoader.getResource("webapp").toExternalForm
      log(s"EMBEDDED JETTY >>> running webapp from $warPath")
      val ctx = new WebAppContext()
      ctx.setContextPath(config.context)
      ctx.setTempDirectory(new File(config.tempDirectory))
      log("EMBEDDED JETTY >>> using temp directory: " + ctx.getTempDirectory)
      ctx.setServer(server)
      ctx.setWar(warPath)
      ctx
    }

    val handlers = new HandlerCollection()
    handlers.setHandlers(Array(context, loggingHandler(handlers)))
    handlers
  }

  private def loggingHandler(handlers: HandlerCollection) = {
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

  private def log: ToLog = (message) => {
    otherLog(message)
    println(message)
  }
}
