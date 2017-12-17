package com.stovokor.editor.state

import com.jme3.app.Application
import com.jme3.app.state.AppStateManager
import com.stovokor.editor.gui.GuiFactory
import com.stovokor.util.EventBus
import com.stovokor.util.ExitApplication
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EditorEvent

class GuiState extends BaseState {

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    val toolbar = GuiFactory.toolbar(app.getCamera.getWidth, app.getCamera.getHeight)
    val statusbar = GuiFactory.statusbar(app.getCamera.getWidth, app.getCamera.getHeight)
    guiNode.attachChild(toolbar)
    guiNode.attachChild(statusbar)
  }

  override def update(tpf: Float) {
  }

}
