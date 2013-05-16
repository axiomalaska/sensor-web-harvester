package com.axiomalaska.sos.source

import org.apache.log4j.Logger
import scala.util.Random
import com.axiomalaska.phenomena.Phenomena
import com.axiomalaska.sos.data.PublisherInfo
import com.axiomalaska.sos.data.SosNetwork
import com.axiomalaska.sos.data.SosNetworkImp
import com.axiomalaska.sos.source.data.LocalPhenomenon
import java.util.Calendar

import collection.JavaConversions._

/**
 * This class manages updating the SOS with all the stations from the metadata database
 * 
 * @param databaseUrl - a database URL of the form <code>jdbc:<em>subprotocol</em>:<em>subname</em></code>
 * 	     example jdbc:postgresql://localhost:5432/sensor
 * @param databaseUser - the username for the database
 * @param databasePassword - the password for the database
 * @param sosUrl - The URL of the SOS example http://localhost:8080/sos/sos
 */
class SosSourcesManager(
    private val databaseUrl:String, 
	private val databaseUser:String, 
	private val databasePassword:String, 
	private val sosUrl:String, 
	private val publisherInfo:PublisherInfo, 
        private val sources: String,
	private val rootNetwork:SosNetwork,
	private val logger: Logger = Logger.getRootLogger()) {

  private val random = new Random(Calendar.getInstance.getTime.getTime)
  
  def updateSos() {
    
    val factory = new ObservationUpdaterFactory()
    val queryBuilder = new StationQueryBuilder(
      databaseUrl, databaseUser, databasePassword)

    queryBuilder.withStationQuery(stationQuery => {
      val observationUpdaters = factory.buildAllSourceObservationUpdaters(
        sosUrl, stationQuery, publisherInfo, sources.toLowerCase, rootNetwork, logger)

      // load phenomenon
      val phenomena = stationQuery.getPhenomena
      for (phenom <- phenomena) {
        try {
          new LocalPhenomenon(phenom, stationQuery)
        } catch {
          case _ => {
              logger.debug("adding phenomenon to instance: " + phenom.tag)
              Phenomena.instance.createHomelessParameter(phenom.tag, "")
          }
        }
      }
      
      for (observationUpdater <- random.shuffle(observationUpdaters)) {
        observationUpdater.update(rootNetwork)
      }
    })
  }
  
  private def getSourceNetwork(sourceName: String) : SosNetworkImp = {
    val network = new SosNetworkImp()
    network.setDescription(sourceName + " networked stations")
    network.setId("network-" + sourceName)
    network.setSourceId(sourceName)
    network.setShortName(sourceName + " stations")
    network.setLongName("stations in the " + sourceName + " network")
    return network
  }
}