package com.stovokor.editor.model

object Border {
  def apply(sectorA: Long, sectorB: Long, line: Line,
            surfaceFloor: Surface, surfaceCeiling: Surface,
            surfaceMiddle: Surface = Surface.empty) = new Border(
    sectorA, sectorB, line,
    surfaceFloor, surfaceCeiling,
    surfaceMiddle = Surface.empty)
}
/**
 * A border (aka portal) between two sectors
 */
case class Border(val sectorA: Long, val sectorB: Long, val line: Line,
                  val surfaceFloor: Surface, val surfaceCeiling: Surface,
                  val surfaceMiddle: Surface) {

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
}