import sbt.Keys._
import sbt._
import spray.revolver.RevolverPlugin._

description := "Spark as a Service: a RESTful job server for Apache Spark"

// This is here so we can easily switch back to Logback when Spark fixes its log4j dependency.
lazy val jobServerLogbackLogging = "-Dlogback.configurationFile=config/logback-local.xml"

lazy val jobServerLogging = "-Dlog4j.configuration=config/log4j-local.properties"

lazy val sparkVersion = sys.env.getOrElse("SPARK_VERSION", "1.3.1")

lazy val deps: Seq[ModuleID] = Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion exclude(
    "io.netty", "netty-all") excludeAll ExclusionRule(organization = "org.scalamacros"),
  //  Force netty version.This avoids some Spark netty dependency problem.
  "io.netty" % "netty-all" % "4.0.23.Final",
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "com.h2database" % "h2" % "1.3.170"
)

Common.moduleSettings("jobserver", deps)

Revolver.settings ++ Seq(
  javaOptions in Revolver.reStart += jobServerLogging,
  // Give job server a bit more PermGen since it does classloading
  javaOptions in Revolver.reStart += "-XX:MaxPermSize=256m",
  javaOptions in Revolver.reStart += "-Djava.security.krb5.realm= -Djava.security.krb5.kdc=",
  // This lets us add Spark back to the classpath without assembly barfing
  fullClasspath in Revolver.reStart := (fullClasspath in Compile).value,
  mainClass in Revolver.reStart := Some("spark.jobserver.JobServer")
) ++ Common.publishSettings
