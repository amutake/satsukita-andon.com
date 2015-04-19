organization := "com.satsukita-andon"

name := "es-load"

version := "0.0.1"

scalaVersion := "2.11.6"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint",
  // "-Xfatal-warnings",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-unused-import",
  "-Ywarn-value-discard"
)

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "2.2.5",
  "org.scalikejdbc" %% "scalikejdbc-config" % "2.2.5",
  "com.h2database" % "h2" % "1.4.186",
  "com.sksamuel.elastic4s" %% "elastic4s" % "1.5.4"
)

assemblyJarName in assembly := "es-load.jar"

assemblyMergeStrategy in assembly := {
  case PathList("org", "joda", "convert", "FromString.class") => MergeStrategy.first
  case PathList("org", "joda", "convert", "ToString.class") => MergeStrategy.first
  case x => {
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
  }
}
