package com.stovokor.editor.state

import com.jme3.app.Application
import com.jme3.app.state.AppStateManager
import com.stovokor.editor.gui.GuiFactory
import com.stovokor.util.EventBus
import com.stovokor.util.ExitApplication
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EditorEvent

class GuiState extends BaseState with EditorEventListener {

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    val toolbar = GuiFactory.toolbar(app.getCamera.getWidth, app.getCamera.getHeight)
    guiNode.attachChild(toolbar)
    EventBus.subscribe(this, ExitApplication())
  }

  override def update(tpf: Float) {
  }

  def onEvent(event: EditorEvent) = event match {
    case ExitApplication() => app.stop()
    case _                 =>
  }
}
