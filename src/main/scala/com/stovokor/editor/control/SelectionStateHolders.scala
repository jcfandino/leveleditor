package com.stovokor.editor.control

import com.stovokor.editor.input.Modes.SelectionMode
import com.stovokor.util.EditorEvent
import com.stovokor.util.EditorEventListener
import com.stovokor.util.SelectionModeSwitch
import com.stovokor.editor.model.SelectionUnit
import com.stovokor.util.SelectionChange

// object to share the selection mode among controls
object SelectionModeHolder extends EditorEventListener {
  var current = SelectionMode.None
  def onEvent(event: EditorEvent) = event match {
    case SelectionModeSwitch(m) => current = m
    case _                      =>
  }
}

// object to share the current selected elements among controls
object SelectionUnitsHolder extends EditorEventListener {
  var current: Set[SelectionUnit] = Set()
  def onEvent(event: EditorEvent) = event match {
    case SelectionChange(m) => current = m
    case _                  =>
  }
}
