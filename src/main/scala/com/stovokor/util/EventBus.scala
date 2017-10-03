package com.stovokor.util

import com.stovokor.editor.model.Polygon
import com.stovokor.editor.model.Point
import com.stovokor.editor.model.Sector

object EventBus {

  var listeners: Map[EditorEvent, Set[EditorEventListener]] =
    Map.empty.withDefaultValue(Set())

  var typeListeners: Map[Class[_ <: EditorEvent], Set[EditorEventListener]] =
    Map.empty.withDefaultValue(Set())

  def subscribe(listener: EditorEventListener, event: EditorEvent) = {
    listeners = listeners.updated(event, listeners(event) + listener)
  }
  def subscribeByType(listener: EditorEventListener, eventType: Class[_ <: EditorEvent]) = {
    typeListeners = typeListeners.updated(eventType, typeListeners(eventType) + listener)
  }

  def remove(listener: EditorEventListener, event: EditorEvent) = {
    listeners = listeners.updated(event, listeners(event) - listener)
  }

  def removeEvent(event: EditorEvent) {
    listeners = listeners.updated(event, Set())
  }

  def removeEvents(clazz: Class[_ <: EditorEvent]) {
    typeListeners = typeListeners.updated(clazz, Set())
  }

  def removeEvents(filter: EditorEvent => Boolean) {
    for (e <- listeners.keys.filter(filter)) {
      listeners = listeners.updated(e, Set())
    }
  }

  def removeFromAll(listener: EditorEventListener) = {
    println(s"Unsubscribing event listener: $listeners")
    for (event <- listeners.keys) {
      println(s"removing from event $event")
      remove(listener, event)
    }
    for (e <- typeListeners.keys) {
      val currentSet = typeListeners(e)
      typeListeners = typeListeners.updated(e, currentSet - listener)
    }

    println(s"Finished unsubscribing event listener: $listeners")
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
case class EditModeSwitch(m: Int) extends EditorEvent
case class SelectionModeSwitch(m: Int) extends EditorEvent
case class ExitApplication() extends EditorEvent

case class SaveMap(overwrite: Boolean) extends EditorEvent
case class OpenMap() extends EditorEvent
case class ExportMap() extends EditorEvent


case class PointClicked(point: Point) extends EditorEvent
case class PointDragged(from: Point, to: Point) extends EditorEvent
case class PointSelectionChange(points: Set[Point]) extends EditorEvent
case class PointerTargetChange(sectorId: Long, target: String) extends EditorEvent
case class ChangeMaterial(sectorId:Long, target:String) extends EditorEvent

case class SplitSelection() extends EditorEvent
case class DeleteSelection() extends EditorEvent

case class SectorUpdated(id: Long, sector: Sector) extends EditorEvent
case class SectorDeleted(id: Long) extends EditorEvent



