package com.stovokor.editor.state

import scala.collection.JavaConverters.asScalaBufferConverter

import com.jme3.app.Application
import com.jme3.app.state.AppStateManager
import com.jme3.scene.Node
import com.simsilica.lemur.input.FunctionId
import com.simsilica.lemur.input.InputState
import com.simsilica.lemur.input.StateFunctionListener
import com.stovokor.editor.factory.MaterialFactoryClient
import com.stovokor.editor.factory.Mesh2dFactory
import com.stovokor.editor.factory.MeshFactory
import com.stovokor.editor.input.InputFunction
import com.stovokor.editor.model.Sector
import com.stovokor.editor.model.repository.BorderRepository
import com.stovokor.editor.model.repository.SectorRepository
import com.stovokor.util.EditorEvent
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EventBus
import com.stovokor.util.JmeExtensions.SpatialExtensions
import com.stovokor.util.LemurExtensions.SpatialExtension
import com.stovokor.util.PointerTargetChange
import com.stovokor.util.SectorDeleted
import com.stovokor.util.SectorUpdated

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
    inputMapper.addStateListener(this, InputFunction.snapToGrid)
    inputMapper.activateGroup(InputFunction.general)
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
      val meshNode = Mesh2dFactory(assetManager).createMesh(id, sector)
      //TODO setup input
      node.attachChild(meshNode)
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