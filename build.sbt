import bintray.Keys._

import scala.util.Try


name := "scala-embedded-jetty"

organization := "io.shaka"

version := Try(sys.env("LIB_VERSION")).getOrElse("1")

scalaVersion := "2.11.5"

crossScalaVersions := Seq("2.10.4", "2.11.5")

val jettyVersion = "9.2.7.v20150116"

libraryDependencies ++= Seq(
  "org.eclipse.jetty" % "jetty-webapp" % jettyVersion,
  "org.eclipse.jetty" % "jetty-plus" % jettyVersion,
  "org.eclipse.jetty" % "jetty-servlets" % jettyVersion,
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
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
