package com.example.alpaca.models

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*
import java.time.Instant

// Account models
case class Account(
  id: String,
  accountNumber: String,
  status: String,
  cryptoStatus: Option[String],
  currency: String,
  buyingPower: String,
  regtBuyingPower: String,
  daytradingBuyingPower: String,
  nonMarginableBuyingPower: String,
  cash: String,
  accruedFees: String,
  pendingTransferOut: String,
  pendingTransferIn: String,
  portfolioValue: String,
  patternDayTrader: Boolean,
  tradingBlocked: Boolean,
  transfersBlocked: Boolean,
  accountBlocked: Boolean,
  createdAt: Instant,
  tradeSuspendedByUser: Boolean,
  multiplier: String,
  shortingEnabled: Boolean,
  equity: String,
  lastEquity: String,
  longMarketValue: String,
  shortMarketValue: String,
  initialMargin: String,
  maintenanceMargin: String,
  lastMaintenanceMargin: String,
  sma: String,
  daytradeCount: Int
)

// Order models
case class Order(
  id: String,
  clientOrderId: String,
  createdAt: Instant,
  updatedAt: Instant,
  submittedAt: Instant,
  filledAt: Option[Instant],
  expiredAt: Option[Instant],
  canceledAt: Option[Instant],
  failedAt: Option[Instant],
  replacedAt: Option[Instant],
  replacedBy: Option[String],
  replaces: Option[String],
  assetId: String,
  symbol: String,
  assetClass: String,
  notional: Option[String],
  qty: Option[String],
  filledQty: String,
  filledAvgPrice: Option[String],
  orderClass: String,
  orderType: String,
  side: String,
  timeInForce: String,
  limitPrice: Option[String],
  stopPrice: Option[String],
  status: String,
  extendedHours: Boolean,
  legs: Option[List[Order]],
  trailPercent: Option[String],
  trailPrice: Option[String],
  hwm: Option[String]
)

// Position models
case class Position(
  assetId: String,
  symbol: String,
  exchange: String,
  assetClass: String,
  avgEntryPrice: String,
  qty: String,
  side: String,
  marketValue: String,
  costBasis: String,
  unrealizedPl: String,
  unrealizedPlpc: String,
  unrealizedIntradayPl: String,
  unrealizedIntradayPlpc: String,
  currentPrice: String,
  lastdayPrice: String,
  changeToday: String
)

// Bar (OHLCV) models
case class Bar(
  t: Instant,
  o: BigDecimal,
  h: BigDecimal,
  l: BigDecimal,
  c: BigDecimal,
  v: Long,
  n: Long,
  vw: BigDecimal
)

// Trade models
case class Trade(
  t: Instant,
  x: String,
  p: BigDecimal,
  s: Int,
  c: List[String],
  i: Long,
  z: String
)

// Quote models
case class Quote(
  t: Instant,
  ax: String,
  ap: BigDecimal,
  as: Int,
  bx: String,
  bp: BigDecimal,
  bs: Int,
  c: List[String],
  z: String
)

// Given encoder/decoder instances
object Account:
  given Decoder[Account] = deriveDecoder[Account]
  given Encoder[Account] = deriveEncoder[Account]

object Order:
  given Decoder[Order] = deriveDecoder[Order]
  given Encoder[Order] = deriveEncoder[Order]

object Position:
  given Decoder[Position] = deriveDecoder[Position]
  given Encoder[Position] = deriveEncoder[Position]

object Bar:
  given Decoder[Bar] = deriveDecoder[Bar]
  given Encoder[Bar] = deriveEncoder[Bar]

object Trade:
  given Decoder[Trade] = deriveDecoder[Trade]
  given Encoder[Trade] = deriveEncoder[Trade]

object Quote:
  given Decoder[Quote] = deriveDecoder[Quote]
  given Encoder[Quote] = deriveEncoder[Quote]
