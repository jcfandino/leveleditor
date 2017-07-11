package com.stovokor.editor.tiles

object SpriteSheet {
  def apply(file: String,
            width: Int,
            height: Int,
            tileWidth: Int,
            tileHeight: Int) =
    new SpriteSheet(file, width / tileWidth, height / tileHeight)

  def apply(file: String,
            tilesX: Int,
            tilesY: Int) = new SpriteSheet(file, tilesX, tilesY)
}
class SpriteSheet(val file: String,
                  val tilesX: Int,
                  val tilesY: Int) {
  val percX = 1f / tilesX
  val percY = 1f / tilesY
  val numberOfTiles = tilesX / tilesY

  def getCoords(idx: Int): (Float, Float, Float, Float) = {
    val xo = percX * (idx % tilesX)
    val xe = xo + percX
    val yo = percY * (idx / tilesX)
    val ye = yo + percY
    (xo, xe, yo, ye)
  }
}
