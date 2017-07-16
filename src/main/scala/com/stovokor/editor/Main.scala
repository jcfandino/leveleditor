package com.stovokor.editor

import com.jme3.app.SimpleApplication
import com.jme3.system.AppSettings
import com.simsilica.lemur.GuiGlobals
import com.simsilica.lemur.style.BaseStyles
import com.stovokor.editor.state.GuiState
import com.stovokor.editor.state.GridState
import com.stovokor.editor.state.Camera2DState
import com.simsilica.lemur.event.MouseAppState
import com.stovokor.editor.state.DrawingState
import com.stovokor.editor.input.InputFunctionsMapper
import com.jme3.input.controls.InputListener
import com.stovokor.editor.state.ModeState
import com.stovokor.editor.state.Camera3DState
import com.stovokor.editor.state.SectorPresenterState
import com.stovokor.editor.state.ModifyingState

object Main extends SimpleApplication {

  def main(args: Array[String]) {
    val sets = new AppSettings(true)
    //    sets.setSettingsDialogImage("/Interface/logo.png")
    sets.setWidth(1024)
    sets.setHeight(768)
    sets.setGammaCorrection(false)
    setSettings(sets)
    setDisplayFps(true)
    setDisplayStatView(true)

    Main.start
  }

  def simpleInitApp() = {
    // Init lemur
    GuiGlobals.initialize(this)
    BaseStyles.loadGlassStyle()
    GuiGlobals.getInstance().getStyles().setDefaultStyle("glass")

    // Init input
    InputFunctionsMapper.initialize(GuiGlobals.getInstance.getInputMapper)
    inputManager.removeListener(getInputListener)

    // Init states
    stateManager.attach(new GuiState)
    stateManager.attach(new GridState)
    stateManager.attach(new Camera2DState)
    stateManager.attach(new MouseAppState(this))
    stateManager.attach(new DrawingState)
    stateManager.attach(new ModeState)
    stateManager.attach(new SectorPresenterState)
    stateManager.attach(new ModifyingState)
  }

  def getInputListener = {
    val field = classOf[SimpleApplication].getDeclaredField("actionListener")
    field.setAccessible(true)
    field.get(this).asInstanceOf[InputListener]
  }

}
