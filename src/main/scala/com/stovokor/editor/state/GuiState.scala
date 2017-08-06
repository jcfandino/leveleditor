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
import com.stovokor.editor.model.repository.SectorRepository
import com.stovokor.util.SectorDeleted
import com.jme3.math.Vector3f

class GuiState extends BaseState {

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)

    val main = createMainPanel()
    main.addChild(createGeneralPanel())
    main.addChild(createSelectionPanel())
    main.addChild(createEditPanel())
  }

  def createMainPanel() = {
    val generalWindow = new Container
    generalWindow.addChild(new Label("M8 Editor"))
    guiNode.attachChild(generalWindow)
    generalWindow.setLocalTranslation(0, app.getCamera.getHeight, 0)
        generalWindow.setSize(new Vector3f(0f, app.getCamera.getHeight, 0))
    generalWindow
  }
  def createGeneralPanel() = {
    val generalPanel = new Container
    val exit = generalPanel.addChild(new Button("Exit"))
    val restart = generalPanel.addChild(new Button("New"))
    restart.addClickCommands(_ => {
      val repo = SectorRepository()
      for ((id, sec) <- repo.sectors) {
        repo.remove(id)
        EventBus.trigger(SectorDeleted(id))
      }
    })
    exit.addClickCommands(_ => app.stop)
    val mode3d = generalPanel.addChild(new Button("2D/3D"))
    mode3d.addClickCommands(_ => EventBus.trigger(ViewModeSwitch()))
    val draw = generalPanel.addChild(new Button("Draw"))
    draw.addClickCommands(_ => {
      EventBus.trigger(SelectionModeSwitch(0))
      EventBus.trigger(EditModeSwitch(1))
    })
    generalPanel
  }

  def createSelectionPanel() = {
    val selectionPanel = new Container
    //    selectionPanel.setLocalTranslation(0, app.getCamera.getHeight - 120, 0)
    selectionPanel.addChild(new Label("Selection"))
    val point = selectionPanel.addChild(new Button("Point"))
    val line = selectionPanel.addChild(new Button("Line"))
    val sector = selectionPanel.addChild(new Button("Sector"))
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
    selectionPanel
  }
  def createEditPanel() = {
    val editPanel = new Container
    //    editPanel.setLocalTranslation(0, app.getCamera.getHeight - 210, 0)
    editPanel.addChild(new Label("Edit"))
    val split = editPanel.addChild(new Button("Split"))
    split.addClickCommands(_ => EventBus.trigger(SplitSelection()))
    val mode3d = editPanel.addChild(new Button("Delete"))
    mode3d.addClickCommands(_ => EventBus.trigger(DeleteSelection()))
    editPanel
  }

  def decorateFirst(b1: Button, bn: Button*) {
    def clean(s: String) = s.replaceAll("^\\[ ", "").replaceAll(" \\]$", "")
    b1.setText(s"[ ${clean(b1.getText)} ]")
    bn.foreach(b => b.setText(clean(b.getText)))
  }

  override def update(tpf: Float) {
  }
}