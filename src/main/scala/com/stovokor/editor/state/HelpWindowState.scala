package com.stovokor.editor.state

import com.jme3.app.Application
import com.jme3.app.state.AppStateManager
import com.simsilica.lemur.OptionPanelState
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
import com.stovokor.util.ShowHelp

class HelpWindowState extends BaseState
    with EditorEventListener
    with CanMapInput
    with StateFunctionListener {

  val settingsRepository = Repositories.settingsRepository

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    EventBus.subscribe(this, ShowHelp())
    setupInput
  }

  override def cleanup() {
    super.cleanup
    EventBus.removeFromAll(this)
    inputMapper.removeStateListener(this, InputFunction.help)
    inputMapper.removeStateListener(this, InputFunction.cancel)
  }

  def onEvent(event: EditorEvent) = event match {
    case ShowHelp() => openHelpDialog()
    case _          =>
  }

  def setupInput {
    inputMapper.addStateListener(this, InputFunction.help)
    inputMapper.addStateListener(this, InputFunction.cancel)
    inputMapper.activateGroup(InputFunction.general)
  }

  def valueChanged(func: FunctionId, value: InputState, tpf: Double) {
    if (value == InputState.Positive) func match {
      case InputFunction.help   => openHelpDialog()
      case InputFunction.cancel => closeDialog()
      case _                    =>
    }
  }

  def openHelpDialog() {
    println("opening help dialog")
    optionPanelState.close()
    val dialog = GuiFactory.createHelpDialog(cam.getWidth, cam.getHeight, closeDialog)
    optionPanelState.show(dialog)
  }

  def closeDialog() {
    optionPanelState.close()
  }

  def optionPanelState = stateManager.getState(classOf[OptionPanelState])

}
