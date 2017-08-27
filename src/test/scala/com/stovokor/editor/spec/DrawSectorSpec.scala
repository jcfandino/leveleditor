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

  /*
   *   D--------C-------F      
   *   | \      |       |      
   *   |  ` o   |       |      
   *   |      \ |       |      
   *   X--------B-------E      
   *                           
   */
  val (x, b, c, d, e, f) = (Point(-1, -1), Point(1, -1), Point(1, 1), Point(-1, 1), Point(2, -1), Point(2, 1))

  it should "be able to draw a square room" in {
    Given("An empty space")

    When("Four points are drawn")
    makeClicks(x, b, c, d, x)

    Then("A new sector should be saved")
    assert(sectorDefinedByPoints(x, b, c, d))
  }

  it should "be able to divide a square room in two triangles" in {
    Given("A squared sector is drawn")
    makeClicks(x, b, c, d, x)

    When("A new line is drawn between opposed corners")
    makeClicks(x, c)

    Then("A two new sectors should be saved")
    assert(sectorDefinedByPoints(x, b, c))
    assert(sectorDefinedByPoints(c, d, x))
  }

  it should "be able to extend a square room to a side (up-bottom)" in {
    Given("A squared sector is drawn")
    makeClicks(x, b, c, d, x)

    When("New lines are drawn continuing the sector to the right")
    makeClicks(c, f, e, b)

    Then("Two new sectors should be saved")
    assert(sectorDefinedByPoints(x, b, c, d))
    assert(sectorDefinedByPoints(c, f, e, b))
  }

  it should "be able to extend a square room to a side (bottom-up)" in {
    Given("A squared sector is drawn")
    makeClicks(x, b, c, d, x)

    When("New lines are drawn continuing the sector to the right")
    makeClicks(b, e, f, c)

    Then("Two new sectors should be saved")
    assert(sectorDefinedByPoints(x, b, c, d))
    assert(sectorDefinedByPoints(c, f, e, b))
  }

  class MockEventListener extends EditorEventListener {
    var events: List[EditorEvent] = List()

    def onEvent(event: EditorEvent) {
      events = events ++ List(event)
    }
  }
}