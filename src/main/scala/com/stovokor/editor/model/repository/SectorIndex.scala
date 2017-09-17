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

  def findInside(point: Point): Set[Long] = {
    solidIndex.find(point)
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

class SectorIndexSolid extends SectorIndex {

  type IdSector = (Long, Sector)

  var fromX: List[IdSector] = List()
  var fromY: List[IdSector] = List()
  var toX: List[IdSector] = List()
  var toY: List[IdSector] = List()

  def indexSector(id: Long, sector: Sector) {
    def order(f: BoundBox => Float): IdSector => Float = (ids) => f(ids._2.polygon.boundBox)

    fromX = ((id, sector) :: fromX).sortBy(order(_.from.x))
    fromY = ((id, sector) :: fromY).sortBy(order(_.from.y))
    toX = ((id, sector) :: toX).sortBy(order(_.to.x))
    toY = ((id, sector) :: toY).sortBy(order(_.to.y))
    //    println(s"Index lists\nfromX = $fromX\nfromY = $fromY\ntoX = $toX\ntoY = $toY")
  }

  def removeSector(id: Long) {
    def filter(col: List[IdSector]) = col.filterNot(_._1 == id)
    fromX = filter(fromX)
    fromY = filter(fromY)
    toX = filter(toX)
    toY = filter(toY)
  }

  def find(point: Point) = {
    def box(ids: IdSector) = ids._2.polygon.boundBox
    val startBeforeX = fromX.takeWhile(s => box(s).from.x < point.x)
    val startBeforeY = fromY.takeWhile(s => box(s).from.y < point.y)
    val endAfterX = toX.dropWhile(s => box(s).to.x < point.x)
    val endAfterY = toY.dropWhile(s => box(s).to.y < point.y)
    val intersection = startBeforeX.intersect(endAfterX).intersect(startBeforeY.intersect(endAfterY))
    //  println(s"`````` startBeforeX = $startBeforeX")
    //  println(s"`````` startBeforeY = $startBeforeY")
    //  println(s"`````` endAfterX = $endAfterX")
    //  println(s"`````` endAfterY = $endAfterY")
    //  println(s"`````` box found? $intersection")
    intersection.filter(_._2.contains(point))
      .map(_._1)
      .toSet
  }
}
