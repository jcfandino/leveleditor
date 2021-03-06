package com.stovokor.editor

import com.jme3.app.SimpleApplication
import com.jme3.input.controls.InputListener
import com.jme3.system.AppSettings
import com.simsilica.lemur.GuiGlobals
import com.simsilica.lemur.event.MouseAppState
import com.simsilica.lemur.style.BaseStyles
import com.stovokor.editor.input.InputFunctionsMapper
import com.stovokor.editor.state.ExportMapState
import com.stovokor.editor.state.GridState
import com.stovokor.editor.state.GuiState
import com.stovokor.editor.state.MaterialSelectionState
import com.stovokor.editor.state.SaveOpenFileState
import com.stovokor.editor.state.SectorPresenterState
import com.stovokor.editor.state.SettingsEditorState
import com.stovokor.editor.state.SettingsLoaderState
import com.stovokor.editor.state.ViewModeState
import com.simsilica.lemur.OptionPanelState
import com.stovokor.editor.state.HelpWindowState
import com.jme3.math.ColorRGBA
import com.stovokor.editor.gui.Palette
import com.jme3.app.DetailedProfilerState
import com.jme3.app.BasicProfilerState

// Level Editor
object Main extends SimpleApplication {

  def main(args: Array[String]) {
    val sets = new AppSettings(true)
    //    sets.setSettingsDialogImage("/Interface/logo.png")
    sets.setWidth(1024)
    sets.setHeight(768)
    sets.setGammaCorrection(false)
    sets.setTitle("M8 Editor")
    setSettings(sets)
    setDisplayFps(true)
    setDisplayStatView(false)

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
    stateManager.attach(new MouseAppState(this))
    stateManager.attach(new ViewModeState)
    stateManager.attach(new SectorPresenterState)
    stateManager.attach(new ExportMapState)
    stateManager.attach(new SettingsLoaderState)
    stateManager.attach(new MaterialSelectionState)
    stateManager.attach(new SaveOpenFileState)
    stateManager.attach(new SettingsEditorState)
    stateManager.attach(new OptionPanelState)
    stateManager.attach(new HelpWindowState)
//        stateManager.attach(new DetailedProfilerState())
//        stateManager.attach(new BasicProfilerState())

    viewPort.setBackgroundColor(Palette.background)
  }

  def getInputListener = {
    val field = classOf[SimpleApplication].getDeclaredField("actionListener")
    field.setAccessible(true)
    field.get(this).asInstanceOf[InputListener]
  }

}
