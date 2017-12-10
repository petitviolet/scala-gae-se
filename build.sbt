lazy val skinnyVersion = "1.2.7"

lazy val commonScalaVersion = "2.12.2"

lazy val commonOrganization = "net.petitviolet"

lazy val commonVersion = "1.0.0"

lazy val commonScalacOptions = Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-unused-import",
  "-Ywarn-value-discard"
)

// scala-metaを使ったライブラリのために必要
lazy val metaMacroSettings: Seq[Def.Setting[_]] = Seq(
  resolvers += Resolver.sonatypeRepo("releases"),
  resolvers += Resolver.bintrayIvyRepo("scalameta", "maven"),
  addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M9" cross CrossVersion.full),
  scalacOptions ++= Seq(
    "-Xplugin-require:macroparadise"
    // "-Ymacro-debug-lite" // for debug
  )
)

lazy val SLF4J_VERSION = "1.7.25"
lazy val commonDependencies = Seq(
   "com.iheart" %% "ficus" % "1.4.3",
  "org.slf4j" % "slf4j-api" % SLF4J_VERSION,
  "org.slf4j" % "slf4j-jdk14" % SLF4J_VERSION,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.lihaoyi" %% "sourcecode" % "0.1.4",
  "commons-io" % "commons-io" % "2.6",

  "org.json4s" %% "json4s-jackson" % "3.5.3",
  "net.petitviolet" %% "operator" % "0.2.3",
  "net.petitviolet" %% "mlogging" % "0.3.1"
) ++ scalazDependencies ++ googleDependencies ++ testDependencies

lazy val testDependencies = Seq(
  "org.mockito" % "mockito-core" % "2.11.0",
  "org.scalatest" % "scalatest_2.12" % "3.0.4",
  "org.scalikejdbc" % "scalikejdbc-test_2.12" % "3.1.0",
  "org.scalacheck" %% "scalacheck" % "1.13.4"
) map { _ % Test }

lazy val databaseDependencies = Seq(
  "mysql" % "mysql-connector-java" % "8.0.8-dmr",
  "com.zaxxer" % "HikariCP" % "2.7.2",
  "org.scalikejdbc" %% "scalikejdbc" % "3.1.0",
  "org.scalikejdbc" %% "scalikejdbc-config" % "3.1.0",
  "org.scalikejdbc" %% "scalikejdbc-syntax-support-macro" % "3.1.0",
  "org.skinny-framework" %% "skinny-orm" % "2.5.1"
)

lazy val googleDependencies = Seq(
  "com.google.cloud" % "google-cloud-bigquery" % "0.26.0-beta",
  "com.google.cloud" % "google-cloud-logging" % "1.8.0",
  "com.google.cloud" % "google-cloud-pubsub" % "0.26.0-beta",
  "com.google.cloud" % "google-cloud-storage" % "1.8.0",
  "com.google.appengine" % "appengine-api-1.0-sdk" % "1.9.59",
  "com.google.cloud.sql" % "mysql-socket-factory" % "1.0.4",
  "com.google.api-client" % "google-api-client-appengine" % "1.23.0",
  "com.google.cloud" % "google-cloud-bigquery" % "0.26.0-beta",
  "com.google.guava" % "guava" % "23.0"
)

lazy val webAppDependencies = {
  val skinnyVersion = "1.2.7"
  Seq(
    "org.skinny-framework" %% "skinny-micro" % skinnyVersion % Compile,
    "org.skinny-framework" %% "skinny-micro-server" % skinnyVersion % Compile,
    "org.skinny-framework" %% "skinny-micro-json4s" % skinnyVersion % Compile
  )
}

lazy val appengineDependencies = Seq(
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "container;provided;test",
  "org.mortbay.jetty" % "jetty" % "6.1.26" % "container"
)

lazy val scalazDependencies = {
  val SCALAZ_VERSION = "7.2.16"
  Seq(
    "org.scalaz" %% "scalaz-core" % SCALAZ_VERSION,
    "org.scalaz" %% "scalaz-concurrent" % SCALAZ_VERSION,
    "io.verizon.delorean" %% "core" % "1.2.40-scalaz-7.2"
  )
}

lazy val commonResolvers = Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.bintrayRepo("iheartradio","maven"),
  Resolver.jcenterRepo,
  Resolver.mavenCentral
)

def commonSettings(moduleName: String) = Seq(
  name := moduleName,
  organization := commonOrganization,
  version := commonVersion,
  scalaVersion := commonScalaVersion,
  scalacOptions ++= commonScalacOptions,
  javacOptions ++= Seq("-encoding", "UTF-8"),
  resolvers ++= commonResolvers,
  libraryDependencies ++= commonDependencies,
  parallelExecution in Test := false,
  scalacOptions in(Compile, console) ~= (_ filterNot (_ == "-Ywarn-unused-import")),
  // skinny-microが誤ってorg.mortbay.jetty % servlet-apiを参照するのを防ぐ
  excludeDependencies ++= Seq(
    ExclusionRule(organization = "org.mortbay.jetty", name = "servlet-api"),
    ExclusionRule(organization = "javax.servlet", name = "servlet-api")
  )
) ++ metaMacroSettings

lazy val scalaGaeSeRoot = (project in file("."))
  .settings(commonSettings("scalaGaeSeRoot"))
  .aggregate(
    common,
    infra,
    main
  )

lazy val common = (project in file("modules/common"))
  .settings(
    commonSettings("common")
  )

lazy val infra = (project in file("modules/infra"))
  .settings(
    commonSettings("infra"),
    libraryDependencies ++= databaseDependencies
  ).dependsOn(common)

lazy val main = (project in file("modules/main"))
  .settings(
    commonSettings("main"),
    libraryDependencies ++= webAppDependencies ++ appengineDependencies
  )
  .dependsOn(common, infra)
  .enablePlugins(AppenginePlugin)

