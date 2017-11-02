package com.stovokor.editor.state

import com.jme3.app.Application
import com.jme3.app.state.AppStateManager
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.Spatial
import com.jme3.scene.shape.Box
import com.jme3.scene.shape.Line
import com.simsilica.lemur.input.FunctionId
import com.simsilica.lemur.input.InputState
import com.simsilica.lemur.input.StateFunctionListener
import com.stovokor.editor.control.SelectableControl
import com.stovokor.editor.factory.MaterialFactoryClient
import com.stovokor.editor.factory.MeshFactory
import com.stovokor.editor.input.InputFunction
import com.stovokor.editor.model.Point
import com.stovokor.editor.model.Polygon
import com.stovokor.editor.model.Sector
import com.stovokor.editor.model.repository.SectorRepository
import com.stovokor.util.EditorEvent
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EventBus
import com.stovokor.util.LemurExtensions._
import com.stovokor.util.JmeExtensions._
import scala.collection.JavaConverters._
import com.stovokor.util.PointClicked
import com.stovokor.util.PointDragged
import com.stovokor.util.SectorUpdated
import com.stovokor.editor.factory.MeshFactory
import com.jme3.scene.Node
import com.stovokor.editor.factory.MeshFactory
import com.stovokor.util.PointerTargetChange
import com.stovokor.editor.control.ConstantSizeOnScreenControl
import com.jme3.scene.Spatial.CullHint
import com.stovokor.editor.gui.K
import com.stovokor.editor.model.repository.BorderRepository
import com.stovokor.util.SectorDeleted
import com.stovokor.editor.gui.Mode2DLayers
import com.jme3.math.Vector2f
import com.stovokor.util.GridSnapper

