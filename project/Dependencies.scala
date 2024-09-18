import sbt.*
object Dependencies {
  object Versions {
    val play = "3.0.0"
    val playJson = "3.0.1"
    val enumeratum = "1.7.4"
    val refined = "0.11.2"
  }

  def playTest(scalaVersion: String): Seq[ModuleID] = Seq(
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 12)) => "com.typesafe.play" %% "play-test" % "2.8.20" % Test
      case _ => "org.playframework" %% "play-test" % Versions.play % Test
    }
  )

  def playRoutesCompiler(scalaVersion: String): Seq[ModuleID] = Seq(
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 12)) => "com.typesafe.play" %% "routes-compiler" % "2.8.20"
      case _ => "org.playframework" %% "play-routes-compiler" % Versions.play
    }
  )

  def playJson(scalaVersion: String): Seq[ModuleID] = Seq(
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 12)) => "com.typesafe.play" %% "play-json" % "2.10.3" % "provided"
      case _ => "org.playframework" %% "play-json" % Versions.playJson % "provided"
    }
  )

  val yaml = Seq(
    "org.yaml" % "snakeyaml" % "2.3"
  )

  val enumeratum: Seq[ModuleID] = Seq(
    "com.beachape" %% "enumeratum" % Versions.enumeratum % Test
  )

  val refined: Seq[ModuleID] = Seq(
    "eu.timepit" %% "refined" % Versions.refined % Test
  )

  def test(scalaVersion: String): Seq[ModuleID] = CrossVersion.partialVersion(scalaVersion) match {
    case Some((3, _)) => Seq("org.specs2" %% "specs2-core" % "5.4.2" % Test)
    case _ => Seq("org.specs2" %% "specs2-core" % "4.20.3" % Test)
  }

}
