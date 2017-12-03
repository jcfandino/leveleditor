package com.stovokor.editor.model

import java.util.Objects

object Sector {
  def apply(polygon: Polygon, floor: Surface, ceiling: Surface, openWalls: List[Wall]) =
    new Sector(polygon, floor, ceiling, openWalls, defaultClosedWalls(polygon, openWalls))

  def apply(polygon: Polygon, floor: Surface, ceiling: Surface, openWalls: List[Wall], closedWalls: List[Wall]) =
    new Sector(polygon, floor, ceiling, openWalls, closedWalls)

  def defaultClosedWalls(polygon: Polygon, openWalls: List[Wall]) = polygon.lines
    .map(l => Wall(l, SurfaceTexture(0, 1f)))
    .filterNot(openWalls.contains)
}

case class Sector(
    val polygon: Polygon,
    val floor: Surface,
    val ceiling: Surface,
    val openWalls: List[Wall],
    val closedWalls: List[Wall],
    val holes: Set[Polygon] = Set()) {

  override lazy val hashCode = Objects.hash(polygon, floor, ceiling, openWalls, closedWalls, holes)

  def updatedPolygon(updated: Polygon) = Sector(updated, floor, ceiling, openWalls, closedWalls, holes)
  def updatedFloor(updated: Surface) = Sector(polygon, updated, ceiling, openWalls, closedWalls, holes)
  def updatedCeiling(updated: Surface) = Sector(polygon, floor, updated, openWalls, closedWalls, holes)

  def cutHole(hole: Polygon) = Sector(polygon, floor, ceiling,
    openWalls,
    closedWalls ++ (hole.pointsSorted ++ List(hole.pointsSorted.head))
      .reverse
      .sliding(2)
      .map(l => Line(l(0), l(1)))
      .map(l => Wall(l, SurfaceTexture())),
    holes ++ Set(hole))

  def updatedOpenWalls(updated: List[Wall]) = Sector(polygon, floor, ceiling, updated,
    closedWalls.filterNot(updated.contains), holes)

  def updatedClosedWalls(updated: List[Wall]) = Sector(polygon, floor, ceiling,
    openWalls.filterNot(updated.contains), updated, holes)

  def updatedClosedWall(idx: Int, updated: Wall) = Sector(polygon, floor, ceiling,
    openWalls.filterNot(updated.equals),
    closedWalls.updated(idx, updated), holes)

  def updatedHoles(updated: Set[Polygon]) = Sector(polygon, floor, ceiling, openWalls,
    closedWalls, updated)

  def moveSinglePoint(point: Point, dx: Float, dy: Float) = {
    def updateWalls(walls: List[Wall]) = {
      walls.map(wall => {
        val line = wall.line
        if (line.a == point) wall.updateLine(Line(point.move(dx, dy), line.b))
        else if (line.b == point) wall.updateLine(Line(line.a, point.move(dx, dy)))
        else wall
      }).filterNot(_.line.length == 0f)
    }
    updatedPolygon(polygon.changePoint(point, point.move(dx, dy)))
      .updatedHoles(holes.map(h => h.changePoint(point, point.move(dx, dy))))
      .updatedOpenWalls(updateWalls(openWalls))
      .updatedClosedWalls(updateWalls(closedWalls))
  }

  def openWall(line: Line) = {
    val wall = closedWalls.find(_.line.alike(line))
    if (wall.isDefined) {
      updatedClosedWalls(closedWalls.filterNot(_ == wall.get))
        .updatedOpenWalls(openWalls ++ List(wall.get))
    } else this
  }

  def closeWall(line: Line) = {
    val wall = openWalls.find(_.line.alike(line))
    if (wall.isDefined) {
      updatedOpenWalls(openWalls.filterNot(_ == wall.get))
        .updatedClosedWalls(closedWalls ++ List(wall.get))
    } else this
  }

  def addPoint(line: Line, factor: Float) = {
    def updateWalls(walls: List[Wall]) = {
      walls.flatMap(wall => {
        if (wall.line == line) {
          val (l1, l2) = line.split(factor)
          List(wall.updateLine(l1), wall.updateLine(l2))
        } else if (wall.line.reverse == line) { // If we got it reversed
          val (l1, l2) = line.split(1f - factor)
          List(wall.updateLine(l1), wall.updateLine(l2))
        } else List(wall)
      })
    }
    val newPolygon = polygon.addPoint(line, factor)
    updatedPolygon(newPolygon)
      .updatedOpenWalls(updateWalls(openWalls))
      .updatedClosedWalls(updateWalls(closedWalls))
  }

  def contains(point: Point) = {
    // this takes holes into consideration.
    polygon.contains(point) && holes.find(_.contains(point)).isEmpty
  }

  def triangulate = Triangulator.triangulate(polygon, holes)

  // TODO not used any more, but I need to use some parts from here maybe.
  def divideBy(cut: List[Point]): List[Sector] = {
    val polys = polygon.divideBy(cut)
    if (polys.size == 1) {
      List(this) // wasn't divided, don't do the rest
    } else {
      def other(poly: Polygon) = polys.filterNot(poly.equals).head
      def andReversedWall(w: Wall) = List(w) // TODO add reverse?: , w.updateLine(w.line.reverse))
      val closedByLine = closedWalls.flatMap(andReversedWall).groupBy(_.line).withDefault(_ => List())
      val openByLine = openWalls.flatMap(andReversedWall).groupBy(_.line).withDefault(_ => List())
      polys.map(poly => {
        val shared = poly.sharedLines(other(poly))
        val newClosedWalls = poly.lines.flatMap(l => {
          // check if there is already such wall, or create it
          val existing = closedByLine(l)
          if (shared.contains(l)) List()
          else if (!existing.isEmpty) existing
          else if (!openByLine(l).isEmpty) List()
          else List(Wall(l, SurfaceTexture()))
        })
        val newOpenWalls = poly.lines
          .flatMap(openByLine) ++
          shared.map(l => Wall(l, SurfaceTexture()))
        Sector(poly, floor, ceiling, newOpenWalls, newClosedWalls, holes)
      })
    }
  }
}

object Wall {
  def apply(line: Line, texture: SurfaceTexture) = new Wall(line, texture)
}
case class Wall(val line: Line, val texture: SurfaceTexture) {
  def updateTexture(t: SurfaceTexture) = Wall(line, t)
  def updateLine(updated: Line) = Wall(updated, texture)

  override lazy val hashCode = Objects.hash(line, texture)
}
