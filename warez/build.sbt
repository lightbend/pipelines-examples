import sbt._
import sbt.Keys._
import scalariform.formatter.preferences._

lazy val root = blueprint

lazy val datamodel = (project in file("./datamodel"))
  .enablePlugins(PipelinesLibraryPlugin)

lazy val blueprint = (project in file("./blueprint"))
  .enablePlugins(PipelinesApplicationPlugin)
  .settings(
    /**
      * NOTE: Can we namespace or sandbox developer instances of this deployment?
      */
    name := "warez"
  )
  .dependsOn(akkaStreamlets, sparkStreamlets)

lazy val akkaStreamlets = (project in file("./akka-streamlets"))
  .enablePlugins(PipelinesAkkaStreamsLibraryPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "com.typesafe.akka"      %% "akka-http-spray-json"              % "10.1.10",
      "com.lightbend.akka"     %% "akka-stream-alpakka-elasticsearch" % "1.1.2",
      "org.scalatest"          %% "scalatest"                         % "3.0.8"    % "test"
    )
  )
  .dependsOn(datamodel)

lazy val sparkStreamlets = (project in file("./spark-streamlets"))
  .enablePlugins(PipelinesSparkLibraryPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.8" % "test"
    )
  )
  .dependsOn(datamodel)


lazy val commonSettings = Seq(
  scalaVersion := "2.12.10",
  scalacOptions ++= Seq(
    "-encoding", "UTF-8",
    "-target:jvm-1.8",
    "-Xlog-reflective-calls",
    "-Xlint",
    "-Ywarn-unused",
    "-Ywarn-unused-import",
    "-deprecation",
    "-feature",
    "-language:_",
    "-unchecked"
  ),

  scalacOptions in (Compile, console) --= Seq("-Ywarn-unused", "-Ywarn-unused-import"),
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value,

  scalariformPreferences := scalariformPreferences.value
    .setPreference(AlignParameters, false)
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 90)
    .setPreference(DoubleIndentConstructorArguments, true)
    .setPreference(DoubleIndentMethodDeclaration, true)
    .setPreference(RewriteArrowSymbols, true)
    .setPreference(DanglingCloseParenthesis, Preserve)
    .setPreference(NewlineAtEndOfFile, true)
    .setPreference(AllowParamGroupsOnNewlines, true)
)
