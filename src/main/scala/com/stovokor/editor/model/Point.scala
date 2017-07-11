package com.stovokor.editor.model

object Point {
  def apply(x: Float, y: Float) = new Point(x, y)
}

class Point(val x: Float, val y: Float) {

  def distance(other: Point) = Math.sqrt(Math.pow(other.x - x, 2) + Math.pow(other.y - y, 2)).toFloat

}