package com.stovokor.editor.gui

import com.jme3.scene.shape.Box
import com.jme3.scene.shape.Quad
import com.jme3.scene.Mesh
import com.jme3.scene.VertexBuffer.Type
import com.stovokor.util.CenteredQuad

object K {

  val vertexBoxSize = 0.15f
  val lineBoxSize = 0.2f
  val sectorBoxSize = 0.2f

  val vertexBox = CenteredQuad(vertexBoxSize)
  val lineBox = CenteredQuad(lineBoxSize)
  val sectorBox = CenteredQuad(sectorBoxSize)

}