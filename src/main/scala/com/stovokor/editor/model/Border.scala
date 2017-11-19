package com.stovokor.editor.model

import java.util.Objects

object Border {
  def apply(sectorA: Long, sectorB: Long, line: Line,
            surfaceFloor: Surface, surfaceCeiling: Surface,
            surfaceMiddle: Surface = Surface.empty) = new Border(
    sectorA, sectorB, line,
    surfaceFloor, surfaceCeiling,
    surfaceMiddle)
}
/**
 * A border (aka portal) between two sectors
 */
case class Border(val sectorA: Long, val sectorB: Long, val line: Line,
                  val surfaceFloor: Surface, val surfaceCeiling: Surface,
                  val surfaceMiddle: Surface) {

  override lazy val hashCode = Objects.hash(sectorA.asInstanceOf[Object], sectorB.asInstanceOf[Object], line, surfaceFloor, surfaceCeiling, surfaceMiddle)

  def updateAny(sectorAUpdated: Long = sectorA, sectorBUpdated: Long = sectorB, lineUpdated: Line = line,
                surfaceFloorUpdated: Surface = surfaceFloor, surfaceCeilingUpdated: Surface = surfaceCeiling,
                surfaceMiddleUpdated: Surface = surfaceMiddle) = Border(
    sectorAUpdated, sectorBUpdated, lineUpdated,
    surfaceFloorUpdated, surfaceCeilingUpdated,
    surfaceMiddleUpdated)

  def updateSectors(updatedA: Long, updatedB: Long) = updateAny(sectorAUpdated = updatedA, sectorBUpdated = updatedB)
  def updateLine(updated: Line) = updateAny(lineUpdated = updated)
  def updateSurfaceFloor(updated: Surface) = updateAny(surfaceFloorUpdated = updated)
  def updateSurfaceCeiling(updated: Surface) = updateAny(surfaceCeilingUpdated = updated)
  def updateSurfaceMiddle(updated: Surface) = updateAny(surfaceMiddleUpdated = updated)

  def updateHeights(from: Sector, other: Sector) = {
    val lowHeight = other.floor.height - from.floor.height
    val highHeight = from.ceiling.height - other.ceiling.height
    updateSurfaceFloor(surfaceFloor.updateHeight(lowHeight))
      .updateSurfaceCeiling(surfaceCeiling.updateHeight(highHeight))
  }
}