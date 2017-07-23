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

class EditModeState extends BaseState with EditorEventListener {

  var modeIndex = 0
  val modes: List[EditMode] = List(SelectionMode, DrawingMode)
  def mode = modes(modeIndex)

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    EventBus.subscribeByType(this, classOf[EditModeSwitch])
    modes(1).exit
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

  def setMode(newMode: Int) {
    println(s"new edit mode $newMode")
    if (newMode != modeIndex) {
      mode.exit
      EventBus.trigger(PointSelectionChange(List()))
      modeIndex = newMode
      mode.enter
    }
  }

  abstract class EditMode(val id: String) {
    def enter
    def exit
  }

  object SelectionMode extends EditMode("selection") {

    def exit {
      println("exiting selection")
      disableStates(classOf[SelectionState],classOf[ModifyingState])
      removeStates(classOf[SelectionState],classOf[ModifyingState])
    }

    def enter {
      println("entering selection")
      stateManager.attach(new SelectionState)
      stateManager.attach(new ModifyingState)
    }
  }

  object DrawingMode extends EditMode("drawing") {
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