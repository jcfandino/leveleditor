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
import com.stovokor.editor.factory.BorderFactory
import com.stovokor.util.SectorDeleted

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
    var toDelete: Set[Long] = Set()
    val pointsToMove = (selectedPoints ++ Set(from))
    pointsToMove.foreach(point => {
      val sectors = sectorRepository.findByPoint(point)
      for ((sectorId, sector) <- sectors) {
        val updated = sector.moveSinglePoint(point, dx, dy)
        if (updated.polygon.isDegenerate) {
          sectorRepository.remove(sectorId)
          toDelete = toDelete ++ Set(sectorId)
        } else {
          sectorRepository.update(sectorId, updated)
          toUpdate = toUpdate ++ Set((sectorId, updated))
        }
      }
    })
    // TODO Doesnt work well yet!!
    //update borders
    pointsToMove.foreach(point => {
      borderRepository
        .find(point)
        .filterNot(p => toDelete.contains(p._1))
        .map(pair => pair match {
          case (id, border) => {
            val updated = border.updateLine(border.line.moveEnd(point, point.move(dx, dy)))
            if (updated.line.length > 0) {
              borderRepository.update(id, updated)
            } else {
              borderRepository.remove(id)
            }
            updated.sectorA
          }
        })
        .distinct
        .foreach(sectorId => {
          //          EventBus.trigger(SectorUpdated(sectorId, sectorRepository.get(sectorId)))
        })
    })
    //    toDelete
    //      .flatMap(id => borderRepository.findFrom(id) ++ borderRepository.findTo(id))
    //      .map(_._1)
    //      .foreach(id => sectorRepository.remove(id))
    // when sector collapses over border, find border with to=sector and delete
    toDelete
      .flatMap(borderRepository.findFrom)
      .map(_._1).foreach(borderRepository.remove)
    toDelete
      .flatMap(id => borderRepository.findTo(id))
      .foreach(p => p match {
        case (borderId, border) => {
          borderRepository.remove(borderId)
          sectorRepository.get(border.sectorA)
            .openWalls.find(w => w.line == border.line)
            .map(wall => {
              (sectorRepository.get(border.sectorA))
                .updatedOpenWalls((sectorRepository.get(border.sectorA)).openWalls.filterNot(_ == wall))
                .updatedClosedWalls((sectorRepository.get(border.sectorA)).closedWalls ++ List(wall))
            }).foreach(sector => {
              toUpdate = toUpdate.filterNot(_._1 == border.sectorA) ++ Set((border.sectorA, sector))
            })
        }
      })
    toUpdate.foreach(is => EventBus.trigger(SectorUpdated(is._1, is._2)))
    toDelete.foreach(id => EventBus.trigger(SectorDeleted(id)))
  }

  def splitSelection() {
    selectedPoints
      .flatMap(sectorRepository.findByPoint)
      .foreach(result => {
        val (id, sector) = result
        val sectorPoints = sector.polygon.pointsUnsorted
        val sectorLines = sector.polygon.lines
        val newSector = sectorLines
          .filter(l => selectedPoints.filter(l.isEnd).size == 2)
          .foldRight(sector)((line, sec) => {
            val updated = sec.addPoint(line, .5f)
            val newLines = updated.polygon.lines.filterNot(sec.polygon.lines.contains)
            borderRepository.find(line).foreach(pair => pair match {
              case (id, border) => {
                val old = borderRepository.remove(id)
                borderRepository.add(old.updateLine(newLines(0)))
                borderRepository.add(old.updateLine(newLines(1)))
              }
            })
            updated
          })
        sectorRepository.update(id, newSector)
        EventBus.trigger(SectorUpdated(id, newSector))
      })
  }

  def deleteSelection() {
  }
}