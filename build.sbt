

name := "CourseProject"

version := "0.1"

scalaVersion := "2.12.10"

val logbackVersion = "1.3.0-alpha10"
val sfl4sVersion = "2.0.0-alpha5"
val typesafeConfigVersion = "1.4.1"
val apacheCommonIOVersion = "2.11.0"
val scalacticVersion = "3.2.9"
val generexVersion = "1.0.2"
val AkkaVersion = "2.6.17"
val SparkVersion = "3.2.0"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-core" % logbackVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "org.slf4j" % "slf4j-api" % sfl4sVersion,
  "org.slf4j" % "slf4j-jdk14" % "2.0.0-alpha5",
  "com.typesafe" % "config" % typesafeConfigVersion,
  "commons-io" % "commons-io" % apacheCommonIOVersion,
  "org.scalactic" %% "scalactic" % scalacticVersion,
  "org.scalatest" %% "scalatest" % scalacticVersion % Test,
  "org.scalatest" %% "scalatest-featurespec" % scalacticVersion % Test,
  "com.typesafe" % "config" % typesafeConfigVersion,
  "com.github.mifmif" % "generex" % generexVersion,
  "com.amazonaws" % "aws-lambda-java-core" % "1.0.0",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.12.99",
  "com.amazonaws" % "aws-java-sdk-ses" % "1.12.125",
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-protobuf-v3" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % "2.1.1",
  "org.apache.kafka" % "kafka-clients" % "2.8.0",
  "org.apache.kafka" % "kafka-streams" % "2.8.0",
  "org.apache.kafka" %% "kafka-streams-scala" % "2.8.0",
  "io.circe" %% "circe-core" % "0.14.1",
  "io.circe" %% "circe-generic" % "0.14.1",
  "io.circe" %% "circe-parser" % "0.14.1",
  "org.apache.spark" %% "spark-sql" % "3.2.0" % "provided",
//  "org.apache.spark" %% "spark-core" % "3.2.0" % "provided",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.2.0" % Test

  //  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
//  "com.typesafe.akka" %% "akka-remote" % AkkaVersion,
//  "com.typesafe.akka" %% "akka-cluster-typed" % AkkaVersion,
//  "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion,
//  "com.typesafe.akka" %% "akka-projection-core" % AkkaVersion,
//  "com.typesafe.akka" %% "akka-persistence-typed" % AkkaVersion,
//  "com.typesafe.akka" %% "akka-cluster-singleton" % AkkaVersion,
//  "com.typesafe.akka" %% "akka-cluster-sharding-typed" % AkkaVersion,

)

assembly / assemblyMergeStrategy := {
  case "reference.conf" => MergeStrategy.concat
  case "META-INF/services/org.apache.spark.sql.sources.DataSourceRegister" => MergeStrategy.concat
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
