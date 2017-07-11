package com.stovokor.editor.state

import com.jme3.app.state.AppStateManager
import com.jme3.app.Application
import com.jme3.math.Vector3f
import com.jme3.input.controls.ActionListener
import com.jme3.input.KeyInput
import com.jme3.input.controls.KeyTrigger

class CameraState extends BaseState with ActionListener {

  def cam = app.getCamera

  var left, right, up, down, zoomMore, zoomLess: Boolean = false
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
    setupKeys
  }

  override def update(tpf: Float) {
    if (left) move(-speed * tpf, 0f, 0f)
    if (right) move(speed * tpf, 0f, 0f)
    if (up) move(0f, speed * tpf, 0f)
    if (down) move(0f, -speed * tpf, 0f)
    if (zoomMore) moveZoom(speed * tpf)
    if (zoomLess) moveZoom(-speed * tpf)
  }

  def move(x: Float, y: Float, z: Float) {
    cam.setLocation(cam.getLocation.add(x, y, z))
  }

  def moveZoom(delta: Float) {
    zoom = Math.max(1f, zoom - delta)
    val aspect = cam.getWidth() / cam.getHeight()
    cam.setFrustum(0f, 10000,
      -zoom * aspect, zoom * aspect,
      zoom, -zoom)
    cam.update()
  }

  def setupKeys() = {
    app.getInputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A), new KeyTrigger(KeyInput.KEY_LEFT))
    app.getInputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D), new KeyTrigger(KeyInput.KEY_RIGHT))
    app.getInputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W), new KeyTrigger(KeyInput.KEY_UP))
    app.getInputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S), new KeyTrigger(KeyInput.KEY_DOWN))
    app.getInputManager.addMapping("Zoom+", new KeyTrigger(KeyInput.KEY_PGDN), new KeyTrigger(KeyInput.KEY_RBRACKET))
    app.getInputManager.addMapping("Zoom-", new KeyTrigger(KeyInput.KEY_PGUP), new KeyTrigger(KeyInput.KEY_LBRACKET))

    app.getInputManager.addListener(this, "Left")
    app.getInputManager.addListener(this, "Right")
    app.getInputManager.addListener(this, "Up")
    app.getInputManager.addListener(this, "Down")
    app.getInputManager.addListener(this, "Zoom+")
    app.getInputManager.addListener(this, "Zoom-")
  }

  def onAction(binding: String, value: Boolean, tpf: Float) = (binding, value) match {
    case ("Left", _)  => left = value
    case ("Right", _) => right = value
    case ("Up", _)    => up = value
    case ("Down", _)  => down = value
    case ("Zoom+", _) => zoomMore = value
    case ("Zoom-", _) => zoomLess = value
    case _            =>
  }
}