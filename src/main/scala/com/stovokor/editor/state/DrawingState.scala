package com.stovokor.editor.state

import com.stovokor.editor.model.PolygonBuilder
import com.jme3.app.state.AppStateManager
import com.jme3.app.Application
import com.stovokor.util.EventBus
import com.stovokor.util.GridClick
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EditorEvent
import com.stovokor.editor.model.Point
import com.stovokor.editor.model.repository.PolygonRepository
import com.stovokor.editor.model.Polygon
import com.jme3.math.ColorRGBA
import com.jme3.material.Material
import com.jme3.scene.Node
import com.jme3.scene.Geometry
import com.jme3.scene.shape.Box
import com.jme3.scene.shape.Line
import com.jme3.math.Vector3f
import com.simsilica.lemur.input.StateFunctionListener
import com.simsilica.lemur.input.AnalogFunctionListener
import com.stovokor.editor.input.InputFunction
import com.simsilica.lemur.input.FunctionId
import com.simsilica.lemur.input.InputState
import com.stovokor.editor.factory.MaterialFactory
import com.stovokor.editor.factory.MeshFactory
import com.stovokor.editor.model.Sector
import com.stovokor.editor.model.Surface
import com.stovokor.util.ViewModeSwitch
import com.stovokor.util.SectorDrawn
import com.stovokor.util.PointClicked
import com.stovokor.editor.model.SectorBuilder
import com.stovokor.editor.model.repository.SectorRepository

class DrawingState extends BaseState
    with EditorEventListener
    with MaterialFactory
    with CanMapInput
    with AnalogFunctionListener
    with StateFunctionListener {

  val minDistance = .1f

  var currentBuilder: Option[SectorBuilder] = None
  var lastClick = 0l

  val sectorRepository = SectorRepository()

  //  val polygonRepository = new PolygonRepository

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    get2DNode.attachChild(new Node("currentDraw"))
    EventBus.subscribeByType(this, classOf[GridClick])
    EventBus.subscribeByType(this, classOf[PointClicked])
    EventBus.subscribe(this, ViewModeSwitch())
    setupInput
  }

  override def cleanup() {
    super.cleanup
    EventBus.removeFromAll(this)
    inputMapper.removeStateListener(this, InputFunction.cancel)
    inputMapper.removeStateListener(this, InputFunction.test1)
  }

  def setupInput {
    inputMapper.addStateListener(this, InputFunction.cancel)
    inputMapper.addStateListener(this, InputFunction.test1)
    inputMapper.activateGroup(InputFunction.general)
  }

  def onEvent(event: EditorEvent) {
    event match {
      case GridClick(x, y) => if (isEnabled) addPoint(x, y)
      case PointClicked(sectorId, Point(x, y)) => if (isEnabled) {
        addPoint(x, y)
        currentBuilder = currentBuilder.map(_.add(sectorId))
      }
      case ViewModeSwitch() => cancelPolygon
      case _                =>
    }
  }

  def addPoint(x: Float, y: Float) {
    val isDoubleClick = System.currentTimeMillis - lastClick < 200
    lastClick = System.currentTimeMillis
    currentBuilder = currentBuilder match {
      case None => Some(SectorBuilder.start(Point(x, y)))
      case Some(builder) => {
        val point = Point(x, y)
        if (point.distance(builder.first) < minDistance || isDoubleClick) {
          if (builder.size > 2) {
            println(s"polygon completed ${builder.size}")
            val sectorId = builder.build(sectorRepository)
            EventBus.trigger(SectorDrawn(sectorId))
            None
          } else {
            println(s"ignored, cannot finish yet")
            Some(builder)
          }
        } else if (point.distance(builder.last) > minDistance) {
          println(s"adding point ($x,$y) size: ${builder.size}")
          Some(builder.add(Point(x, y)))
        } else {
          println(s"ignored, too close to last")
          Some(builder)
        }
      }
    }
    redrawCurrent
  }

  def redrawCurrent {
    val points = currentBuilder.map(_.points).orElse(Some(List())).get
    val lines = currentBuilder.map(_.lines).orElse(Some(List())).get
    getDrawNode.removeFromParent
    val node = new Node("currentDraw")
    for (point <- points) {
      val vertex = new Geometry("point", new Box(0.05f, 0.05f, 0.05f))
      vertex.setLocalTranslation(point.x, point.y, 0f)
      vertex.setMaterial(plainColor(ColorRGBA.Green))
      node.attachChild(vertex)
    }
    for (line <- lines) {
      val geo = new Geometry("line", new Line(
        new Vector3f(line.a.x, line.a.y, 0f), new Vector3f(line.b.x, line.b.y, 0f)))
      geo.setMaterial(plainColor(ColorRGBA.Green))
      node.attachChild(geo)
    }
    get2DNode.attachChild(node)
  }

  def getDrawNode = get2DNode.getChild("currentDraw")

  def isDrawing = currentBuilder.isDefined

  def valueActive(func: FunctionId, value: Double, tpf: Double) {
  }

  def valueChanged(func: FunctionId, value: InputState, tpf: Double) {
    func match {
      case InputFunction.cancel => cancelPolygon
      case InputFunction.test1  => drawTestRoom
      case _                    =>
    }
  }

  def cancelPolygon {
    currentBuilder = None
    redrawCurrent
  }

  def drawTestRoom {
    println("drawing test room")
    val sector = SectorBuilder
      .start(Point(-10, -10))
      .add(Point(10, -10))
      .add(Point(10, 10))
      .add(Point(-10, 10))
      .build()
    val sectorId = sectorRepository.add(sector)
    EventBus.trigger(SectorDrawn(sectorId))
    inputMapper.removeStateListener(this, InputFunction.test1)
  }

}