package io.shaka.jetty

import java.io.File

import io.shaka.http.Http.HttpHandler
import io.shaka.jetty.EmbeddedJetty.ToLog
import org.eclipse.jetty.server.handler.{ContextHandler, ContextHandlerCollection, RequestLogHandler}
import org.eclipse.jetty.server.{Handler, HttpConfiguration, HttpConnectionFactory, NCSARequestLog, Server, ServerConnector}
import org.eclipse.jetty.webapp.WebAppContext

object EmbeddedJetty {
  type ToLog = String => Unit
  private val printlnLog: ToLog = (s) => println(s)

  def jetty: EmbeddedJetty = jetty(JettyConfiguration())

  def jetty(port: Int): EmbeddedJetty = jetty(JettyConfiguration(port = port))

  def jetty(config: JettyConfiguration, log: ToLog = printlnLog): EmbeddedJetty = new EmbeddedJetty(config, log)
}

class EmbeddedJetty private(config: JettyConfiguration, log: ToLog) {
  private val server: Server = new Server()
  private val build: JettyComponentBuilder = JettyComponentBuilder(config, log)
  private val contextHandlers: ContextHandlerCollection = build.contextHandlers
  private val requestLogHandler: RequestLogHandler = build.requestLogHandler
  requestLogHandler.setHandler(contextHandlers)

  private val httpConnector: ServerConnector = build.httpConnector(server)
  server.setConnectors(Array(httpConnector))
  server.setHandler(requestLogHandler)
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

  def addHandler(path: String, handler: HttpHandler) = addJettyHandler(path, Handlers.adaptToJettyHandler(handler))

  def addJettyHandler(path: String, handler: Handler): EmbeddedJetty = {
    val contextHandler = new ContextHandler(path)
    contextHandler.setHandler(handler)
    contextHandlers.addHandler(contextHandler)
    if (server.isStarted) contextHandler.start()
    this
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

  def webAppHandler(contextConfig: ContextConfiguration): WebAppContext = {
    val warPath = contextConfig.webappLocation
    log(s"EMBEDDED JETTY >>> running webapp from $warPath")
    val webAppContext = new WebAppContext()
    webAppContext.setContextPath(contextConfig.context)
    webAppContext.setTempDirectory(new File(contextConfig.tempDirectory))
    log("EMBEDDED JETTY >>> using temp directory: " + webAppContext.getTempDirectory)
    webAppContext.setWar(warPath)
    webAppContext
  }

  def requestLogHandler: RequestLogHandler = {
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

  def contextHandlers: ContextHandlerCollection = {
    val contexts = new ContextHandlerCollection()
    config.contexts.foreach { contextConfig =>
      contexts.addHandler(webAppHandler(contextConfig))
    }
    contexts
  }
  
}
