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
import com.simsilica.lemur.event.DefaultMouseListener
import com.jme3.input.event.MouseMotionEvent
import com.jme3.scene.Spatial
import com.simsilica.lemur.HAlignment
import com.simsilica.lemur.Panel

object GuiFactory {

  def toolbar(width: Int, height: Int) = {
    val bar = createMainPanel(width, height)
    val infoText = createInfoText(width)
    bar.addChild(createGeneralPanel(infoText))
    bar.addChild(createSelectionPanel(infoText))
    bar.addChild(createEditPanel(infoText))
    bar.addChild(createViewPanel(infoText))
    val filling = new Container
    filling.setPreferredSize(new Vector3f(width, 0, 0))
    filling.addChild(infoText)
    bar.addChild(filling)
    bar
  }

  def statusbar(width: Int, height: Int) = {
    val bar = new Container
    val text = new Label("Hello")
    text.setPreferredSize(new Vector3f(width, 0, 0))
    bar.addChild(text)
    bar.setLocalTranslation(200, 22, 0)
    bar
  }

  def createMainPanel(width: Int, height: Int) = {
    val toolbarPanel = new Container(new SpringGridLayout(Axis.X, Axis.Y))
    val title = new Label(" M8 Editor ")
    title.setColor(ColorRGBA.Orange)
    toolbarPanel.addChild(title)
    toolbarPanel.setLocalTranslation(0, height, 0)
    toolbarPanel
  }

  def createInfoText(width: Int) = {
    val infoText = new Label("")
    infoText
  }

  def createGeneralPanel(infoText: Label) = {
    val generalPanel = new Container(new SpringGridLayout(Axis.X, Axis.Y))
    generalPanel.addChild(new Label("|"))
    val exit = generalPanel.addChild(button("application-exit-2.png", "Exit editor", infoText))
    exit.addClickCommands(_ => EventBus.trigger(ExitApplication()))
    val restart = generalPanel.addChild(button("edit-clear-3.png", "Reset map", infoText))
    restart.addClickCommands(_ => {
      for ((id, sec) <- SectorRepository().sectors) {
        EventBus.trigger(SectorDeleted(id))
      }
      SectorRepository().removeAll
    })
    val mode3d = generalPanel.addChild(button("blockdevice.png", "Switch 2D/3D mode", infoText))
    mode3d.addClickCommands(_ => EventBus.trigger(ViewModeSwitch()))
    val draw = generalPanel.addChild(button("draw-freehand.png", "Draw sector", infoText))
    draw.addClickCommands(_ => {
      EventBus.trigger(SelectionModeSwitch(0))
      EventBus.trigger(EditModeSwitch(1))
    })
    generalPanel
  }

  def createSelectionPanel(infoText: Label) = {
    val selectionPanel = new Container(new SpringGridLayout(Axis.X, Axis.Y))
    selectionPanel.addChild(new Label("|"))
    val point = selectionPanel.addChild(button("office-chart-scatter.png", "Select points", infoText))
    val line = selectionPanel.addChild(button("office-chart-polar.png", "Select lines", infoText))
    val sector = selectionPanel.addChild(button("office-chart-polar-stacked.png", "Select sectors", infoText))
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

  def createViewPanel(infoText: Label) = {
    val selectionPanel = new Container(new SpringGridLayout(Axis.X, Axis.Y))
    selectionPanel.addChild(new Label("|"))
    val grid = selectionPanel.addChild(button("view-grid.png", "Grid size", infoText))
    val zoomout = selectionPanel.addChild(button("zoom-out-3.png", "Zoom out", infoText))
    val zoomin = selectionPanel.addChild(button("zoom-in-3.png", "Zoom in", infoText))
    grid.addClickCommands(_ => {
    })
    zoomout.addClickCommands(_ => {
    })
    zoomin.addClickCommands(_ => {
    })
    selectionPanel
  }

  def createEditPanel(infoText: Label) = {
    val editPanel = new Container(new SpringGridLayout(Axis.X, Axis.Y))

    //    editPanel.setLocalTranslation(0, app.getCamera.getHeight - 210, 0)
    editPanel.addChild(new Label("|"))
    val split = editPanel.addChild(button("format-add-node.png", "Split line", infoText))
    split.addClickCommands(_ => EventBus.trigger(SplitSelection()))
    val mode3d = editPanel.addChild(button("format-remove-node.png", "Delete selected points", infoText))
    mode3d.addClickCommands(_ => EventBus.trigger(DeleteSelection()))
    editPanel
  }

  def decorateFirst(b1: Button, bn: Button*) {
    def clean(s: String) = s.replaceAll("\\*", "")
    b1.setText(b1.getText + "*")
    bn.foreach(b => b.setText(clean(b.getText)))
  }

  def button(icon: String = null, description: String, infoText: Label, label: String = "") = {
    val button = new Button(label)
    if (icon != null) {
      button.setIcon(new IconComponent("Interface/Icons/" + icon))
    }
    button.addMouseListener(new DefaultMouseListener() {
      override def mouseEntered(e: MouseMotionEvent, t: Spatial, s: Spatial) {
        infoText.setText(description)
      }
      override def mouseExited(e: MouseMotionEvent, t: Spatial, s: Spatial) {
        infoText.setText("")
      }
    })
    button
  }
}