package com.stovokor.editor.state

import com.jme3.app.Application
import com.jme3.app.state.AppStateManager
import com.simsilica.lemur.input.AnalogFunctionListener
import com.simsilica.lemur.input.FunctionId
import com.simsilica.lemur.input.InputState
import com.simsilica.lemur.input.StateFunctionListener
import com.stovokor.editor.model.Point
import com.stovokor.editor.model.repository.BorderRepository
import com.stovokor.editor.model.repository.SectorRepository
import com.stovokor.util.EditorEvent
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EventBus
import com.stovokor.util.PointClicked
import com.stovokor.editor.factory.SectorFactory

class FillHoleState extends BaseState
    with EditorEventListener
    with CanMapInput
    with AnalogFunctionListener
    with StateFunctionListener {

  val sectorRepository = SectorRepository()
  val borderRepository = BorderRepository()

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    EventBus.subscribeByType(this, classOf[PointClicked])
    setupInput
  }

  override def cleanup() {
    super.cleanup
    EventBus.removeFromAll(this)
  }

  def setupInput {
    //    inputMapper.addStateListener(this, InputFunction.cancel)
    //    inputMapper.activateGroup(InputFunction.general)
  }

  def onEvent(event: EditorEvent) {
    event match {
      case PointClicked(point) => if (isEnabled) {
        fillHole(point)
      }
      case _ =>
    }
  }

  def fillHole(point: Point) {
    val sectors = sectorRepository.findInside(point)
    val polygons = sectors.map(_._2.polygon).toSet
    sectors.foreach(p => p match {
      case (id, sector) => {
        sector.holes.find(_.contains(point)).foreach(polygon => {
          if (!polygons.contains(polygon)) {
            SectorFactory.create(sectorRepository, borderRepository, polygon)
          } else {
            //TODO maybe make a hole again???
            println(s"hole is already a sector")
          }
        })
      }
    })
  }

  def valueActive(func: FunctionId, value: Double, tpf: Double) {
  }

  def valueChanged(func: FunctionId, value: InputState, tpf: Double) {
    func match {
      case _ =>
    }
  }

}