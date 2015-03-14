name := "api"

organization := "com.satsukita-andon"

version := "0.0.0"

scalaVersion := "2.11.6"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Xfatal-warnings",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-unused-import",
  "-Ywarn-value-discard"
)

libraryDependencies ++= {
  val akkaHttp = "1.0-M4"
  Seq(
    "com.typesafe.akka" %% "akka-stream-experimental" % akkaHttp,
    "com.typesafe.akka" %% "akka-http-core-experimental" % akkaHttp,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaHttp,
    "org.json4s" %% "json4s-jackson" % "3.2.11",
    "org.scalikejdbc" %% "scalikejdbc" % "2.2.4",
    "com.h2database" % "h2" % "1.4.185",
    "ch.qos.logback" % "logback-classic" % "1.1.2"
  )
}
