name := """example"""

version := "1.0-SNAPSHOT"

ThisBuild / scalafixDependencies ++= Seq(
  "com.github.liancheng" %% "organize-imports" % "0.6.0",
  "com.sandinh" %% "scala-rewrites" % "1.1.0-M1",
  "net.pixiv" %% "scalafix-pixiv-rule" % "4.5.3",
  "com.github.xuwei-k" %% "scalafix-rules" % "0.3.1",
  "com.github.jatcwang" %% "scalafix-named-params" % "0.2.3"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala, SwaggerPlugin) //enable plugin

scalaVersion := "2.12.18"

libraryDependencies ++= Seq(
  jdbc,
  cacheApi,
  ws,
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
  "org.webjars" % "swagger-ui" % "4.18.1" // play-swagger ui integration
)

scalacOptions ++= Seq("-Xlint:unused")

swaggerDomainNameSpaces := Seq("models")
