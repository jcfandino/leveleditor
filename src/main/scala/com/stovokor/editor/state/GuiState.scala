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
import com.stovokor.util.ViewModeSwitch
import com.stovokor.util.SelectionModeSwitch
import com.stovokor.util.DeleteSelection
import com.stovokor.util.SplitSelection
import com.stovokor.util.EditModeSwitch
import com.stovokor.util.LemurExtensions._

class GuiState extends BaseState {

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)

    createGeneralWindow()
    createSelectionWindow()
    createEditWindow()
  }

  def createGeneralWindow() {
    val generalWindow = new Container
    guiNode.attachChild(generalWindow)
    generalWindow.setLocalTranslation(0, app.getCamera.getHeight, 0)
    generalWindow.addChild(new Label("Level Editor"))
    val exit = generalWindow.addChild(new Button("Exit"))
    exit.addClickCommands(_ => app.stop)
    val mode3d = generalWindow.addChild(new Button("2D/3D"))
    mode3d.addClickCommands(_ => EventBus.trigger(ViewModeSwitch()))
    val draw = generalWindow.addChild(new Button("Draw"))
    draw.addClickCommands(_ => {
      EventBus.trigger(SelectionModeSwitch(0))
      EventBus.trigger(EditModeSwitch(1))
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
    point.addClickCommands(_ => {
      EventBus.trigger(EditModeSwitch(0))
      EventBus.trigger(SelectionModeSwitch(1))
      decorateFirst(point, line, sector)
    })
    line.addClickCommands(_ => {
      EventBus.trigger(EditModeSwitch(0))
      EventBus.trigger(SelectionModeSwitch(2))
      decorateFirst(line, point, sector)
    })
    sector.addClickCommands(_ => {
      EventBus.trigger(EditModeSwitch(0))
      EventBus.trigger(SelectionModeSwitch(3))
      decorateFirst(sector, point, line)
    })
  }
  def createEditWindow() {
    val editWindow = new Container
    guiNode.attachChild(editWindow)
    editWindow.setLocalTranslation(0, app.getCamera.getHeight - 200, 0)
    editWindow.addChild(new Label("Edit"))
    val split = editWindow.addChild(new Button("Split"))
    split.addClickCommands(_ => EventBus.trigger(SplitSelection()))
    val mode3d = editWindow.addChild(new Button("Delete"))
    mode3d.addClickCommands(_ => EventBus.trigger(DeleteSelection()))
  }

  def decorateFirst(b1: Button, bn: Button*) {
    def clean(s: String) = s.replaceAll("^\\[ ", "").replaceAll(" \\]$", "")
    b1.setText(s"[ ${clean(b1.getText)} ]")
    bn.foreach(b => b.setText(clean(b.getText)))
  }

  override def update(tpf: Float) {
  }
}