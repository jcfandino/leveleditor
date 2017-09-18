package com.stovokor.editor.gui

import com.simsilica.lemur.Button
import com.simsilica.lemur.component.IconComponent
import com.simsilica.lemur.component.SpringGridLayout
import com.simsilica.lemur.Axis
import com.simsilica.lemur.Container
import com.stovokor.editor.model.repository.SectorRepository
import com.jme3.math.Vector3f
import com.stovokor.util.SelectionModeSwitch
import com.stovokor.util.SectorDeleted
import com.stovokor.util.SplitSelection
import com.stovokor.util.DeleteSelection
import com.stovokor.util.ViewModeSwitch
import com.stovokor.util.EditModeSwitch
import com.stovokor.util.EventBus
import com.simsilica.lemur.Label
import com.stovokor.util.ExitApplication
import com.jme3.math.ColorRGBA

object GuiFactory {

  def toolbar(width: Int, height: Int) = {
    val bar = createMainPanel(width, height)
    bar.addChild(createGeneralPanel())
    bar.addChild(createSelectionPanel())
    bar.addChild(createEditPanel())
    bar.addChild(createViewPanel())
    bar
  }

  def createMainPanel(width: Int, height: Int) = {
    val generalWindow = new Container(new SpringGridLayout(Axis.X, Axis.Y))
    generalWindow.addChild(new Label("M8 Editor"))
    generalWindow.setLocalTranslation(0, height, 0)
    generalWindow.setSize(new Vector3f(width, 0, 0))
    generalWindow
  }
  def createGeneralPanel() = {
    val generalPanel = new Container(new SpringGridLayout(Axis.X, Axis.Y))
    val exit = generalPanel.addChild(button("application-exit-2.png"))
    exit.addClickCommands(_ => EventBus.trigger(ExitApplication()))
    val restart = generalPanel.addChild(button("edit-clear-3.png"))
    restart.addClickCommands(_ => {
      for ((id, sec) <- SectorRepository().sectors) {
        EventBus.trigger(SectorDeleted(id))
      }
      SectorRepository().removeAll
    })
    val mode3d = generalPanel.addChild(button("blockdevice.png"))
    mode3d.addClickCommands(_ => EventBus.trigger(ViewModeSwitch()))
    val draw = generalPanel.addChild(button("draw-freehand.png"))
    draw.addClickCommands(_ => {
      EventBus.trigger(SelectionModeSwitch(0))
      EventBus.trigger(EditModeSwitch(1))
    })
    generalPanel
  }

  def createSelectionPanel() = {
    val selectionPanel = new Container(new SpringGridLayout(Axis.X, Axis.Y))
    selectionPanel.addChild(new Label("|"))
    val point = selectionPanel.addChild(button("office-chart-scatter.png"))
    val line = selectionPanel.addChild(button("office-chart-polar.png"))
    val sector = selectionPanel.addChild(button("office-chart-polar-stacked.png"))
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

  def createViewPanel() = {
    val selectionPanel = new Container(new SpringGridLayout(Axis.X, Axis.Y))
    selectionPanel.addChild(new Label("|"))
    val grid = selectionPanel.addChild(button("view-grid.png"))
    val zoomout = selectionPanel.addChild(button("zoom-out-3.png"))
    val zoomin = selectionPanel.addChild(button("zoom-in-3.png"))
    grid.addClickCommands(_ => {
    })
    zoomout.addClickCommands(_ => {
    })
    zoomin.addClickCommands(_ => {
    })
    selectionPanel
  }

  def createEditPanel() = {
    val editPanel = new Container(new SpringGridLayout(Axis.X, Axis.Y))

    //    editPanel.setLocalTranslation(0, app.getCamera.getHeight - 210, 0)
    editPanel.addChild(new Label("|"))
    val split = editPanel.addChild(button("format-add-node.png"))
    split.addClickCommands(_ => EventBus.trigger(SplitSelection()))
    val mode3d = editPanel.addChild(button("format-remove-node.png"))
    mode3d.addClickCommands(_ => EventBus.trigger(DeleteSelection()))
    editPanel
  }

  def decorateFirst(b1: Button, bn: Button*) {
    def clean(s: String) = s.replaceAll("\\*", "")
    b1.setText(b1.getText + "*")
    bn.foreach(b => b.setText(clean(b.getText)))
  }

  def button(icon: String = null, label: String = "", description: String = "") = {
    val button = new Button(label)
    if (icon != null) {
      button.setIcon(new IconComponent("Interface/Icons/" + icon))
    }
    // TODO on hover show description
    button
  }
}