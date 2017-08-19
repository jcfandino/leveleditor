package com.stovokor.editor.spec

import com.stovokor.editor.model.Point
import com.stovokor.editor.model.repository.SectorRepository
import com.stovokor.editor.model.Polygon

trait TestSectorSupport {

  def sectorDefinedByPoints(points: List[Point]) = {
    val ps = Polygon(points).pointsSorted
    val stream = Stream continually (ps ++ ps.init sliding ps.length) flatten
    val lists = stream take ps.length
    SectorRepository()
      .find(ps.head)
      .find(s => lists.contains(s._2().polygon.pointsSorted))
      .isDefined
  }
}