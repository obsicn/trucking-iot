package com.orendainx.hortonworks.trucking.enrichment

import com.orendainx.hortonworks.trucking.commons.models.TruckEventTypes
import com.typesafe.config.ConfigFactory

import scala.util.Random

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object WeatherAPI {

  private val conf = ConfigFactory.load()

  /** Queries the weatherAPI for fog status.
    *
    * @param eventType The type of a driving event (e.g. "normal", "speeding", etc.)
    * @return true if the weather is foggy, false otherwise
    */
  def isFoggy(eventType: String): Boolean =
    if (eventType == TruckEventTypes.Normal) Random.nextInt(100) < conf.getInt("weatherapi.foggy.normal-chance")
    else Random.nextInt(100) < conf.getInt("weatherapi.foggy.anomalous-chance")

  /** Queries the weatherAPI for rain status.
    *
    * @param eventType The type of a driving event (e.g. "normal", "speeding", etc.)
    * @return true if the weather is rainy, false otherwise
    */
  def isRainy(eventType: String): Boolean =
    if (eventType == TruckEventTypes.Normal) Random.nextInt(100) < conf.getInt("weatherapi.rainy.normal-chance")
    else Random.nextInt(100) < conf.getInt("weatherapi.rainy.anomalous-chance")

  /** Queries the weatherAPI for wind status.
    *
    * @param eventType The type of a driving event (e.g. "normal", "speeding", etc.)
    * @return true if the weather is windy, false otherwise
    */
  def isWindy(eventType: String): Boolean =
    if (eventType == TruckEventTypes.Normal) Random.nextInt(100) < conf.getInt("weatherapi.windy.normal-chance")
    else Random.nextInt(100) < conf.getInt("weatherapi.windy.anomalous-chance")
}
