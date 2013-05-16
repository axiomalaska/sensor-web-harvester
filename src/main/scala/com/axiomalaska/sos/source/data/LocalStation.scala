package com.axiomalaska.sos.source.data

import com.axiomalaska.sos.data.SosSensor
import com.axiomalaska.sos.data.Location
import com.axiomalaska.sos.data.SosStation
import com.axiomalaska.sos.source.StationQuery
import scala.collection.JavaConversions._
import com.axiomalaska.sos.data.SosNetwork
import scala.collection.mutable

class LocalStation(val localSource:LocalSource, 
    val databaseStation: DatabaseStation,
  private val stationQuery: StationQuery,
  private val rootNetwork:SosNetwork) extends SosStation {

  private var networks: java.util.List[SosNetwork] = new java.util.ArrayList()

  /**
   * A list of phenomena that this station has readings for
   */
  def getSensors(): java.util.List[SosSensor] = {
    val sensors = stationQuery.getActiveSensors(databaseStation)
    sensors.map(sensor => new LocalSensor(sensor, stationQuery))
  }

  /**
   * This ID should be unique for each station. For example '11111'
   */
  def getId() = databaseStation.tag

  /**
   * The default name of the location with which the station takes its
   * reading from.
   *
   * Maximum characters 80
   *
   * If characters are over 100 they will be truncated to 80
   */
  def getFeatureOfInterestName() =
    "station: " + databaseStation.name + " of source: " + localSource.getName

  /**
   * The location of the station
   */
  def getLocation() = 
    new Location(databaseStation.latitude, databaseStation.longitude)

  /**
   * A list of networks this station is associated to
   * @return
   */
  def getNetworks(): java.util.List[SosNetwork] = {
    val sourceNetworks = stationQuery.getNetworks(localSource.source).map(
        network => new LocalNetwork(network))
    val stationNetworks = stationQuery.getNetworks(databaseStation).map(
        network => new LocalNetwork(network))
    val combinedNetworks = rootNetwork :: sourceNetworks ::: stationNetworks
    val set = new mutable.HashSet[String]
    // fill in set with items in local network list
    for {
      network <- networks
      val networkId = network.getSourceId + network.getId
      if (!set.contains(networkId))
    } {
      set += networkId
    }
    // get networks from DB
    for {
      network <- sourceNetworks ::: stationNetworks
      val networkId = network.getSourceId + network.getId
      if (!set.contains(networkId))
    } {
      set += networkId
      addNetwork(network)
    }
    // return combo of DB and local networks
    networks
  }
  
  def addNetwork(network: SosNetwork) = {
    networks.add(network)
  }
  
  def setNetworks(nets: java.util.List[SosNetwork]) {
    networks.clear
    for (net <- nets) {
      networks.add(net)
    }
  }

  def isMoving = false
  
  def getSource() = localSource
  
  def getName() = databaseStation.name
  
  def getDescription() = databaseStation.description
    
  def getPlatformType() = databaseStation.platformType
  
  def getWmoId() = ""
  def getHistory() = new java.util.ArrayList[com.axiomalaska.sos.data.HistoryEvent]()
  def getDocumentation() = new java.util.ArrayList[com.axiomalaska.sos.data.DocumentMember]()
  def getSponsor() = ""
}
