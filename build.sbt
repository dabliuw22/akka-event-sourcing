name := "akka-event-sourcing"

version := "0.1"

scalaVersion := "2.13.0"

val akkaParent = "com.typesafe.akka"
val akkaVersion = "2.5.23"
val levelDbParent = "org.fusesource.leveldbjni"
val levelDbVersion = "1.8"
val levelDbComplementParent = "org.iq80.leveldb"
val levelDbComplementVersion = "0.7"
val postgresqlParent = "org.postgresql"
val postgresqlVersion = "42.2.2"
val akkaJdbcParent = "com.github.dnvriend"
val akkaJdbcVersion = "3.5.2"
val cassandraVersion = "0.98"
val redisParent = "com.hootsuite"
val redisVersion = "0.9.0"
val protobufParent = "com.google.protobuf"
val protobufVersion = "3.6.1"

libraryDependencies ++= Seq(
  akkaParent %% "akka-persistence" % akkaVersion,
  
  levelDbParent % "leveldbjni-all" % levelDbVersion,
  levelDbComplementParent % "leveldb"  % levelDbComplementVersion,
  
  postgresqlParent % "postgresql" % postgresqlVersion,
  akkaJdbcParent %% "akka-persistence-jdbc" % akkaJdbcVersion,
  
  akkaParent %% "akka-persistence-cassandra" % cassandraVersion,
  akkaParent %% "akka-persistence-cassandra-launcher" % cassandraVersion % Test,

  // for scala 2.12.0
  //redisParent %% "akka-persistence-redis" % redisVersion,
  
  protobufParent % "protobuf-java"  % protobufVersion,
)