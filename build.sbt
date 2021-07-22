import scala.util.Try

name := "scala-embedded-jetty"

organization := "io.shaka"

version := Try(sys.env("LIB_VERSION")).getOrElse("1")

scalaVersion := "2.13.5"

crossScalaVersions := Seq("2.12.13", "2.13.5")

homepage := Some(url("https://github.com/timt/scala-embedded-jetty"))

licenses +=("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

val jettyVersion = "9.2.11.v20150529"

libraryDependencies ++= Seq(
  "org.eclipse.jetty"   %   "jetty-webapp"      % jettyVersion  % "provided",
  "org.eclipse.jetty"   %   "jetty-servlets"    % jettyVersion  % "provided",
  "io.shaka"            %%  "naive-http"        % "122"         % "provided",
  "org.scalatest"       %%  "scalatest"         % "3.1.2"       % "test"
)

developers := List(
  Developer("timt", "Tim Tennant", "", url("https://github.com/timt"))
)

usePgpKeyHex("timt-ci bot")

publishMavenStyle := true

publishArtifact in Test := false