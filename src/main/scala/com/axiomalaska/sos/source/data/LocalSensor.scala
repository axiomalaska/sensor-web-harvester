package com.axiomalaska.sos.source.data

import com.axiomalaska.phenomena.Phenomenon
import com.axiomalaska.sos.data.SosNetwork
import com.axiomalaska.sos.data.SosSensor
import com.axiomalaska.sos.source.StationQuery
import scala.collection.JavaConversions._

class LocalSensor(
  val databaseSensor: DatabaseSensor,
  private val stationQuery: StationQuery) extends SosSensor {

  def getId(): String = {
    databaseSensor.tag
  }

  def getDescription() = databaseSensor.description

  def getPhenomena(): java.util.List[Phenomenon] = {
    val phenomena = stationQuery.getPhenomena(databaseSensor)
    phenomena.map(phenomenon => new LocalPhenomenon(phenomenon)).toList
  }

  /**
   * A list of networks this station is associated to
   * @return
   */
  def getNetworks(): java.util.List[SosNetwork] = {
    Nil
  }
}