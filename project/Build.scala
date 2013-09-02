import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "andon"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    "com.typesafe.slick" %% "slick" % "1.0.1",
    "org.pegdown" % "pegdown" % "1.1.0",
    "org.twitter4j" % "twitter4j-core" % "3.0.3"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
  )

}
