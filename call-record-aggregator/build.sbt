import sbt._
import sbt.Keys._
import scalariform.formatter.preferences._

//tag::docs-PipelinesApplicationPlugin-example[]
lazy val callRecordPipeline = (project in file("./call-record-pipeline"))
  .enablePlugins(PipelinesApplicationPlugin)
  .settings(commonSettings)
  .settings(
    name := "call-record-aggregator"
  )
  .dependsOn(akkaCdrIngestor, akkaJavaAggregationOutput, sparkAggregation)
//end::docs-PipelinesApplicationPlugin-example[]

lazy val datamodel = (project in file("./datamodel"))
  .enablePlugins(PipelinesLibraryPlugin)

lazy val akkaCdrIngestor= (project in file("./akka-cdr-ingestor"))
    .enablePlugins(PipelinesAkkaStreamsLibraryPlugin)
    .settings(
      commonSettings,
      libraryDependencies ++= Seq(
        "com.typesafe.akka"         %% "akka-http-spray-json"   % "10.1.10",
        "ch.qos.logback"            %  "logback-classic"        % "1.2.3",
        "org.scalatest"             %% "scalatest"              % "3.0.8"    % "test"
      )
    )
  .dependsOn(datamodel)

lazy val akkaJavaAggregationOutput= (project in file("./akka-java-aggregation-output"))
  .enablePlugins(PipelinesAkkaStreamsLibraryPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "com.typesafe.akka"      %% "akka-http-spray-json"   % "10.1.10",
      "ch.qos.logback"         %  "logback-classic"        % "1.2.3",
      "org.scalatest"          %% "scalatest"              % "3.0.8"    % "test"
    )
  )
  .dependsOn(datamodel)

lazy val sparkAggregation = (project in file("./spark-aggregation"))
    .enablePlugins(PipelinesSparkLibraryPlugin)
    .settings(
      commonSettings,
      Test / parallelExecution := false,
      Test / fork := true,
      libraryDependencies ++= Seq(
        "ch.qos.logback" %  "logback-classic"        % "1.2.3",
        "org.scalatest"  %% "scalatest" % "3.0.8"  % "test"
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
