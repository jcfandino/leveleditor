package com.stovokor.editor.state

import com.jme3.app.Application
import com.jme3.app.state.AppStateManager
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import com.jme3.scene.Spatial.CullHint
import com.jme3.scene.debug.Arrow
import com.jme3.scene.shape.Line
import com.jme3.scene.shape.Quad
import com.simsilica.lemur.event.CursorButtonEvent
import com.simsilica.lemur.event.CursorEventControl
import com.simsilica.lemur.event.CursorMotionEvent
import com.simsilica.lemur.event.DefaultCursorListener
import com.simsilica.lemur.input.FunctionId
import com.simsilica.lemur.input.InputState
import com.simsilica.lemur.input.StateFunctionListener
import com.stovokor.editor.control.ConstantSizeOnScreenControl
import com.stovokor.editor.factory.MaterialFactory
import com.stovokor.editor.gui.K
import com.stovokor.editor.gui.Palette
import com.stovokor.editor.input.InputFunction
import com.stovokor.editor.model.Point
import com.stovokor.util.EventBus
import com.stovokor.util.PointClicked

class GridState extends BaseState
    with MaterialFactory
    with CanMapInput
    with StateFunctionListener {

  val spanX = 1000f
  val spanY = 1000f

  val stepX = 1f
  val stepY = 1f

  val zPos = -1000f

  var batched = true
  var snapToGrid = true

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)

    val node = new Node("gridParent")
    node.attachChild(createOrigin())
    node.attachChild(createAxis())
    node.attachChild(createGrid())
    node.attachChild(createPickPlane())
    node.setCullHint(CullHint.Never)
    get2DNode.attachChild(node)
    setupInput(node.getChild("pickPlane"))
  }

  var clicked = false
  var mousePos: Vector3f = new Vector3f

  override def setEnabled(enabled: Boolean) {
    super.setEnabled(enabled)
    println(s"set enabled $enabled")
    if (enabled) {
      inputMapper.activateGroup(InputFunction.general)
    } else {
      inputMapper.deactivateGroup(InputFunction.general)
    }
  }

  def setupInput(spatial: Spatial) {
    CursorEventControl.addListenersToSpatial(spatial, new DefaultCursorListener() {
      override def click(event: CursorButtonEvent, target: Spatial, capture: Spatial) {
        if (isEnabled && event.getButtonIndex == 0) {
          clicked = event.isPressed()
          EventBus.trigger(PointClicked(Point(snapX(mousePos.x), snapY(mousePos.y))))
          println(s"grid click -> $mousePos")
        }
      }
    })
    CursorEventControl.addListenersToSpatial(spatial, new DefaultCursorListener() {
      override def cursorMoved(event: CursorMotionEvent, target: Spatial, capture: Spatial) {
        if (isEnabled) {
          val col = event.getCollision
          mousePos.set(col.getContactPoint)
        }
      }
    })
    inputMapper.addStateListener(this, InputFunction.snapToGrid)
    inputMapper.activateGroup(InputFunction.general)
  }

  def valueChanged(func: FunctionId, value: InputState, tpf: Double) {
    if (isEnabled && value == InputState.Positive) func match {
      case InputFunction.snapToGrid => {
        snapToGrid = !snapToGrid
        println(s"snap to grid is $snapToGrid")
      }
      case _ =>
    }
  }
  def createOrigin(): Spatial = {
    val origin = new Geometry("origin", K.vertexBox)
    origin.setMaterial(plainColor(Palette.origin))
    origin.setLocalTranslation(0f, 0f, -10f)
    origin.addControl(new ConstantSizeOnScreenControl())
    origin
  }

  def createAxis(): Spatial = {
    val arrowX = new Geometry("arrowX", new Arrow(Vector3f.UNIT_X))
    arrowX.setMaterial(plainColor(Palette.axisX))
    arrowX.addControl(new ConstantSizeOnScreenControl())

    val arrowY = new Geometry("arrowY", new Arrow(Vector3f.UNIT_Y))
    arrowY.setMaterial(plainColor(Palette.axisY))
    arrowY.addControl(new ConstantSizeOnScreenControl())

    val node = new Node("axis")
    node.attachChild(arrowX)
    node.attachChild(arrowY)
    node.setLocalTranslation(0f, 0f, -10f)
    node
  }

  def createGrid(): Spatial = {
    val grid = new Node("grid")
    val mat = plainColor(Palette.grid)
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
    grid.setLocalTranslation(0f, 0f, -20f)
    grid
  }

  def createPickPlane(): Spatial = {
    val plane = new Geometry("pickPlane", new Quad(2 * spanX, 2 * spanY))
    plane.setMaterial(plainColor(ColorRGBA.Black))
    plane.setCullHint(CullHint.Always)
    plane.move(-spanX, -spanY, -1f)
    plane
  }

  override def update(tpf: Float) {
  }

  def snapX(c: Float) = snapped(c, stepX)
  def snapY(c: Float) = snapped(c, stepY)
  def snapped(coor: Float, step: Float) = {
    if (snapToGrid) step * (coor / step).round
    else coor
  }
}