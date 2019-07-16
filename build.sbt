name := "akka-event-sourcing"

version := "0.1"

scalaVersion := "2.13.0"

val akkaParent = "com.typesafe.akka"
val akkaVersion = "2.5.23"
val levelDbParent = "org.fusesource.leveldbjni"
val levelDbVersion = "1.8"

libraryDependencies ++= Seq(
  akkaParent %% "akka-actor" % akkaVersion,
  akkaParent %% "akka-persistence" % akkaVersion,
  levelDbParent % "leveldbjni-all" % levelDbVersion,
  "org.iq80.leveldb" % "leveldb"  % "0.7"
)