package com.stovokor.editor.builder

import com.stovokor.editor.model.repository.SectorRepository
import com.stovokor.util.SectorUpdated
import com.stovokor.util.EventBus
import com.stovokor.editor.model.repository.BorderRepository
import com.stovokor.editor.factory.BorderFactory
import com.stovokor.editor.model.Line
import com.stovokor.editor.model.Point
import com.stovokor.editor.model.Polygon
import com.stovokor.editor.model.PolygonBuilder
import com.stovokor.editor.model.Sector
import com.stovokor.editor.model.Wall
import com.stovokor.editor.model.SurfaceTexture
import com.stovokor.editor.model.Surface
import com.stovokor.editor.factory.SectorFactory

object SectorBuilder {
  def start(p: Point) = new SectorBuilder(PolygonBuilder.start(p), List())
  def start(p: Point, id: Long) = new SectorBuilder(PolygonBuilder.start(p), List(id))
}

class SectorBuilder(
    val polygonBuilder: PolygonBuilder, val neighbours: List[Long]) {

  def first = polygonBuilder.first
  def last = polygonBuilder.last
  def size = polygonBuilder.size
  def lines = polygonBuilder.lines
  def points = polygonBuilder.points

  def add(p: Point) = new SectorBuilder(polygonBuilder.add(p), neighbours)
  def add(id: Long) = new SectorBuilder(polygonBuilder, neighbours ++ List(id))

  def isCuttingSector(sectorId: Long, sectorRepository: SectorRepository) = {
    //TODO this makes some problems, maybe I should delete it and force the user to
    // redraw the common line
    neighbours.size > 1 &&
      neighbours.head == neighbours.last &&
      neighbours.head == sectorId &&
      sectorRepository.get(sectorId).polygon.pointsUnsorted.contains(first)
    //    val value = neighbours.filter(sectorId.equals).size > 1 && neighbours.filter(neighbours(0).equals).size > 1
    //    println(s"isCuttingSector? $value")
    //    value
    // false
  }

  def build(sectorRepo: SectorRepository, borderRepo: BorderRepository) = {
    val polygon = polygonBuilder.build
    // check if this new polygon is inscribed inside another, in which case cut a hole
    val cuttingHole = points.map(p => sectorRepo.findInside(p, true))
    if (cuttingHole.isEmpty || !cuttingHole.forall(s => !s.isEmpty)) {
      // not a hole
      println("building new sector (not hole)")
      println(s"why not? ($cuttingHole) and (${!cuttingHole.forall(s => !s.isEmpty)})")
      SectorFactory.create(sectorRepo, borderRepo, polygon)
    } else {
      println("cutting hole in sector")
      val (id, sector) = findMostInnerSector(cuttingHole.flatMap(s => s))
      val updated = sectorRepo.update(id, sector.cutHole(polygon))
      EventBus.trigger(SectorUpdated(id, updated, true))
    }
  }

  def findMostInnerSector(sectors: List[(Long, Sector)]) = {
    sectors.sortBy(_._2.polygon.boundBox.area).head
  }
}
