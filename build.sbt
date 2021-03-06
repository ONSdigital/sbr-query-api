import play.sbt.PlayScala
import sbtbuildinfo.BuildInfoPlugin.autoImport._
import sbtassembly.AssemblyPlugin.autoImport._
import com.typesafe.sbt.SbtNativePackager.Universal

licenses := Seq("MIT-License" -> url("https://github.com/ONSdigital/sbr-query-api/blob/master/LICENSE"))

// key-bindings
lazy val ITest = config("it") extend Test

lazy val Versions = new {
  val scala = "2.11.11"
  val appVersion = "0.1-SNAPSHOT"
  val scapegoatVersion = "1.1.0"
}

lazy val Constant = new {
  val appName = "sbr-query-api"
  val projectStage = "alpha"
  val organisation = "ons"
  val team = "sbr"
}

lazy val testSettings = Seq(
  sourceDirectory in ITest := baseDirectory.value / "/test/it",
  resourceDirectory in ITest := baseDirectory.value / "/test/resources",
  scalaSource in ITest := baseDirectory.value / "test/it",
  // test setup
  parallelExecution in Test := false
)


lazy val Resolvers = Seq(
  Resolver.typesafeRepo("releases"),
  "Hadoop Releases" at "https://repository.cloudera.com/content/repositories/releases/"
)

lazy val commonSettings = Seq (
  scalaVersion := Versions.scala,
  scalacOptions in ThisBuild ++= Seq(
    "-language:experimental.macros",
    "-target:jvm-1.8",
    "-encoding", "UTF-8",
    "-language:reflectiveCalls",
    "-language:experimental.macros",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:postfixOps",
    "-deprecation", // warning and location for usages of deprecated APIs
    "-feature", // warning and location for usages of features that should be imported explicitly
    "-unchecked", // additional warnings where generated code depends on assumptions
    "-Xlint", // recommended additional warnings
    "-Xcheckinit", // runtime error when a val is not initialized due to trait hierarchies (instead of NPE somewhere else)
    "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
    //"-Yno-adapted-args", // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver
    "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
    "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures
    "-Ywarn-dead-code", // Warn when dead code is identified
    "-Ywarn-unused", // Warn when local and private vals, vars, defs, and types are unused
    //"-Ywarn-unused-import", //  Warn when imports are unused (don't want IntelliJ to do it automatically)
    "-Ywarn-numeric-widen" // Warn when numerics are widened
  ),
  resolvers ++= Resolvers,
  coverageExcludedPackages := ".*Routes.*;.*ReverseRoutes.*;.*javascript.*"
)

lazy val api = (project in file("."))
  .enablePlugins(BuildInfoPlugin, GitVersioning, GitBranchPrompt, PlayScala)
  .configs(ITest)
  .settings(inConfig(ITest)(Defaults.testSettings) : _*)
  .settings(commonSettings: _*)
  .settings(testSettings:_*)
  .settings(
    scalaVersion := Versions.scala,
    name := Constant.appName,
    moduleName := "sbr-query-api",
    version := Versions.appVersion,
    buildInfoPackage := "controllers",
    buildInfoKeys := Seq[BuildInfoKey](
      organization,
      name,
      version,
      scalaVersion,
      sbtVersion,
      BuildInfoKey.action("gitVersion") {
        git.gitTagToVersionNumber.?.value.getOrElse(Some(Constant.projectStage))+"@"+ git.formattedDateVersion.?.value.getOrElse("")
    }),
    // di router -> swagger
    routesGenerator := InjectedRoutesGenerator,
    buildInfoOptions += BuildInfoOption.ToMap,
    buildInfoOptions += BuildInfoOption.ToJson,
    buildInfoOptions += BuildInfoOption.BuildTime,
    buildInfoPackage := "controllers",
    // Run with proper default env vars set for h2 in memory
    javaOptions in Universal ++= Seq(
      "-Dsource=h2Local"
    ),
    libraryDependencies ++= Seq (
      filters,
      jdbc,
      javaJdbc,
      "org.webjars"                  %%    "webjars-play"        %    "2.5.0-3",
      "com.typesafe.scala-logging"   %%    "scala-logging"       %    "3.5.0",
      "org.scalatestplus.play"       %%    "scalatestplus-play"  %    "2.0.0"           % Test,
      "io.swagger"                   %%    "swagger-play2"       %    "1.5.3",
      "org.webjars"                  %     "swagger-ui"          %    "2.2.10-1"
    ),
    assemblyJarName in assembly := s"${Constant.appName}-${Versions.appVersion}.jar",
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    },
    mainClass in assembly := Some("play.core.server.ProdServerStart"),
    fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value)
  )
