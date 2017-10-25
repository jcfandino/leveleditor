package com.stovokor.editor.model

object Surface {
  def apply(height: Float, texture: SurfaceTexture) = new Surface(height, texture)
  def apply(height: Float) = new Surface(height, SurfaceTexture(0, 1f))
  def empty = Surface(0f)
}

case class Surface(
    val height: Float,
    val texture: SurfaceTexture) {

  def move(d: Float) = updateHeight(height + d)
  def updateTexture(t: SurfaceTexture) = Surface(height, t)
  def updateHeight(h: Float) = Surface(h, texture)
}

object SurfaceTexture {
  def apply(
    index: Int = 0,
    texScaleX: Float = 1f,
    texScaleY: Float = 1f,
    texOffsetX: Float = 0f,
    texOffsetY: Float = 0f) = new SurfaceTexture(index, texScaleX, texScaleY, texOffsetX, texOffsetY)

  def apply(index: Int, texScale: Float) = new SurfaceTexture(index, texScale, texScale, 0f, 0f)

}

case class SurfaceTexture(
    val index: Int,
    val texScaleX: Float,
    val texScaleY: Float,
    val texOffsetX: Float,
    val texOffsetY: Float) {

  def uvx(x: Float) = x / texScaleX + (texOffsetX / texScaleX)
  def uvy(y: Float) = y / texScaleY + (texOffsetY / texScaleY)

  def move(dx: Float, dy: Float) = //TODO wrap values (1.1 => 0.1)
    SurfaceTexture(index, texScaleX, texScaleY, texOffsetX + dx, texOffsetY + dy)

  def scale(dx: Float, dy: Float) = SurfaceTexture(
    index,
    Math.max(0.01f, texScaleX + dx),
    Math.max(0.01f, texScaleY + dy),
    texOffsetX,
    texOffsetY)

  def updateIndex(idx: Int) =
    SurfaceTexture(idx, texScaleX, texScaleY, texOffsetX, texOffsetY)
}
