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
import com.stovokor.editor.model.repository.BorderRepository
import com.stovokor.editor.state.ModifyingState
import com.stovokor.editor.builder.SectorBuilder
import com.stovokor.editor.model.repository.Repositories
import com.stovokor.util.SelectionModeSwitch
import com.stovokor.util.EditModeSwitch

class ModifySectorSpec extends FlatSpec
    with Matchers
    with MockitoSugar
    with BeforeAndAfterEach
    with GivenWhenThen
    with TestSectorSupport {

  val modifyingState = new ModifyingState
  val stateManager = mock[AppStateManager]
  val app = mock[SimpleApplication]
  val eventListener = mock[EditorEventListener]
  val sectorRepository = SectorRepository()

  override def beforeEach() {
    GuiGlobals.setInstance(mock[GuiGlobals])
    when(GuiGlobals.getInstance.getInputMapper).thenReturn(mock[InputMapper])
    when(app.getRootNode).thenReturn(new Node)
    MaterialFactory.setInstance(mock[MaterialFactory])
    modifyingState.initialize(stateManager, app)
  }

  override def afterEach() {
    modifyingState.cleanup()
    SectorRepository().removeAll
    BorderRepository().removeAll
  }

  def defineSector(start: Point, others: Point*) = {
    others
      .foldLeft(SectorBuilder.start(start))(_ add _)
      .build(Repositories.sectorRepository, Repositories.borderRepository)
  }

  behavior of "Modifying state"

  /*
   * 
   *           G
   *         / | \
   *       /   |   \
   *     /     |     \
   *   D-------C-------F------I
   *   |     / |       |      |
   *   |   ø   |       |      |
   *   | /     |       |      |
   *   X-------B-------E------H
   *                           
   */
  val (d, c, f, i, x, b, e, h, g) = (
    Point(-1, 1), Point(1, 1), Point(2, 1), Point(3, 1),
    Point(-1, -1), Point(1, -1), Point(2, -1), Point(3, -1),
    Point(1, 2))

  it should "be able to move points to resize sectors" in {
    Given("A simple triangular sector")
    defineSector(x, c, e)

    When("A point is dragged away")
    drag(c, f)

    Then("The sector exists and it was modified")
    assert(sectorDefinedByPoints(x, f, e))
  }

  it should "be able to delete points by joining them together" in {
    Given("A simple square sector")
    defineSector(x, b, c, d)

    When("A point is dragged over another")
    drag(d, c)

    Then("The sector exists and is triangular")
    assert(sectorDefinedByPoints(x, c, b))
  }

  it should "be able to delete sectors by joining their points together" in {
    Given("A simple square sector")
    defineSector(x, b, c, d)

    When("Two points are joined to form a triangule")
    drag(d, c)
    And("Two points are joined to form a segment")
    drag(x, b)

    Then("The sector no longer exists")
    assert(Repositories.sectorRepository.sectors.isEmpty)
  }

  it should "be able to move points where sectors meet" in {
    Given("A simple triangular sector")
    defineSector(x, b, c)
    And("A another triangular sector that shares two points")
    defineSector(x, c, d)

    When("One point is moved away")
    drag(c, f)

    Then("The both sectors exist and were modified")
    assert(sectorDefinedByPoints(x, b, f))
    assert(sectorDefinedByPoints(x, f, d))
    assert(borderDefinedByPoints(x, f))
  }

  it should "be able to delete sectors neighbouring others" in {
    Given("A simple triangular sector")
    defineSector(x, b, c)
    And("A another triangular sector that shares two points")
    defineSector(x, c, d)

    When("One sector is deleted by merging two points")
    drag(d, c)

    Then("The only one sector exists and has no borders")
    assert(sectorDefinedByPoints(x, b, c))
    assert(Repositories.sectorRepository.sectors.size == 1)
    assert(Repositories.sectorRepository.findByPoint(x).head._2.openWalls.isEmpty)
    assert(!borderDefinedByPoints(x, c))
  }

  it should "be able to add borders when merging walls" in {
    Given("A square sector A")
    defineSector(x, b, c, d)
    And("A another square sector B that shares one side")
    defineSector(b, e, f, c)

    When("Sector A top-left corner is moved on top of top-right corner")
    drag(d, g)
    And("Sector B top-right corner is moved on top of top-left corner")
    drag(f, g)

    Then("Both sectors exists")
    assert(sectorDefinedByPoints(x, b, c, g))
    assert(sectorDefinedByPoints(e, b, c, g))
    assert(Repositories.sectorRepository.sectors.size == 2)

    And("They share two borders")
    assert(borderDefinedByPoints(b, c))
    // TODO this should be true
    assert(borderDefinedByPoints(c, g))
  }
}
