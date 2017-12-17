package com.stovokor.editor.state

import com.jme3.app.Application
import com.jme3.app.state.AppStateManager
import com.simsilica.lemur.Container
import com.simsilica.lemur.input.FunctionId
import com.simsilica.lemur.input.InputState
import com.simsilica.lemur.input.StateFunctionListener
import com.stovokor.editor.gui.GuiFactory
import com.stovokor.editor.input.InputFunction
import com.stovokor.editor.model.Settings
import com.stovokor.editor.model.repository.Repositories
import com.stovokor.util.EditSettings
import com.stovokor.util.EditorEvent
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EventBus
import com.stovokor.util.SettingsUpdated

class SettingsEditorState extends BaseState
    with EditorEventListener
    with CanMapInput
    with StateFunctionListener {

  val settingsRepository = Repositories.settingsRepository

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    EventBus.subscribe(this, EditSettings())
    setupInput
  }

  override def cleanup() {
    super.cleanup
    EventBus.removeFromAll(this)
    inputMapper.removeStateListener(this, InputFunction.settings)
  }

  def onEvent(event: EditorEvent) = event match {
    case EditSettings() => openSettingsDialog()
    case _              =>
  }

  def setupInput {
    inputMapper.addStateListener(this, InputFunction.settings)
    inputMapper.activateGroup(InputFunction.general)
  }

  def valueChanged(func: FunctionId, value: InputState, tpf: Double) {
    if (value == InputState.Positive) func match {
      case InputFunction.settings => openSettingsDialog()
      case _                      =>
    }
  }
  var settingsDialog: Option[Container] = None
  var updatedSettings = Settings()

  def openSettingsDialog() {
    println("opening settings dialog")
    settingsDialog.foreach(_.removeFromParent())
    updatedSettings = settingsRepository.get()
    val dialog = GuiFactory.createSettingsPanel(cam.getWidth, cam.getHeight, updatedSettings, settingsUpdated, closeDialog)
    dialog.setName("material-dialog")
    guiNode.attachChild(dialog)
    settingsDialog = Some(dialog)
  }

  def settingsUpdated(updated: Settings) {
    updatedSettings = updated
  }

  def closeDialog(save: Boolean) {
    settingsDialog.foreach(_.removeFromParent)
    if (save) {
      println(s"Settings saved $updatedSettings")
      settingsRepository.update(updatedSettings)
      EventBus.trigger(SettingsUpdated())
    }
  }

}
