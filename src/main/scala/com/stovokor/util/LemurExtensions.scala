package com.stovokor.util

import com.jme3.scene.Spatial
import com.simsilica.lemur.event.CursorButtonEvent
import com.simsilica.lemur.event.CursorEventControl
import com.simsilica.lemur.event.DefaultCursorListener
import com.simsilica.lemur.event.CursorMotionEvent

object LemurExtensions {

  implicit class SpatialExtension(spatial: Spatial) {

    def onCursorClick(action: (CursorButtonEvent, Spatial, Spatial) => Unit) {
      CursorEventControl.addListenersToSpatial(spatial, new DefaultCursorListener() {
        override def cursorButtonEvent(event: CursorButtonEvent, target: Spatial, capture: Spatial) {
          action(event, target, capture)
        }
      })
    }
    def onCursorMove(action: (CursorMotionEvent, Spatial, Spatial) => Unit) {
      CursorEventControl.addListenersToSpatial(spatial, new DefaultCursorListener() {
        override def cursorMoved(event: CursorMotionEvent, target: Spatial, capture: Spatial) {
          action(event, target, capture)
        }
      })
    }
  }
}