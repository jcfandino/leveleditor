package com.stovokor.editor.model

object Sector {
  def apply(polygon: Polygon, floor: Surface, ceiling: Surface, openWalls: List[Wall]) =
    new Sector(polygon, floor, ceiling, openWalls, defaultClosedWalls(polygon, openWalls))

  def apply(polygon: Polygon, floor: Surface, ceiling: Surface, openWalls: List[Wall], closedWalls: List[Wall]) =
    new Sector(polygon, floor, ceiling, openWalls, closedWalls)

  def defaultClosedWalls(polygon: Polygon, openWalls: List[Wall]) = polygon.lines
    .map(l => Wall(l, SurfaceTexture(1f)))
    .filterNot(openWalls.contains)
}

case class Sector(
    val polygon: Polygon,
    val floor: Surface,
    val ceiling: Surface,
    val openWalls: List[Wall],
    val closedWalls: List[Wall]) {

  def updatedPolygon(updated: Polygon) = Sector(updated, floor, ceiling, openWalls, closedWalls)
  def updatedFloor(updated: Surface) = Sector(polygon, updated, ceiling, openWalls, closedWalls)
  def updatedCeiling(updated: Surface) = Sector(polygon, floor, updated, openWalls, closedWalls)

  def updatedOpenWalls(updated: List[Wall]) = Sector(polygon, floor, ceiling, updated,
    closedWalls.filterNot(updated.contains))

  def updatedClosedWalls(updated: List[Wall]) = Sector(polygon, floor, ceiling,
    openWalls.filterNot(updated.contains), updated)

  def updatedClosedWall(idx: Int, updated: Wall) = Sector(polygon, floor, ceiling,
    openWalls.filterNot(updated.equals),
    closedWalls.updated(idx, updated))

  def moveSinglePoint(point: Point, dx: Float, dy: Float) = {
    def updateWalls(walls: List[Wall]) = {
      walls.map(wall => {
        val line = wall.line
        if (line.a == point) wall.updateLine(Line(point.move(dx, dy), line.b))
        else if (line.b == point) wall.updateLine(Line(line.a, point.move(dx, dy)))
        else wall
      })
    }
    updatedPolygon(polygon.changePoint(point, point.move(dx, dy)))
      .updatedOpenWalls(updateWalls(openWalls))
      .updatedClosedWalls(updateWalls(closedWalls))
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
}

object Surface {
  def apply(height: Float, texture: SurfaceTexture) = new Surface(height, texture)
  def apply(height: Float) = new Surface(height, SurfaceTexture(1f))
}

case class Surface(
    val height: Float,
    val texture: SurfaceTexture) {

  def move(d: Float) = Surface(height + d, texture)
  def updateTexture(t: SurfaceTexture) = Surface(height, t)
}

object SurfaceTexture {
  def apply(
    texScaleX: Float = 1f,
    texScaleY: Float = 1f,
    texOffsetX: Float = 0f,
    texOffsetY: Float = 0f) = new SurfaceTexture(texScaleX, texScaleY, texOffsetX, texOffsetY)

  def apply(texScale: Float) = new SurfaceTexture(texScale, texScale, 0f, 0f)
}

case class SurfaceTexture(
    val texScaleX: Float,
    val texScaleY: Float,
    val texOffsetX: Float,
    val texOffsetY: Float) {

  def uvx(x: Float) = x / texScaleX + (texOffsetX / texScaleX)
  def uvy(y: Float) = y / texScaleY + (texOffsetY / texScaleY)

  def move(dx: Float, dy: Float) = //TODO wrap values (1.1 => 0.1)
    SurfaceTexture(texScaleX, texScaleY, texOffsetX + dx, texOffsetY + dy)

  def scale(dx: Float, dy: Float) = SurfaceTexture(
    Math.max(0.01f, texScaleX + dx),
    Math.max(0.01f, texScaleY + dy),
    texOffsetX,
    texOffsetY)
}

object Wall {
  def apply(line: Line, texture: SurfaceTexture) = new Wall(line, texture)
}
case class Wall(val line: Line, val texture: SurfaceTexture) {
  def updateTexture(t: SurfaceTexture) = Wall(line, t)
  def updateLine(updated: Line) = Wall(updated, texture)
}
