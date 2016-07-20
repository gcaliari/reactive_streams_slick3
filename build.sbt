name := """reactive_streams_slick3"""

version := "1.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  cache,
  ws,
  specs2 % Test,
  "com.typesafe.play"  % "play-slick_2.11"                % "2.0.2",
  "com.typesafe.play"  % "play-slick-evolutions_2.11"     % "2.0.2",
  "com.typesafe.play"  % "play-streams-experimental_2.11" % "2.4.8",
  "com.h2database"     % "h2"                             % "1.4.192"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"


// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
