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
  pendingTransferOut: Option[String],
  pendingTransferIn: Option[String],
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
  given Decoder[Account] = for {
    id <- Decoder[String].at("id")
    accountNumber <- Decoder[String].at("account_number")
    status <- Decoder[String].at("status")
    cryptoStatus <- Decoder[Option[String]].at("crypto_status")
    currency <- Decoder[String].at("currency")
    buyingPower <- Decoder[String].at("buying_power")
    regtBuyingPower <- Decoder[String].at("regt_buying_power")
    daytradingBuyingPower <- Decoder[String].at("daytrading_buying_power")
    nonMarginableBuyingPower <- Decoder[String].at("non_marginable_buying_power")
    cash <- Decoder[String].at("cash")
    accruedFees <- Decoder[String].at("accrued_fees")
    pendingTransferOut <- Decoder[Option[String]].at("pending_transfer_out")
    pendingTransferIn <- Decoder[Option[String]].at("pending_transfer_in")
    portfolioValue <- Decoder[String].at("portfolio_value")
    patternDayTrader <- Decoder[Boolean].at("pattern_day_trader")
    tradingBlocked <- Decoder[Boolean].at("trading_blocked")
    transfersBlocked <- Decoder[Boolean].at("transfers_blocked")
    accountBlocked <- Decoder[Boolean].at("account_blocked")
    createdAt <- Decoder[Instant].at("created_at")
    tradeSuspendedByUser <- Decoder[Boolean].at("trade_suspended_by_user")
    multiplier <- Decoder[String].at("multiplier")
    shortingEnabled <- Decoder[Boolean].at("shorting_enabled")
    equity <- Decoder[String].at("equity")
    lastEquity <- Decoder[String].at("last_equity")
    longMarketValue <- Decoder[String].at("long_market_value")
    shortMarketValue <- Decoder[String].at("short_market_value")
    initialMargin <- Decoder[String].at("initial_margin")
    maintenanceMargin <- Decoder[String].at("maintenance_margin")
    lastMaintenanceMargin <- Decoder[String].at("last_maintenance_margin")
    sma <- Decoder[String].at("sma")
    daytradeCount <- Decoder[Int].at("daytrade_count")
  } yield Account(
    id, accountNumber, status, cryptoStatus, currency, buyingPower, regtBuyingPower,
    daytradingBuyingPower, nonMarginableBuyingPower, cash, accruedFees,
    pendingTransferOut, pendingTransferIn, portfolioValue, patternDayTrader,
    tradingBlocked, transfersBlocked, accountBlocked, createdAt, tradeSuspendedByUser,
    multiplier, shortingEnabled, equity, lastEquity, longMarketValue, shortMarketValue,
    initialMargin, maintenanceMargin, lastMaintenanceMargin, sma, daytradeCount
  )
  
  given Encoder[Account] = deriveEncoder[Account]

object Order:
  given Decoder[Order] = for {
    id <- Decoder[String].at("id")
    clientOrderId <- Decoder[String].at("client_order_id")
    createdAt <- Decoder[Instant].at("created_at")
    updatedAt <- Decoder[Instant].at("updated_at")
    submittedAt <- Decoder[Instant].at("submitted_at")
    filledAt <- Decoder[Option[Instant]].at("filled_at")
    expiredAt <- Decoder[Option[Instant]].at("expired_at")
    canceledAt <- Decoder[Option[Instant]].at("canceled_at")
    failedAt <- Decoder[Option[Instant]].at("failed_at")
    replacedAt <- Decoder[Option[Instant]].at("replaced_at")
    replacedBy <- Decoder[Option[String]].at("replaced_by")
    replaces <- Decoder[Option[String]].at("replaces")
    assetId <- Decoder[String].at("asset_id")
    symbol <- Decoder[String].at("symbol")
    assetClass <- Decoder[String].at("asset_class")
    notional <- Decoder[Option[String]].at("notional")
    qty <- Decoder[Option[String]].at("qty")
    filledQty <- Decoder[String].at("filled_qty")
    filledAvgPrice <- Decoder[Option[String]].at("filled_avg_price")
    orderClass <- Decoder[String].at("order_class")
    orderType <- Decoder[String].at("order_type")
    side <- Decoder[String].at("side")
    timeInForce <- Decoder[String].at("time_in_force")
    limitPrice <- Decoder[Option[String]].at("limit_price")
    stopPrice <- Decoder[Option[String]].at("stop_price")
    status <- Decoder[String].at("status")
    extendedHours <- Decoder[Boolean].at("extended_hours")
    legs <- Decoder[Option[List[Order]]].at("legs")
    trailPercent <- Decoder[Option[String]].at("trail_percent")
    trailPrice <- Decoder[Option[String]].at("trail_price")
    hwm <- Decoder[Option[String]].at("hwm")
  } yield Order(
    id, clientOrderId, createdAt, updatedAt, submittedAt, filledAt, expiredAt,
    canceledAt, failedAt, replacedAt, replacedBy, replaces, assetId, symbol,
    assetClass, notional, qty, filledQty, filledAvgPrice, orderClass, orderType,
    side, timeInForce, limitPrice, stopPrice, status, extendedHours,
    legs, trailPercent, trailPrice, hwm
  )
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
