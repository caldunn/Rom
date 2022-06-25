ThisBuild / scalaVersion     := "3.1.2"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.pushme"
ThisBuild / organizationName := "PushMe"

val ZioVersion      = "2.0.0-RC6"
val ZioLogging      = "2.0.0-RC10"
val Http4sVersion   = "0.23.12"
val CirceVersion    = "0.14.2"
val TapirVersion    = "1.0.0-RC3"
val postgresVersion = "42.3.6"

lazy val ZHttp = (project in file("."))
  .settings(
//    run / fork := true,
    reStart / mainClass := Some("ZHttp"),
    name                := "Rom",
    libraryDependencies ++= Seq(
      "dev.zio"       %% "zio"                 % ZioVersion,
      "dev.zio"       %% "zio-test"            % ZioVersion % Test,
      "org.postgresql" % "postgresql"          % postgresVersion,
      "org.http4s"    %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s"    %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"    %% "http4s-circe"        % Http4sVersion,
      "org.http4s"    %% "http4s-dsl"          % Http4sVersion,
      "io.circe"      %% "circe-generic"       % CirceVersion,
      "dev.zio"       %% "zio-interop-cats"    % "3.3.0-RC7",
      "dev.zio"       %% "zio-logging"         % ZioLogging,
      "dev.zio"       %% "zio-logging-slf4j"   % ZioLogging,

// Tapir. Typed REST seems pree kewl.
      "com.softwaremill.sttp.tapir"   %% "tapir-http4s-server-zio" % TapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-zio-http-server"   % TapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-core"              % TapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-json-circe"        % TapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui-bundle" % TapirVersion,
      "com.softwaremill.sttp.client3" %% "zio"                     % "3.6.2",

      // Password hashing
      "com.kosprov.jargon2" % "jargon2-api"               % "1.1.1",
      "com.kosprov.jargon2" % "jargon2-native-ri-backend" % "1.1.1"
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),

    // Deployable build.
    assembly / mainClass := Some("ZHttp"),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "maven", "org.webjars", "swagger-ui", "pom.properties") =>
        MergeStrategy.singleOrError
      case x if x.contains("io.netty.versions.properties") => MergeStrategy.first
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    },
    envVars  := Map("ARGON_PASSWORD_SECRET" -> "abc"),
    reLogTag := "[TAPIR-ZHTTP]",
    welcomeMessage // Nice guide
  )

def welcomeMessage = onLoadMessage := {
  import scala.Console

  def header(text: String): String = s"${Console.RED}$text${Console.RESET}"

  def item(text: String): String = s"${Console.GREEN}> ${Console.CYAN}$text${Console.RESET}"

  def subItem(text: String): String = s"  ${Console.YELLOW}> ${Console.CYAN}$text${Console.RESET}"

  s"""
     |Useful sbt tasks:
     |${item("reStart")} - Use sbt-revolver to watch and re-run program.
     |${item("assemble")} - Create a fat-jar
      """.stripMargin
}
