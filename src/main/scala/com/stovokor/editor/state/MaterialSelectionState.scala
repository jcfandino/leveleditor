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
import com.simsilica.lemur.Panel
import com.simsilica.lemur.OptionPanel
import com.simsilica.lemur.OptionPanelState
import com.simsilica.lemur.input.FunctionId
import com.simsilica.lemur.input.InputState

class MaterialSelectionState extends BaseState
    with EditorEventListener
    with CanMapInput
    with StateFunctionListener {

  val materialRepository = Repositories.materialRepository

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    EventBus.subscribeByType(this, classOf[ChangeMaterial])
    setupInput
  }

  def setupInput {
    inputMapper.addStateListener(this, InputFunction.cancel)
    inputMapper.activateGroup(InputFunction.general)
  }

  override def cleanup() {
    super.cleanup
    EventBus.removeFromAll(this)
    inputMapper.removeStateListener(this, InputFunction.cancel)
  }

  def valueChanged(func: FunctionId, value: InputState, tpf: Double) {
    if (value == InputState.Positive) func match {
      case InputFunction.cancel => optionPanelState.close()
      case _                    =>
    }
  }

  def onEvent(event: EditorEvent) = event match {
    case ChangeMaterial(sectorId: Long, target: String) => changeMaterial(sectorId, target)
    case _ =>
  }

  def changeMaterial(sectorId: Long, target: String) {
    openMaterialDialog(sectorId, target)
  }

  def openMaterialDialog(sectorId: Long, target: String) {
    optionPanelState.close()
    println("opening material dialog")
    val options = materialRepository.materials
    val description = s"Sector $sectorId - $target"
    val dialog = GuiFactory.createMaterialDialog(
      cam.getWidth, cam.getHeight, description, options, materialChosen(sectorId, target))
    optionPanelState.show(dialog)
  }

  def materialChosen(sectorId: Long, target: String)(matOption: Option[SurfaceMaterial]) {
    matOption.foreach(mat => {
      println(s"Changing material $sectorId, $target -> $mat")
      SectorSurfaceMutator.mutate(sectorId, target, surface =>
        surface.updateIndex(materialRepository.materials.indexOf(mat)))
    })
    optionPanelState.close()
  }

  def optionPanelState = stateManager.getState(classOf[OptionPanelState])
}