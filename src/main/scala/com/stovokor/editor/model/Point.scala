package com.stovokor.editor.model

import java.util.Objects

object Point {
  def apply(x: Float, y: Float) = new Point(x, y)
  def apply(x: Double, y: Double) = new Point(x.toFloat, y.toFloat)
}

case class Point(val x: Float, val y: Float) {

  def distance(other: Point) = Math.sqrt(Math.pow(other.x - x, 2) + Math.pow(other.y - y, 2)).toFloat
  def move(dx: Float, dy: Float) = Point(x + dx, y + dy)

  override lazy val hashCode = Objects.hash(x.asInstanceOf[Object], y.asInstanceOf[Object])

}