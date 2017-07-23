package com.stovokor.editor.state

import com.jme3.app.Application
import com.jme3.app.state.AppStateManager
import com.stovokor.editor.model.Polygon
import com.stovokor.editor.model.Sector
import com.stovokor.editor.model.Surface
import com.stovokor.editor.model.SurfaceTexture
import com.stovokor.editor.model.repository.SectorRepository
import com.stovokor.util.EditorEvent
import com.stovokor.util.EditorEventListener
import com.stovokor.util.SectorDrawn
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.stovokor.editor.factory.MeshFactory
import com.jme3.scene.shape.Box
import com.jme3.scene.Node
import com.jme3.math.ColorRGBA
import com.stovokor.editor.factory.MaterialFactory
import com.jme3.scene.shape.Line
import com.stovokor.util.EventBus
import com.simsilica.lemur.event.DefaultCursorListener
import com.simsilica.lemur.event.CursorEventControl
import com.stovokor.editor.input.InputFunction
import com.jme3.scene.Spatial
import com.simsilica.lemur.event.CursorMotionEvent
import com.stovokor.util.GridClick
import com.simsilica.lemur.event.CursorButtonEvent
import com.simsilica.lemur.input.StateFunctionListener
import com.simsilica.lemur.input.FunctionId
import com.simsilica.lemur.input.InputState
import com.stovokor.editor.model.Point
import com.stovokor.util.PointDragged
import com.stovokor.util.SectorUpdated
import com.stovokor.util.PointClicked
import com.stovokor.editor.control.SelectableControl

// this state works in 2d and 3d
class SectorPresenterState extends BaseState
    with MaterialFactory
    with EditorEventListener
    with CanMapInput
    with StateFunctionListener {

  val sectorRepository = SectorRepository()

  // TODO make this async, check PathFindScheduler

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    EventBus.subscribeByType(this, classOf[SectorDrawn])
    EventBus.subscribeByType(this, classOf[SectorUpdated])
  }

  override def update(tpf: Float) {
  }

  def onEvent(event: EditorEvent) = event match {
    case SectorDrawn(sectorId)     => saveNewSector(sectorRepository.get(sectorId))
    case SectorUpdated(id, sector) => redrawSector(id, sector)
    case _                         =>
  }

  def saveNewSector(sector: Sector) {
    val id = sectorRepository.add(sector)
    drawSector(id, sector)
  }

  def redrawSector(id: Long, sector: Sector) {
    getOrCreateNode(get2DNode, "polygon-" + id).removeFromParent
    getOrCreateNode(get3DNode, "polygon-" + id).removeFromParent
    drawSector(id, sector)
  }

  def drawSector(id: Long, sector: Sector) {
    def draw2d() {
      val node = getOrCreateNode(get2DNode, "polygon-" + id)
      for (point <- sector.polygon.pointsSorted) {
        val vertex = new Geometry("point", new Box(0.05f, 0.05f, 0.05f))
        vertex.setLocalTranslation(point.x, point.y, 0f)
        vertex.setMaterial(plainColor(ColorRGBA.LightGray))
        vertex.setUserData("sectorId", id)
        vertex.addControl(new SelectableControl(ColorRGBA.LightGray, id, Set(point)))
        setupDraggableInput(vertex, id, point)
        node.attachChild(vertex)
      }
      for (line <- sector.polygon.lines) {
        val geo = new Geometry("line", new Line(
          new Vector3f(line.a.x, line.a.y, 0f), new Vector3f(line.b.x, line.b.y, 0f)))
        geo.setMaterial(plainColor(ColorRGBA.LightGray))
        geo.addControl(new SelectableControl(ColorRGBA.LightGray, id, Set(line.a, line.b)))
        node.attachChild(geo)
      }
    }
    def draw3d() {
      val node = getOrCreateNode(get3DNode, "polygon-" + id)
      val geom = new MeshFactory(assetManager).createMesh(sector)
      node.attachChild(geom)
    }
    draw2d()
    draw3d()
  }

  var isDragging = false
  var oldPos = new Vector3f()
  var newPos = new Vector3f()

  def setupDraggableInput(spatial: Spatial, sectorId: Long, point: Point) {
    CursorEventControl.addListenersToSpatial(spatial, new DefaultCursorListener() {
      override def cursorButtonEvent(event: CursorButtonEvent, target: Spatial, capture: Spatial) {
        //        println(s"click $target - $event")
        if (event.getButtonIndex == 0) {
          if (!isDragging) {
            oldPos.set(spatial.getLocalTranslation)
            newPos.set(
              snapX(spatial.getLocalTranslation.x),
              snapY(spatial.getLocalTranslation.y),
              0f)
          } else if (!event.isPressed()) {
            // released
            if (oldPos.distanceSquared(newPos) > 0.1f) { // button released
              println(s"moved point ${spatial.getLocalTranslation} -> ${newPos}")
              EventBus.trigger(
                PointDragged(sectorId, point, Point(snapX(newPos.x), snapY(newPos.y))))
              spatial.setLocalTranslation(oldPos) //move back.
              //TODO improve, disable dragging if not in selection
            } else {
              EventBus.trigger(PointClicked(sectorId, point))
            }
          }
          isDragging = event.isPressed()
        }
      }
    })
    CursorEventControl.addListenersToSpatial(spatial, new DefaultCursorListener() {
      override def cursorMoved(event: CursorMotionEvent, target: Spatial, capture: Spatial) {
        if (isDragging) {
          val cam = event.getViewPort.getCamera
          val coord = cam.getWorldCoordinates(event.getLocation, 0f)
          newPos.set(snapX(coord.x), snapY(coord.y), 0f)
          spatial.setLocalTranslation(newPos)
        }
      }
    })
    inputMapper.addStateListener(this, InputFunction.snapToGrid)
    inputMapper.activateGroup(InputFunction.general)
  }

  //TODO duplicated
  def snapX(c: Float) = snapped(c, 1f)
  def snapY(c: Float) = snapped(c, 1f)
  def snapped(coor: Float, step: Float) = {
    step * (coor / step).round
  }

  def valueChanged(f: FunctionId, i: InputState, d: Double) {
  }
}