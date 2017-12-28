package com.stovokor.editor.control

import com.jme3.math.ColorRGBA
import com.jme3.renderer.RenderManager
import com.jme3.renderer.ViewPort
import com.jme3.scene.Geometry
import com.jme3.scene.control.AbstractControl
import com.stovokor.editor.gui.Palette
import com.stovokor.editor.model.Point
import com.stovokor.util.EditorEvent
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EventBus
import com.stovokor.util.SelectionChange
import com.stovokor.editor.model.SelectionUnit

class SelectableControl(
  val baseColor: ColorRGBA, selectionUnit: SelectionUnit)
    extends AbstractControl {

  var selected = false
  var initialized = false

  def controlUpdate(tpf: Float) {
    if (!initialized) {
      initialized = false
      EventBus.subscribeByType(SelectionUnitsHolder, classOf[SelectionChange])
    }
    setSelected(SelectionUnitsHolder.current contains selectionUnit)
  }

  def setSelected(s: Boolean) {
    if (selected != s) {
      doIfGeometry(geo => {
        geo.getMaterial.setColor("Color", getColor(s))
      })
      selected = s
    }
  }

  def getColor(select: Boolean) = if (select) Palette.selectedElement else baseColor

  def doIfGeometry(action: Geometry => Unit) {
    if (spatial.isInstanceOf[Geometry]) {
      action.apply(spatial.asInstanceOf[Geometry])
    }
  }

  def controlRender(rm: RenderManager, vp: ViewPort) {
  }
}