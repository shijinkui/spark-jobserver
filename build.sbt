
//  define all the module and their relationship
//  the setting will be define in module own build.sbt file

//Common.appSettings

lazy val common = project in file("common")

lazy val jobserver = (project in file("jobserver")).enablePlugins(UniversalPlugin).dependsOn(common)

lazy val tests = project in file("tests")

lazy val extras = project in file("extras")

lazy val root = (project in file("."))
  .settings(Common.outReleaseSettings ++ Common.rootSettings)
  .aggregate(jobserver, common, extras, tests)
  .dependsOn(jobserver, common, extras)
