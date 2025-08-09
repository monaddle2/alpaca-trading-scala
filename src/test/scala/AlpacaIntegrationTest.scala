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

class AlpacaIntegrationTest extends AnyFunSuite with Matchers with BeforeAndAfterAll with LazyLogging:
  
  private val baseUrl = "http://localhost:8080"
  private var serverFiber: FiberIO[Unit] = _
  private var shutdownSignal: Deferred[IO, Unit] = _
  private var backend: SttpBackend[Future, Any] = _
  
  override def beforeAll(): Unit =
    logger.info("Starting test server...")
    
    // Create HTTP client backend
    backend = HttpClientFutureBackend()
    
    // Create server components
    val config = Config.load()
    val client = new AlpacaTradingClient(config)
    val dal = new AlpacaDAL(client)
    
    // Define the account endpoint
    val accountEndpoint = endpoint
      .get
      .in("api" / "alpaca" / "account")
      .out(jsonBody[AccountResponse])
      .description("Get Alpaca account information")
      .tag("Alpaca")
    
    // Define the account server logic
    val accountServerLogic = accountEndpoint.serverLogic { _ =>
      dal.getAccount()
    }
    
    // Create the routes
    val accountRoutes = Http4sServerInterpreter[IO]()
      .toRoutes(accountServerLogic)
    
    // Add logging middleware
    val loggedRoutes = Logger.httpApp[IO](true, true)(accountRoutes.orNotFound)
    
    // Create shutdown signal
    shutdownSignal = Deferred[IO, Unit].unsafeRunSync()
    
    // Create and start the server
    val server = EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(loggedRoutes)
      .build
    
    // Start the server in the background
    serverFiber = server.use { _ =>
      IO.println("Test server started on http://localhost:8080") *> 
      shutdownSignal.get
    }.start.unsafeRunSync()
    
    // Wait a bit for the server to start
    Thread.sleep(2000)
    logger.info("Test server started successfully")
  
  override def afterAll(): Unit =
    logger.info("Stopping test server...")
    
    if shutdownSignal != null then
      shutdownSignal.complete(()).unsafeRunSync()
    
    if serverFiber != null then
      // Run cancellation in a background thread with timeout
      val cancellationThread = new Thread(() => {
        try
          serverFiber.cancel.unsafeRunSync()
        catch
          case _: Exception =>
            logger.warn("Background cancellation failed")
      })
      
      cancellationThread.start()
      
      // Wait for the thread to complete with timeout
      val cancellationTimeout = 1000L // 3 seconds
      try
        cancellationThread.join(cancellationTimeout)
        if cancellationThread.isAlive then
          logger.warn("Cancellation thread is still running, interrupting...")
          cancellationThread.interrupt()
      catch
        case _: InterruptedException =>
          logger.warn("Cancellation thread interrupted")
    
    if backend != null then
      backend.close()
  
  test("Integration test: Account endpoint") {
    logger.info("Starting integration test...")
    
    // Make request to the account endpoint using the shared backend
    val request = basicRequest
      .get(sttp.model.Uri.unsafeParse(s"$baseUrl/api/alpaca/account"))
      .response(asJson[AccountResponse])
    
    val response = Await.result(request.send(backend), 10.seconds)
    
    // Verify the response
    response.code.isSuccess shouldBe true
    response.body.isRight shouldBe true
    
    val account = response.body.toOption.get
    account.id should not be empty
    account.accountNumber should not be empty
    account.status shouldBe "ACTIVE"
    account.currency shouldBe "USD"
    account.buyingPower should not be empty
    account.cash should not be empty
    account.portfolioValue should not be empty
    account.patternDayTrader shouldBe false
    account.tradingBlocked shouldBe false
    account.createdAt should not be null
    
    logger.info(s"✅ Account test passed! Account ID: ${account.id}, Status: ${account.status}")
  }
