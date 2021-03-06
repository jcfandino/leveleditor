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
    allLinesFound && sectors > 1
  }

  def borderBetweenSectors(pointSectorA: Point, pointSectorB: Point) = {
    val resultA = SectorRepository().findInside(pointSectorA)
    val resultB = SectorRepository().findInside(pointSectorB)
    resultA.foreach(x => println(s" - $x"))
    resultB.foreach(x => println(s" - $x"))
    if (!resultA.isEmpty && !resultB.isEmpty) {
      val (idA, sectorA) = resultA.head
      val (idB, sectorB) = resultB.head
      BorderRepository().findFrom(idA).map(_._2).exists(_.sectorB == idB)
    } else false
  }

  def holeDefinedByPoints(points: Point*) = {
    val ps = Polygon(points.toList).pointsSorted
    val stream = Stream continually (ps ++ ps.init sliding ps.length) flatten
    val lists = stream take ps.length
    SectorRepository()
      .findByPoint(ps.head)
      .find(s => s._2.holes.find(h => lists.contains(h.pointsSorted)).isDefined)
      .isDefined
  }

  def makeClicks(points: Point*) {
    points.foreach(p => EventBus.trigger(PointClicked(p)))
  }

  def drag(from: Point, to: Point) {
    EventBus.trigger(PointDragged(from, to))
  }
}