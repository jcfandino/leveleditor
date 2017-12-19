package com.stovokor.editor.state

import com.jme3.app.Application
import com.jme3.app.state.AppStateManager
import com.stovokor.editor.gui.GuiFactory
import com.stovokor.util.EventBus
import com.stovokor.util.ExitApplication
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EditorEvent
import com.stovokor.util.SelectionModeSwitch
import com.stovokor.editor.input.Modes.SelectionMode
import com.simsilica.lemur.Container
import com.simsilica.lemur.Button
import com.simsilica.lemur.component.QuadBackgroundComponent
import com.jme3.math.ColorRGBA
import com.stovokor.util.EditModeSwitch
import com.stovokor.editor.input.Modes.EditMode
import com.stovokor.util.ToggleSnapToGrid
import com.stovokor.util.GridSnapper
import com.stovokor.util.ChangeGridSize
import com.simsilica.lemur.Label

class GuiState extends BaseState
    with EditorEventListener {

  def unselectedBackground = new Button("").getBackground
  def selectedBackground = new QuadBackgroundComponent(ColorRGBA.Blue)

  var selectionModeToUpdate: Option[SelectionMode] = None
  var editModeToUpdate: Option[EditMode] = None
  var snapToGridUpdate = true
  var statusTextToUpdate = true

  var toolbar: Container = null
  var statusbar: Container = null

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    toolbar = GuiFactory.toolbar(app.getCamera.getWidth, app.getCamera.getHeight)
    statusbar = GuiFactory.statusbar(app.getCamera.getWidth, app.getCamera.getHeight)
    guiNode.attachChild(toolbar)
    guiNode.attachChild(statusbar)
    EventBus.subscribeByType(this, classOf[SelectionModeSwitch])
    EventBus.subscribeByType(this, classOf[EditModeSwitch])
    EventBus.subscribe(this, ToggleSnapToGrid())
    EventBus.subscribe(this, ChangeGridSize())
  }

  override def cleanup {
    EventBus.removeFromAll(this)
  }

  override def update(tpf: Float) {
    if (selectionModeToUpdate.isDefined) {
      decorate("selectPoint", selectionModeToUpdate, SelectionMode.Point)
      decorate("selectLine", selectionModeToUpdate, SelectionMode.Line)
      decorate("selectSector", selectionModeToUpdate, SelectionMode.Sector)
      selectionModeToUpdate = None
    }
    if (editModeToUpdate.isDefined) {
      decorate("drawSector", editModeToUpdate, EditMode.Draw)
      decorate("fillHole", editModeToUpdate, EditMode.Fill)
      editModeToUpdate = None
    }
    if (snapToGridUpdate) {
      decorate("snapToGrid", Some(GridSnapper.snapToGrid), true)
      snapToGridUpdate = false
    }
    if (statusTextToUpdate) {
      val message = s"Grid size: ${GridSnapper.gridStep}"
      val text = statusbar.getChild("statusText").asInstanceOf[Label].setText(message)
      statusTextToUpdate = false
    }
  }

  def decorate(name: String, mode: Option[Any], when: Any) {
    val button = toolbar.getChild(name).asInstanceOf[Button]
    button.setBackground(if (mode.contains(when)) selectedBackground else unselectedBackground)
  }

  def onEvent(event: EditorEvent) = event match {
    case SelectionModeSwitch(m) => setSelectionMode(m)
    case EditModeSwitch(m)      => setEditMode(m)
    case ToggleSnapToGrid()     => snapToGridUpdate = true
    case ChangeGridSize()       => statusTextToUpdate = true
    case _                      =>
  }

  def setSelectionMode(newMode: SelectionMode) {
    selectionModeToUpdate = Some(newMode)
    if (newMode != SelectionMode.None) {
      editModeToUpdate = Some(EditMode.Select)
    }
  }

  def setEditMode(newMode: EditMode) {
    editModeToUpdate = Some(newMode)
    if (newMode != EditMode.Select) {
      selectionModeToUpdate = Some(SelectionMode.None)
    }
  }
}
