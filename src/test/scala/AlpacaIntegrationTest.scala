package com.example.alpaca

import sttp.client3.*
import sttp.client3.circe.*
import io.circe.generic.auto.*
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterAll
import scala.concurrent.duration.*
import scala.concurrent.Await
import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.ExecutionContext.Implicits.global
import cats.effect.*
import cats.effect.unsafe.implicits.global
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.implicits.*
import sttp.tapir.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.generic.auto.*
import com.comcast.ip4s.*
import com.typesafe.scalalogging.LazyLogging
import com.example.alpaca.config.Config
import com.example.alpaca.trading.AlpacaTradingClient
import com.example.alpaca.{AlpacaDAL, AccountResponse}

class AlpacaIntegrationTest extends AnyFunSuite with Matchers with BeforeAndAfterAll {

  private val baseUrl = "http://localhost:8080"
  private var serverRelease: IO[Unit] = IO.unit
  private var backend: SttpBackend[Future, Any] = _

  override def beforeAll(): Unit = {
    // http client (you *can* keep this Future backend, but see note below)
    backend = HttpClientFutureBackend()

    val config = Config.load()
    val client = new AlpacaTradingClient(config)
    val dal = new AlpacaDAL(client)

    val accountEndpoint = endpoint
      .get.in("api" / "alpaca" / "account")
      .out(jsonBody[AccountResponse])

    val routes = Http4sServerInterpreter[IO]()
      .toRoutes(accountEndpoint.serverLogic(_ => dal.getAccount()))
    val app = Logger.httpApp[IO](true, true)(routes.orNotFound)

    // Build server Resource and allocate it, keeping the release action
    val serverRes = EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(app)
      .build

    val (_, release) = serverRes.allocated.unsafeRunSync()
    serverRelease = release

    // Give the server a brief moment to bind the port (or poll with a socket check)
    Thread.sleep(500)
  }

  override def afterAll(): Unit = {
    // Release the server cleanly; no fibers, no interrupts
    serverRelease
      .timeoutTo(5.seconds, IO.unit)    // don’t hang tests forever
      .unsafeRunSync()

    if (backend != null) backend.close()
  }

  test("Integration test: Account endpoint") {
    val req = basicRequest
      .get(sttp.model.Uri.unsafeParse(s"$baseUrl/api/alpaca/account"))
      .response(asJson[AccountResponse])

    val resp = Await.result(req.send(backend), 10.seconds)
    resp.code.isSuccess shouldBe true
    resp.body.isRight shouldBe true
  }
}
