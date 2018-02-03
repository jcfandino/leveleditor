package com.stovokor.editor.state

import com.jme3.app.Application
import com.jme3.app.state.AppStateManager
import com.stovokor.editor.input.Modes.SelectionMode
import com.stovokor.editor.model.SelectionLine
import com.stovokor.editor.model.SelectionPoint
import com.stovokor.editor.model.SelectionSector
import com.stovokor.editor.model.SelectionUnit
import com.stovokor.editor.model.repository.SectorRepository
import com.stovokor.util.EditorEvent
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EventBus
import com.stovokor.util.LineClicked
import com.stovokor.util.PointClicked
import com.stovokor.util.SectorClicked
import com.stovokor.util.SelectionChange
import com.stovokor.util.SelectionModeSwitch
import com.stovokor.util.SplitSelection
import com.stovokor.util.DeleteSelection
import com.stovokor.util.SectorUpdated
import com.stovokor.editor.model.Sector
import com.stovokor.util.ViewModeSwitch

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
    EventBus.subscribeByType(this, classOf[SectorUpdated])
    EventBus.subscribe(this, ViewModeSwitch())
  }

  override def cleanup() {
    super.cleanup
    EventBus.removeFromAll(this)
  }

  def onEvent(event: EditorEvent) = event match {
    case SelectionModeSwitch(m)          => if (modeKey != m) setMode(m)
    case PointClicked(point)             => select(SelectionPoint(point))
    case LineClicked(line)               => select(SelectionLine(line))
    case SectorClicked(id)               => select(SelectionSector(id, sectorRepository.get(id)))
    case ViewModeSwitch()                => clearSelection()
    case SectorUpdated(id, newSector, _) => adjustSelectionAfterChange(newSector)
    case _                               =>
  }

  /**
   * Cleanup selection after an update (e.g. dragging) to avoid weird behavior.
   * It would be nice to update with the updated unit (e.g. a point/line moved) but it's only
   * easy to do for the sector.
   */
  def adjustSelectionAfterChange(newSector: Sector) {
    selection = selection.flatMap[SelectionUnit, Set[SelectionUnit]](s => s match {
      case SelectionPoint(point)       => if (newSector.inside(point)) Set(s) else Set()
      case SelectionLine(line)         => if (newSector.polygon.lines.contains(line)) Set(s) else Set()
      case SelectionSector(id, sector) => Set(SelectionSector(id, newSector))
      case _                           => Set(s)
    })
    EventBus.trigger(SelectionChange(selection))
  }

  def setMode(newMode: SelectionMode) {
    println(s"new selection mode $newMode")
    selection = Set()
    modeKey = newMode
  }

  def select(unit: SelectionUnit) {
    mode.select(unit)
    EventBus.trigger(SelectionChange(selection))
  }

  def clearSelection() = {
    selection = Set()
    EventBus.trigger(SelectionChange(selection))
  }

  abstract trait SelectionModeStrategy {
    def select(unit: SelectionUnit) {
      if (sectorsMatching(unit).isEmpty) {
        clearSelection()
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
      clearSelection()
    }
  }

  object ModePoint extends SelectionModeStrategy {
  }

  object ModeLine extends SelectionModeStrategy {
  }

  // sector mode is special
  object ModeSector extends SelectionModeStrategy {
    override def sectorsMatching(unit: SelectionUnit) = unit match {
      case SelectionPoint(p)           => sectorRepository.findInside(p, false)
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

}