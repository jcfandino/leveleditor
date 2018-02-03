package com.stovokor.editor.model.repository

import com.stovokor.editor.model.Point
import com.stovokor.editor.model.Sector
import com.stovokor.editor.model.Polygon
import com.stovokor.editor.model.BoundBox

object SectorIndex {
  def apply() = new CombinedSectorIndex
}

trait SectorIndex {

  def indexSector(id: Long, sector: Sector)
  def removeSector(id: Long)
  def find(point: Point): Set[Long]
}

class CombinedSectorIndex extends SectorIndex {
  val solidIndex = new SectorIndexSolid
  val pointIndex = new SectorIndexPoints

  def findByPoint(point: Point): Set[Long] = {
    pointIndex.find(point)
  }

  def findInside(point: Point, ignoreHoles: Boolean): Set[Long] = {
    solidIndex.find(point, ignoreHoles)
  }

  def find(point: Point): Set[Long] = {
    // not needed.
    pointIndex.find(point) ++ solidIndex.find(point)
  }

  def indexSector(id: Long, sector: Sector): Unit = {
    solidIndex.indexSector(id, sector)
    pointIndex.indexSector(id, sector)
  }

  def removeSector(id: Long): Unit = {
    solidIndex.removeSector(id)
    pointIndex.removeSector(id)
  }

}

class SectorIndexPoints extends SectorIndex {

  var index: Map[Point, Set[Long]] = Map().withDefaultValue(Set())
  var inverted: Map[Long, Set[Point]] = Map().withDefaultValue(Set())

  def indexSector(id: Long, sector: Sector) {
    indexPoints(id, sector.polygon.pointsUnsorted ++ sector.holes.flatMap(_.pointsUnsorted))
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

class SectorIndexSolid extends SectorIndex {

  type IdSector = (Long, Sector)

  var fromX: List[IdSector] = List()
  var fromY: List[IdSector] = List()
  var toX: List[IdSector] = List()
  var toY: List[IdSector] = List()

  def indexSector(id: Long, sector: Sector) {
    def order(f: BoundBox => Float): IdSector => Float = (ids) => f(ids._2.polygon.boundBox)

    removeSector(id) // need to remove previous value first
    fromX = ((id, sector) :: fromX).sortBy(order(_.from.x))
    fromY = ((id, sector) :: fromY).sortBy(order(_.from.y))
    toX = ((id, sector) :: toX).sortBy(order(_.to.x))
    toY = ((id, sector) :: toY).sortBy(order(_.to.y))
  }

  def removeSector(id: Long) {
    def filter(col: List[IdSector]) = col.filterNot(_._1 == id)
    fromX = filter(fromX)
    fromY = filter(fromY)
    toX = filter(toX)
    toY = filter(toY)
  }

  // inside polygon? ignore holes
  def find(point: Point) = {
    find(point, false)
  }

  def find(point: Point, ignoreHoles: Boolean) = {
    def box(ids: IdSector) = ids._2.polygon.boundBox
    val startBeforeX = fromX.takeWhile(s => box(s).from.x < point.x)
    val startBeforeY = fromY.takeWhile(s => box(s).from.y < point.y)
    val endAfterX = toX.dropWhile(s => box(s).to.x < point.x)
    val endAfterY = toY.dropWhile(s => box(s).to.y < point.y)
    val intersection = startBeforeX.intersect(endAfterX).intersect(startBeforeY.intersect(endAfterY))
    intersection
      .groupBy(_._1)
      .flatMap(r => List(r._2.head))
      .filter(pair =>
        if (ignoreHoles) pair._2.insidePolygon(point)
        else pair._2.inside(point))
      .map(_._1)
      .toSet
  }
}
