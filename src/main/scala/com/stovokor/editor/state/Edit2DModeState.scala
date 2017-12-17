package com.stovokor.editor.state

import com.jme3.app.Application
import com.jme3.app.state.AppState
import com.jme3.app.state.AppStateManager
import com.jme3.scene.Spatial.CullHint
import com.stovokor.util.EditModeSwitch
import com.stovokor.util.EditorEvent
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EventBus
import com.stovokor.util.SelectionChange
import com.stovokor.editor.input.Modes.EditMode
import com.simsilica.lemur.FillMode
import com.stovokor.editor.input.InputFunction
import com.simsilica.lemur.input.FunctionId
import com.simsilica.lemur.input.StateFunctionListener
import com.simsilica.lemur.input.InputState
import com.stovokor.util.SelectionModeSwitch
import com.stovokor.editor.input.Modes.SelectionMode

class Edit2DModeState extends BaseState
    with EditorEventListener
    with CanMapInput
    with StateFunctionListener {

  var modeKey = EditMode.Select
  val modes: Map[EditMode, EditModeStrategy] = Map(
    (EditMode.Select, SelectingMode),
    (EditMode.Draw, DrawingMode),
    (EditMode.Fill, FillMode))

  def mode = modes(modeKey)

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    EventBus.subscribeByType(this, classOf[EditModeSwitch])
    modes(EditMode.Draw).exit
    mode.enter
    setupInput
  }

  override def cleanup() {
    super.cleanup
    EventBus.removeFromAll(this)
    inputMapper.removeStateListener(this, InputFunction.clickKey)
    inputMapper.removeStateListener(this, InputFunction.insertMode)
    inputMapper.removeStateListener(this, InputFunction.selectPoints)
    inputMapper.removeStateListener(this, InputFunction.selectLines)
    inputMapper.removeStateListener(this, InputFunction.selectSectors)
    inputMapper.removeStateListener(this, InputFunction.fillMode)
  }

  def setupInput {
    inputMapper.addStateListener(this, InputFunction.cancel)
    inputMapper.addStateListener(this, InputFunction.clickKey)
    inputMapper.addStateListener(this, InputFunction.insertMode)
    inputMapper.addStateListener(this, InputFunction.selectPoints)
    inputMapper.addStateListener(this, InputFunction.selectLines)
    inputMapper.addStateListener(this, InputFunction.selectSectors)
    inputMapper.addStateListener(this, InputFunction.fillMode)
    inputMapper.activateGroup(InputFunction.general)
  }

  def valueChanged(func: FunctionId, value: InputState, tpf: Double) {
    if (value == InputState.Positive) func match {
      case InputFunction.insertMode => setMode(EditMode.Draw)
      case InputFunction.fillMode => setMode(EditMode.Fill)
      case InputFunction.selectPoints => {
        setMode(EditMode.Select)
        EventBus.trigger(SelectionModeSwitch(SelectionMode.Point))
      }
      case InputFunction.selectLines => {
        setMode(EditMode.Select)
        EventBus.trigger(SelectionModeSwitch(SelectionMode.Line))
      }
      case InputFunction.selectSectors => {
        setMode(EditMode.Select)
        EventBus.trigger(SelectionModeSwitch(SelectionMode.Sector))
      }
      case _ =>
    }
  }

  def onEvent(event: EditorEvent) = event match {
    case EditModeSwitch(m) => setMode(m)
    case _                 =>
  }

  def setMode(newMode: EditMode) {
    if (newMode != modeKey) {
      println(s"edit mode $newMode")
      mode.exit
      EventBus.trigger(SelectionChange(Set()))
      modeKey = newMode
      mode.enter
    }
  }

  abstract class EditModeStrategy(val id: String) {
    def enter
    def exit
  }

  object SelectingMode extends EditModeStrategy("selecting") {

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

  object FillMode extends EditModeStrategy("fill") {
    def exit {
      println("exiting fill mode")
      disableStates(classOf[FillHoleState])
      removeStates(classOf[FillHoleState])
    }
    def enter {
      println("entering fill mode")
      stateManager.attach(new FillHoleState)
    }
  }

}