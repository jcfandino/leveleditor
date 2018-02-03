package com.stovokor.editor.state

import com.jme3.app.state.AppStateManager
import com.jme3.app.Application
import com.jme3.math.Vector3f
import com.jme3.input.controls.ActionListener
import com.jme3.input.KeyInput
import com.jme3.input.controls.KeyTrigger
import com.simsilica.lemur.input.AnalogFunctionListener
import com.simsilica.lemur.input.StateFunctionListener
import com.simsilica.lemur.input.FunctionId
import com.simsilica.lemur.input.InputState
import com.stovokor.editor.input.InputFunction
import com.jme3.input.InputManager
import com.jme3.input.FlyByCamera
import com.jme3.input.CameraInput
import com.jme3.input.controls.MouseAxisTrigger
import com.jme3.input.controls.MouseButtonTrigger
import com.jme3.input.MouseInput
import com.jme3.light.AmbientLight
import com.jme3.math.ColorRGBA
import com.jme3.post.filters.FogFilter
import com.jme3.post.ssao.SSAOFilter
import com.jme3.post.FilterPostProcessor
import com.stovokor.util.EditorEventListener
import com.stovokor.util.ToggleEffects
import com.stovokor.util.EditorEvent
import com.stovokor.util.EventBus

class EffectsState extends BaseState
    with CanMapInput
    with AnalogFunctionListener
    with StateFunctionListener
    with EditorEventListener {

  var fpp: FilterPostProcessor = null
  var effectsEnabled = false

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    fpp = new FilterPostProcessor(assetManager)
    fpp.addFilter(new SSAOFilter(4, 1.2f, 0.2f, 0.1f))
    fpp.addFilter(new FogFilter(ColorRGBA.Black, 2f, 100))

    EventBus.subscribe(this, ToggleEffects())
    setupInput
    setEffectsEnabled(true)
  }

  override def cleanup() {
    EventBus.removeFromAll(this)
    setEffectsEnabled(false)
  }

  def onEvent(event: EditorEvent) = event match {
    case ToggleEffects() => setEffectsEnabled(!effectsEnabled)
    case _               =>
  }

  def setEffectsEnabled(value: Boolean) {
    effectsEnabled = value
    if (value) {
      app.getViewPort.addProcessor(fpp)
    } else {
      if (fpp != null) app.getViewPort.removeProcessor(fpp)
    }
  }

  override def update(tpf: Float) {
  }

  def setupInput() = {
  }

  def valueActive(func: FunctionId, value: Double, tpf: Double) {
  }

  def valueChanged(func: FunctionId, value: InputState, tpf: Double) {

  }

}