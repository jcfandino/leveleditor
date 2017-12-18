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
import com.stovokor.editor.factory.MaterialFactoryClient
import com.stovokor.editor.gui.K
import com.stovokor.editor.gui.Palette
import com.stovokor.editor.input.InputFunction
import com.stovokor.editor.model.Point
import com.stovokor.editor.builder.SectorBuilder
import com.stovokor.editor.model.repository.SectorRepository
import com.stovokor.util.EditorEvent
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EventBus
import com.stovokor.util.PointClicked
import com.stovokor.util.SectorUpdated
import com.stovokor.util.ViewModeSwitch
import com.stovokor.editor.model.repository.BorderRepository
import com.stovokor.editor.model.Sector
import com.stovokor.editor.factory.BorderFactory
import com.stovokor.util.SectorDeleted
import com.stovokor.editor.factory.SectorFactory
import com.stovokor.util.EditModeSwitch
import com.stovokor.editor.input.Modes.EditMode
import com.stovokor.util.JmeExtensions._

class DrawingState extends BaseState
    with EditorEventListener
    with MaterialFactoryClient
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
    EventBus.subscribeByType(this, classOf[EditModeSwitch])
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

  def onEvent(event: EditorEvent) = event match {
    case PointClicked(point) => if (isEnabled) {
      addPoint(point.x, point.y)
      for ((sectorId, sector) <- sectorRepository.findByPoint(point)) {
        currentBuilder = currentBuilder.map(_.add(sectorId))
        checkCuttingSector(sectorId, sector)
      }
    }
    case ViewModeSwitch()  => cancelPolygon
    case EditModeSwitch(m) => if (m != EditMode.Draw) cancelPolygon
    case _                 =>
  }

  def addPoint(x: Float, y: Float) {
    val isDoubleClick = System.currentTimeMillis - lastClick < 200 &&
      currentBuilder.map(b => b.last.distance(Point(x, y)) < 0.1f).orElse(Some(false)).get
    lastClick = System.currentTimeMillis
    currentBuilder = currentBuilder match {
      case None => Some(SectorBuilder.start(Point(x, y)))
      case Some(builder) => {
        val point = Point(x, y)
        if (point.distance(builder.first) < minDistance || isDoubleClick) {
          if (builder.size > 2) {
            println(s"polygon completed ${builder.size}")
            builder.build(sectorRepository, borderRepository)
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
    val node = getDrawNode
    node.detachAllChildren
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

  def getDrawNode = getOrCreateNode(get2DNode, "currentDraw")

  def isDrawing = currentBuilder.isDefined

  def valueActive(func: FunctionId, value: Double, tpf: Double) {
  }

  def valueChanged(func: FunctionId, value: InputState, tpf: Double) {
    if (value == InputState.Positive) func match {
      case InputFunction.cancel => cancelPolygon
      case InputFunction.test1  => drawTestRoom(0f, 0f)
      case InputFunction.test2  => drawTestRoom(0f, 10f)
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
    // This is the simplified version reusing the SectorFactory.
    // It redoes the sectors without keeping the properties, need to implement that.
    // Requires a lot of cleanup and bug fixing.
    // TODO recover sector and border properties from previous sector.
    currentBuilder.foreach(builder => {
      if (builder.isCuttingSector(sectorId, sectorRepository)) {
        // Remove old sector
        sectorRepository.remove(sectorId)
        for ((borderId, border) <- borderRepository.findFrom(sectorId)) {
          borderRepository.remove(borderId)
        }
        for ((borderId, border) <- borderRepository.findTo(sectorId)) {
          borderRepository.remove(borderId)
        }
        EventBus.trigger(SectorDeleted(sectorId))
        // Create new sectors
        val polygons = sector.polygon.divideBy(builder.points)
        polygons.foreach(polygon => {
          val holes = sector.holes.filter(polygon.contains)
          SectorFactory.create(SectorRepository(), BorderRepository(), polygon, holes)
        })
        cancelPolygon
      }
    })
  }

  // TODO Remove this method, it's keept for future reference.
  def checkCuttingSectorOld(sectorId: Long, sector: Sector) = {
    currentBuilder.foreach(builder => {
      if (builder.isCuttingSector(sectorId, sectorRepository)) {
        val newSectors = sector.divideBy(builder.points)
        println(s"~~ Sector divided ~~")
        println(s"Old sector ${sector.polygon}")
        for (sector <- newSectors) { println(s"- New sector ${sector}") }
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
            println(s"Changed Border to: $replacement from sector: $from")
          }
        }
        for ((borderId, border) <- borderRepository.findTo(sectorId)) {
          //          println(s"''''found to ${border}")
          //          println(s"''''secsByLine ${secsByLine.get(border.line)}")
          //          println(s"''''secsByLine inverted? ${secsByLine.get(border.line.reverse)}")
          borderRepository.remove(borderId)
          for (
            (toId, to) <- secsByLine.get(border.line)
              .orElse(secsByLine.get(border.line.reverse))
          ) {
            val from = sectorRepository.get(border.sectorA)
            val replacement = BorderFactory
              .createBorder(border.sectorA, from, toId, to, border.line)
            borderRepository.add(replacement)
            println(s"Changed Border from: $replacement to sector: $to")
            EventBus.trigger(SectorUpdated(border.sectorA, from, true))
          }
        }
        // generate new ones
        newIdAndSec.sliding(2).foreach(pair => pair match {
          case List((newIdA, newSecA), (newIdB, newSecB)) => {
            val lines = newSecA.polygon.sharedLines(newSecB.polygon) //builder.lines //TODO may not be these
            val newBorders = BorderFactory.createBorders(newIdA, newSecA, newIdB, newSecB, lines)
            println(s"New borders: $newBorders")
            newBorders.map(borderRepository.add)
          }
        })
        //TODO I'm forgetting to find borders with pre-existing sectors!!

        // ...
        // clean builder
        cancelPolygon
        // Trigger events to redraw sectors
        EventBus.trigger(SectorDeleted(sectorId))
        for ((id, sec) <- newIdAndSec) EventBus.trigger(SectorUpdated(id, sec, true))
      }
    })
  }

}