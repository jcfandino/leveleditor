package com.stovokor.editor.model

import earcut4j.Earcut
import scala.collection.JavaConversions._
import com.jme3.math.FastMath
import com.jme3.math.Vector2f

object Polygon {
  def apply(points: List[Point]) = new Polygon(points)
}
case class Polygon(pointsUnsorted: List[Point]) {

  lazy val points = {
    if (isSortedClockWise) pointsUnsorted
    else pointsUnsorted.reverse
  }

  lazy val lines = {
    ((points zip points.tail) map ((p) => Line(p._1, p._2))) ++ List(Line(points.last, points.head))
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

  val limY = 10000000f

  def isSortedClockWise = {
    //    println(s"> points pointsUnsorted")
    val sorted = pointsUnsorted.sortBy(_.x) //TODO fix same x coor
    val leftmost = sorted.head
    val rightmost = sorted.last
    //    println(s"> leftmost $leftmost")
    //    println(s"> rightmost $rightmost")
    val i1 = pointsUnsorted.indexOf(leftmost)
    val i2 = pointsUnsorted.indexOf(rightmost)
    val path1 =
      if (i1 < i2) pointsUnsorted.slice(i1, i2 + 1)
      else pointsUnsorted.slice(i1, pointsUnsorted.size) ++ pointsUnsorted.slice(0, i2 + 1)
    val path2 = (
      if (i1 > i2) pointsUnsorted.slice(i2, i1 + 1)
      else pointsUnsorted.slice(i2, pointsUnsorted.size) ++ pointsUnsorted.slice(0, i1 + 1)).reverse
    //    println(s"> path1 $path1")
    //    println(s"> path2 $path2")
    val area1 = path1.sliding(2).map(ab => {
      val (a, b) = (ab(0), ab(1))
      0.5f * (b.x - a.x) * (b.y + a.y + 2 * limY)
    }).sum
    val area2 = path2.sliding(2).map(ab => {
      val (a, b) = (ab(0), ab(1))
      0.5f * (b.x - a.x) * (b.y + a.y + 2 * limY)
      //      0.5f * (b.x - a.x) * (2 * limY - a.y - b.y)
    }).sum
    //    println(s"> area1 $area1")
    //    println(s"> area2 $area2")
    //    println(s"> diff  ${area1 - area2}")
    val diff = area1 - area2
    diff > 0 && diff < limY
  }
}

object Triangle {
  def apply(p1: Point, p2: Point, p3: Point) = new Triangle(p1, p2, p3)
}

class Triangle(val p1: Point, val p2: Point, val p3: Point) extends Polygon(List(p1, p2, p3)) {
  override def triangulate = List(this)

  def reverse = Triangle(p1, p3, p2)
}