// this state works in 2d and 3d
class SectorPresenterState extends BaseState
    with MaterialFactoryClient
    with EditorEventListener
    with CanMapInput
    with StateFunctionListener {

  val sectorRepository = SectorRepository()
  val borderRepository = BorderRepository()

  // TODO make this async, check PathFindScheduler

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    EventBus.subscribeByType(this, classOf[SectorUpdated])
    EventBus.subscribeByType(this, classOf[SectorDeleted])
  }

  override def update(tpf: Float) {
  }

  def onEvent(event: EditorEvent) = event match {
    case SectorUpdated(id, sector) => redrawSector(id, sector)
    case SectorDeleted(id)         => eraseSector(id)
    case _                         =>
  }

  def eraseSector(id: Long) {
    getOrCreateNode(get2DNode, "sector-" + id).removeFromParent
    getOrCreateNode(get3DNode, "sector-" + id).removeFromParent
  }
  def redrawSector(id: Long, sector: Sector) {
    eraseSector(id)
    drawSector(id, sector)
  }

  def drawSector(id: Long, sector: Sector) {
    def draw2d() {
      val node = getOrCreateNode(get2DNode, "sector-" + id)
      for (point <- sector.polygon.pointsSorted) {
        val vertex = new Node("point")
        // visual point
        val pointView = new Geometry("point", K.vertexBox)
        pointView.setMaterial(plainColor(ColorRGBA.LightGray))
        pointView.addControl(new SelectableControl(ColorRGBA.LightGray, Set(point)))
        // clickable point
        val clickableRadius = 0.2f
        val clickableVertex = new Geometry("clickablePoint", new Box(clickableRadius, clickableRadius, clickableRadius))
        clickableVertex.setMaterial(plainColor(ColorRGBA.DarkGray))
        clickableVertex.setCullHint(CullHint.Always)

        vertex.attachChild(pointView)
        vertex.attachChild(clickableVertex)
        vertex.setLocalTranslation(point.x, point.y, Mode2DLayers.vertices)
        vertex.addControl(new ConstantSizeOnScreenControl())
        setupDraggableInput(vertex, point)
        node.attachChild(vertex)
      }
      def draw2dLine(node: Node, color: ColorRGBA, line: com.stovokor.editor.model.Line) {
        val geo = new Geometry("line", new Line(
          new Vector3f(line.a.x, line.a.y, Mode2DLayers.lines),
          new Vector3f(line.b.x, line.b.y, Mode2DLayers.lines)))
        geo.setMaterial(plainColor(color))
        geo.addControl(new SelectableControl(color, Set(line.a, line.b)))
        node.attachChild(geo)
      }
      sector.openWalls.foreach(w => draw2dLine(node, ColorRGBA.Brown.mult(2), w.line))
      sector.closedWalls.foreach(w => draw2dLine(node, ColorRGBA.LightGray, w.line))
    }
    def draw3d() {
      val node = getOrCreateNode(get3DNode, "sector-" + id)
      val borders = borderRepository.findFrom(id).map(_._2)
      val meshNode = MeshFactory(assetManager).createMesh(id, sector, borders)
      setup3dInput(id, meshNode)
      node.attachChild(meshNode)
    }
    draw2d()
    draw3d()
  }

  var isDragging = false
  var oldPos = new Vector2f()
  var newPos = new Vector2f()

  def setupDraggableInput(spatial: Spatial, point: Point) {
    spatial.onCursorClick((event, target, capture) => {
      if (event.getButtonIndex == 0 && spatial.isVisible) {
        println(s"CursorClick -> isDragging:$isDragging event: $event")
        if (!isDragging) {
          oldPos.set(spatial.getLocalTranslation.to2f)
          newPos.set(GridSnapper.snapX(spatial.getLocalTranslation.x),
            GridSnapper.snapY(spatial.getLocalTranslation.y))
        } else if (!event.isPressed()) {
          // released
          println(s"oldpos $oldPos vs newpos $newPos = ${oldPos.distance(newPos)}")
          if (oldPos.distance(newPos) > .1f) { // button released
            println(s"moved point ${spatial.getLocalTranslation} -> ${newPos}")
            EventBus.trigger(
              PointDragged(point, GridSnapper.snap(Point(newPos.x, newPos.y))))
            spatial.setLocalTranslation(oldPos.to3f(Mode2DLayers.vertices)) //move back.
            //TODO improve, disable dragging if not in selection
          } else {
            println(s"point clicked over point $point")
            EventBus.trigger(PointClicked(point))
          }
        }
        isDragging = event.isPressed()
      }
    })
    spatial.onCursorMove((event, target, capture) => {
      if (isDragging && spatial.isVisible) {
        val cam = event.getViewPort.getCamera
        val coord = cam.getWorldCoordinates(event.getLocation, 0f)
        newPos.set(GridSnapper.snapX(coord.x), GridSnapper.snapY(coord.y))
        spatial.setLocalTranslation(newPos.to3f(Mode2DLayers.vertices))
      }
    })
    inputMapper.addStateListener(this, InputFunction.snapToGrid)
    inputMapper.activateGroup(InputFunction.general)
  }

  var lastTarget: Option[(Long, String)] = None

  def setup3dInput(sectorId: Long, meshNode: Node) {
    meshNode.getChild("floor").onCursorMove((event, spatial, target) => {
      event.setConsumed()
      if (spatial.isVisible) updateTarget(sectorId, "floor")
    })
    meshNode.getChild("ceiling").onCursorMove((event, spatial, target) => {
      event.setConsumed()
      if (spatial.isVisible) updateTarget(sectorId, "ceiling")
    })
    meshNode.getChildren.asScala
      .filter(_.getName.startsWith("wall"))
      .foreach(_.onCursorMove((event, spatial, target) => {
        event.setConsumed()
        if (spatial.isVisible) updateTarget(sectorId, spatial.getName)
      }))
    meshNode.getChildren.asScala
      .filter(_.getName.startsWith("border"))
      .foreach(_.onCursorMove((event, spatial, target) => {
        event.setConsumed()
        if (spatial.isVisible) updateTarget(sectorId, spatial.getName)
      }))
  }

  def updateTarget(sectorId: Long, target: String) {
    if (lastTarget != Some((sectorId, target))) {
      lastTarget = Some((sectorId, target))
      EventBus.trigger(PointerTargetChange(sectorId, target))
    }
  }

  def valueChanged(f: FunctionId, i: InputState, d: Double) {
  }
}