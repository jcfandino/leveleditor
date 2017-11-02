package com.stovokor.util

import com.stovokor.editor.model.Point

object GridSnapper extends EditorEventListener {

  var snapToGrid = true
  var gridStep = 1f

  def initialize() {
    EventBus.subscribeByType(this, classOf[GridSizeChanged])
  }

  def onEvent(event: EditorEvent) = event match {
    case GridSizeChanged(step) => gridStep = step
    case _                     =>
  }

  def toggle = snapToGrid = !snapToGrid
  def setStep(step: Float) { gridStep = step }

  def snap(p: Point) = Point(snapX(p.x), snapY(p.y))
  def snapX(c: Float) = snapped(c, gridStep)
  def snapY(c: Float) = snapped(c, gridStep)

  def snapped(coor: Float, step: Float) = {
    if (snapToGrid) step * (coor / step).round
    else coor
  }

}