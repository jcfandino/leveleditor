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
import com.stovokor.util.ChangeGridSize
import com.stovokor.util.ChangeZoom
import com.stovokor.editor.input.Modes.EditMode
import com.stovokor.editor.input.Modes.SelectionMode
import com.stovokor.util.ToggleEffects
import com.simsilica.lemur.Insets3f
import com.simsilica.lemur.FillMode
import com.simsilica.lemur.component.QuadBackgroundComponent
import com.simsilica.lemur.OptionPanel
import com.simsilica.lemur.Action
import com.simsilica.lemur.grid.ArrayGridModel
import com.jme3.texture.Texture
import com.simsilica.lemur.GuiGlobals
import com.stovokor.util.StartNewMap

object GuiFactory {

  val iconBasePath = "Interface/Icons/"

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
    newMap.addClickCommands(_ => EventBus.trigger(StartNewMap()))
    open.addClickCommands(_ => EventBus.trigger(OpenMap()))
    save.addClickCommands(_ => EventBus.trigger(SaveMap(true)))
    saveAs.addClickCommands(_ => EventBus.trigger(SaveMap(false)))
    export.addClickCommands(_ => EventBus.trigger(ExportMap()))
    settings.addClickCommands(_ => EventBus.trigger(EditSettings()))
    val mode3d = generalPanel.addChild(button("blockdevice.png", "Switch 2D/3D mode", infoText))
    mode3d.addClickCommands(_ => EventBus.trigger(ViewModeSwitch()))
    val draw = generalPanel.addChild(button("draw-freehand.png", "Draw sector", infoText))
    draw.addClickCommands(_ => {
      EventBus.trigger(SelectionModeSwitch(SelectionMode.None))
      EventBus.trigger(EditModeSwitch(EditMode.Draw))
    })
    val fillHole = generalPanel.addChild(button("applications-development-4.png", "Hole to sector", infoText))
    fillHole.addClickCommands(_ => {
      EventBus.trigger(SelectionModeSwitch(SelectionMode.None))
      EventBus.trigger(EditModeSwitch(EditMode.Fill))
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
      EventBus.trigger(EditModeSwitch(EditMode.Select))
      EventBus.trigger(SelectionModeSwitch(SelectionMode.Point))
      decorateFirst(point, line, sector)
    })
    line.addClickCommands(_ => {
      EventBus.trigger(EditModeSwitch(EditMode.Select))
      EventBus.trigger(SelectionModeSwitch(SelectionMode.Line))
      decorateFirst(line, point, sector)
    })
    sector.addClickCommands(_ => {
      EventBus.trigger(EditModeSwitch(EditMode.Select))
      EventBus.trigger(SelectionModeSwitch(SelectionMode.Sector))
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
    val fog = selectionPanel.addChild(button("weather-fog-2.png", "Enable 3D Effects", infoText))
    grid.addClickCommands(_ => EventBus.trigger(ChangeGridSize()))
    zoomout.addClickCommands(_ => EventBus.trigger(ChangeZoom(1)))
    zoomin.addClickCommands(_ => EventBus.trigger(ChangeZoom(-1)))
    fog.addClickCommands(_ => EventBus.trigger(ToggleEffects()))
    selectionPanel
  }

  def createEditPanel(infoText: Label) = {
    val editPanel = new Container(new SpringGridLayout(Axis.X, Axis.Y))
    editPanel.addChild(new Label("|"))
    val split = editPanel.addChild(button("format-add-node.png", "Split line", infoText))
    split.addClickCommands(_ => EventBus.trigger(SplitSelection()))
    // TODO Not implemented yet
    // val remove = editPanel.addChild(button("format-remove-node.png", "Delete selected points", infoText))
    // remove.addClickCommands(_ => EventBus.trigger(DeleteSelection()))
    editPanel
  }

  def decorateFirst(b1: Button, bn: Button*) {
    b1.setBackground(new QuadBackgroundComponent(ColorRGBA.Blue))
    bn.foreach(b => b.setBackground(new Button("").getBackground))
  }

  def button(icon: String = null, description: String, infoText: Label = null, label: String = ""): Button = {
    val button = new Button(label)
    if (icon != null) {
      val base = if (icon.contains("/")) "" else iconBasePath
      val iconComponent = new IconComponent(base + icon)
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

  def createMaterialPanel(width: Int, height: Int, options: List[SurfaceMaterial], callback: Option[SurfaceMaterial] => Unit) = {
    val (buttonWidth, buttonHeight) = (75, 75)
    val materialPanel = new Container(new SpringGridLayout())
    val infoText = new Label("")
    val cols = ((width - 150) / buttonWidth) - 1
    val rows = (height - 200) / buttonHeight
    def padding(i: Int) = 1 to i map (_ => new Panel)
    val matrix: Array[Array[Panel]] = options.map(mat => {
      val preview = mat match {
        case SimpleMaterial(path) => path
        case _                    => iconBasePath + "no-preview.png"
      }
      val matButton = button(description = preview, infoText = infoText)
      val tex = GuiGlobals.getInstance().loadTexture(preview, false, false);
      matButton.setBackground(new QuadBackgroundComponent(tex))
      matButton.setSize(new Vector3f(buttonWidth, buttonHeight, 0))
      matButton.setMaxWidth(buttonWidth)
      matButton.addClickCommands(_ => callback(Some(mat)))
      matButton.asInstanceOf[Panel]
    })
      .sliding(cols, cols)
      .map(l => if (l.size == cols) l else l ++ padding(cols - l.size))
      .map(_.toArray)
      .toArray

    val gridModel = new ArrayGridModel(matrix)
    val optionsPanel = materialPanel.addChild(new GridPanel(gridModel))
    optionsPanel.setPreferredSize(new Vector3f(width - 100, height - 150, 0))
    optionsPanel.setVisibleColumns(cols)
    optionsPanel.setVisibleRows(rows)

    val navPanel = materialPanel.addChild(new Container(new SpringGridLayout(Axis.X, Axis.Y, FillMode.First, FillMode.First)))
    navPanel.addChild(infoText)
    val pgup = navPanel.addChild(button(null, "", label = " << "))
    val less = navPanel.addChild(button(null, "", label = " < "))
    val more = navPanel.addChild(button(null, "", label = " > "))
    val pgdn = navPanel.addChild(button(null, "", label = " >> "))
    val maxRow = (matrix.length - rows).max(0)
    less.addClickCommands(_ => optionsPanel.setRow((optionsPanel.getRow - 1).max(0)))
    more.addClickCommands(_ => optionsPanel.setRow((optionsPanel.getRow + 1).min(maxRow)))
    pgup.addClickCommands(_ => optionsPanel.setRow((optionsPanel.getRow - rows).max(0)))
    pgdn.addClickCommands(_ => optionsPanel.setRow((optionsPanel.getRow + rows).min(maxRow)))
    materialPanel
  }

  def createMaterialDialog(width: Int, height: Int, desc: String, options: List[SurfaceMaterial], callback: Option[SurfaceMaterial] => Unit) = {
    val materialPanel = createMaterialPanel(width, height, options, callback)
    val window = new OptionPanel("Material selection for " + desc, action("cancel", "dialog-cancel-3.png", _ => callback(None)))
    window.getContainer.addChild(materialPanel)
    window
  }

  def action(label: String, icon: String, action: Button => Unit): Action = {
    new Action(label, new IconComponent(iconBasePath + icon)) {
      def execute(b: Button) = action(b)
    }
  }

  def createDialog(width: Int, height: Int, title: String, content: Panel, buttons: Button*) = {
    val window = new Container
    window.setLocalTranslation(50, height - 50, 0)
    window.setPreferredSize(new Vector3f(width - 100, height - 100, 0))
    val titleLabel = window.addChild(new Label(title))
    titleLabel.setBackground(new QuadBackgroundComponent(ColorRGBA.Brown))
    titleLabel.setColor(ColorRGBA.LightGray)
    titleLabel.setTextHAlignment(HAlignment.Center)

    window.addChild(content)
    val buttonsContainer = window.addChild(new Container(new SpringGridLayout(Axis.X, Axis.Y, FillMode.First, FillMode.First)));
    buttonsContainer.addChild(new Container)
    buttons.foreach(buttonsContainer.addChild(_))
    window
  }

  def createSettingsPanel(width: Int, height: Int, current: => Settings, update: Settings => Unit, close: Boolean => Unit) = {
    val dialogOpener = new CanOpenDialog {
      def openDirectoryFinder() = {
        val frame = getSwingFrame
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

    val save = button("dialog-apply.png", "save", label = "save")
    val cancel = button("dialog-cancel-3.png", "cancel", label = "cancel")
    val window = createDialog(width, height, "Settings", settingsPanel, save, cancel)
    save.addClickCommands(_ => {
      close(true)
    })
    cancel.addClickCommands(_ => {
      close(false)
    })
    window
  }
}