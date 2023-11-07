ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
publish / skip := true
ThisBuild / scalafixDependencies ++= Seq(
  "com.github.liancheng" %% "organize-imports" % "0.6.0",
  "net.pixiv" %% "scalafix-pixiv-rule" % "4.5.3"
)
ThisBuild / scalafixScalaBinaryVersion := CrossVersion.binaryScalaVersion(scalaVersion.value)

addCommandAlias(
  "publishForExample",
  ";set ThisBuild / version := \"0.0.1-EXAMPLE\"; +publishLocal"
)

lazy val scalaV = "2.12.18"

lazy val root = project.in(file("."))
  .aggregate(playSwagger, sbtPlaySwagger)
  .settings(
    sonatypeProfileName := "io.github.play-swagger",
    publish / skip := true,
    sourcesInBase := false,
    scalaVersion := scalaV
  )

lazy val playSwagger = project.in(file("core"))
  .enablePlugins(GitBranchPrompt)
  .settings(
    publish / skip := false,
    Publish.coreSettings,
    Format.settings,
    Testing.settings,
    name := "play-swagger",
    libraryDependencies ++= Dependencies.playTest(scalaVersion.value) ++
      Dependencies.playRoutesCompiler(scalaVersion.value) ++
      Dependencies.playJson(scalaVersion.value) ++
      Dependencies.enumeratum ++
      Dependencies.refined ++
      Dependencies.test ++
      Dependencies.yaml ++ Seq(
        "com.github.takezoe" %% "runtime-scaladoc-reader" % "1.0.3",
        "org.scalameta" %% "scalameta" % "4.8.11",
        "net.steppschuh.markdowngenerator" % "markdowngenerator" % "1.3.1.1",
        "joda-time" % "joda-time" % "2.12.5" % Test,
        "com.google.errorprone" % "error_prone_annotations" % "2.22.0" % Test
      ),
    libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
    addCompilerPlugin("com.github.takezoe" %% "runtime-scaladoc-reader" % "1.0.3"),
    scalaVersion := scalaV,
    crossScalaVersions := Seq(scalaVersion.value, "2.13.12"),
    scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 13)) => Seq("-Wunused")
      case _ => Seq("-Xlint:unused")
    })
  )

lazy val sbtPlaySwagger = project.in(file("sbtPlugin"))
  .enablePlugins(GitBranchPrompt)
  .settings(
    publish / skip := false,
    Publish.coreSettings,
    Format.settings,
    addSbtPlugin("com.github.sbt" %% "sbt-native-packager" % "1.9.16" % Provided),
    addSbtPlugin("com.github.sbt" %% "sbt-web" % "1.5.2" % Provided)
  )
  .enablePlugins(BuildInfoPlugin, SbtPlugin)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version),
    buildInfoPackage := "com.iheart.playSwagger",
    name := "sbt-play-swagger",
    description := "sbt plugin for play swagger spec generation",
    sbtPlugin := true,
    scalaVersion := scalaV,
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false,
    scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 13)) => Seq("-Wunused")
      case _ => Seq("-Xlint:unused")
    })
  )
