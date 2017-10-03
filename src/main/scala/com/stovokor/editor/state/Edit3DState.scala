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
import com.stovokor.editor.model.Sector
import com.stovokor.editor.model.repository.BorderRepository
import com.stovokor.editor.factory.BorderFactory
import com.simsilica.lemur.input.AnalogFunctionListener
import com.stovokor.util.ChangeMaterial
import com.stovokor.editor.model.SurfaceTexture

class Edit3DState extends BaseState
    with EditorEventListener
    with CanMapInput
    with AnalogFunctionListener
    with StateFunctionListener {

  val sectorRepository = SectorRepository()
  val borderRepository = BorderRepository()

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
    inputMapper.addStateListener(this, InputFunction.changeMaterial)
    inputMapper.addAnalogListener(this, InputFunction.mouseWheel)
    inputMapper.activateGroup(InputFunction.edit3d)
    inputMapper.activateGroup(InputFunction.mouse)
  }

  def onEvent(event: EditorEvent) = event match {
    case PointerTargetChange(sectorId, target) => {
      println(s"Pointing $target of sector $sectorId")
      lastTarget = Some((sectorId, target))
    }
    case _ =>
  }

  val heightStep = 0.01f
  val offsetStep = 0.01f
  val scaleStep = 0.1f

  def valueChanged(functionId: FunctionId, state: InputState, factor: Double) {
    functionId match {
      case InputFunction.editHeight => {
        if (state == InputState.Positive) changeHeight(heightStep)
        else if (state == InputState.Negative) changeHeight(heightStep)
      }
      case InputFunction.editTextureOffsetX => {
        if (state == InputState.Positive) changeTextureOffset(-offsetStep, 0f)
        else if (state == InputState.Negative) changeTextureOffset(offsetStep, 0f)
      }
      case InputFunction.editTextureOffsetY => {
        if (state == InputState.Positive) changeTextureOffset(0f, offsetStep)
        else if (state == InputState.Negative) changeTextureOffset(0f, -offsetStep)
      }
      case InputFunction.editTextureScaleX => {
        if (state == InputState.Positive) changeTextureScale(scaleStep, 0f)
        else if (state == InputState.Negative) changeTextureScale(-scaleStep, 0f)
      }
      case InputFunction.editTextureScaleY => {
        if (state == InputState.Positive) changeTextureScale(0f, scaleStep)
        else if (state == InputState.Negative) changeTextureScale(0f, -scaleStep)
      }
      case InputFunction.changeMaterial => {
        if (state == InputState.Positive) changeMaterial()
      }
      case _ => println(s"got: $functionId")
    }
  }

  def valueActive(functionId: FunctionId, value: Double, tpf: Double) = functionId match {
    case InputFunction.mouseWheel => {
      if (value > 0.0) changeHeight(.1f)
      else if (value < 0.0) changeHeight(-.1f)
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
      recalculateBorders(sectorId, updated)
      sectorRepository.update(sectorId, updated)
      EventBus.trigger(SectorUpdated(sectorId, updated))
    }
  }

  def changeMaterial() {
    lastTarget.foreach(p => p match {
      case (sectorId, target) => EventBus.trigger(ChangeMaterial(sectorId, target))
    })
  }

  def changeTextureOffset(factorX: Float, factorY: Float) {
    lastTarget.foreach(p => p match {
      case (sectorId, target) => {
        SectorSurfaceMutator.mutate(sectorId, target, _.move(factorX, factorY))
      }
    })
  }
  def changeTextureScale(factorX: Float, factorY: Float) {
    lastTarget.foreach(p => p match {
      case (sectorId, target) => {
        SectorSurfaceMutator.mutate(sectorId, target, _.scale(factorX, factorY))
      }
    })
  }


  object SectorSurfaceMutator {

    def mutate(sectorId: Long, target: String, mutation: SurfaceTexture => SurfaceTexture) {
      val sector = sectorRepository.get(sectorId)
      val updated =
        if (target == "floor") {
          sector.updatedFloor(sector.floor.updateTexture(mutation(sector.floor.texture)))
        } else if (target == "ceiling") {
          sector.updatedCeiling(sector.ceiling.updateTexture(mutation(sector.ceiling.texture)))
        } else if (target.startsWith("wall-")) {
          val idx = target.replace("wall-", "").toInt
          val wall = sector.closedWalls(idx)
          sector.updatedClosedWall(idx, wall.updateTexture(mutation(wall.texture)))
        } else if (target.startsWith("borderLow-")) {
          val idx = target.replace("borderLow-", "").toInt
          val wall = sector.openWalls(idx)
          borderRepository.find(wall.line).foreach(pair => pair match {
            case (id, border) => {
              borderRepository.update(id, border.updateSurfaceFloor(
                border.surfaceFloor.updateTexture(mutation(border.surfaceFloor.texture))))
            }
          })
          sector
        } else if (target.startsWith("borderHi-")) {
          val idx = target.replace("borderHi-", "").toInt
          val wall = sector.openWalls(idx)
          borderRepository.find(wall.line).foreach(pair => pair match {
            case (id, border) => {
              borderRepository.update(id, border.updateSurfaceCeiling(
                border.surfaceCeiling.updateTexture(mutation(border.surfaceCeiling.texture))))
            }
          })
          sector
        } else sector
      sectorRepository.update(sectorId, updated)
      EventBus.trigger(SectorUpdated(sectorId, updated))
    }
  }

  def recalculateBorders(sectorId: Long, sector: Sector) {
    val bordersFrom = borderRepository.findFrom(sectorId)
    bordersFrom.foreach(pair => pair match {
      case (id, border) => {
        val other = sectorRepository.get(border.sectorB)
        val updated = border.updateHeights(sector, other)
        borderRepository.update(id, updated)
      }
    })
    val bordersTo = borderRepository.findTo(sectorId)
    val sectorsToUpdate = bordersTo.map(pair => pair match {
      case (id, border) => {
        val from = sectorRepository.get(border.sectorA)
        val updated = border.updateHeights(from, sector)
        borderRepository.update(id, updated)
        (border.sectorA, from)
      }
    })
    sectorsToUpdate.distinct.foreach(pair => pair match {
      case (id, sec) => EventBus.trigger(SectorUpdated(id, sec))
    })
  }
}