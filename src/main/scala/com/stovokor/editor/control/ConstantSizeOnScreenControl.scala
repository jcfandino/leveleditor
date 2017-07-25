package com.stovokor.editor.control

import com.jme3.renderer.RenderManager
import com.jme3.renderer.ViewPort
import com.jme3.scene.control.AbstractControl

class ConstantSizeOnScreenControl(val ratio: Float = 10f) extends AbstractControl {

  var zoom = 1f

  def controlUpdate(tpf: Float) {
    spatial.setLocalScale(zoom / ratio)
  }

  def controlRender(rm: RenderManager, vp: ViewPort) {
    zoom = vp.getCamera.getFrustumTop
  }

}