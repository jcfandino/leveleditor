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
import com.stovokor.editor.state.FillHoleState

class FillHoleSpec extends FlatSpec
    with Matchers
    with MockitoSugar
    with BeforeAndAfterEach
    with GivenWhenThen
    with TestSectorSupport {

  val drawingState = new DrawingState
  val fillHoleState = new FillHoleState
  val stateManager = mock[AppStateManager]
  val app = mock[SimpleApplication]
  val eventListener = mock[EditorEventListener]
  val sectorRepository = SectorRepository()

  override def beforeEach() {
    GuiGlobals.setInstance(mock[GuiGlobals])
    when(GuiGlobals.getInstance.getInputMapper).thenReturn(mock[InputMapper])
    when(app.getRootNode).thenReturn(new Node)
    MaterialFactory.setInstance(mock[MaterialFactory])
    drawingState.initialize(stateManager, app)
    fillHoleState.initialize(stateManager, app)
    startDrawing()
  }

  override def afterEach() {
    drawingState.cleanup()
    fillHoleState.cleanup()
    SectorRepository().removeAll
    BorderRepository().removeAll
  }

  behavior of "Filling hole state"

  /*
   * 3  M-------N-------O-------P-------U
   *    |                       |       |
   *    |                       |       |
   *    |                       |       |
   * 2  J       K-------G-------L-------V
   *    |       | T---S |       |       |
   *    |       | |   | |       |       |
   *    |       | Q---R |       |       |
   * 1  D       C-------F-------I-------W
   *    |                       |       |
   * 0  |   ø                   |       |
   *    |                       |       |
   * -1 X-------B-------E-------H-------Y
   *
   *   -1   0   1       2       3       4
   */
  val (m, n, o, p, u) =
    (Point(-1, 3), Point(1, 3), Point(2, 3), Point(3, 3), Point(4, 3))
  val (j, k, g, l, v) =
    (Point(-1, 2), Point(1, 2), Point(2, 2), Point(3, 2), Point(4, 2))
  val (d, c, f, i, w) =
    (Point(-1, 1), Point(1, 1), Point(2, 1), Point(3, 1), Point(4, 1))
  val (x, b, e, h, y) =
    (Point(-1, -1), Point(1, -1), Point(2, -1), Point(3, -1), Point(4, -1))
  val (q, r, s, t) =
    (Point(1.2, 1.2), Point(1.8, 1.2), Point(1.8, 1.8), Point(1.2, 1.8))

  it should "be able to draw a hole in a sector" in {
    Given("A big squared sector")
    makeClicks(x, h, p, m, x)

    When("Another square is drawn inside it")
    makeClicks(c, f, g, k, c)

    Then("There should be one sector with a hole in the middle")
    assert(sectorDefinedByPoints(x, h, p, m))
    assert(holeDefinedByPoints(c, f, g, k))
  }

  it should "Be able to draw a hole and convert to a inner sector" in {
    Given("A big squared sector")
    makeClicks(x, h, p, m, x)

    When("Another square is drawn inside it")
    makeClicks(c, f, g, k, c)

    And("Click using the fill tool inside the hole")
    startFillHole()
    makeClicks(corner(c))

    Then("There should be one sector with a hole in the middle")
    assert(sectorDefinedByPoints(x, h, p, m))
    assert(holeDefinedByPoints(c, f, g, k))

    And("another sector filling the hole")
    assert(sectorDefinedByPoints(c, f, g, k))

    And("borders connecting both sectors")
    assert(borderDefinedByPoints(c, f, g, k))
    assert(borderBetweenSectors(corner(x), corner(c)))
  }

  it should "Be able to draw inner sectors recursively" in {
    Given("A big squared sector")
    makeClicks(x, h, p, m, x)

    And("An inner square is drawn inside it")
    makeClicks(c, f, g, k, c)
    startFillHole()
    makeClicks(corner(c))

    When("A hole is drawn inside the inner hole")
    startDrawing()
    makeClicks(q, r, s, t, q)
    startFillHole()
    //    fillHoleState.setEnabled(true)
    makeClicks(corner(q))

    Then("There should be a big sector with a hole filled")
    assert(sectorDefinedByPoints(x, h, p, m))
    assert(holeDefinedByPoints(c, f, g, k))

    And("another sector filling the hole, also with a hole")
    assert(sectorDefinedByPoints(c, f, g, k))
    assert(borderDefinedByPoints(c, f, g, k))
    assert(holeDefinedByPoints(q, r, s, t))

    And("yet another sector filling the small hole")
    assert(sectorDefinedByPoints(q, r, s, t))

    And("borders connecting the inner sector")
    assert(borderDefinedByPoints(q, r, s, t))
  }

  it should "Be able to extend a sector with a hole in it" in {
    Given("A big squred sector")
    makeClicks(c, f, g, k, c)

    And("An inner sector is drawn inside it")
    makeClicks(q, r, s, t, q)
    startFillHole()
    makeClicks(q.move(.1f, .1f))

    When("I extend the big sector to a side")
    startDrawing()
    makeClicks(f, i, l, g)

    Then("There should be a big sector with a hole filled")
    assert(sectorDefinedByPoints(c, f, g, k))
    assert(holeDefinedByPoints(q, r, s, t))

    And("another sector filling the hole")
    assert(sectorDefinedByPoints(q, r, s, t))
    assert(borderDefinedByPoints(q, r, s, t))

    And("another sector to the right of the big one")
    assert(sectorDefinedByPoints(f, i, l, g))
    assert(borderDefinedByPoints(f, g))
  }

  it should "Be able to draw two holes next to each other" in {
    Given("A big squared sector")
    makeClicks(x, y, u, m, x)

    And("Another square is drawn inside it")
    println("~ Drawing first hole")
    makeClicks(c, f, g, k, c)

    When("Another square is drawn next to the hole")
    println("~ Drawing second hole")
    makeClicks(f, i, l, g, f)

    Then("There should be a big sector with two holes")
    assert(sectorDefinedByPoints(x, y, u, m))
    assert(holeDefinedByPoints(c, f, g, k))
    assert(holeDefinedByPoints(f, i, l, g))
  }

  it should "Be able to draw two subsectors next to each other" in {
    Given("A big squared sector")
    makeClicks(x, y, u, m, x)

    And("Another square is drawn inside it")
    println("~ Drawing first hole")
    makeClicks(c, f, g, k, c)

    When("Another square is next to the subsector")
    makeClicks(f, i, l, g, f)
    startFillHole()
    makeClicks(corner(c))
    makeClicks(corner(f))

    Then("There should be a big sector with two holes filled")
    assert(sectorDefinedByPoints(x, y, u, m))
    assert(holeDefinedByPoints(c, f, g, k))
    assert(holeDefinedByPoints(f, i, l, g))

    And("the first subsector is filled")
    assert(sectorDefinedByPoints(c, f, g, k))
    assert(borderDefinedByPoints(c, f, g, k))

    And("the second subsector is filled")
    assert(sectorDefinedByPoints(f, i, l, g))
    assert(borderDefinedByPoints(f, i, l, g))
    // TODO need to check the borders are between the right sectors
    //assert(borderBetweenSectors(corner(x), corner(c)))
  }

  def corner(point: Point) = point.move(.1f, .1f)

  def startDrawing() {
    fillHoleState.setEnabled(false)
    drawingState.setEnabled(true)
  }
  def startFillHole() {
    fillHoleState.setEnabled(true)
    drawingState.setEnabled(false)
  }
}