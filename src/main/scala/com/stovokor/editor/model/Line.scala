package com.stovokor.editor.model

object Line {
  def apply(a: Point, b: Point) = new Line(a, b)
}
class Line(val a: Point,val b: Point) {

  lazy val length = a distance b
}