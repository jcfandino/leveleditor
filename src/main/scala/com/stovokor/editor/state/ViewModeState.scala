package com.stovokor.editor.state

import com.jme3.app.state.AppStateManager
import com.jme3.app.Application
import com.stovokor.util.EventBus
import com.stovokor.util.ViewModeSwitch
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EditorEvent
import com.jme3.scene.Node
import com.jme3.scene.Spatial.CullHint
import com.jme3.app.state.AppState
import com.stovokor.util.SelectionModeSwitch
import com.stovokor.util.EditModeSwitch
import com.stovokor.editor.input.Modes.SelectionMode
import com.stovokor.editor.input.Modes.EditMode
import com.stovokor.editor.input.InputFunction
import com.simsilica.lemur.input.StateFunctionListener
import com.simsilica.lemur.input.FunctionId
import com.simsilica.lemur.input.InputState

class ViewModeState extends BaseState
    with EditorEventListener
    with CanMapInput
    with StateFunctionListener {

  private var current: Mode = M2D()

  var nodes: Map[String, Node] = Map()

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    nodes = nodes.updated("2d", get2DNode).updated("3d", get3DNode)
    M3D().hide
    M3D().exit
    EventBus.subscribe(this, ViewModeSwitch())
    M2D().show
    M2D().enter
    setupInput
  }

  def setupInput() = {
    inputMapper.addStateListener(this, InputFunction.switchViewMode)
    inputMapper.activateGroup(InputFunction.general)
  }

  def valueChanged(function: FunctionId, state: InputState, value: Double) = function match {
    case InputFunction.switchViewMode => if (state == InputState.Positive) switch()
    case _                            =>
  }

  def onEvent(event: EditorEvent) = event match {
    case ViewModeSwitch() => switch()
    case _                =>
  }

  def switch() {
    enterMode(if (current == M2D()) M3D() else M2D())
  }

  def enterMode(mode: Mode) {
    if (mode == current) return
    current.hide
    current.exit
    current = mode
    mode.show
    mode.enter
  }

  private abstract class Mode(val id: String) {
    def enter
    def exit

    def hide {
      rootNode.getChild(id).setCullHint(CullHint.Always)
    }
    def show {
      rootNode.getChild(id).setCullHint(CullHint.Inherit)
    }
  }

  private case class M2D() extends Mode("2d") {

    def exit {
      disableStates(
        classOf[GridState],
        classOf[Camera2DState])
      removeStates(classOf[Camera2DState]) //Problem, it forgets were it was
      removeStates(classOf[Edit2DModeState])
    }

    def enter {
      println("entering 2d")
      enableStates(classOf[GridState])
      stateManager.attach(new Camera2DState)
      stateManager.attach(new Edit2DModeState)
      //      disableStates(classOf[DrawingState]) // we want this to be disable at start
      EventBus.trigger(SelectionModeSwitch(SelectionMode.None))
      EventBus.trigger(EditModeSwitch(EditMode.Select))
    }
  }

  private case class M3D() extends Mode("3d") {
    def exit {
      disableStates(classOf[Camera3DState], classOf[Edit3DState], classOf[EffectsState])
      removeStates(classOf[Camera3DState], classOf[Edit3DState], classOf[EffectsState])
    }
    def enter {
      println("entering 3d")
      stateManager.attach(new Camera3DState)
      stateManager.attach(new Edit3DState)
      stateManager.attach(new EffectsState)
    }

  }

}
