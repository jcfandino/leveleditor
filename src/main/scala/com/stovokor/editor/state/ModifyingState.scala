package com.stovokor.editor.state

import com.jme3.app.Application
import com.jme3.app.state.AppStateManager
import com.stovokor.editor.model.Line
import com.stovokor.editor.model.Point
import com.stovokor.editor.model.Sector
import com.stovokor.editor.model.repository.SectorRepository
import com.stovokor.util.DeleteSelection
import com.stovokor.util.EditorEvent
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EventBus
import com.stovokor.util.PointDragged
import com.stovokor.util.PointSelectionChange
import com.stovokor.util.SectorUpdated
import com.stovokor.util.SplitSelection

// in 2d to modify sector shapes
class ModifyingState extends BaseState
    with EditorEventListener {

  val sectorRepository = SectorRepository()

  var selectedPoints: List[(Long, Point)] = List()

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    EventBus.subscribeByType(this, classOf[PointDragged])
    EventBus.subscribeByType(this, classOf[PointSelectionChange])
    EventBus.subscribeByType(this, classOf[SplitSelection])
    EventBus.subscribeByType(this, classOf[DeleteSelection])
  }

  override def cleanup() {
    super.cleanup
    EventBus.removeFromAll(this)
  }

  def onEvent(event: EditorEvent) = event match {
    case PointDragged(id, from, to) => movePoints(id, from, to)
    case PointSelectionChange(ps)   => selectedPoints = ps
    case SplitSelection()           => splitSelection()
    case DeleteSelection()          => deleteSelection()
    case _                          =>
  }

  // TODO clean this up
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

  def splitSelection() {
    selectedPoints.map(_._1).foreach(id => {
      val sector = sectorRepository.get(id)
      val sectorLines = sector.polygon.lines
      val newPolygon = selectedPoints
        .filter(sp => sp._1 == id)
        .map(_._2)
        .sliding(2)
        .map(s => Line(s(0), s(1)))
        .foldRight(sector.polygon)((line, pol) => pol.addPoint(line, .5f))
      val newSector = sector.updatedPolygon(newPolygon)
      sectorRepository.update(id, newSector)
      EventBus.trigger(SectorUpdated(id, newSector))
    })
  }

  def deleteSelection() {
  }
}