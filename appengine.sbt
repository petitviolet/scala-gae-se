//lazy val jettyVersion = "9.4.5.v20170502"

libraryDependencies ++= Seq(
  "org.mortbay.jetty" % "jetty" % "6.1.26" % "container"
//  , "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "container"
//  , "org.eclipse.jetty" % "jetty-plus" % jettyVersion % "container"
)
enablePlugins(AppenginePlugin)
