package com.stovokor.editor.state

import com.jme3.app.state.AppStateManager
import com.stovokor.util.PolygonDrawn
import com.stovokor.util.EventBus
import com.jme3.app.Application
import com.stovokor.util.EditorEventListener
import com.stovokor.util.PointMoved
import com.stovokor.util.EditorEvent
import com.stovokor.editor.model.repository.SectorRepository
import com.stovokor.editor.model.Point
import com.stovokor.util.SectorUpdated

// in 2d to modify sector shapes
class ModifyingState extends BaseState
    with EditorEventListener {

  val sectorRepository = SectorRepository()

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    EventBus.subscribeByType(this, classOf[PointMoved])
  }

  def onEvent(event: EditorEvent) = event match {
    case PointMoved(id, from, to) => movePoint(id, from, to)
    case _                        =>
  }

  def movePoint(id: Long, from: Point, to: Point) {
    val sector = sectorRepository.get(id)
    val polygon = sector.polygon.changePoint(from, to)
    val updated = sector.updatedPolygon(polygon)
    sectorRepository.update(id, updated)
    EventBus.trigger(SectorUpdated(id, updated))
  }
}