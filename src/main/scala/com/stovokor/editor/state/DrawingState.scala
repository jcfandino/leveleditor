package com.stovokor.editor.state

import com.jme3.app.Application
import com.jme3.app.state.AppStateManager
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.scene.shape.Line
import com.simsilica.lemur.input.AnalogFunctionListener
import com.simsilica.lemur.input.FunctionId
import com.simsilica.lemur.input.InputState
import com.simsilica.lemur.input.StateFunctionListener
import com.stovokor.editor.control.ConstantSizeOnScreenControl
import com.stovokor.editor.factory.MaterialFactory
import com.stovokor.editor.gui.K
import com.stovokor.editor.gui.Palette
import com.stovokor.editor.input.InputFunction
import com.stovokor.editor.model.Point
import com.stovokor.editor.model.SectorBuilder
import com.stovokor.editor.model.repository.SectorRepository
import com.stovokor.util.EditorEvent
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EventBus
import com.stovokor.util.PointClicked
import com.stovokor.util.SectorUpdated
import com.stovokor.util.ViewModeSwitch
import com.stovokor.editor.model.repository.BorderRepository
import com.stovokor.editor.model.Sector
import com.stovokor.editor.model.creation.BorderFactory
import com.stovokor.util.SectorDeleted

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
  val borderRepository = BorderRepository()

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    get2DNode.attachChild(new Node("currentDraw"))
    EventBus.subscribeByType(this, classOf[PointClicked])
    EventBus.subscribe(this, ViewModeSwitch())
    setupInput
  }

  override def cleanup() {
    super.cleanup
    EventBus.removeFromAll(this)
    inputMapper.removeStateListener(this, InputFunction.cancel)
    inputMapper.removeStateListener(this, InputFunction.test1)
    inputMapper.removeStateListener(this, InputFunction.test2)
  }

  def setupInput {
    inputMapper.addStateListener(this, InputFunction.cancel)
    inputMapper.addStateListener(this, InputFunction.test1)
    inputMapper.addStateListener(this, InputFunction.test2)
    inputMapper.activateGroup(InputFunction.general)
  }

  def onEvent(event: EditorEvent) {
    event match {
      case PointClicked(point) => if (isEnabled) {
        addPoint(point.x, point.y)
        for ((sectorId, sector) <- sectorRepository.find(point)) {
          currentBuilder = currentBuilder.map(_.add(sectorId))
          checkCuttingSector(sectorId, sector())
        }
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
            val (sectorId, sector) = builder.build(sectorRepository, borderRepository)
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
      val vertex = new Geometry("point", K.vertexBox)
      vertex.setLocalTranslation(point.x, point.y, 0f)
      vertex.setMaterial(plainColor(Palette.drawing))
      vertex.addControl(new ConstantSizeOnScreenControl())
      node.attachChild(vertex)
    }
    for (line <- lines) {
      val geo = new Geometry("line", new Line(
        new Vector3f(line.a.x, line.a.y, 0f), new Vector3f(line.b.x, line.b.y, 0f)))
      geo.setMaterial(plainColor(Palette.drawing))
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
      case InputFunction.test1  => if (value == InputState.Positive) drawTestRoom(0f, 0f)
      case InputFunction.test2  => if (value == InputState.Positive) drawTestRoom(0f, 10f)
      case _                    =>
    }
  }

  def cancelPolygon {
    currentBuilder = None
    redrawCurrent
  }

  def drawTestRoom(x: Float, y: Float) {
    println("drawing test room")
    EventBus.trigger(PointClicked(Point(-5, -5).move(x, y)))
    Thread.sleep(200)
    EventBus.trigger(PointClicked(Point(5, -5).move(x, y)))
    Thread.sleep(200)
    EventBus.trigger(PointClicked(Point(5, 5).move(x, y)))
    Thread.sleep(200)
    EventBus.trigger(PointClicked(Point(-5, 5).move(x, y)))
    EventBus.trigger(PointClicked(Point(-5, 5).move(x, y)))
  }

  def checkCuttingSector(sectorId: Long, sector: Sector) = {
    currentBuilder.foreach(builder => {
      if (builder.isCuttingSector(sectorId, sectorRepository)) {
        val newSectors = sector.divideBy(builder.points)
        println(s"~~ Sector divided ~~")
        println(s"Old sector $sector")
        println(s"New sectors $newSectors")
        sectorRepository.remove(sectorId)
        val newIdAndSec = newSectors.map(s => (sectorRepository.add(s), s))
        val secsByLine = newIdAndSec.flatMap(p => p._2.openWalls.map(w => (w.line, p))).toMap
        // replace old borders
        for ((borderId, border) <- borderRepository.findFrom(sectorId)) {
          borderRepository.remove(borderId)
          for ((fromId, from) <- secsByLine.get(border.line)) {
            val other = sectorRepository.get(border.sectorB)
            val replacement = BorderFactory
              .createBorder(fromId, from, border.sectorB, other, border.line)
            borderRepository.add(replacement)
            println(s"Changed Border: $replacement from sector: $from")
          }
        }
        for ((borderId, border) <- borderRepository.findTo(sectorId)) {
          borderRepository.remove(borderId)
          for ((toId, to) <- secsByLine.get(border.line)) {
            val from = sectorRepository.get(border.sectorA)
            val replacement = BorderFactory
              .createBorder(border.sectorA, from, toId, to, border.line)
            borderRepository.add(replacement)
            println(s"Changed Border: $replacement to sector: $to")
            EventBus.trigger(SectorUpdated(border.sectorA, from))
          }
        }
        // generate new ones
        newIdAndSec.sliding(2).foreach(pair => pair match {
          case List((newIdA, newSecA), (newIdB, newSecB)) => {
            val newBorders = BorderFactory.createBorders(newIdA, newSecA, newIdB, newSecB, builder.lines)
            println(s"New borders: $newBorders")
            newBorders.map(borderRepository.add)
          }
        })
        // clean builder
        cancelPolygon
        // Trigger events to redraw sectors
        EventBus.trigger(SectorDeleted(sectorId))
        for ((id, sec) <- newIdAndSec) EventBus.trigger(SectorUpdated(id, sec))
      }
    })
  }

}