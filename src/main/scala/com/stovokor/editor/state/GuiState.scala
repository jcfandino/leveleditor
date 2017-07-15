package com.stovokor.editor.state

import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import com.jme3.app.Application
import com.jme3.app.SimpleApplication
import com.jme3.scene.Node
import com.simsilica.lemur.Container
import com.simsilica.lemur.Label
import com.simsilica.lemur.Button
import com.simsilica.lemur.Command
import com.stovokor.util.EventBus
import com.stovokor.util.ModeSwitch

class GuiState extends BaseState {

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)

    // Create a simple container for our elements
    val window = new Container
    guiNode.attachChild(window)

    // Put it somewhere that we will see it.
    // Note: Lemur GUI elements grow down from the upper left corner.
    window.setLocalTranslation(0, app.getCamera.getHeight, 0)

    // Add some elements
    window.addChild(new Label("Level Editor"))
    val clickMe = window.addChild(new Button("Exit"))
    clickMe.addClickCommands(new Command[Button]() {
      def execute(source: Button) {
        println("God bye!")
        app.stop();
      }
    })
    val mode3d = window.addChild(new Button("2D/3D"))
    mode3d.addClickCommands(new Command[Button]() {
      def execute(source: Button) {
        println("mode switch")
        EventBus.trigger(ModeSwitch())
      }
    })
  }
  override def update(tpf: Float) {
  }
}