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
import com.stovokor.util.PolygonDrawn
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.stovokor.editor.factory.MeshFactory
import com.jme3.scene.shape.Box
import com.jme3.scene.Node
import com.jme3.math.ColorRGBA
import com.stovokor.editor.factory.MaterialFactory
import com.jme3.scene.shape.Line
import com.stovokor.util.EventBus

// this state works in 2d and 3d
class SectorPresenterState extends BaseState
    with MaterialFactory
    with EditorEventListener {

  val sectorRepository = new SectorRepository()

  // TODO make this async, check PathFindScheduler

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    EventBus.subscribeByType(this, classOf[PolygonDrawn])
  }

  override def update(tpf: Float) {
  }

  def onEvent(event: EditorEvent) = event match {
    case PolygonDrawn(polygon) => saveNewSector(polygon)
    case _                     =>
  }

  def saveNewSector(polygon: Polygon) {
    val sector = Sector(
      polygon = polygon,
      floor = Surface(0f, SurfaceTexture()),
      ceiling = Surface(2f, SurfaceTexture()))
    val id = sectorRepository.add(sector)
    drawSector(id, sector)
  }

  def drawSector(id: Long, sector: Sector) {
    def draw2d() {
      val node = getOrCreateNode(get2DNode, "polygon-" + id)
      for (point <- sector.polygon.pointsSorted) {
        val vertex = new Geometry("point", new Box(0.05f, 0.05f, 0.05f))
        vertex.setLocalTranslation(point.x, point.y, 0f)
        vertex.setMaterial(plainColor(ColorRGBA.White))
        node.attachChild(vertex)
      }
      for (line <- sector.polygon.lines) {
        val geo = new Geometry("line", new Line(
          new Vector3f(line.a.x, line.a.y, 0f), new Vector3f(line.b.x, line.b.y, 0f)))
        geo.setMaterial(plainColor(ColorRGBA.LightGray))
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
}