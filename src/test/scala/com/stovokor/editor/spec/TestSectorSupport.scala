package com.stovokor.editor.spec

import com.stovokor.editor.model.Point
import com.stovokor.editor.model.repository.SectorRepository
import com.stovokor.editor.model.Polygon
import com.stovokor.util.PointClicked
import com.stovokor.util.EventBus
import com.stovokor.editor.model.repository.BorderRepository
import com.stovokor.editor.model.Line
import com.stovokor.util.PointDragged

trait TestSectorSupport {

  def sectorDefinedByPoints(points: Point*) = {
    val ps = Polygon(points.toList).pointsSorted
    val stream = Stream continually (ps ++ ps.init sliding ps.length) flatten
    val lists = stream take ps.length
    SectorRepository()
      .findByPoint(ps.head)
      .find(s => lists.contains(s._2.polygon.pointsSorted))
      .isDefined
  }

  def borderDefinedByPoints(points: Point*) = {
    val found = points.sliding(2)
      .map(ps => Line((ps(0), ps(1))))
      .map(l => BorderRepository().find(l))
      .toList
    val allLinesFound = found.forall(l => !l.isEmpty)
    val sectors = found.flatMap(l => l)
      .flatMap(b => List(b._2.sectorA, b._2.sectorB))
      .toSet
      .size
    allLinesFound && sectors == 2
  }

  def makeClicks(points: Point*) {
    points.foreach(p => EventBus.trigger(PointClicked(p)))
  }

  def drag(from: Point, to: Point) {
    EventBus.trigger(PointDragged(from, to))
  }
}