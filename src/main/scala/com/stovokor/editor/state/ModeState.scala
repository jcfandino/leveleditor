package com.stovokor.editor.state

import com.jme3.app.state.AppStateManager
import com.jme3.app.Application
import com.stovokor.util.EventBus
import com.stovokor.util.ModeSwitch
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EditorEvent
import com.jme3.scene.Node
import com.jme3.scene.Spatial.CullHint
import com.jme3.app.state.AppState

class ModeState extends BaseState with EditorEventListener {

  var current: Mode = M2D()

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    get2DNode
    get3DNode
    M3D().hide
    M3D().exit
    EventBus.subscribe(this, ModeSwitch())
  }

  def onEvent(event: EditorEvent) = event match {
    case ModeSwitch() => switch()
    case _            =>
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

  abstract class Mode(val id: String) {
    def enter
    def exit

    def hide {
      rootNode.getChild(id).setCullHint(CullHint.Always)
    }
    def show {
      rootNode.getChild(id).setCullHint(CullHint.Inherit)
    }
  }

  case class M2D() extends Mode("2d") {

    def exit {
      disableStates(
        classOf[GridState],
        classOf[Camera2DState],
        classOf[SelectionState],
        classOf[DrawingState])
      removeStates(classOf[Camera2DState]) //Problem, it forgets were it was
    }

    def enter {
      println("entering 2d")
      enableStates(
        classOf[GridState],
        classOf[SelectionState],
        classOf[DrawingState])
      stateManager.attach(new Camera2DState)
    }
  }

  case class M3D() extends Mode("3d") {
    def exit {
      disableStates(classOf[Camera3DState])
      removeStates(classOf[Camera3DState])
    }
    def enter {
      println("entering 3d")
      stateManager.attach(new Camera3DState)
    }

  }

  def enableStates(classes: Class[_ <: AppState]*) = setEnabledToStates(true, classes: _*)
  def disableStates(classes: Class[_ <: AppState]*) = setEnabledToStates(false, classes: _*)

  def setEnabledToStates(enabled: Boolean, classes: Class[_ <: AppState]*) {
    for (clazz <- classes) {
      val st = stateManager.getState(clazz)
      if (st != null) st.setEnabled(enabled)
    }
  }

  def removeStates(classes: Class[_ <: AppState]*) = {
    for (clazz <- classes) {
      val st = stateManager.getState(clazz)
      if (st != null) stateManager.detach(st)
    }
  }

}