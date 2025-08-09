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
import com.comcast.ip4s.*
import com.typesafe.scalalogging.LazyLogging
import com.example.alpaca.server.AlpacaServer
import com.example.alpaca.{AccountResponse}

class AlpacaIntegrationTest extends AnyFunSuite with Matchers with BeforeAndAfterAll with LazyLogging:

  private val baseUrl = "http://localhost:8080"
  private var serverRelease: IO[Unit] = IO.unit
  private var backend: SttpBackend[Future, Any] = _

  override def beforeAll(): Unit =
    // http client
    backend = HttpClientFutureBackend()

    val server = new AlpacaServer()
    
    // Create the app and server resource
    val serverResource = for
      app <- Resource.eval(server.createApp())
      serverRes <- server.createServerResource(app)
    yield serverRes

    // Build server Resource and allocate it, keeping the release action
    val (_, release) = serverResource.allocated.unsafeRunSync()
    serverRelease = release

    // Give the server a brief moment to bind the port
    Thread.sleep(500)

  override def afterAll(): Unit =
    // Release the server cleanly; no fibers, no interrupts
    serverRelease
      .timeoutTo(5.seconds, IO.unit)    // don't hang tests forever
      .unsafeRunSync()

    if (backend != null) backend.close()

  test("Integration test: Account endpoint") {
    val req = basicRequest
      .get(sttp.model.Uri.unsafeParse(s"$baseUrl/api/alpaca/account"))
      .response(asJson[AccountResponse])

    val resp = Await.result(req.send(backend), 10.seconds)
    resp.code.isSuccess shouldBe true
    resp.body.isRight shouldBe true
  }
