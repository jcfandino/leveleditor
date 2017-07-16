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
import com.stovokor.util.ModeSwitch
import com.stovokor.util.PolygonDrawn

class DrawingState extends BaseState
    with EditorEventListener
    with MaterialFactory
    with CanMapInput
    with AnalogFunctionListener
    with StateFunctionListener {

  val minDistance = .1f

  var currentBuilder: Option[PolygonBuilder] = None
  var lastClick = 0l

  val polygonRepository = new PolygonRepository

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    get2DNode.attachChild(new Node("currentDraw"))
    EventBus.subscribeByType(this, classOf[GridClick])
    EventBus.subscribe(this, ModeSwitch())
    setupInput
  }

  def setupInput {
    inputMapper.addStateListener(this, InputFunction.cancel)
    inputMapper.activateGroup(InputFunction.general)
  }

  def onEvent(event: EditorEvent) {
    event match {
      case GridClick(x, y) => if (isEnabled) addPoint(x, y)
      case ModeSwitch()    => cancelPolygon
      case _               =>
    }
  }

  def addPoint(x: Float, y: Float) {
    val isDoubleClick = System.currentTimeMillis - lastClick < 200
    lastClick = System.currentTimeMillis
    currentBuilder = currentBuilder match {
      case None => Some(PolygonBuilder.start(Point(x, y)))
      case Some(builder) => {
        val point = Point(x, y)
        if (point.distance(builder.first) < minDistance || isDoubleClick) {
          if (builder.size > 2) {
            println(s"polygon completed ${builder.size}")
            val polygon = builder.build()
            polygonRepository.add(polygon)
            //            drawPolygon(polygon)
            EventBus.trigger(PolygonDrawn(polygon))
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
      case _                    =>
    }
  }

  def cancelPolygon {
    currentBuilder = None
    redrawCurrent
  }

}