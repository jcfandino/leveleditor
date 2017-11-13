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
import com.stovokor.editor.model.Border
import com.stovokor.editor.model.Surface
import com.stovokor.util.LineDragged

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
    EventBus.subscribeByType(this, classOf[LineDragged])
    EventBus.subscribeByType(this, classOf[PointSelectionChange])
    EventBus.subscribeByType(this, classOf[SplitSelection])
    EventBus.subscribeByType(this, classOf[DeleteSelection])
  }

  override def cleanup() {
    super.cleanup
    EventBus.removeFromAll(this)
  }

  def onEvent(event: EditorEvent) = event match {
    case PointDragged(from, to)    => movePoints(to.x - from.x, to.y - from.y, from)
    case LineDragged(line, dx, dy) => movePoints(dx, dy, line.a, line.b)
    case PointSelectionChange(ps)  => selectedPoints = ps
    case SplitSelection()          => splitSelection()
    case DeleteSelection()         => deleteSelection()
    case _                         =>
  }

  // TODO clean this up
  def movePoints(dx: Float, dy: Float, extraPoints: Point*) {
    var toUpdate: Map[Long, Sector] = Map()
    var toDelete: Set[Long] = Set()
    val pointsToMove = (selectedPoints) ++ extraPoints // ++ Set(from))
    pointsToMove.foreach(point => {
      val sectors = sectorRepository.findByPoint(point)
      for ((sectorId, sector) <- sectors) {
        val updated = sector.moveSinglePoint(point, dx, dy)
        if (updated.polygon.isDegenerate) {
          sectorRepository.remove(sectorId)
          toDelete = toDelete ++ Set(sectorId)
        } else {
          sectorRepository.update(sectorId, updated)
          toUpdate = toUpdate.updated(sectorId, updated)
        }
      }
    })
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
    })
    // find new borders that may be created
    toUpdate.foreach(p1 => p1 match {
      case (sectorId1, sector1) => {
        sector1.closedWalls.foreach(w => {
          sectorRepository.find(w.line)
            .filterNot(_._1 == sectorId1)
            .foreach(p2 => p2 match {
              case (sectorId2, sector2) => {
                println(s"~~~ Found new border with ${sector2}")
                val updated1 = sector1.openWall(w.line)
                val updated2 = sector2.openWall(w.line)
                sectorRepository.update(sectorId1, updated1)
                sectorRepository.update(sectorId2, updated2)
                borderRepository.add(Border(sectorId1, sectorId2, w.line, Surface(0f, w.texture), Surface(0f, w.texture), Surface(0f, w.texture)))
                borderRepository.add(Border(sectorId2, sectorId1, w.line.reverse, Surface(0f, w.texture), Surface(0f, w.texture), Surface(0f, w.texture)))
                toUpdate = toUpdate
                  .updated(sectorId1, updated1)
                  .updated(sectorId2, updated2)
              }
            })
        })
      }
    })
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
            .openWalls.find(_.line == border.line)
            .map(wall => sectorRepository.get(border.sectorA).closeWall(wall.line))
            .foreach(sector => {
              sectorRepository.update(border.sectorA, sector)
              toUpdate = toUpdate.updated(border.sectorA, sector)
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