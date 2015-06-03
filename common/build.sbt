import sbt._

Common.moduleSettings("common", deps)

lazy val deps: Seq[ModuleID] = Seq(
  // Akka is provided because Spark already includes it, and Spark's version is shaded so it's not safe
  // to use this one "com.typesafe.akka" %% "akka-slf4j" % "2.3.4" % "provided",
  "io.spray" %% "spray-json" % "1.3.1" withSources(),
  "io.spray" %% "spray-can" % "1.3.2" withSources(),
  "io.spray" %% "spray-routing" % "1.3.2" withSources(),
  "io.spray" %% "spray-client" % "1.3.2" withSources(),
  "com.typesafe.akka" %% "akka-actor" % "2.3.4",
  "com.yammer.metrics" % "metrics-core" % "2.2.0"
  //  "io.dropwizard.metrics" % "metrics-core" % "3.1.2" withSources()
)
