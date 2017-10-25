package com.stovokor.util

import com.stovokor.editor.model.SurfaceTexture
import com.stovokor.editor.model.repository.BorderRepository
import com.stovokor.editor.model.repository.SectorRepository

object SectorSurfaceMutator {
  val sectorRepository = SectorRepository()
  val borderRepository = BorderRepository()

  def mutate(sectorId: Long, target: String, mutation: SurfaceTexture => SurfaceTexture) {
    val sector = sectorRepository.get(sectorId)
    val updated =
      if (target == "floor") {
        sector.updatedFloor(sector.floor.updateTexture(mutation(sector.floor.texture)))
      } else if (target == "ceiling") {
        sector.updatedCeiling(sector.ceiling.updateTexture(mutation(sector.ceiling.texture)))
      } else if (target.startsWith("wall-")) {
        val idx = target.replace("wall-", "").toInt
        val wall = sector.closedWalls(idx)
        sector.updatedClosedWall(idx, wall.updateTexture(mutation(wall.texture)))
      } else if (target.startsWith("borderLow-")) {
        val idx = target.replace("borderLow-", "").toInt
        val wall = sector.openWalls(idx)
        borderRepository.find(wall.line).foreach(pair => pair match {
          case (id, border) => {
            borderRepository.update(id, border.updateSurfaceFloor(
              border.surfaceFloor.updateTexture(mutation(border.surfaceFloor.texture))))
          }
        })
        sector
      } else if (target.startsWith("borderHi-")) {
        val idx = target.replace("borderHi-", "").toInt
        val wall = sector.openWalls(idx)
        borderRepository.find(wall.line).foreach(pair => pair match {
          case (id, border) => {
            borderRepository.update(id, border.updateSurfaceCeiling(
              border.surfaceCeiling.updateTexture(mutation(border.surfaceCeiling.texture))))
          }
        })
        sector
      } else sector
    sectorRepository.update(sectorId, updated)
    EventBus.trigger(SectorUpdated(sectorId, updated))
  }
}