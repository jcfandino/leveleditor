package com.stovokor.editor.model

abstract class SelectionUnit() {
  def getPoints: Set[Point]
}
case class SelectionPoint(point: Point) extends SelectionUnit() {
  def getPoints = Set(point)
}
case class SelectionLine(line: Line) extends SelectionUnit {
  def getPoints = Set(line.a, line.b)
}
case class SelectionSector(sectorId: Long, sector: Sector) extends SelectionUnit {
  def getPoints = sector.polygon.pointsUnsorted.toSet
}
