package com.stovokor.editor.gui

import com.jme3.math.ColorRGBA

object Palette {

  val origin = ColorRGBA.Orange
  val axisX = ColorRGBA.Red
  val axisY = ColorRGBA.Green
  val grid1 = ColorRGBA.DarkGray
  val grid2 = ColorRGBA.DarkGray.mult(0.6f)

  val selectedElement = ColorRGBA.Red
  val drawing = ColorRGBA.Green
  val hoveredSurfaceMin = new ColorRGBA(.8f, .8f, .8f, 1)
  val hoveredSurfaceMax = new ColorRGBA(1.4f, 1.4f, 1f, 1)

}