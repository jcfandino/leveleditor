package com.stovokor.editor.model

case class BoundBox(from: Point, to: Point) {

  def minX = from.x
  def maxX = to.x
  def minY = from.y
  def maxY = to.y
}