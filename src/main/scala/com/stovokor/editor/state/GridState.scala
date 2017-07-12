package com.stovokor.editor.state

import com.jme3.app.Application
import com.jme3.app.SimpleApplication
import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import com.jme3.asset.AssetManager
import com.jme3.scene.Node
import com.jme3.scene.Geometry
import com.jme3.scene.shape.Box
import com.jme3.math.ColorRGBA
import com.jme3.material.Material
import com.jme3.scene.Spatial
import com.jme3.scene.debug.Arrow
import com.jme3.math.Vector3f
import com.jme3.scene.shape.Line
import com.jme3.scene.BatchNode
import com.jme3.scene.Spatial.CullHint
import com.simsilica.lemur.event.CursorEventControl
import com.simsilica.lemur.event.MouseEventControl
import com.simsilica.lemur.event.DefaultMouseListener
import com.jme3.input.event.MouseButtonEvent
import com.simsilica.lemur.event.DefaultCursorListener
import com.simsilica.lemur.event.CursorButtonEvent
import com.simsilica.lemur.event.CursorMotionEvent
import com.jme3.scene.shape.Quad
import com.stovokor.util.EventBus
import com.stovokor.util.GridClick

class GridState extends BaseState with MaterialFactory {

  val spanX = 1000f
  val spanY = 1000f

  val stepX = 1f
  val stepY = 1f

  val zPos = -1000f

  var batched = true

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)

    val node = new Node("gridParent")
    node.attachChild(createOrigin())
    node.attachChild(createAxis())
    node.attachChild(createGrid())
    node.attachChild(createPickPlane())
    node.setCullHint(CullHint.Never)
    rootNode.attachChild(node)
    registerMouseEvents(node.getChild("pickPlane"))
  }

  var clicked = false
  var mousePos: Vector3f = new Vector3f

  def registerMouseEvents(spatial: Spatial) {
    CursorEventControl.addListenersToSpatial(spatial, new DefaultCursorListener() {
      override def click(event: CursorButtonEvent, target: Spatial, capture: Spatial) {
        clicked = event.getButtonIndex == 0 && event.isPressed()
        EventBus.trigger(GridClick(mousePos.x, mousePos.y))
        println(s"click -> $mousePos")
      }
    })
    CursorEventControl.addListenersToSpatial(spatial, new DefaultCursorListener() {
      override def cursorMoved(event: CursorMotionEvent, target: Spatial, capture: Spatial) {
        //        println(s"moved - ${event.getCollision}")
        val col = event.getCollision
        mousePos.set(col.getContactPoint)
      }
    })
  }

  def createOrigin(): Spatial = {
    val origin = new Geometry("origin", new Box(0.05f, 0.05f, 0.05f))
    origin.setMaterial(plainColor(ColorRGBA.Orange))
    origin
  }

  def createAxis(): Spatial = {
    val arrowX = new Geometry("arrowX", new Arrow(Vector3f.UNIT_X))
    arrowX.setMaterial(plainColor(ColorRGBA.Red))

    val arrowY = new Geometry("arrowY", new Arrow(Vector3f.UNIT_Y))
    arrowY.setMaterial(plainColor(ColorRGBA.Green))

    val node = new Node("axis")
    node.attachChild(arrowX)
    node.attachChild(arrowY)
    node
  }

  def createGrid(): Spatial = {
    val grid = new Node("grid")
    val mat = plainColor(ColorRGBA.DarkGray)
    for (x <- (-spanX to spanX by stepX)) {
      val line = new Geometry("line",
        new Line(new Vector3f(x, -spanY, 0f), new Vector3f(x, spanY, 0f)))
      line.setMaterial(mat)
      grid.attachChild(line)
    }
    for (y <- (-spanY to spanY by stepY)) {
      val line = new Geometry("line",
        new Line(new Vector3f(-spanX, y, 0f), new Vector3f(spanX, y, 0f)))
      line.setMaterial(mat)
      grid.attachChild(line)
    }
    grid
  }

  def createPickPlane(): Spatial = {
    val plane = new Geometry("pickPlane", new Quad(2 * spanX, 2 * spanY))
    plane.setMaterial(plainColor(ColorRGBA.Blue))
    plane.setCullHint(CullHint.Always)
    plane.move(-spanX, -spanY, 1f)
    plane
  }

  override def update(tpf: Float) {
    //    if (!batched) {
    //      rootNode.getChild("gridParent").asInstanceOf[BatchNode].batch()
    //      batched = true
    //    }
  }
}