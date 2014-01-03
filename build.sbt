name := "libanius-play"

version := "0.2"

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.2.0",
  "org.webjars" % "angularjs" % "1.1.5-1",
  "org.webjars" % "bootstrap" % "2.3.2",
  "org.scalaz" %% "scalaz-core" % "7.0.3"
)     

play.Project.playScalaSettings
