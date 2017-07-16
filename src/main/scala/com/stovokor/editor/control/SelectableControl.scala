package com.stovokor.editor.control

import com.jme3.scene.control.AbstractControl
import com.jme3.renderer.ViewPort
import com.jme3.renderer.RenderManager
import com.jme3.math.ColorRGBA
import com.jme3.scene.Geometry

class SelectableControl(val baseColor: ColorRGBA) extends AbstractControl {

  val selectedColor = ColorRGBA.Red

  var selected = false

  def controlUpdate(tpf: Float) {
  }

  def setSelected(s: Boolean) {
    doIfGeometry(geo => {
      geo.getMaterial.setColor("Color", getColor(s))
    })
    selected = s
  }

  def getColor(select: Boolean) = if (select) selectedColor else baseColor

  def doIfGeometry(action: Geometry => Unit) {
    if (spatial.isInstanceOf[Geometry]) {
      action.apply(spatial.asInstanceOf[Geometry])
    }
  }

  def controlRender(rm: RenderManager, vp: ViewPort) {}
}