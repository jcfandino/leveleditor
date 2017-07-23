package com.stovokor.editor.model

import com.stovokor.editor.model.repository.SectorRepository
import scala.util.Random
import com.stovokor.util.SectorUpdated
import com.stovokor.util.EventBus

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
    ceiling = Surface(2f, SurfaceTexture()),
    openWalls = List())

  //TODO move this out of the builder
  def build(repo: SectorRepository) = {
    println(s"drawing with neighbours $neighbours")
    def floorAndCeiling(nextSector: Option[Sector]) = {
      val floor = nextSector.map(_.floor)
        .orElse(Some(Surface(Random.nextFloat, SurfaceTexture()))).get
      val ceiling = nextSector.map(_.ceiling)
        .orElse(Some(Surface(2 + Random.nextFloat, SurfaceTexture()))).get
      (floor, ceiling)
    }
    def updateNeighbour(id: Long, sector: Sector, border: List[Line]) {
      println(s"updating neighbour $id, $border")
      // The line needs to be reversed. is this always the case?
      val updatedWalls = sector.openWalls ++ border.map(l => Wall(l.reverse, SurfaceTexture()))
      println(s" -> updated walls $updatedWalls")
      val updatedSector = sector.updatedOpenWalls(updatedWalls)
      repo.update(id, updatedSector)
      EventBus.trigger(SectorUpdated(id, updatedSector))
    }
    val neighbourSectors = neighbours.toList.sorted.map(id => (id, repo.get(id)))
    val (floor, ceiling) = floorAndCeiling(neighbourSectors.find(_ => true).map(_._2))

    val polygon = polygonBuilder.build
    val borders = neighbourSectors.map(pair => pair match {
      case (id, other) => (id, other, polygon.borderWith(other.polygon))
    })
    borders.foreach(b => updateNeighbour(b._1, b._2, b._3))
    val openWalls = neighbourSectors
      .flatMap(other => polygon.borderWith(other._2.polygon))
      .map(l => Wall(l, SurfaceTexture()))
    val newSector = Sector(polygon, floor, ceiling, openWalls)
    repo.add(newSector)
  }
}