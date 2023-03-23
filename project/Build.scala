import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "andon"
  val appVersion      = "1.0-SNAPSHOT"

  val scalaVersion    = "2.11.12"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    "com.typesafe.slick" %% "slick" % "1.0.1",
    "org.pegdown" % "pegdown" % "1.2.1",
    "org.twitter4j" % "twitter4j-core" % "4.0.2",
    "org.eclipse.jgit" % "org.eclipse.jgit" % "3.4.1.201406201815-r",
    "com.sksamuel.elastic4s" %% "elastic4s" % "1.5.4",
    "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
    "org.json4s" %% "json4s-jackson" % "3.2.11"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
  )

}
