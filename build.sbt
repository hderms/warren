name := "warren"

version := "1.0"

scalaVersion := "2.11.11"

//Oprabbit
resolvers ++= Seq(
  "SpinGo OSS" at "http://spingo-oss.s3.amazonaws.com/repositories/releases"
)

val opRabbitVersion = "1.6.0"

libraryDependencies ++= Seq(
  "com.spingo" %% "op-rabbit-core"        % opRabbitVersion,
  "com.spingo" %% "op-rabbit-play-json"   % opRabbitVersion,
  "com.spingo" %% "op-rabbit-json4s"      % opRabbitVersion,
  "com.spingo" %% "op-rabbit-airbrake"    % opRabbitVersion,
  "com.spingo" %% "op-rabbit-akka-stream" % opRabbitVersion
)

// major.minor are in sync with the elasticsearch releases
val elastic4sVersion = "5.4.5"

libraryDependencies ++= Seq(
  "com.sksamuel.elastic4s" %% "elastic4s-core" % elastic4sVersion,
  // for the tcp client
  "com.sksamuel.elastic4s" %% "elastic4s-tcp" % elastic4sVersion,
  //http
  "com.sksamuel.elastic4s" %% "elastic4s-http" % elastic4sVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-http-streams" % elastic4sVersion,


  //Circe
  "com.sksamuel.elastic4s" %% "elastic4s-circe" % elastic4sVersion,

  // testing
  "com.sksamuel.elastic4s" %% "elastic4s-testkit" % elastic4sVersion % "test",
  "com.sksamuel.elastic4s" %% "elastic4s-embedded" % elastic4sVersion % "test"

)

//HTTP4s
val http4sVersion = "0.15.13"
libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion
)

//convert future to task
libraryDependencies += "io.verizon.delorean" %% "core" % "1.2.40-scalaz-7.1"


//Settings
target in Compile in doc := baseDirectory.value / "doc"
