package com.stovokor.editor.state

import com.jme3.app.state.AppStateManager
import com.jme3.app.SimpleApplication
import com.jme3.app.Application
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EditorEvent
import com.stovokor.util.SelectionModeSwitch
import com.stovokor.util.EventBus
import com.stovokor.util.PointSelected
import com.stovokor.editor.model.Point
import com.stovokor.editor.model.repository.SectorRepository
import com.stovokor.editor.model.Line

// only for 2d
class SelectionState extends BaseState
    with EditorEventListener {

  val sectorRepository = SectorRepository()

  var modeIndex = 0
  val modes = List(ModePoint, ModeLine, ModeSector)
  def mode = modes(modeIndex)

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    EventBus.subscribeByType(this, classOf[SelectionModeSwitch])
    EventBus.subscribeByType(this, classOf[PointSelected])
  }

  def onEvent(event: EditorEvent) = event match {
    case SelectionModeSwitch(m)         => if (modeIndex != m) setMode(m)
    case PointSelected(sectorId, point) => selectPoint(sectorId, point)
    case _                              =>
  }

  def setMode(newMode: Int) {
    println(s"new selection mode $newMode")
    selectedPoints = List()
    modeIndex = newMode
  }

  var selectedPoints: List[Point] = List()

  def selectPoint(sectorId: Long, point: Point) {
    mode.selectPoint(sectorId, point)
    println(s"Selected points $selectedPoints")
  }

  abstract trait SelectionMode {
    def selectPoint(sectorId: Long, point: Point)

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
        if (line.isDefined) selectedPoints = List(line.get.a, line.get.b)
        else selectedPoints = List(point)
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