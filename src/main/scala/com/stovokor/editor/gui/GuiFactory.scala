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
import com.stovokor.util.SaveMap
import com.stovokor.util.OpenMap
import com.stovokor.editor.model.repository.BorderRepository
import com.stovokor.util.ExportMap
import com.jme3.material.Material
import com.stovokor.editor.model.SurfaceMaterial
import com.jme3.math.Vector2f
import com.sun.java.swing.plaf.gtk.GTKConstants.IconSize
import com.simsilica.lemur.GridPanel
import com.stovokor.editor.model.SimpleMaterial
import com.stovokor.editor.model.MatDefMaterial
import com.stovokor.util.EditSettings
import com.stovokor.editor.model.Settings
import com.stovokor.editor.state.CanOpenDialog
import javax.swing.JFileChooser
import java.io.File
import javax.swing.filechooser.FileFilter

object GuiFactory {

  val matIconSize = new Vector2f(75, 75)

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
    val newMap = generalPanel.addChild(button("document-new-8.png", "Open", infoText))
    val open = generalPanel.addChild(button("document-open-2.png", "Open", infoText))
    val save = generalPanel.addChild(button("document-save-5.png", "Save", infoText))
    val saveAs = generalPanel.addChild(button("document-save-as-5.png", "Save as...", infoText))
    val export = generalPanel.addChild(button("lorry-go.png", "Export...", infoText))
    val settings = generalPanel.addChild(button("configure-5.png", "Settings...", infoText))
    val exit = generalPanel.addChild(button("application-exit-2.png", "Exit editor", infoText))
    exit.addClickCommands(_ => EventBus.trigger(ExitApplication()))
    open.addClickCommands(_ => EventBus.trigger(OpenMap()))
    save.addClickCommands(_ => EventBus.trigger(SaveMap(true)))
    saveAs.addClickCommands(_ => EventBus.trigger(SaveMap(false)))
    export.addClickCommands(_ => EventBus.trigger(ExportMap()))
    settings.addClickCommands(_ => EventBus.trigger(EditSettings()))
    val restart = generalPanel.addChild(button("edit-clear-3.png", "Reset map", infoText))
    restart.addClickCommands(_ => {
      // TODO Extract this to a state and call with an event
      for ((id, sec) <- SectorRepository().sectors) {
        EventBus.trigger(SectorDeleted(id))
      }
      SectorRepository().removeAll
      BorderRepository().removeAll
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

  def button(icon: String = null, description: String, infoText: Label = null, label: String = "", iconSize: Vector2f = null): Button = {
    val button = new Button(label)
    if (icon != null) {
      val base = if (icon.contains("/")) "" else "Interface/Icons/"
      val iconComponent = new IconComponent(base + icon)
      if (iconSize != null) {
        val width = iconComponent.getImageTexture.getImage.getWidth
        val height = iconComponent.getImageTexture.getImage.getWidth
        iconComponent.setIconScale(new Vector2f(iconSize.x / width, iconSize.y / height))
      }
      button.setIcon(iconComponent)
    }
    if (infoText != null) {
      button.addMouseListener(new DefaultMouseListener() {
        override def mouseEntered(e: MouseMotionEvent, t: Spatial, s: Spatial) {
          infoText.setText(description)
        }
        override def mouseExited(e: MouseMotionEvent, t: Spatial, s: Spatial) {
          infoText.setText("")
        }
      })
    }
    button
  }

  def createMaterialPanel(width: Int, height: Int, desc: String, options: List[SurfaceMaterial], callback: Option[SurfaceMaterial] => Unit) = {
    val materialPanel = new Container(new SpringGridLayout()) //Axis.Y, Axis.X))

    materialPanel.setLocalTranslation(50, height - 50, 0)
    materialPanel.setPreferredSize(new Vector3f(width - 100, height - 100, 0))
    materialPanel.addChild(new Label("Material selection for " + desc))
    val infoText = materialPanel.addChild(new Label(""))
    val optionsPanel = materialPanel.addChild(new Container(new SpringGridLayout(Axis.X, Axis.Y)))
    optionsPanel.setPreferredSize(new Vector3f(width - 100, height - 150, 0))

    options.foreach(mat => {
      val preview = mat match {
        case SimpleMaterial(path) => path
        case _                    => "no-preview.png"
      }
      val matButton = optionsPanel.addChild(button(preview, mat.path, infoText, iconSize = matIconSize))
      matButton.setSize(new Vector3f(50, 50, 0))
      matButton.setPreferredSize(new Vector3f(matIconSize.x, matIconSize.y, 0))
      matButton.setMaxWidth(matIconSize.x)
      matButton.addClickCommands(_ => {
        materialPanel.removeFromParent()
        callback(Some(mat))
      })
    })

    val cancel = materialPanel.addChild(button("dialog-cancel-3.png", "cancel", infoText, label = "cancel"))
    cancel.addClickCommands(_ => {
      materialPanel.removeFromParent()
      callback(None)
    })
    materialPanel
  }

  def createSettingsPanel(width: Int, height: Int, current: => Settings, update: Settings => Unit, close: Boolean => Unit) = {
    val dialogOpener = new CanOpenDialog {
      def openDirectoryFinder() = {
        val frame = createFrame
        val fileChooser = new JFileChooser
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
        val result = fileChooser.showOpenDialog(frame)
        var selected: Option[String] = None
        if (result == JFileChooser.APPROVE_OPTION) {
          val file = fileChooser.getSelectedFile
          selected = Some(file.getAbsolutePath)
        }
        frame.dispose()
        selected
      }
    }
    val settingsPanel = new Container(new SpringGridLayout()) //Axis.Y, Axis.X))

    settingsPanel.setLocalTranslation(50, height - 50, 0)
    settingsPanel.setPreferredSize(new Vector3f(width - 100, height - 100, 0))
    val title = settingsPanel.addChild(new Container)
    title.addChild(new Label("Settings"))

    val optionsPanel = settingsPanel.addChild(new Container(new SpringGridLayout(Axis.Y, Axis.X)))
    optionsPanel.setPreferredSize(new Vector3f(width - 100, height - 150, 0))

    val assetPathPanel = optionsPanel.addChild(new Container(new SpringGridLayout(Axis.X, Axis.Y)))
    assetPathPanel.addChild(new Label("Assets path:"))
    val currentPath = assetPathPanel.addChild(new Label(current.assetsBasePath))
    val change = assetPathPanel.addChild(button("magnifier.png", "Find"))
    change.setPreferredSize(new Vector3f())
    change.addClickCommands(_ => {
      dialogOpener.openDirectoryFinder().foreach(path => {
        println(s"Selected path: $path")
        update(current.updateAssetBasePath(path))
        currentPath.setText(path)
      })
    })
    val filling = new Container
    filling.setPreferredSize(new Vector3f(width - 100, height - 200, 0))
    optionsPanel.addChild(filling)

    val buttons = settingsPanel.addChild(new Container(new SpringGridLayout(Axis.X, Axis.Y)))
    buttons.addChild(new Label(""))
    val save = buttons.addChild(button("dialog-apply.png", "save", label = "save"))
    save.addClickCommands(_ => {
      settingsPanel.removeFromParent()
      close(true)
    })
    val cancel = buttons.addChild(button("dialog-cancel-3.png", "cancel", label = "cancel"))
    cancel.addClickCommands(_ => {
      settingsPanel.removeFromParent()
      close(false)
    })
    buttons.addChild(new Label(""))

    settingsPanel
  }
}