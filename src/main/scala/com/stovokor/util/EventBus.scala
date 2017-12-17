package com.stovokor.util

import com.stovokor.editor.model.Polygon
import com.stovokor.editor.model.Point
import com.stovokor.editor.model.Sector
import com.stovokor.editor.model.Line
import com.stovokor.editor.input.Modes.EditMode
import com.stovokor.editor.input.Modes.SelectionMode
import com.stovokor.editor.model.SelectionUnit

object EventBus {

  var listeners: Map[EditorEvent, Set[EditorEventListener]] =
    Map.empty.withDefaultValue(Set())

  var typeListeners: Map[Class[_ <: EditorEvent], Set[EditorEventListener]] =
    Map.empty.withDefaultValue(Set())

  // this is to avoid iterating every key in the map
  var allListeners: Set[EditorEventListener] = Set()

  def subscribe(listener: EditorEventListener, event: EditorEvent) = {
    listeners = listeners.updated(event, listeners(event) + listener)
    allListeners = allListeners + listener
  }
  def subscribeByType(listener: EditorEventListener, eventType: Class[_ <: EditorEvent]) = {
    typeListeners = typeListeners.updated(eventType, typeListeners(eventType) + listener)
    allListeners = allListeners + listener
  }

  def remove(listener: EditorEventListener, event: EditorEvent) = {
    listeners = listeners.updated(event, listeners(event) - listener)
    allListeners = allListeners - listener
  }

  def removeEvent(event: EditorEvent) {
    allListeners = allListeners -- listeners(event)
    listeners = listeners.updated(event, Set())
  }

  def removeEvents(clazz: Class[_ <: EditorEvent]) {
    allListeners = allListeners -- typeListeners(clazz)
    typeListeners = typeListeners.updated(clazz, Set())
  }

  def removeEvents(filter: EditorEvent => Boolean) {
    for (e <- listeners.keys.filter(filter)) {
      allListeners = allListeners -- listeners(e)
      listeners = listeners.updated(e, Set())
    }
  }

  def removeFromAll(listener: EditorEventListener) = {
    if (allListeners.contains(listener)) {
      allListeners = allListeners - listener
      for (event <- listeners.keys) {
        remove(listener, event)
      }
      for (e <- typeListeners.keys) {
        val currentSet = typeListeners(e)
        typeListeners = typeListeners.updated(e, currentSet - listener)
      }
    }
  }

  def trigger(event: EditorEvent) = {
    for (listener <- listeners(event))
      listener.onEvent(event)

    for (listener <- typeListeners(event.getClass))
      if (!listeners.contains(event))
        listener.onEvent(event)
  }
}

trait EditorEventListener {
  def onEvent(event: EditorEvent)
}

abstract class EditorEvent

case class ViewModeSwitch() extends EditorEvent
case class EditModeSwitch(m: EditMode) extends EditorEvent
case class SelectionModeSwitch(m: SelectionMode) extends EditorEvent
case class ExitApplication() extends EditorEvent

case class SaveMap(overwrite: Boolean) extends EditorEvent
case class OpenMap() extends EditorEvent
case class StartNewMap() extends EditorEvent
case class ExportMap() extends EditorEvent
case class EditSettings() extends EditorEvent
case class SettingsUpdated() extends EditorEvent
case class ChangeGridSize() extends EditorEvent
case class ShowHelp() extends EditorEvent

case class PointClicked(point: Point) extends EditorEvent
case class PointDragged(from: Point, to: Point) extends EditorEvent
case class LineClicked(line: Line) extends EditorEvent
case class LineDragged(line: Line, dx: Float, dy: Float) extends EditorEvent
case class SectorClicked(sectorId: Long) extends EditorEvent
case class SectorDragged(sectorId: Long, dx: Float, dy: Float) extends EditorEvent

case class SelectionChange(unit: Set[SelectionUnit]) extends EditorEvent
case class PointerTargetChange(sectorId: Long, target: String) extends EditorEvent
case class ChangeMaterial(sectorId: Long, target: String) extends EditorEvent
case class ChangeZoom(factor: Float) extends EditorEvent
case class ToggleEffects() extends EditorEvent

case class SplitSelection() extends EditorEvent
case class DeleteSelection() extends EditorEvent

case class SectorUpdated(id: Long, sector: Sector, fullRedraw: Boolean) extends EditorEvent
case class SectorDeleted(id: Long) extends EditorEvent

