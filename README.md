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

    libraryDependencies += "io.shaka" %% "scala-embedded-jetty" % "3"

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

Code license
------------
Apache License 2.0
