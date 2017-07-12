package com.stovokor.editor.model

object PolygonBuilder {
  def start(p: Point) = new PolygonBuilder(List(p))
}

class PolygonBuilder(val points: List[Point]) {

  def first = points.head
  def last = points.last

  def size = points.size

  def lines = (points zip points.tail) map ((p) => Line(p._1, p._2))

  def add(p: Point) = new PolygonBuilder(points ++ List(p))

  def build() = Polygon(points)
}