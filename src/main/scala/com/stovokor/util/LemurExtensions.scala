package com.stovokor.util

import com.jme3.scene.Spatial
import com.simsilica.lemur.event.CursorButtonEvent
import com.simsilica.lemur.event.CursorEventControl
import com.simsilica.lemur.event.DefaultCursorListener
import com.simsilica.lemur.event.CursorMotionEvent
import com.simsilica.lemur.input.InputMapper
import com.simsilica.lemur.input.FunctionId
import com.simsilica.lemur.input.InputState
import com.simsilica.lemur.input.InputMapper.Mapping
import com.simsilica.lemur.input.Axis

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

  implicit class InputMapperExtension(mapper: InputMapper) {
    def map(fun: FunctionId, primary: Axis, modifiers: Int*): Mapping = {
      mapper.map(fun, primary, modifiers.map(_.asInstanceOf[Object]): _*)
    }
    def map(fun: FunctionId, primary: Int, modifiers: Int*): Mapping = {
      map(fun, InputState.Positive, primary, modifiers: _*)
    }
    def map(fun: FunctionId, bias: InputState, primary: Int, modifiers: Int*): Mapping = {
      mapper.map(fun, bias, primary, modifiers.map(_.asInstanceOf[Object]): _*)
    }
  }
}