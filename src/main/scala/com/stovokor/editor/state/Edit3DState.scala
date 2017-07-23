package com.stovokor.editor.state

import com.stovokor.util.EditorEventListener
import com.stovokor.util.EditorEvent
import com.stovokor.util.EditModeSwitch
import com.jme3.app.state.AppStateManager
import com.stovokor.util.EventBus
import com.jme3.app.Application
import com.stovokor.util.PointerTargetChange
import com.stovokor.editor.input.InputFunction
import com.simsilica.lemur.input.StateFunctionListener
import com.simsilica.lemur.input.InputState
import com.simsilica.lemur.input.FunctionId
import com.stovokor.editor.model.repository.SectorRepository
import com.stovokor.util.SectorUpdated

class Edit3DState extends BaseState
    with EditorEventListener
    with CanMapInput
    with StateFunctionListener {

  val sectorRepository = SectorRepository()

  var lastTarget: Option[(Long, String)] = None

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    EventBus.subscribeByType(this, classOf[PointerTargetChange])
    setupInput
  }

  def setupInput() {
    inputMapper.addStateListener(this, InputFunction.editHeight)
    inputMapper.addStateListener(this, InputFunction.editTextureOffsetX)
    inputMapper.addStateListener(this, InputFunction.editTextureOffsetY)
    inputMapper.addStateListener(this, InputFunction.editTextureScaleX)
    inputMapper.addStateListener(this, InputFunction.editTextureScaleY)
    inputMapper.activateGroup(InputFunction.edit3d)
  }

  def onEvent(event: EditorEvent) = event match {
    case PointerTargetChange(sectorId, target) => {
      println(s"Pointing $target of sector $sectorId")
      lastTarget = Some((sectorId, target))
    }
    case _ =>
  }

  def valueChanged(functionId: FunctionId, state: InputState, factor: Double) {
    functionId match {
      case InputFunction.editHeight => {
        if (state == InputState.Positive) changeHeight(.1f)
        else if (state == InputState.Negative) changeHeight(-.1f)
      }
      case InputFunction.editTextureOffsetX => {
        if (state == InputState.Positive) changeTextureOffset(.1f, 0f)
        else if (state == InputState.Negative) changeTextureOffset(-.1f, 0f)
      }
      case InputFunction.editTextureOffsetY => {
        if (state == InputState.Positive) changeTextureOffset(0f, .1f)
        else if (state == InputState.Negative) changeTextureOffset(0f, -.1f)
      }
      case InputFunction.editTextureScaleX => {
        if (state == InputState.Positive) changeTextureScale(.1f, 0f)
        else if (state == InputState.Negative) changeTextureScale(-.1f, 0f)
      }
      case InputFunction.editTextureScaleY => {
        if (state == InputState.Positive) changeTextureScale(0f, .1f)
        else if (state == InputState.Negative) changeTextureScale(0f, -.1f)
      }
      case _ => println(s"got: $functionId")
    }
  }

  def changeHeight(factor: Float) {
    if (lastTarget.isDefined) {
      val (sectorId, target) = lastTarget.get
      println(s"edit height of $lastTarget by $factor")
      val sector = sectorRepository.get(sectorId)
      val updated =
        if (target == "floor") sector.updatedFloor(sector.floor.move(factor))
        else sector.updatedCeiling(sector.ceiling.move(factor))
      sectorRepository.update(sectorId, updated)
      EventBus.trigger(SectorUpdated(sectorId, updated))
    }
  }

  // TODO refactor here
  def changeTextureOffset(factorX: Float, factorY: Float) {
    if (lastTarget.isDefined) {
      val (sectorId, target) = lastTarget.get
      println(s"changing texture offset $factorX,$factorY")
      val sector = sectorRepository.get(sectorId)
      val updated =
        if (target == "floor") {
          sector.updatedFloor(
            sector.floor.updateTexture(
              sector.floor.texture.move(factorX, factorY)))
        } else if (target == "ceiling") {
          sector.updatedCeiling(
            sector.ceiling.updateTexture(
              sector.ceiling.texture.move(factorX, factorY)))
        } else if (target.startsWith("wall-")) {
          val idx = target.replace("wall-", "").toInt
          val wall = sector.closedWalls(idx)
          sector.updatedClosedWall(idx,
            wall.updateTexture(
              wall.texture.move(factorX, factorY)))
        } else sector
      sectorRepository.update(sectorId, updated)
      EventBus.trigger(SectorUpdated(sectorId, updated))
    }
  }

  def changeTextureScale(factorX: Float, factorY: Float) {
    if (lastTarget.isDefined) {
      val (sectorId, target) = lastTarget.get
      println(s"changing texture scale $factorX,$factorY")
      val sector = sectorRepository.get(sectorId)
      val updated =
        if (target == "floor") {
          sector.updatedFloor(
            sector.floor.updateTexture(
              sector.floor.texture.scale(factorX, factorY)))
        } else if (target == "ceiling") {
          sector.updatedCeiling(
            sector.ceiling.updateTexture(
              sector.ceiling.texture.scale(factorX, factorY)))
        } else if (target.startsWith("wall-")) {
          val idx = target.replace("wall-", "").toInt
          val wall = sector.closedWalls(idx)
          sector.updatedClosedWall(idx,
            wall.updateTexture(
              wall.texture.scale(factorX, factorY)))
        } else sector
      sectorRepository.update(sectorId, updated)
      EventBus.trigger(SectorUpdated(sectorId, updated))
    }
  }
}