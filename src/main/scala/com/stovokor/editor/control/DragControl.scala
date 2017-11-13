package com.stovokor.editor.control

import com.jme3.math.Vector2f
import com.jme3.renderer.RenderManager
import com.jme3.renderer.ViewPort
import com.jme3.scene.control.AbstractControl
import com.stovokor.editor.gui.Mode2DLayers
import com.stovokor.editor.model.Point
import com.stovokor.util.EventBus
import com.stovokor.util.GridSnapper
import com.stovokor.util.JmeExtensions.SpatialExtensions
import com.stovokor.util.JmeExtensions.Vector2fExtensions
import com.stovokor.util.JmeExtensions.Vector3fExtensions
import com.stovokor.util.LemurExtensions.SpatialExtension
import com.stovokor.util.PointClicked
import com.stovokor.util.PointDragged
import com.stovokor.editor.model.Line
import com.stovokor.util.LineDragged
import com.stovokor.util.LineClicked

abstract class DragControl extends AbstractControl {

  var isDragging = false
  var oldPos = new Vector2f()
  var newPos = new Vector2f()
  var initialized = false

  override def controlUpdate(tpf: Float) = {
    if (!initialized) {
      setup()
      initialized = true
    }
  }
  def currentPos = spatial.getLocalTranslation.to2f

  def setup() {
    val z = spatial.getLocalTranslation.z
    spatial.onCursorClick((event, target, capture) => {
      if (event.getButtonIndex == 0 && spatial.isVisible) {
        println(s"CursorClick -> isDragging:$isDragging event: $event")
        if (!isDragging) {
          //TODO improve, disable dragging if not in selection
          oldPos.set(currentPos)
          newPos.set(GridSnapper.snapX(currentPos.x), GridSnapper.snapY(currentPos.y))
        } else if (!event.isPressed()) {
          // released
          println(s"oldpos $oldPos vs newpos $newPos = ${oldPos.distance(currentPos)}")
          if (oldPos.distance(currentPos) > .1f) { // button released
            dragged(newPos.subtract(oldPos))
            spatial.setLocalTranslation(oldPos.to3f(z)) //move back.
          } else {
            clicked
          }
        }
        isDragging = event.isPressed()
      }
    })
    spatial.onCursorMove((event, target, capture) => {
      if (isDragging && spatial.isVisible) {
        val cam = event.getViewPort.getCamera
        val coord = cam.getWorldCoordinates(event.getLocation, 0f)
        if (oldPos.distance(coord.to2f) > .1f) { // button released
          //          newPos.set(coord.x, coord.y)
          newPos.set(GridSnapper.snapX(coord.x), GridSnapper.snapY(coord.y))
          spatial.setLocalTranslation(newPos.to3f(z))
        }
      }
    })
  }

  def dragged(movement: Vector2f)
  def clicked

  override def controlRender(rm: RenderManager, vp: ViewPort) = {}
}

class PointDragControl(point: Point) extends DragControl {

  override def dragged(movement: Vector2f) {
    println(s"moved point ${spatial.getLocalTranslation} -> ${movement}")
    EventBus.trigger(PointDragged(point, GridSnapper.snap(point.move(movement.x, movement.y))))
    //        EventBus.trigger(PointDragged(point, GridSnapper.snap(Point(droppedPos.x, droppedPos.y))))
  }

  override def clicked {
    println(s"point clicked over point $point")
    EventBus.trigger(PointClicked(point))
  }
}

class LineDragControl(line: Line) extends DragControl {

  override def dragged(movement: Vector2f) {
    println(s"moved line ${spatial.getLocalTranslation} -> ${movement}")
    EventBus.trigger(LineDragged(line, movement.x, movement.y))
  }

  override def clicked {
    println(s"line clicked $line")
    EventBus.trigger(LineClicked(line))
  }
}