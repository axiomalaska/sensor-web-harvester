package com.axiomalaska.sos.source.data

import com.axiomalaska.sos.data.SosPhenomenon

/**
 * A SosPhenomenon built from the DatabasePhenomenon
 */
class LocalPhenomenon(val databasePhenomenon:DatabasePhenomenon) extends SosPhenomenon {
  def getName() = databasePhenomenon.name
  def getId() = "http://mmisw.org/ont/ioos/parameter/" + databasePhenomenon.tag
  def getUnits() = databasePhenomenon.units
}