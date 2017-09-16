package com.stovokor.editor.model

import com.stovokor.editor.model.repository.SectorRepository
import com.stovokor.util.SectorUpdated
import com.stovokor.util.EventBus
import com.stovokor.editor.model.repository.BorderRepository
import com.stovokor.editor.model.creation.BorderFactory

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
    neighbours.filter(sectorId.equals).size > 1 && neighbours.filter(neighbours(0).equals).size > 1
//    false
    //TODO need to check it's not a border of the polygon something like
    // points.sliding(2).map(s => Line(s(0), s(1))).forall(polygon.lines.contains))
    // need to get the sector for that.
  }

  def build() = Sector(
    polygon = polygonBuilder.build,
    floor = Surface(0f, SurfaceTexture()),
    ceiling = Surface(3f, SurfaceTexture()),
    openWalls = List())

  def build(sectorRepo: SectorRepository, borderRepo: BorderRepository) = {
    val polygon = polygonBuilder.build
    SectorFactory.create(sectorRepo, borderRepo, polygon)
  }

  object SectorFactory {

    def floorAndCeiling(nextSector: Option[Sector]) = {
      val floor = nextSector.map(_.floor)
        .orElse(Some(Surface(0f, SurfaceTexture()))).get
      val ceiling = nextSector.map(_.ceiling)
        .orElse(Some(Surface(3f, SurfaceTexture()))).get
      (floor, ceiling)
    }

    def updateNeighbour(id: Long, sector: Sector, border: List[Line]) = {
      println(s"updating neighbour $id, $border")
      // The line needs to be reversed. is this always the case?
      // TODO Find the right texture.
      val updatedWalls = sector.openWalls ++ border.map(l => Wall(l.reverse, SurfaceTexture()))
      println(s" -> updated walls $updatedWalls")
      sector.updatedOpenWalls(updatedWalls)
    }

    def create(sectorRepo: SectorRepository, borderRepo: BorderRepository, polygon: Polygon) = {
      println(s"drawing with neighbours $neighbours")

      val neighbourSectors = polygon.lines.flatMap(sectorRepo.find)
      //      val neighbourSectors = neighbours.sorted.map(id => (id, sectorRepo.get(id)))
      val (floor, ceiling) = floorAndCeiling(neighbourSectors.find(_ => true).map(_._2))

      val borders = neighbourSectors.map(pair => pair match {
        case (id, other) => (id, other, polygon.borderWith(other.polygon))
      })
      val openWalls = neighbourSectors
        .flatMap(other => polygon.borderWith(other._2.polygon))
        .map(l => Wall(l, SurfaceTexture()))
      val newSector = Sector(polygon, floor, ceiling, openWalls)
      val newSectorId = sectorRepo.add(newSector)
      // update borders
      BorderFactory
        .createBorders(newSectorId, newSector, borders)
        .map(borderRepo.add)
      borders.foreach(b => {
        val (id, sector, border) = b
        val updatedSector = updateNeighbour(id, sector, border)
        sectorRepo.update(id, updatedSector)
        EventBus.trigger(SectorUpdated(id, updatedSector))
      })
      EventBus.trigger(SectorUpdated(newSectorId, newSector))
      (newSectorId, newSector)
    }
  }

}