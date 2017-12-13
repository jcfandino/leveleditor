package com.stovokor.editor.state

import com.simsilica.lemur.input.StateFunctionListener
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EditorEvent
import com.jme3.app.state.AppStateManager
import com.stovokor.editor.input.InputFunction
import com.stovokor.util.ExportMap
import com.stovokor.util.EventBus
import com.jme3.app.Application
import com.stovokor.util.ChangeMaterial
import com.stovokor.editor.model.repository.BorderRepository
import com.stovokor.editor.model.repository.SectorRepository
import com.stovokor.util.SectorSurfaceMutator
import com.stovokor.editor.gui.GuiFactory
import com.simsilica.lemur.Container
import com.stovokor.editor.model.repository.Repositories
import com.stovokor.editor.model.SimpleMaterial
import com.stovokor.editor.model.SurfaceMaterial
import com.stovokor.editor.model.MatDefMaterial
import com.stovokor.util.EditSettings
import com.stovokor.editor.model.Settings
import com.stovokor.util.SettingsUpdated

class SettingsEditorState extends BaseState with EditorEventListener {

  val settingsRepository = Repositories.settingsRepository

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    EventBus.subscribe(this, EditSettings())
  }

  override def cleanup() {
    super.cleanup
    EventBus.removeFromAll(this)
  }

  def onEvent(event: EditorEvent) = event match {
    case EditSettings() => openSettingsDialog()
    case _              =>
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
