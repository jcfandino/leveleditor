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

class DragControl(point: Point) extends AbstractControl {

  var isDragging = false
  var oldPos = new Vector2f()
  var newPos = new Vector2f()
  var initialized = false

  def controlUpdate(tpf: Float) {
    if (!initialized) {
      setup()
      initialized = true
    }
  }

  def setup() {
    getSpatial.onCursorClick((event, target, capture) => {
      if (event.getButtonIndex == 0 && getSpatial.isVisible) {
        println(s"CursorClick -> isDragging:$isDragging event: $event")
        if (!isDragging) {
          //TODO improve, disable dragging if not in selection
          oldPos.set(spatial.getLocalTranslation.to2f)
          newPos.set(GridSnapper.snapX(getSpatial.getLocalTranslation.x),
            GridSnapper.snapY(spatial.getLocalTranslation.y))
        } else if (!event.isPressed()) {
          // released
          println(s"oldpos $oldPos vs newpos $newPos = ${oldPos.distance(newPos)}")
          if (oldPos.distance(newPos) > .1f) { // button released
            println(s"moved point ${spatial.getLocalTranslation} -> ${newPos}")
            EventBus.trigger(
              PointDragged(point, GridSnapper.snap(Point(newPos.x, newPos.y))))
            getSpatial.setLocalTranslation(oldPos.to3f(Mode2DLayers.vertices)) //move back.
          } else {
            println(s"point clicked over point $point")
            EventBus.trigger(PointClicked(point))
          }
        }
        isDragging = event.isPressed()
      }
    })
    getSpatial.onCursorMove((event, target, capture) => {
      if (isDragging && getSpatial.isVisible) {
        val cam = event.getViewPort.getCamera
        val coord = cam.getWorldCoordinates(event.getLocation, 0f)
        newPos.set(GridSnapper.snapX(coord.x), GridSnapper.snapY(coord.y))
        spatial.setLocalTranslation(newPos.to3f(Mode2DLayers.vertices))
      }
    })
  }

  def controlRender(rm: RenderManager, vp: ViewPort) {}
}