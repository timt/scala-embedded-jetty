package io.shaka.jetty

import java.io.File

import io.shaka.jetty.EmbeddedJetty.ToLog
import org.eclipse.jetty.server._
import org.eclipse.jetty.server.handler.{HandlerCollection, RequestLogHandler}
import org.eclipse.jetty.webapp.WebAppContext

object EmbeddedJetty{
  type ToLog = String => Unit
  private val doNothingLog: ToLog = _ => ()
  def jetty: EmbeddedJetty = jetty(JettyConfiguration())
  def jetty(port: Int): EmbeddedJetty = jetty(JettyConfiguration(port = port))
  def jetty(config: JettyConfiguration, log: ToLog = doNothingLog): EmbeddedJetty = new EmbeddedJetty(config, log)
}

class EmbeddedJetty private(config: JettyConfiguration, otherLog: ToLog) {
  private val server: Server = new Server()
  private val httpConnector: ServerConnector = createConnector
  private val handlers = new HandlerCollection()
  handlers.setHandlers(Array(createWebAppHandler, loggingHandler))
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

  def addHandler(s: String, handler: Handler): EmbeddedJetty = {

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


  private def createWebAppHandler: WebAppContext = {
      val warPath = config.webappLocation
      log(s"EMBEDDED JETTY >>> running webapp from $warPath")
      val ctx = new WebAppContext()
      ctx.setContextPath(config.context)
      ctx.setTempDirectory(new File(config.tempDirectory))
      log("EMBEDDED JETTY >>> using temp directory: " + ctx.getTempDirectory)
      ctx.setServer(server)
      ctx.setWar(warPath)
      ctx
  }

  private def loggingHandler: RequestLogHandler = {
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
