package com.stovokor.editor.factory

import com.stovokor.editor.model.SurfaceTexture
import com.stovokor.editor.model.repository.SectorRepository
import com.stovokor.editor.model.Sector
import com.stovokor.util.SectorUpdated
import com.stovokor.editor.model.repository.BorderRepository
import com.stovokor.editor.model.Polygon
import com.stovokor.editor.model.Line
import com.stovokor.util.EventBus
import com.stovokor.editor.model.Surface
import com.stovokor.editor.model.Wall

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

  def create(sectorRepo: SectorRepository, borderRepo: BorderRepository, polygon: Polygon, holes: Set[Polygon] = Set()) = {
    val neighbourSectors = polygon.lines.flatMap(sectorRepo.find)

    val (floor, ceiling) = floorAndCeiling(neighbourSectors.find(_ => true).map(_._2))

    val borders = neighbourSectors.map(pair => pair match {
      case (id, other) => (id, other, polygon.borderWith(other.polygon) ++
        other.holes.flatMap(polygon.borderWith)) // also check with holes??
    })
    val openWalls = neighbourSectors
      .flatMap(other => polygon.borderWith(other._2.polygon) ++ other._2.holes.flatMap(polygon.borderWith))
      .map(l => Wall(l, SurfaceTexture()))
    val newSectorNoHoles = Sector(polygon, floor, ceiling, openWalls)
    val newSector = holes.foldLeft(newSectorNoHoles)((sec, hole) => sec.cutHole(hole))
    val newSectorId = sectorRepo.add(newSector)
    // update borders
    BorderFactory
      .createBorders(newSectorId, newSector, borders)
      .map(borderRepo.add)
    borders.foreach(b => {
      val (id, sector, border) = b
      val updatedSector = updateNeighbour(id, sector, border)
      sectorRepo.update(id, updatedSector)
      EventBus.trigger(SectorUpdated(id, updatedSector, true))
    })
    EventBus.trigger(SectorUpdated(newSectorId, newSector, true))
    (newSectorId, newSector)
  }
}