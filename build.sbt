name := "scala-mongo-example"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
  "org.mongodb.scala"          %% "mongo-scala-driver" % "2.7.0",
  "org.typelevel"              %% "cats-core"          % "2.0.0",
  "org.typelevel"              %% "cats-effect"        % "2.0.0",
  "ch.qos.logback"             % "logback-classic"     % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging"      % "3.9.2"
)

addCompilerPlugin(
  "org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full
)
