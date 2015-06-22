import bintray.Keys.{bintray, bintrayOrganization, repository}

import scala.util.Try

name := "scala-embedded-jetty"

organization := "io.shaka"

version := Try(sys.env("LIB_VERSION")).getOrElse("1")

scalaVersion := "2.11.6"

crossScalaVersions := Seq("2.10.5", "2.11.6")

val jettyVersion = "9.2.10.v20150310"

externalResolvers := Seq("Bintray JCenter" at "https://jcenter.bintray.com/")

libraryDependencies ++= Seq(
  "org.eclipse.jetty"   %   "jetty-webapp"      % jettyVersion  % "provided",
  "org.eclipse.jetty"   %   "jetty-plus"        % jettyVersion  % "provided",
  "org.eclipse.jetty"   %   "jetty-servlets"    % jettyVersion  % "provided",
  "io.shaka"            %%  "naive-http-server" % "47",
  "org.scalatest"       %%  "scalatest"         % "2.2.4"       % "test"
)

pgpPassphrase := Some(Try(sys.env("SECRET")).getOrElse("goaway").toCharArray)

pgpSecretRing := file("./publish/sonatype.asc")

bintrayPublishSettings

repository in bintray := "repo"

bintrayOrganization in bintray := None

publishMavenStyle := true

publishArtifact in Test := false

homepage := Some(url("https://github.com/timt/scala-embedded-jetty"))

licenses +=("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

pomExtra :=
  <scm>
    <url>git@github.com:timt/scala-embedded-jetty.git</url>
    <connection>scm:git:git@github.com:timt/scala-embedded-jetty.git</connection>
  </scm>
    <developers>
      <developer>
        <id>timt</id>
      </developer>
    </developers>
