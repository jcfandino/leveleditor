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
import com.stovokor.util.SectorClicked

// only for 2d
class SelectionState extends BaseState
    with EditorEventListener {

  val sectorRepository = SectorRepository()

  var selection: Set[SelectionUnit] = Set()
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
    EventBus.subscribeByType(this, classOf[SectorClicked])
  }

  override def cleanup() {
    super.cleanup
    EventBus.removeFromAll(this)
  }

  def onEvent(event: EditorEvent) = event match {
    case SelectionModeSwitch(m) => if (modeKey != m) setMode(m)
    case PointClicked(point)    => select(SelectionPoint(point))
    case LineClicked(line)      => select(SelectionLine(line))
    case SectorClicked(id)      => select(SelectionSector(id, sectorRepository.get(id)))
    case _                      =>
  }

  def setMode(newMode: SelectionMode) {
    println(s"new selection mode $newMode")
    selection = Set()
    modeKey = newMode
  }

  def select(unit: SelectionUnit) {
    mode.select(unit)
    EventBus.trigger(PointSelectionChange(selectedPoints))
  }

  abstract trait SelectionModeStrategy {
    def select(unit: SelectionUnit) {
      if (sectorsMatching(unit).isEmpty) {
        selection = Set()
      } else if (selection.contains(unit)) {
        selection = selection -- Set(unit)
      } else {
        selection = selection ++ Set(unit)
      }
    }

    def sectorsMatching(unit: SelectionUnit) = {
      // not very efficient
      unit.getPoints.map(sectorRepository.findByPoint)
        .foldLeft(sectorRepository.sectors.toSet)(_.intersect(_))
    }
  }

  object ModeOff extends SelectionModeStrategy {
    override def select(unit: SelectionUnit) {
      selection = Set()
    }
  }

  object ModePoint extends SelectionModeStrategy {
  }

  object ModeLine extends SelectionModeStrategy {
  }

  // sector mode is special
  object ModeSector extends SelectionModeStrategy {
    override def sectorsMatching(unit: SelectionUnit) = unit match {
      case SelectionPoint(p)           => sectorRepository.findInside(p)
      case SelectionSector(id, sector) => Set((id, sector))
      case _                           => Set()
    }

    override def select(unit: SelectionUnit) {
      val sectors = sectorsMatching(unit)
      if (sectors.isEmpty) {
        selection = Set()
      } else {
        sectors.map(p => SelectionSector(p._1, p._2)).foreach(su => {
          if (selection.contains(su)) {
            selection = selection -- Set(su)
          } else {
            selection = selection ++ Set(su)
          }
        })
      }
    }
  }

  def selectedPoints = selection.flatMap(_.getPoints)

  abstract class SelectionUnit() {
    def getPoints: Set[Point]
  }
  case class SelectionPoint(point: Point) extends SelectionUnit() {
    def getPoints = Set(point)
  }
  case class SelectionLine(line: Line) extends SelectionUnit {
    def getPoints = Set(line.a, line.b)
  }
  case class SelectionSector(sectorId: Long, sector: Sector) extends SelectionUnit {
    def getPoints = sector.polygon.pointsUnsorted.toSet
  }
}