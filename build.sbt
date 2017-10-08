lazy val skinnyVersion = "1.2.7"

lazy val gae_app = (project in file("."))
  .settings(
    inThisBuild(List(
      organization := "net.petitviolet",
      scalaVersion := "2.12.3",
      version := "0.1.0"
    )),
    name := "scala-gae-se",
    libraryDependencies ++= Seq(
      "org.skinny-framework" %% "skinny-micro" % skinnyVersion % Compile
      , "org.skinny-framework" %% "skinny-micro-server" % skinnyVersion % Compile
      , "ch.qos.logback" % "logback-classic" % "1.2.3" % Compile
      , "javax.servlet" % "javax.servlet-api" % "3.1.0" % "container;provided;test"
      , "org.mortbay.jetty" % "jetty" % "6.1.26" % "container"
      , "com.google.appengine" % "appengine-api-1.0-sdk" % "1.9.57"
    ),
    Nil
  )
  .enablePlugins(AppenginePlugin)

