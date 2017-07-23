package com.stovokor.editor.model

object Sector {
  def apply(polygon: Polygon, floor: Surface, ceiling: Surface, openWalls: List[Wall]) =
    new Sector(polygon, floor, ceiling, openWalls)
}

case class Sector(
    val polygon: Polygon,
    val floor: Surface,
    val ceiling: Surface,
    val openWalls: List[Wall]) {

  def closedWalls = polygon.lines
    .map(l => Wall(l, SurfaceTexture(1f)))
    .filterNot(openWalls.contains)

  def updatedPolygon(updated: Polygon) = Sector(updated, floor, ceiling, openWalls)
  def updatedFloor(updated: Surface) = Sector(polygon, updated, ceiling, openWalls)
  def updatedCeiling(updated: Surface) = Sector(polygon, floor, updated, openWalls)
  def updatedOpenWalls(updated: List[Wall]) = Sector(polygon, floor, ceiling, updated)
}

object Surface {
  def apply(height: Float, texture: SurfaceTexture) = new Surface(height, texture)
  def apply(height: Float) = new Surface(height, SurfaceTexture(1f))
}

case class Surface(
  val height: Float,
  val texture: SurfaceTexture)

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
}

object Wall {
  def apply(line: Line, texture: SurfaceTexture) = new Wall(line, texture)
}
case class Wall(val line: Line, val texture: SurfaceTexture)
