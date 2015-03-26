scala-embedded-jetty  [![Build Status](https://travis-ci.org/timt/scala-embedded-jetty.png?branch=master)](https://travis-ci.org/timt/scala-embedded-jetty) [ ![Download](https://api.bintray.com/packages/timt/repo/scala-embedded-jetty/images/download.png) ](https://bintray.com/timt/repo/scala-embedded-jetty/_latestVersion)
====================
A scala shim for running embedded jetty (http only currently)

Requirements
------------

* [scala](http://www.scala-lang.org) 2.10.4
* [scala](http://www.scala-lang.org) 2.11.5

Usage
-----
Add the following lines to your build.sbt

    resolvers += "Tim Tennant's repo" at "http://dl.bintray.com/timt/repo/"

    val jettyVersion="9.2.9.v20150224"

    libraryDependencies ++= Seq(
      "io.shaka"            %%  "scala-embedded-jetty"  % "9"
      "org.eclipse.jetty"   %   "jetty-webapp"          % jettyVersion,
      "org.eclipse.jetty"   %   "jetty-plus"            % jettyVersion,
      "org.eclipse.jetty"   %   "jetty-servlets"        % jettyVersion,


Starting a server

    import io.shaka.jetty.EmbeddedJetty
    val embeddedJetty = jetty.start()  //Start on some free port
    ...
    val embeddedJetty = jetty(1234).start() //Start on port 1234


Customise configuration

    import io.shaka.jetty.JettyConfiguration
    val jettyConfiguration = JettyConfiguration(port = 1234, webappDirectory = "src/resource/webapp")
    val embeddedJetty = jetty(jettyConfiguration).start()

Stopping the server

    embeddedJetty.stop()

Providing a custom context handler

    import io.shaka.jetty.Handlers._
    jetty
        .addHandler("/my-context", (request: Request) => {
          Response(status = 200, entity = Some("<h1>Hello World</h1>".getBytes))
        })
        .start()

For more examples see

* [HandlerSpec.scala](https://github.com/timt/scala-embedded-jetty/blob/master/src/test/scala/io/shaka/jetty/HandlerSpec.scala)
* [EmbeddedJettySpec.scala](https://github.com/timt/scala-embedded-jetty/blob/master/src/test/scala/io/shaka/jetty/EmbeddedJettySpec.scala)


Code license
------------
Apache License 2.0
