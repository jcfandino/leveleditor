package com.stovokor.editor.model

import earcut4j.Earcut
import scala.collection.JavaConversions._
import com.jme3.math.FastMath
import com.jme3.math.Vector2f

object Polygon {
  def apply(points: List[Point]) = new Polygon(points)
}
case class Polygon(val pointsUnsorted: List[Point]) {

  lazy val pointsSorted = {
    if (isClockwise) pointsUnsorted
    else pointsUnsorted.reverse
  }

  lazy val lines = {
    ((pointsSorted zip pointsSorted.tail) map ((p) => Line(p._1, p._2))) ++ List(Line(pointsSorted.last, pointsSorted.head))
  }

  def triangulate: List[Triangle] = {
    val vertices = pointsSorted
      .flatMap(p => List(p.x, p.y))
      .map(_.toDouble)
      .toArray
    def px(i: Int) = vertices(2 * i)
    def py(i: Int) = vertices(2 * i + 1)
    // the result is a list of indexes pointing to the original array
    val result = Earcut.earcut(vertices, null, 2)
    val indexes = result.sliding(3, 3)
    val triangles = indexes.map(l => (
      Point(px(l(0)), py(l(0))),
      Point(px(l(1)), py(l(1))),
      Point(px(l(2)), py(l(2)))))
      .map(ps => ps match { case (a, b, c) => Triangle(a, b, c) })
      .map(_.asClockwise)
      .toList
    triangles
  }

  private val limY = 10000000f

  def isClockwise = {
    def areaBelow(a: Point, b: Point) = 0.5f * (b.x - a.x) * (b.y + a.y + 2 * limY)
    val sorted = pointsUnsorted.sortBy(_.x)
    val leftmost = sorted.head
    val rightmost = sorted.last
    val i1 = pointsUnsorted.indexOf(leftmost)
    val i2 = pointsUnsorted.indexOf(rightmost)
    val path1 =
      if (i1 < i2) pointsUnsorted.slice(i1, i2 + 1)
      else pointsUnsorted.slice(i1, pointsUnsorted.size) ++ pointsUnsorted.slice(0, i2 + 1)
    val path2 = (
      if (i1 > i2) pointsUnsorted.slice(i2, i1 + 1)
      else pointsUnsorted.slice(i2, pointsUnsorted.size) ++ pointsUnsorted.slice(0, i1 + 1)).reverse
    val area1 = path1.sliding(2).map(ab => areaBelow(ab(0), ab(1))).sum
    val area2 = path2.sliding(2).map(ab => areaBelow(ab(0), ab(1))).sum
    val diff = area1 - area2
    diff > 0 && diff < limY
  }

  def changePoint(from: Point, to: Point): Polygon = {
    val idx = pointsSorted.indexOf(from)
    if (idx >= 0) Polygon(pointsSorted.updated(idx, to))
    else this
  }
}

object Triangle {
  def apply(p1: Point, p2: Point, p3: Point) = new Triangle(p1, p2, p3)
}

class Triangle(val p1: Point, val p2: Point, val p3: Point) extends Polygon(List(p1, p2, p3)) {
  override def triangulate = List(this)

  def asClockwise = if (isClockwise) this else reverse
  def asCounterClockwise = if (!isClockwise) this else reverse
  def reverse = Triangle(p3, p2, p1)

  override def isClockwise = FastMath.counterClockwise(
    new Vector2f(p1.x, p1.y),
    new Vector2f(p2.x, p2.y),
    new Vector2f(p3.x, p3.y)) == -1
}
