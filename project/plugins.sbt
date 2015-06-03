addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.5")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2") // For quick restarts for web development

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.7.0")

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "1.1.0")

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.2.1")

addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.3")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.5")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.0")

resolvers += Classpaths.sbtPluginReleases
