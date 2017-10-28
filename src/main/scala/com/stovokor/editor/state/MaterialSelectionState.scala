package com.stovokor.editor.state

import com.simsilica.lemur.input.StateFunctionListener
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EditorEvent
import com.jme3.app.state.AppStateManager
import com.stovokor.editor.input.InputFunction
import com.stovokor.util.ExportMap
import com.stovokor.util.EventBus
import com.jme3.app.Application
import com.stovokor.util.ChangeMaterial
import com.stovokor.editor.model.repository.BorderRepository
import com.stovokor.editor.model.repository.SectorRepository
import com.stovokor.util.SectorSurfaceMutator
import com.stovokor.editor.gui.GuiFactory
import com.simsilica.lemur.Container
import com.stovokor.editor.model.repository.Repositories
import com.stovokor.editor.model.SimpleMaterial
import com.stovokor.editor.model.SurfaceMaterial
import com.stovokor.editor.model.MatDefMaterial

class MaterialSelectionState extends BaseState with EditorEventListener {

  val materialRepository = Repositories.materialRepository

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    EventBus.subscribeByType(this, classOf[ChangeMaterial])
    // Create debug materials
    materialRepository.add(SimpleMaterial("Textures/Debug1.png"))
    materialRepository.add(SimpleMaterial("Textures/Debug2.png"))
    materialRepository.add(SimpleMaterial("Textures/Debug3.png"))
    materialRepository.add(MatDefMaterial("Materials/DebugDef1.j3m"))
  }

  override def cleanup() {
    super.cleanup
    EventBus.removeFromAll(this)
  }

  def onEvent(event: EditorEvent) = event match {
    case ChangeMaterial(sectorId: Long, target: String) => changeMaterial(sectorId, target)
    case _ =>
  }

  def changeMaterial(sectorId: Long, target: String) {
    openMaterialDialog(sectorId, target)
  }

  var materialDialog: Option[Container] = None

  def openMaterialDialog(sectorId: Long, target: String) {
    materialDialog.foreach(_.removeFromParent())
    println("opening material dialog")
    val options = materialRepository.materials
    val description = s"Sector $sectorId - $target"
    val dialog = GuiFactory.createMaterialPanel(
      cam.getWidth, cam.getHeight, description, options, materialChosen(sectorId, target))
    dialog.setName("material-dialog")
    guiNode.attachChild(dialog)
    materialDialog = Some(dialog)
  }

  def materialChosen(sectorId: Long, target: String)(matOption: Option[SurfaceMaterial]) {
    matOption.foreach(mat => {
      println(s"Changing material $sectorId, $target -> $mat")
      SectorSurfaceMutator.mutate(sectorId, target, surface =>
        surface.updateIndex(materialRepository.materials.indexOf(mat)))
    })
    materialDialog = None
  }
}