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

  def animSpeed = 0.02f * animZoom
  var animZoom = zoom + 0.1f

  override def update(tpf: Float) {
    if (animZoom != zoom) {
      animZoom += (if (animZoom > zoom) -animSpeed else animSpeed)
      if ((animZoom - zoom).abs < animSpeed) animZoom = zoom
      val aspect = cam.getWidth() / cam.getHeight()
      cam.setFrustum(0f, 10000f,
        -animZoom * aspect, animZoom * aspect,
        animZoom, -animZoom)
      cam.update()
    }
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
  }

  def setupInput() = {
    inputMapper.addAnalogListener(this,
      InputFunction.moveX,
      InputFunction.moveY,
      InputFunction.moveZ,
      InputFunction.mouseWheel)
    inputMapper.activateGroup(InputFunction.camera);
    inputMapper.activateGroup(InputFunction.mouse);
  }

  def valueActive(func: FunctionId, value: Double, tpf: Double) {
    def boundSpeed = Math.max(.1f, Math.min(50f, zoom * speed))
    func match {
      case InputFunction.moveX      => move(value * speed * tpf, 0, 0)
      case InputFunction.moveY      => move(0, value * speed * tpf, 0)
      case InputFunction.moveZ      => moveZoom(value * speed * tpf)
      case InputFunction.mouseWheel => moveZoom(-1 * value * boundSpeed * tpf)
      case _                        =>
    }
  }

  def valueChanged(func: FunctionId, value: InputState, tpf: Double) {

  }

}