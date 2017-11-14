package com.stovokor.editor.state

import com.jme3.app.Application
import com.jme3.app.state.AppState
import com.jme3.app.state.AppStateManager
import com.jme3.scene.Spatial.CullHint
import com.stovokor.util.EditModeSwitch
import com.stovokor.util.EditorEvent
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EventBus
import com.stovokor.util.PointSelectionChange
import com.stovokor.editor.input.Modes.EditMode

class Edit2DModeState extends BaseState with EditorEventListener {

  var modeKey = EditMode.Select
  val modes: Map[EditMode, EditModeStrategy] = Map((EditMode.Select, SelectionMode), (EditMode.Draw, DrawingMode))
  def mode = modes(modeKey)

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    EventBus.subscribeByType(this, classOf[EditModeSwitch])
    modes(EditMode.Draw).exit
    mode.enter
  }

  override def cleanup() {
    super.cleanup
    EventBus.removeFromAll(this)
  }

  def onEvent(event: EditorEvent) = event match {
    case EditModeSwitch(m) => setMode(m)
    case _                 =>
  }

  def setMode(newMode: EditMode) {
    println(s"new edit mode $newMode")
    if (newMode != modeKey) {
      mode.exit
      EventBus.trigger(PointSelectionChange(Set()))
      modeKey = newMode
      mode.enter
    }
  }

  abstract class EditModeStrategy(val id: String) {
    def enter
    def exit
  }

  object SelectionMode extends EditModeStrategy("selection") {

    def exit {
      println("exiting selection")
      disableStates(classOf[SelectionState], classOf[ModifyingState])
      removeStates(classOf[SelectionState], classOf[ModifyingState])
    }

    def enter {
      println("entering selection")
      stateManager.attach(new SelectionState)
      stateManager.attach(new ModifyingState)
    }
  }

  object DrawingMode extends EditModeStrategy("drawing") {
    def exit {
      println("exiting drawing")
      disableStates(classOf[DrawingState])
      removeStates(classOf[DrawingState])
    }
    def enter {
      println("entering drawing")
      stateManager.attach(new DrawingState)
    }

  }

}