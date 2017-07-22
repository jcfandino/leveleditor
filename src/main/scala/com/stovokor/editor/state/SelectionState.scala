package com.stovokor.editor.state

import com.jme3.app.state.AppStateManager
import com.jme3.app.SimpleApplication
import com.jme3.app.Application
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EditorEvent
import com.stovokor.util.SelectionModeSwitch
import com.stovokor.util.EventBus
import com.stovokor.util.PointClicked
import com.stovokor.editor.model.Point
import com.stovokor.editor.model.repository.SectorRepository
import com.stovokor.editor.model.Line
import com.stovokor.util.PointSelectionChange
import com.stovokor.util.SectorUpdated

// only for 2d
class SelectionState extends BaseState
    with EditorEventListener {

  val sectorRepository = SectorRepository()

  var selectedPoints: List[Point] = List()
  var modeIndex = 0
  val modes = List(ModeOff, ModePoint, ModeLine, ModeSector)
  def mode = modes(modeIndex)

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    EventBus.subscribeByType(this, classOf[SelectionModeSwitch])
    EventBus.subscribeByType(this, classOf[PointClicked])
  }

  override def cleanup() {
    super.cleanup
    EventBus.removeFromAll(this)
  }

  def onEvent(event: EditorEvent) = event match {
    case SelectionModeSwitch(m)        => if (modeIndex != m) setMode(m)
    case PointClicked(sectorId, point) => selectPoint(sectorId, point)
    case _                             =>
  }

  def setMode(newMode: Int) {
    println(s"new selection mode $newMode")
    selectedPoints = List()
    modeIndex = newMode
  }

  def selectPoint(sectorId: Long, point: Point) {
    mode.selectPoint(sectorId, point)
    EventBus.trigger(PointSelectionChange(selectedPoints.map(p => (sectorId, p))))
    println(s"Selected points $selectedPoints")
  }

  abstract trait SelectionMode {
    def selectPoint(sectorId: Long, point: Point)
  }

  object ModeOff extends SelectionMode {
    def selectPoint(sectorId: Long, point: Point) {
      selectedPoints = List()
    }
  }

  object ModePoint extends SelectionMode {
    def selectPoint(sectorId: Long, point: Point) {
      selectedPoints = List(point)
    }
  }

  object ModeLine extends SelectionMode {
    def selectPoint(sectorId: Long, point: Point) {
      if (selectedPoints.isEmpty) {
        selectedPoints = List(point)
      } else {
        val previousPoint = selectedPoints.last
        val polygon = sectorRepository.get(sectorId).polygon
        val line = polygon.lines
          .find(line => line match {
            case Line(a, b) => a == previousPoint && b == point || a == point && b == previousPoint
            case _          => false
          })
        if (line.isDefined) {
          selectedPoints = List(line.get.a, line.get.b)
        } else selectedPoints = List(point)
      }
    }
  }

  object ModeSector extends SelectionMode {
    def selectPoint(sectorId: Long, point: Point) {
      val polygon = sectorRepository.get(sectorId).polygon
      selectedPoints = polygon.pointsSorted
    }
  }
}