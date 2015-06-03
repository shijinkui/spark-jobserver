import bintray.Plugin._
import com.typesafe.sbt.SbtScalariform._
import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin.ReleaseKeys._
import sbtrelease.ReleasePlugin.releaseSettings
import sbtrelease.ReleaseStateTransformations._

import scalariform.formatter.preferences._

object Common {

  def appName = "spark-jobserver"

  val appSettings = settings(appName)

  // Settings for every module, i.e. for every subproject
  def moduleSettings(module: String, deps: Seq[ModuleID] = Seq.empty) = settings(module, deps)

  // Common settings for every module
  def settings(moduleName: String, deps: Seq[ModuleID] = Seq.empty) = Seq(
    name := moduleName,
    organization := "spark.jobserver",
    version := "0.5.2-SNAPSHOT",
    scalaVersion := "2.10.4",
    crossPaths := true,
    crossScalaVersions := Seq("2.10.4", "2.11.6"),
    doc in Compile <<= target.map(_ / "none"),
    parallelExecution in Test := false,
    fork in Test := true,
    libraryDependencies ++= (commonDep ++ deps),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-unchecked",
      "-language:reflectiveCalls",
      "-language:postfixOps",
      "-language:implicitConversions"
    ),
    // scalastyleFailOnError := true,
    runScalaStyle := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value,
    (compile in Compile) <<= (compile in Compile) dependsOn runScalaStyle
  ) ++: scalariformPrefs ++: scoverageSettings

  //  common dependency for every module
  lazy val commonDep = Seq(
    "org.joda" % "joda-convert" % "1.7" withSources(),
    "joda-time" % "joda-time" % "2.8" withSources(),
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "org.scalatest" %% "scalatest" % "2.2.1" % "test",
    "com.typesafe.akka" %% "akka-testkit" % "2.3.4" % "test",
    "io.spray" %% "spray-testkit" % "1.3.2" % "test"
  )

  ///////// other setting ////////


  // Create a default Scala style task to run with compiles
  lazy val runScalaStyle = taskKey[Unit]("testScalaStyle")

  lazy val scoverageSettings = {
    import scoverage.ScoverageSbtPlugin
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages := ".+Benchmark.*"
  }

  // change to scalariformSettings for auto format on compile; defaultScalariformSettings to disable
  // See https://github.com/mdr/scalariform for formatting options
  lazy val scalariformPrefs = defaultScalariformSettings ++ Seq(
    ScalariformKeys.preferences := FormattingPreferences()
      .setPreference(AlignParameters, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(DoubleIndentClassDeclaration, true)
  )

  lazy val publishSettings = bintrayPublishSettings ++ Seq(
    licenses +=("Apache-2.0", url("http://choosealicense.com/licenses/apache/")),
    bintray.Keys.bintrayOrganization in bintray.Keys.bintray := Some("spark-jobserver")
  )

  lazy val outReleaseSettings = releaseSettings ++ Seq(
    releaseProcess := Seq(
      checkSnapshotDependencies,
      runClean,
      runTest,
      inquireVersions,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      // lsync seems broken, always returning: Error synchronizing project libraries Unexpected response status: 404
      // syncWithLs(thisProjectRef.value),
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )

  lazy val rootSettings = Seq(
    // Must run Spark tests sequentially because they compete for port 4040!
    parallelExecution in Test := false,
    publishArtifact := false,
    concurrentRestrictions := Seq(
      Tags.limit(Tags.CPU, java.lang.Runtime.getRuntime.availableProcessors()),
      // limit to 1 concurrent test task, even across sub-projects
      // Note: some components of tests seem to have the "Untagged" tag rather than "Test" tag.
      // So, we limit the sum of "Test", "Untagged" tags to 1 concurrent
      Tags.limitSum(1, Tags.Test, Tags.Untagged))
  )

}


