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
import com.stovokor.editor.model.Wall
import com.stovokor.editor.model.repository.BorderRepository

// in 2d to modify sector shapes
class ModifyingState extends BaseState
    with EditorEventListener {

  val sectorRepository = SectorRepository()
  val borderRepository = BorderRepository()

  //  var selectedPoints: List[(Long, Point)] = List()
  var selectedPoints: Set[Point] = Set()

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
    case PointDragged(from, to)   => movePoints(from, to)
    case PointSelectionChange(ps) => selectedPoints = ps
    case SplitSelection()         => splitSelection()
    case DeleteSelection()        => deleteSelection()
    case _                        =>
  }

  // TODO clean this up
  def movePoints(from: Point, to: Point) {
    val (dx, dy) = (to.x - from.x, to.y - from.y)
    var toUpdate: Set[(Long, Sector)] = Set()
    val pointsToMove = (selectedPoints ++ Set(from))
    pointsToMove.foreach(point => {
      val sectors = sectorRepository.find(point)
      for ((sectorId, getter) <- sectors) {
        val updated = getter().moveSinglePoint(point, dx, dy)
        sectorRepository.update(sectorId, updated)
        toUpdate = toUpdate ++ Set((sectorId, updated))
      }
    })
    toUpdate.foreach(is => EventBus.trigger(SectorUpdated(is._1, is._2)))
    //update borders
    pointsToMove.foreach(point => {
      borderRepository
        .find(point)
        .map(pair => pair match {
          case (id, border) => {
            val updated = border.updateLine(border.line.moveEnd(point, point.move(dx, dy)))
            borderRepository.update(id, updated)
            updated.sectorA
          }
        })
        .distinct
        .foreach(sectorId => {
          EventBus.trigger(SectorUpdated(sectorId, sectorRepository.get(sectorId)))
        })
    })
  }

  def splitSelection() {
    selectedPoints.flatMap(sectorRepository.find).foreach(result => {
      val id = result._1
      val sector = result._2()
      val sectorPoints = sector.polygon.pointsUnsorted
      val sectorLines = sector.polygon.lines
      val newSector = selectedPoints.toList
        .sliding(2)
        .map(s => Line(s(0), s(1)))
        .filter(sectorLines.contains)
        .foldRight(sector)((line, sec) => sec.addPoint(line, .5f))
      sectorRepository.update(id, newSector)
      EventBus.trigger(SectorUpdated(id, newSector))
    })
  }

  def deleteSelection() {
  }
}