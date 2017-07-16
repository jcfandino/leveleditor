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

class Camera2DState extends BaseState
    with CanMapInput
    with AnalogFunctionListener
    with StateFunctionListener {

  def cam = app.getCamera

  var zoom = 10f
  val speed = 10f

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    app.getFlyByCamera.setEnabled(false)
    cam.setParallelProjection(true)
    cam.setLocation(new Vector3f(0f, 0f, 100f))
    cam.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Z.negate)
    moveZoom(0f)
    cam.update
    setupInput
  }

  override def cleanup() {
    inputMapper.removeAnalogListener(this,
      InputFunction.moveX,
      InputFunction.moveY,
      InputFunction.moveZ)
  }

  override def update(tpf: Float) {
  }

  def move(x: Double, y: Double, z: Double) {
    cam.setLocation(cam.getLocation.add(x.toFloat, y.toFloat, z.toFloat))
  }
  def move(x: Float, y: Float, z: Float) {
    cam.setLocation(cam.getLocation.add(x, y, z))
  }

  def moveZoom(delta: Double) {
    moveZoom(delta.toFloat)
  }

  def moveZoom(delta: Float) {
    zoom = Math.max(1f, zoom + delta)
    val aspect = cam.getWidth() / cam.getHeight()
    cam.setFrustum(0f, 10000,
      -zoom * aspect, zoom * aspect,
      zoom, -zoom)
    cam.update()
  }

  def setupInput() = {
    inputMapper.addAnalogListener(this,
      InputFunction.moveX,
      InputFunction.moveY,
      InputFunction.moveZ)
    inputMapper.activateGroup(InputFunction.camera);
  }

  def valueActive(func: FunctionId, value: Double, tpf: Double) {
    func match {
      case InputFunction.moveX => move(value * speed * tpf, 0, 0)
      case InputFunction.moveY => move(0, value * speed * tpf, 0)
      case InputFunction.moveZ => moveZoom(value * speed * tpf)
      case _                   =>
    }
  }

  def valueChanged(func: FunctionId, value: InputState, tpf: Double) {

  }

}