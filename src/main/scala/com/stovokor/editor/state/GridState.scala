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
import com.stovokor.editor.factory.MaterialFactoryClient
import com.stovokor.editor.gui.K
import com.stovokor.editor.gui.Palette
import com.stovokor.editor.input.InputFunction
import com.stovokor.editor.model.Point
import com.stovokor.util.EventBus
import com.stovokor.util.PointClicked
import com.stovokor.util.LemurExtensions._
import com.jme3.scene.shape.Box
import com.jme3.material.Material
import jme3tools.optimize.GeometryBatchFactory
import com.stovokor.util.JmeExtensions._
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EditorEvent
import com.stovokor.util.ChangeGridSize
import com.stovokor.editor.gui.Mode2DLayers
import com.stovokor.util.GridSnapper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Callable
import com.jme3.app.SimpleApplication

class GridState extends BaseState
    with MaterialFactoryClient
    with CanMapInput
    with StateFunctionListener
    with EditorEventListener {

  val gridSteps = List(0.125f, 0.25f, 0.5f, 1f, 2f, 4f, 8f)
  val (spanX, spanY) = (1000f, 1000f)

  var gridStepIndex = 2
  var clicked = false
  var mousePos = new Vector3f

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)

    val node = new Node("gridParent")
    node.attachChild(createOrigin())
    node.attachChild(createAxis())
    node.attachChild(createPickPlane())
    node.setCullHint(CullHint.Never)
    get2DNode.attachChild(node)
    setupInput(node.getChild("pickPlane"))
    EventBus.subscribe(this, ChangeGridSize())
    GridSnapper.setStep(gridSteps(gridStepIndex))
    new GridLoader(app).load(gridSteps, node)
  }

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
    spatial.onCursorClick((event, target, capture) => {
      if (isEnabled && event.getButtonIndex == 0 && event.isPressed) {
        event.setConsumed()
        clicked = event.isPressed()
        EventBus.trigger(PointClicked(GridSnapper.snap(Point(mousePos.x, mousePos.y))))
        println(s"grid click -> $mousePos")
      }
    })
    spatial.onCursorMove((event: CursorMotionEvent, target, capture) => {
      if (isEnabled) {
        event.setConsumed()
        val col = event.getCollision
        mousePos.set(col.getContactPoint)
      }
    })
    inputMapper.addStateListener(this, InputFunction.snapToGrid)
    inputMapper.addStateListener(this, InputFunction.resizeGrid)
    inputMapper.activateGroup(InputFunction.general)
  }

  def valueChanged(func: FunctionId, value: InputState, tpf: Double) {
    if (isEnabled && value == InputState.Positive) func match {
      case InputFunction.snapToGrid => {
        val enabled = GridSnapper.toggle
        println(s"snap to grid is $enabled")
      }
      case InputFunction.resizeGrid => {
        changeGridSize()
      }
      case _ =>
    }
  }

  def onEvent(event: EditorEvent) = event match {
    case ChangeGridSize() => changeGridSize()
    case _                =>
  }

  def createOrigin(): Spatial = {
    val origin = new Geometry("origin", K.vertexBox)
    origin.setMaterial(plainColor(Palette.origin))
    origin.setLocalTranslation(0f, 0f, Mode2DLayers.origin)
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
    node.setLocalTranslation(0f, 0f, Mode2DLayers.axis)
    node
  }

  def createPickPlane(): Spatial = {
    val plane = new Geometry("pickPlane", new Quad(2 * spanX, 2 * spanY))
    plane.setMaterial(plainColor(ColorRGBA.Black))
    plane.setCullHint(CullHint.Always)
    plane.move(-spanX, -spanY, Mode2DLayers.pickPlane)
    // I set this lower could get unresponsive
    // I think it can be conflicting with geometries in 3d space
    plane
  }

  def changeGridSize() {
    val node = get2DNode.getChild("gridParent").asNode
    setGridVisible(node, gridStepIndex, false)
    gridStepIndex = if (gridStepIndex + 1 < gridSteps.size) gridStepIndex + 1 else 0
    println(s"New grid size $gridStep")
    GridSnapper.setStep(gridStep)
    setGridVisible(node, gridStepIndex, true)
  }

  def setGridVisible(node: Node, idx: Int, visible: Boolean) {
    val grid = node.getChild("grid-" + idx)
    if (grid != null) {
      grid.setCullHint(if (visible) CullHint.Inherit else CullHint.Always)
    }
  }

  def gridStep = gridSteps(gridStepIndex)

  override def update(tpf: Float) {
  }

  class GridLoader(app: SimpleApplication) {
    val executor: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors)

    def load(sizes: List[Float], node: Node) {
      sizes.zipWithIndex
        .sortBy(_._1).reverse
        .map(p => p match {
          case (size, idx) => executor.submit(new Callable[Spatial]() {
            def call() = {
              val grid = createGrid(size, idx)
              app.enqueue(new Callable[Unit]() {
                grid.setCullHint(if (idx == gridStepIndex) CullHint.Inherit else CullHint.Always)
                def call = node.attachChild(grid)
              })
              null
            }
          })
        })
      executor.shutdown()
    }

    def createGrid(size: Float, idx: Int): Spatial = {
      val grid = new Node("grid-" + idx)
      val mat1 = plainColor(Palette.grid1)
      val mat2 = plainColor(Palette.grid2)
      val mat3 = plainColor(Palette.grid3)
      for (x <- (-spanX to spanX by size)) {
        val line = new Geometry("line",
          new Line(new Vector3f(x, -spanY, 0f), new Vector3f(x, spanY, 0f)))
        val mat = if (x == 0f) mat3 else if (x % 1f == 0f) mat1 else mat2
        line.setMaterial(mat)
        grid.attachChild(line)
      }
      for (y <- (-spanY to spanY by size)) {
        val line = new Geometry("line",
          new Line(new Vector3f(-spanX, y, 0f), new Vector3f(spanX, y, 0f)))
        val mat = if (y == 0f) mat3 else if (y % 1f == 0f) mat1 else mat2
        line.setMaterial(mat)
        grid.attachChild(line)
      }
      grid.setLocalTranslation(0f, 0f, Mode2DLayers.grid)
      GeometryBatchFactory.optimize(grid)
    }
  }
}