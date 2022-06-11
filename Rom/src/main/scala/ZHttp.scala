import io.circe.generic.auto.*
import sttp.tapir.PublicEndpoint
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.*
import zhttp.http.HttpApp
import zhttp.service.Server
import zio.{ExitCode, Task, URIO, ZIO, ZIOAppDefault}
import java.io.IOException
import ServiceTypeDefs.*

object ZHttp extends ZIOAppDefault {
  case class Pet(species: String, url: String)

  val petEndpoint: PublicEndpoint[Int, String, Pet, Any] =
    endpoint
      .get
      .in("pet" / path[Int]("petId"))
      .errorOut(stringBody)
      .out(jsonBody[Pet])

  import sys.process.*
  val petServerEndpoint: ZServerEndpoint[FirstService, Any] = petEndpoint.zServerLogic {
    case 35 =>
      ZIO
        .succeed(Pet("Tapirus terrestris", "https://en.wikipedia.org/wiki/Tapir"))
        .tap(_ => ZIO.logInfo(s"${HashMe.checkHash(HashMe.getPassword)}"))
    case 1 =>
      ZIO
        .succeed(Pet("GigaChad", "Amogus"))
        .tap(p =>
          ZIO
            .attemptBlockingInterrupt(
              "echo \"GigaChad has been spotted! Sound the Alarm!! Roh Roh Raggy\"".#|("espeak").!
            )
            .forkDaemon // Fork this ;)
        )

    case _ =>
      for {
        magicNum <- FirstService.magic.orElseFail("DELETE THIS!")
        pet       = Pet("Fake", "Pet")
        _        <- ZIO.fail(s"$magicNum")
      } yield pet

  }

  val kekEndpoint: PublicEndpoint[String, Unit, String, Any] =
    endpoint
      .get
      .in("name" / path[String]("name"))
      .out(stringBody)

  val kekServerEndpoint: ZServerEndpoint[Any, Any] =
    kekEndpoint.zServerLogic(name => ZIO.succeed(s"Hi ${name.capitalize}"))

  val kekServerRoutes: HttpApp[Any, Throwable]          = ZioHttpInterpreter().toHttp(List(kekServerEndpoint))
  val petServerRoutes: HttpApp[FirstService, Throwable] = ZioHttpInterpreter().toHttp(List(petServerEndpoint))

  // Docs
  val swaggerEndpoints: List[ZServerEndpoint[Any, Any]] =
    SwaggerInterpreter().fromEndpoints[Task](List(petEndpoint, kekEndpoint), "My First Server", "0.1")

  // Starting the server
  val routes: HttpApp[FirstService, Throwable] =
    val kek = ZioHttpInterpreter().toHttp(swaggerEndpoints)
    ZioHttpInterpreter().toHttp(List(petServerEndpoint)) ++ kekServerRoutes ++ kek

  val bootStrap: ZIO[FirstService with SecondService, IOException, Unit] =
    for {
      api    <- ZIO.service[FirstService]
      magic  <- api.magicNumber
      api2   <- ZIO.service[SecondService]
      magic2 <- api2.magicNumber
      _      <- zio.Console.printLine(s"Magic Numbers:\n1: $magic\t2: $magic2")
    } yield ()

  val server: ZIO[FirstService, Throwable, Nothing] =
    Server
      .start(8080, routes)

  val endOfWorld: ZIO[AllServices, Any, Nothing] =
    bootStrap *> HashMe.checkForSecret.tapError(x => ZIO.logError(x.msg)) *> server

  import sttp.client3.httpclient.zio.HttpClientZioBackend
  import sttp.client3.*
  //  val request = HttpClientZioBackend().flatMap { backend =>
  //    backend.send()
  //  }
  val backend = HttpClientSyncBackend()
  val response = basicRequest
    .body("Hello World!!")
    .get(uri"http://localhost:8090/api/v1")
    .send(backend)

  println(response.body.isRight)

  override def run: ZIO[Any, Any, Any] =
    endOfWorld
      .provideSome(FirstServiceLive.layer, SecondServiceLive.layer)
      .exitCode
}
