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
import com.stovokor.editor.input.Modes.SelectionMode

// only for 2d
class SelectionState extends BaseState
    with EditorEventListener {

  val sectorRepository = SectorRepository()

  var selectedPoints: Set[Point] = Set()
  var modeKey = SelectionMode.None

  val modes = Map(
    (SelectionMode.None, ModeOff),
    (SelectionMode.Point, ModePoint),
    (SelectionMode.Line, ModeLine),
    (SelectionMode.Sector, ModeSector))
  def mode = modes(modeKey)

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
    case SelectionModeSwitch(m) => if (modeKey != m) setMode(m)
    case PointClicked(point)    => selectPoint(point)
    case LineClicked(line)      => selectLine(line)
    case _                      =>
  }

  def setMode(newMode: SelectionMode) {
    println(s"new selection mode $newMode")
    selectedPoints = Set()
    modeKey = newMode
  }

  def selectPoint(point: Point) {
    mode.select(point, Line(point, point))
    EventBus.trigger(PointSelectionChange(selectedPoints.toSet))
  }
  def selectLine(line: Line) {
    mode.select(line.a, line)
    EventBus.trigger(PointSelectionChange(selectedPoints.toSet))
  }

  abstract trait SelectionModeStrategy {
    def select(point: Point, line: Line)
  }

  object ModeOff extends SelectionModeStrategy {
    def select(point: Point, line: Line) {
      selectedPoints = Set()
    }
  }

  object ModePoint extends SelectionModeStrategy {
    def select(point: Point, line: Line) {
      if (sectorRepository.findByPoint(point).isEmpty) {
        selectedPoints = Set()
      } else if (selectedPoints.contains(point)) {
        selectedPoints = selectedPoints -- List(point)
      } else {
        selectedPoints = selectedPoints ++ List(point)
      }
    }
  }

  object ModeLine extends SelectionModeStrategy {
    def select(point: Point, line: Line) {
      if (point == null) {
        selectedPoints = Set()
      } else if (selectedPoints.contains(line.a) && selectedPoints.contains(line.b)) {
        selectedPoints = selectedPoints -- Set(line.a, line.b)
      } else {
        selectedPoints = selectedPoints ++ Set(line.a, line.b)
      }
    }
  }

  object ModeSector extends SelectionModeStrategy {
    def select(point: Point, line: Line) {
      val sectors = sectorRepository.findInside(point)
      val points = sectors.map(_._2).flatMap(_.polygon.pointsUnsorted)
      if (sectors.isEmpty) {
        selectedPoints = Set()
      } else if (points.subsetOf(selectedPoints)) {
        selectedPoints = selectedPoints -- points
      } else {
        selectedPoints = selectedPoints ++ points
      }
    }
  }
}