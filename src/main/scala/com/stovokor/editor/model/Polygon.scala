package com.stovokor.editor.model

import earcut4j.Earcut
import scala.collection.JavaConversions._

object Polygon {
  def apply(points: List[Point]) = new Polygon(points)
}
class Polygon(val points: List[Point]) {

  lazy val lines = {
    ((points zip points.tail) map ((p) => Line(p._1, p._2))) ++ List(Line(points.head, points.last))
  }

  def triangulate: List[Triangle] = {
    val vertices = points
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
      .toList
    triangles
  }

}

object Triangle {
  def apply(p1: Point, p2: Point, p3: Point) = new Triangle(p1, p2, p3)
}

class Triangle(p1: Point, p2: Point, p3: Point) extends Polygon(List(p1, p2, p3)) {
  override def triangulate = List(this)
}