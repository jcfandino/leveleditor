package com.stovokor.editor.model

object Line {
  def apply(a: Point, b: Point) = new Line(a, b)
  def apply(ab: (Point, Point)) = new Line(ab._1, ab._2)
}
case class Line(val a: Point, val b: Point) {

  lazy val length = a distance b

  def split(factor: Float = 0.5f): (Line, Line) = {
    val point = Point(a.x + factor * (b.x - a.x), a.y + factor * (b.y - a.y))
    (Line(a, point), Line(point, b))
  }

  def reverse = Line(b, a)

  def isEnd(point: Point) = a == point || b == point

  def moveEnd(from: Point, to: Point) =
    if (a == from) Line(to, b)
    else if (b == from) Line(a, to)
    else this
}
