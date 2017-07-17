package com.stovokor.editor.state

import com.jme3.app.state.AppStateManager
import com.stovokor.util.PolygonDrawn
import com.stovokor.util.EventBus
import com.jme3.app.Application
import com.stovokor.util.EditorEventListener
import com.stovokor.util.PointDragged
import com.stovokor.util.EditorEvent
import com.stovokor.editor.model.repository.SectorRepository
import com.stovokor.editor.model.Point
import com.stovokor.util.SectorUpdated
import com.stovokor.util.PointSelectionChange
import com.stovokor.editor.model.Sector

// in 2d to modify sector shapes
class ModifyingState extends BaseState
    with EditorEventListener {

  val sectorRepository = SectorRepository()

  var selectedPoints: Set[(Long, Point)] = Set()

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    EventBus.subscribeByType(this, classOf[PointDragged])
    EventBus.subscribeByType(this, classOf[PointSelectionChange])
  }

  def onEvent(event: EditorEvent) = event match {
    case PointDragged(id, from, to) => movePoints(id, from, to)
    case PointSelectionChange(ps)   => selectedPoints = ps
    case _                          =>
  }

  def movePoints(id: Long, from: Point, to: Point) {
    val (dx, dy) = (to.x - from.x, to.y - from.y)
    var toUpdate: Set[(Long, Sector)] = Set()
    (selectedPoints ++ Set((id, from))).foreach(pair => {
      val pp = pair._2
      val sector = sectorRepository.get(pair._1)
      val polygon = sector.polygon.changePoint(pp, Point(pp.x + dx, pp.y + dy))
      val updated = sector.updatedPolygon(polygon)
      sectorRepository.update(pair._1, updated)
      toUpdate = toUpdate ++ Set((id, updated))
    })
    toUpdate.foreach(is => EventBus.trigger(SectorUpdated(is._1, is._2)))
  }
}