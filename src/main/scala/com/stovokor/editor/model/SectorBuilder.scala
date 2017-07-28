package com.stovokor.editor.model

import com.stovokor.editor.model.repository.SectorRepository
import com.stovokor.util.SectorUpdated
import com.stovokor.util.EventBus
import com.stovokor.editor.model.repository.BorderRepository
import com.stovokor.editor.model.creation.BorderFactory

object SectorBuilder {
  def start(p: Point) = new SectorBuilder(PolygonBuilder.start(p), Set())
  def start(p: Point, id: Long) = new SectorBuilder(PolygonBuilder.start(p), Set(id))
}

class SectorBuilder(
    val polygonBuilder: PolygonBuilder, val neighbours: Set[Long]) {

  def first = polygonBuilder.first
  def last = polygonBuilder.last
  def size = polygonBuilder.size
  def lines = polygonBuilder.lines
  def points = polygonBuilder.points

  def add(p: Point) = new SectorBuilder(polygonBuilder.add(p), neighbours)
  def add(id: Long) = new SectorBuilder(polygonBuilder, neighbours ++ Set(id))

  def build() = Sector(
    polygon = polygonBuilder.build,
    floor = Surface(0f, SurfaceTexture()),
    ceiling = Surface(3f, SurfaceTexture()),
    openWalls = List())

  def build(sectorRepo: SectorRepository, borderRepo: BorderRepository) = {
    SectorFactory.create(sectorRepo, borderRepo)
  }

  object SectorFactory {

    def floorAndCeiling(nextSector: Option[Sector]) = {
      val floor = nextSector.map(_.floor)
        .orElse(Some(Surface(0f, SurfaceTexture()))).get
      val ceiling = nextSector.map(_.ceiling)
        .orElse(Some(Surface(3f, SurfaceTexture()))).get
      //      (floor, ceiling) // TODO uncomment, this is a test
      (Surface(1f, SurfaceTexture()), Surface(2f, SurfaceTexture()))
    }

    def updateNeighbour(id: Long, sector: Sector, border: List[Line]) = {
      println(s"updating neighbour $id, $border")
      // The line needs to be reversed. is this always the case?
      // TODO Find the right texture.
      val updatedWalls = sector.openWalls ++ border.map(l => Wall(l.reverse, SurfaceTexture()))
      println(s" -> updated walls $updatedWalls")
      sector.updatedOpenWalls(updatedWalls)
    }

    def create(sectorRepo: SectorRepository, borderRepo: BorderRepository) = {
      println(s"drawing with neighbours $neighbours")
      val neighbourSectors = neighbours.toList.sorted.map(id => (id, sectorRepo.get(id)))
      val (floor, ceiling) = floorAndCeiling(neighbourSectors.find(_ => true).map(_._2))

      val polygon = polygonBuilder.build
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