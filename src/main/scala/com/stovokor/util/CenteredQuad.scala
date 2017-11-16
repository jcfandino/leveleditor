package com.stovokor.util

import com.jme3.scene.Mesh
import com.jme3.scene.VertexBuffer.Type

object CenteredQuad {
  def apply(size: Float) = {
    val quad = new CenteredQuad(size, size)
    quad.init
    quad
  }
}

class CenteredQuad(width: Float, height: Float) extends Mesh {

  def init() {
    val (hw, hh) = (width / 2, height / 2)
    setBuffer(Type.Position, 3, Array[Float](
      -hw, -hh, 0,
      hw, -hh, 0,
      hw, hh, 0,
      -hw, hh, 0))

    setBuffer(Type.TexCoord, 2, Array[Float](
      0, 0,
      1, 0,
      1, 1,
      0, 1))
    setBuffer(Type.Normal, 3, Array[Float](
      0, 0, 1,
      0, 0, 1,
      0, 0, 1,
      0, 0, 1))
    setBuffer(Type.Index, 3, Array[Short](
      0, 1, 2,
      0, 2, 3))
    updateBound()
    setStatic()
  }
}