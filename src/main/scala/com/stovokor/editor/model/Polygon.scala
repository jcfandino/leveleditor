package com.stovokor.editor.model

object Polygon {
  def apply(points: List[Point]) = new Polygon(points)
}
class Polygon(val points: List[Point]) {

  lazy val lines = {
    ((points zip points.tail) map ((p) => Line(p._1, p._2))) ++ List(Line(points.head, points.last))
  }
}