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

class Camera3DState extends BaseState
    with CanMapInput
    with AnalogFunctionListener
    with StateFunctionListener {

  var zoom = 10f
  val speed = 10f

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    app.getFlyByCamera.setEnabled(true)
    app.getFlyByCamera.setDragToRotate(false)
    app.getFlyByCamera.setMoveSpeed(10f)
    app.getFlyByCamera.setRotationSpeed(2f)
    cam.setParallelProjection(false)
    cam.setLocation(new Vector3f(0f, 1f, 0f))
    cam.lookAt(new Vector3f(0f, 1f, 10f), Vector3f.UNIT_Y)
    cam.setFrustumPerspective(45f, (cam.getWidth / cam.getHeight).toFloat, 1f, 1000f)
    cam.update
    setupInput
  }

  override def cleanup() {
    app.getFlyByCamera.setEnabled(false)
  }

  override def update(tpf: Float) {
  }

  def setupInput() = {
    // rewrite flyCam mappings, ignore mouse, keep keys

    inputManager.setCursorVisible(true)
    inputManager.deleteMapping(CameraInput.FLYCAM_LEFT)
    inputManager.deleteMapping(CameraInput.FLYCAM_RIGHT)
    inputManager.deleteMapping(CameraInput.FLYCAM_UP)
    inputManager.deleteMapping(CameraInput.FLYCAM_DOWN)
    inputManager.deleteMapping(CameraInput.FLYCAM_ZOOMIN)
    inputManager.deleteMapping(CameraInput.FLYCAM_ZOOMOUT)
    inputManager.deleteMapping(CameraInput.FLYCAM_ROTATEDRAG)

    inputManager.addMapping(CameraInput.FLYCAM_LEFT, new KeyTrigger(KeyInput.KEY_LEFT))
    inputManager.addMapping(CameraInput.FLYCAM_RIGHT, new KeyTrigger(KeyInput.KEY_RIGHT))
    inputManager.addMapping(CameraInput.FLYCAM_UP, new KeyTrigger(KeyInput.KEY_UP))
    inputManager.addMapping(CameraInput.FLYCAM_DOWN, new KeyTrigger(KeyInput.KEY_DOWN))

    inputManager.addListener(app.getFlyByCamera,
      CameraInput.FLYCAM_LEFT,
      CameraInput.FLYCAM_RIGHT,
      CameraInput.FLYCAM_UP,
      CameraInput.FLYCAM_DOWN)

  }

  def valueActive(func: FunctionId, value: Double, tpf: Double) {
    func match {
      case InputFunction.moveX =>
      case InputFunction.moveY =>
      case InputFunction.moveZ =>
      case _                   =>
    }
  }

  def valueChanged(func: FunctionId, value: InputState, tpf: Double) {

  }

}