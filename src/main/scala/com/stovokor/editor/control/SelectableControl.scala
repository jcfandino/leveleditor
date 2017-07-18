package com.stovokor.editor.control

import com.jme3.math.ColorRGBA
import com.jme3.renderer.RenderManager
import com.jme3.renderer.ViewPort
import com.jme3.scene.Geometry
import com.jme3.scene.control.AbstractControl
import com.stovokor.editor.model.Point
import com.stovokor.util.EditorEvent
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EventBus
import com.stovokor.util.PointSelectionChange

class SelectableControl(
  val baseColor: ColorRGBA, sectorId: Long, points: Set[Point])
    extends AbstractControl
    with EditorEventListener {

  val selectedColor = ColorRGBA.Red

  var selected = false
  var initialized = false

  def controlUpdate(tpf: Float) {
    if (!initialized) {
      initialized = false
      EventBus.subscribeByType(this, classOf[PointSelectionChange])
    }
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

  def onEvent(event: EditorEvent) = event match {
    case PointSelectionChange(ps) => {
      val intersection = ps
        .filter(p => p._1 == sectorId && points.contains(p._2))
        .map(_._2)
      //      println(s"This point $points - intersection $intersection")
      setSelected(intersection.size == points.size)
    }
    case _ =>
  }
}