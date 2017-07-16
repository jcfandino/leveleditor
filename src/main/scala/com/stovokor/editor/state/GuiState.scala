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
import com.stovokor.util.SelectionModeSwitch

class GuiState extends BaseState {

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)

    createGeneralWindow()
    createSelectionWindow()
  }

  def createGeneralWindow() {
    val generalWindow = new Container
    guiNode.attachChild(generalWindow)
    generalWindow.setLocalTranslation(0, app.getCamera.getHeight, 0)
    generalWindow.addChild(new Label("Level Editor"))
    val clickMe = generalWindow.addChild(new Button("Exit"))
    clickMe.addClickCommands(new Command[Button]() {
      def execute(source: Button) {
        println("God bye!")
        app.stop();
      }
    })
    val mode3d = generalWindow.addChild(new Button("2D/3D"))
    mode3d.addClickCommands(new Command[Button]() {
      def execute(source: Button) {
        println("mode switch")
        EventBus.trigger(ModeSwitch())
      }
    })
  }

  def createSelectionWindow() {
    val selectionWindow = new Container
    guiNode.attachChild(selectionWindow)
    selectionWindow.setLocalTranslation(0, app.getCamera.getHeight - 100, 0)
    selectionWindow.addChild(new Label("Selection"))
    val point = selectionWindow.addChild(new Button("Point"))
    val line = selectionWindow.addChild(new Button("Line"))
    val sector = selectionWindow.addChild(new Button("Sector"))
    point.addClickCommands(new Command[Button]() {
      def execute(source: Button) {
        EventBus.trigger(SelectionModeSwitch(0))
        decorateFirst(point, line, sector)
      }
    })
    line.addClickCommands(new Command[Button]() {
      def execute(source: Button) {
        EventBus.trigger(SelectionModeSwitch(1))
        decorateFirst(line, point, sector)
      }
    })
    sector.addClickCommands(new Command[Button]() {
      def execute(source: Button) {
        EventBus.trigger(SelectionModeSwitch(2))
        decorateFirst(sector, point, line)
      }
    })
  }

  def decorateFirst(b1: Button, bn: Button*) {
    def clean(s: String) = s.replaceAll("^\\[ ", "").replaceAll(" \\]$", "")
    b1.setText(s"[ ${clean(b1.getText)} ]")
    bn.foreach(b => b.setText(clean(b.getText)))
  }

  override def update(tpf: Float) {
  }
}