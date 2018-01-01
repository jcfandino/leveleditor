package com.stovokor.editor.gui

import com.jme3.math.ColorRGBA

// TODO load this from the settings file.
object Palette {

  val origin = ColorRGBA.Orange
  val axisX = ColorRGBA.Red
  val axisY = ColorRGBA.Green
  val grid1 = ColorRGBA.DarkGray
  val grid2 = ColorRGBA.DarkGray.mult(0.6f)
  val grid3 = ColorRGBA.Cyan.mult(0.5f)

  val title = ColorRGBA.Orange
  val background = new ColorRGBA(.0f, 0.05f, 0.1f, 1)
  val buttonSelected = ColorRGBA.Blue

  val selectedElement = ColorRGBA.Red
  val drawing = ColorRGBA.Green
  val hoveredSurfaceMin = new ColorRGBA(.8f, .8f, .8f, 1)
  val hoveredSurfaceMax = new ColorRGBA(1.4f, 1.4f, 1f, 1)

  val helpBackground = new ColorRGBA(0, 0, 0, .6f)
  val helpForeground = ColorRGBA.White

  val lineOpenWall = ColorRGBA.Brown.mult(2)
  val lineClosedWall = ColorRGBA.LightGray
  val vertex = ColorRGBA.LightGray
  val sectorCenter = ColorRGBA.LightGray
}