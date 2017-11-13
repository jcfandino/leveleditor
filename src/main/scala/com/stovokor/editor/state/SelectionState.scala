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
import com.stovokor.editor.model.Sector
import com.stovokor.util.LineClicked

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
    EventBus.subscribeByType(this, classOf[LineClicked])
  }

  override def cleanup() {
    super.cleanup
    EventBus.removeFromAll(this)
  }

  def onEvent(event: EditorEvent) = event match {
    case SelectionModeSwitch(m) => if (modeIndex != m) setMode(m)
    case PointClicked(point)    => selectPoint(point)
    case LineClicked(line)    => selectLine(line)
    case _                      =>
  }

  def setMode(newMode: Int) {
    println(s"new selection mode $newMode")
    selectedPoints = List()
    modeIndex = newMode
  }

  def selectPoint(point: Point) {
    val sectors = sectorRepository.findByPoint(point).map(_._2)
    mode.selectPoint(point, sectors)
    EventBus.trigger(PointSelectionChange(selectedPoints.toSet))
  }
  def selectLine(line: Line) {
    val sectors = sectorRepository.find(line).map(_._2)
    mode.selectPoint(line.a, sectors) // TODO rethink this, works for now..
    mode.selectPoint(line.b, sectors)
    EventBus.trigger(PointSelectionChange(selectedPoints.toSet))
  }

  abstract trait SelectionMode {
    def selectPoint(point: Point, sectors: Set[Sector])
  }

  object ModeOff extends SelectionMode {
    def selectPoint(point: Point, sectors: Set[Sector]) {
      selectedPoints = List()
    }
  }

  object ModePoint extends SelectionMode {
    def selectPoint(point: Point, sectors: Set[Sector]) {
      selectedPoints = List(point)
    }
  }

  object ModeLine extends SelectionMode {
    def selectPoint(point: Point, sectors: Set[Sector]) {
      if (selectedPoints.isEmpty) {
        selectedPoints = List(point)
      } else {
        val previousPoint = selectedPoints.last
        val line = sectors.map(_.polygon)
          .flatMap(_.lines)
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
    def selectPoint(point: Point, sectors: Set[Sector]) {
      val polygons = sectors.toList.map(_.polygon)
      selectedPoints = polygons.flatMap(_.pointsSorted)
    }
  }
}