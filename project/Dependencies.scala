import sbt._

object Dependencies {
  object Versions {
    val play = "2.9.0"
    val playJson = "2.10.6"
    val specs2 = "4.20.6"
    val enumeratum = "1.7.3"
    val refined = "0.11.1"
  }

  def playTest(scalaVersion: String): Seq[ModuleID] = Seq(
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 12)) => "com.typesafe.play" %% "play-test" % "2.8.20" % Test
      case _ => "com.typesafe.play" %% "play-test" % Versions.play % Test
    }
  )

  def playRoutesCompiler(scalaVersion: String): Seq[ModuleID] = Seq(
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 12)) => "com.typesafe.play" %% "routes-compiler" % "2.8.20"
      case _ => "com.typesafe.play" %% "routes-compiler" % "2.9.0-M6" // 暫定
    }
  )

  def playJson(scalaVersion: String): Seq[ModuleID] = Seq(
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 12)) => "com.typesafe.play" %% "play-json" % "2.10.3" % "provided"
      case _ => "com.typesafe.play" %% "play-json" % Versions.playJson % "provided"
    }
  )

  val yaml: Seq[ModuleID] = Seq(
    "org.yaml" % "snakeyaml" % "2.2"
  )

  val enumeratum: Seq[ModuleID] = Seq(
    "com.beachape" %% "enumeratum" % Versions.enumeratum % Test
  )

  val refined: Seq[ModuleID] = Seq(
    "eu.timepit" %% "refined" % Versions.refined % Test
  )

  val test: Seq[ModuleID] = Seq(
    "org.specs2" %% "specs2-core" % Versions.specs2 % "test",
    "org.specs2" %% "specs2-mock" % Versions.specs2 % "test"
  )

}
