package com.stovokor.editor.model.repository

import com.stovokor.editor.model.Point
import com.stovokor.editor.model.Sector
import com.stovokor.editor.model.Polygon

object SectorIndex {
  def apply() = new SectorIndex
}

class SectorIndex {

  var index: Map[Point, Set[Long]] = Map().withDefaultValue(Set())
  var inverted: Map[Long, Set[Point]] = Map().withDefaultValue(Set())

  def indexSector(id: Long, sector: Sector) {
    indexPoints(id, sector.polygon.pointsUnsorted)
  }

  def removeSector(id: Long) {
    indexPoints(id, List())
  }

  def find(point: Point) = {
    index(point)
  }

  def indexPoints(sectorId: Long, points: List[Point]) {
    for (p <- inverted(sectorId)) {
      index = index.updated(p, index(p) -- Set(sectorId))
    }
    for (p <- points) {
      index = index.updated(p, index(p) ++ Set(sectorId))
    }
    inverted = inverted.updated(sectorId, points.toSet)
  }
}