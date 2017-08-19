package com.stovokor.editor.spec

import org.scalatest._
import org.mockito.Mockito._
import scala.io.Source
import org.scalatest.Matchers
import org.scalatest.mockito.MockitoSugar
import com.stovokor.editor.state.DrawingState
import com.jme3.app.state.AppStateManager
import com.jme3.app.SimpleApplication
import com.jme3.scene.Node
import com.simsilica.lemur.GuiGlobals
import com.simsilica.lemur.input.InputMapper
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EditorEvent
import com.stovokor.util.EventBus
import com.stovokor.util.PointClicked
import com.stovokor.editor.model.Point
import com.stovokor.util.SectorUpdated
import com.stovokor.editor.model.repository.SectorRepository
import com.stovokor.editor.model.Line
import com.stovokor.editor.factory.MaterialFactory

class DrawSectorSpec extends FlatSpec
    with Matchers
    with MockitoSugar
    with BeforeAndAfterEach
    with GivenWhenThen
    with TestSectorSupport {

  val drawindState = new DrawingState
  val stateManager = mock[AppStateManager]
  val app = mock[SimpleApplication]
  val eventListener = mock[EditorEventListener]
  val sectorRepository = SectorRepository()

  override def beforeEach() {
    GuiGlobals.setInstance(mock[GuiGlobals])
    when(GuiGlobals.getInstance.getInputMapper).thenReturn(mock[InputMapper])
    when(app.getRootNode).thenReturn(new Node)
    MaterialFactory.setInstance(mock[MaterialFactory])
    drawindState.initialize(stateManager, app)
    SectorRepository().removeAll
  }

  behavior of "Drawing state"

  it should "be able to draw a square room" in {
    Given("An empty space")
    val (a, b, c, d) = (Point(-1, -1), Point(1, -1), Point(1, 1), Point(-1, 1))

    When("Four points are drawn")
    EventBus.trigger(PointClicked(a))
    EventBus.trigger(PointClicked(b))
    EventBus.trigger(PointClicked(c))
    EventBus.trigger(PointClicked(d))
    EventBus.trigger(PointClicked(a))

    Then("A new sector should be saved")
    assert(sectorDefinedByPoints(List(a, b, c, d)))
  }

  it should "be able to divide a square room in two triangles" in {
    Given("A squared sector is drawn")
    val (a, b, c, d) = (Point(-1, -1), Point(1, -1), Point(1, 1), Point(-1, 1))
    EventBus.trigger(PointClicked(a))
    EventBus.trigger(PointClicked(b))
    EventBus.trigger(PointClicked(c))
    EventBus.trigger(PointClicked(d))
    EventBus.trigger(PointClicked(a))

    When("A new line is drawn between opposed corners")
    EventBus.trigger(PointClicked(a))
    EventBus.trigger(PointClicked(c))

    Then("A two new sectors should be saved")
    assert(sectorDefinedByPoints(List(a, b, c)))
    assert(sectorDefinedByPoints(List(c, d, a)))
  }

  class MockEventListener extends EditorEventListener {
    var events: List[EditorEvent] = List()

    def onEvent(event: EditorEvent) {
      events = events ++ List(event)
    }
  }
}