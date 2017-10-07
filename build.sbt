lazy val gae_app = (project in file("."))
  .settings(
    inThisBuild(List(
      organization := "net.petitviolet",
      scalaVersion := "2.12.3",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "scala-gae-se",
    libraryDependencies ++= Seq(
      "org.skinny-framework" %% "skinny-micro"         % "1.2.7",
      "ch.qos.logback"       %  "logback-classic"      % "1.2.3",
      "javax.servlet"        %  "javax.servlet-api"    % "4.0.0",
      "com.google.appengine" % "appengine-api-1.0-sdk" % "1.9.53"
    )
  )

