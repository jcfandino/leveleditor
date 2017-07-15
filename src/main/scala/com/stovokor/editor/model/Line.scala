package com.stovokor.editor.model

object Line {
  def apply(a: Point, b: Point) = new Line(a, b)
}
case class Line(val a: Point,val b: Point) {

  lazy val length = a distance b
}