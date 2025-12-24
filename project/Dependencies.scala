import sbt._
object Dependencies {
  object Versions {
    val play = "3.0.10"
    val playJson = "3.0.6"
    val specs2 = "4.23.0"
    val enumeratum = "1.9.2"
    val refined = "0.11.3"
  }

  def playTest(scalaVersion: String): Seq[ModuleID] = Seq(
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 12)) => "com.typesafe.play" %% "play-test" % "2.8.22" % Test
      case _ => "org.playframework" %% "play-test" % Versions.play % Test
    }
  )

  def playRoutesCompiler(scalaVersion: String): Seq[ModuleID] = Seq(
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 12)) => "com.typesafe.play" %% "routes-compiler" % "2.8.22"
      case _ => "org.playframework" %% "play-routes-compiler" % Versions.play
    }
  )

  def playJson(scalaVersion: String): Seq[ModuleID] = Seq(
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 12)) => "com.typesafe.play" %% "play-json" % "2.10.8" % "provided"
      case _ => "org.playframework" %% "play-json" % Versions.playJson % "provided"
    }
  )

  val yaml = Seq(
    "org.yaml" % "snakeyaml" % "2.5"
  )

  val enumeratum: Seq[ModuleID] = Seq(
    "com.beachape" %% "enumeratum" % Versions.enumeratum % Test
  )

  val refined: Seq[ModuleID] = Seq(
    "eu.timepit" %% "refined" % Versions.refined
  )

  val test: Seq[ModuleID] = Seq(
    "org.specs2" %% "specs2-core" % Versions.specs2 % "test"
  )

}
