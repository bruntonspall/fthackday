import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "fthack"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "joda-time" % "joda-time" % "1.6"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}
