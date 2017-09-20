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
import com.stovokor.util.PointSelectionChange
import com.stovokor.util.PointerTargetChange
import com.jme3.math.FastMath

class HighlightControl(sectorId: Long, target: String)
    extends AbstractControl
    with EditorEventListener {

  var selected = false
  var initialized = false

  def controlUpdate(tpf: Float) {
    if (!initialized) {
      initialized = false
      EventBus.subscribeByType(this, classOf[PointerTargetChange])
    }
    doIfGeometry(geo => {
      geo.getMaterial.setColor("Color", getColor(selected))
    })
    t += tpf
  }

  def setSelected(s: Boolean) {
    selected = s
  }
  var t = 0f
  def getColor(select: Boolean) = if (select) {
    val color = Palette.hoveredSurfaceMin.clone
    color.interpolateLocal(Palette.hoveredSurfaceMax, .5f * (1f + FastMath.sin(5 * t)))
  } else ColorRGBA.White

  def doIfGeometry(action: Geometry => Unit) {
    if (spatial.isInstanceOf[Geometry]) {
      action.apply(spatial.asInstanceOf[Geometry])
    }
  }

  def controlRender(rm: RenderManager, vp: ViewPort) {
  }

  def onEvent(event: EditorEvent) = event match {
    case PointerTargetChange(this.sectorId, this.target) => setSelected(true)
    case PointerTargetChange(_, _) => setSelected(false)
    case _ =>
  }
}