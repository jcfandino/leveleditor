package com.stovokor.editor.model

import earcut4j.Earcut
import scala.collection.JavaConversions._
import com.jme3.math.FastMath
import com.jme3.math.Vector2f

object Polygon {
  def apply(points: List[Point]) = new Polygon(points)
}
case class Polygon(val pointsUnsorted: List[Point]) {

  lazy val pointsSorted = {
    if (isClockwise) pointsUnsorted
    else pointsUnsorted.reverse
  }

  lazy val lines = {
    ((pointsSorted zip pointsSorted.tail) map ((p) => Line(p._1, p._2))) ++ List(Line(pointsSorted.last, pointsSorted.head))
  }

  def triangulate: List[Triangle] = {
    val vertices = pointsSorted
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
      .map(_.asClockwise)
      .toList
    triangles
  }

  private val limY = 10000000f

  def isClockwise = {
    def areaBelow(a: Point, b: Point) = 0.5f * (b.x - a.x) * (b.y + a.y + 2 * limY)
    val sorted = pointsUnsorted.sortBy(_.x)
    val leftmost = sorted.head
    val rightmost = sorted.last
    val (path1, path2) = findPathsBetween(leftmost, rightmost)
    val area1 = path1.sliding(2).map(ab => areaBelow(ab(0), ab(1))).sum
    val area2 = path2.sliding(2).map(ab => areaBelow(ab(0), ab(1))).sum
    val diff = area1 - area2
    diff > 0 && diff < limY
  }

  def findPathsBetween(points: List[Point], p1: Point, p2: Point) = {
    val i1 = points.indexOf(p1)
    val i2 = points.indexOf(p2)
    val path1 =
      if (i1 < i2) points.slice(i1, i2 + 1)
      else points.slice(i1, points.size) ++ points.slice(0, i2 + 1)
    val path2 = (
      if (i1 > i2) points.slice(i2, i1 + 1)
      else points.slice(i2, points.size) ++ points.slice(0, i1 + 1)).reverse
    (path1, path2)
  }
  /**
   * path1 = p1->p2 Can be counter clockwise or clockwise
   * path2 = p2->p1 The second one is the way back
   */
  def findPathsBetween(p1: Point, p2: Point): (List[Point], List[Point]) =
    findPathsBetween(pointsUnsorted, p1, p2)

  /**
   * Same but with sorted points in clockwise direction.
   * Paths start and end in the same points.
   */
  def findPathsBetweenSorted(p1: Point, p2: Point): (List[Point], List[Point]) =
    findPathsBetween(pointsSorted, p1, p2)

  def changePoint(from: Point, to: Point): Polygon = {
    val idx = pointsSorted.indexOf(from)
    if (idx >= 0) Polygon(pointsSorted.updated(idx, to))
    else this
  }

  def addPoint(between: Line, factor: Float): Polygon = {
    println(s"Splitting line $between")
    val idxa = pointsSorted.indexOf(between.a)
    val idxb = pointsSorted.indexOf(between.b)
    val (idx1, idx2) = (Math.min(idxa, idxb), Math.max(idxa, idxb))
    val newPoint = between.split(factor)._1.b
    if (idx1 == 0 && idx2 == pointsSorted.size - 1)
      Polygon(pointsSorted ++ List(newPoint))
    else
      Polygon(
        (pointsSorted.slice(0, idx1 + 1) ++
          List(newPoint)) ++
          pointsSorted.slice(idx2, pointsSorted.size))
  }

  def borderWith(other: Polygon): List[Line] = {
    println(s"finding border")
    val otherLines = other.lines
    val inter = lines.filter(l => otherLines.contains(l) || otherLines.contains(l.reverse))
    println(s"result: $inter")
    inter
  }

  // if the point is right in the border, the result in undetermined!
  def contains(point: Point) = {
    // https://wrf.ecse.rpi.edu//Research/Short_Notes/pnpoly.html
    lines
      .map(line => (line.a.y > point.y) != (line.b.y > point.y) &&
        (point.x < (line.b.x - line.a.x) * (point.y - line.a.y) /
          (line.b.y - line.a.y) + line.a.x))
      .count(identity) % 2 == 1
  }

  def innerPoints(ps: List[Point]) = if (ps.length < 3) List() else ps.slice(1, ps.length - 1)
  def cutInside(cut: List[Point]) = cut.size == 2 || innerPoints(cut).find(contains).isDefined

  def sharedLines(other: Polygon): List[Line] = {
    val otherLines = (other.lines.flatMap(_.andReverse)).toSet
    lines.filter(otherLines.contains)
  }

  def divideBy(cut: List[Point]): List[Polygon] = {
    def createPolygons(path1: List[Point], path2: List[Point], cut: List[Point]) = {
      List(Polygon(path1 ++ innerPoints(cut.reverse)),
        Polygon(innerPoints(cut.reverse) ++ path2)) // I don't understand why reverse here
    }
    def extend(path1: List[Point], path2: List[Point], cut: List[Point]): List[Polygon] = {
      // assume the cut is outside and goes in path1 direction
      val candidate = Polygon(path2 ++ innerPoints(cut.reverse))
      //      println(s"---> Candidate is $candidate")
      if (candidate.cutInside(path1)) { // candidate was wrong
        //        println(s"candidate rejected")
        List(Polygon(path1 ++ innerPoints(cut.reverse)), this)
      } else {
        //        println(s"candidate accepted")
        List(candidate, this)
      }
    }
    if (cut.size < 2 ||
      !pointsUnsorted.contains(cut.head) ||
      !pointsUnsorted.contains(cut.last) ||
      cut.sliding(2).map(s => Line(s(0), s(1))).forall(lines.contains)) {
      println(s"Cannot divide polygon, cutting line is border")
      List(this)
    }
    val isInside = cutInside(cut)
    println(s"Cutted inside $isInside")
    val (path1, path2) = findPathsBetweenSorted(cut.head, cut.last)
    val cutSorted = if (path1.head == cut.head) cut else cut.reverse // same dir as path1
    val polys = if (isInside) {
      createPolygons(path1, path2, cutSorted)
    } else {
      extend(path1, path2, cutSorted)
    }
    polys
  }
}

object Triangle {
  def apply(p1: Point, p2: Point, p3: Point) = new Triangle(p1, p2, p3)
}

class Triangle(val p1: Point, val p2: Point, val p3: Point) extends Polygon(List(p1, p2, p3)) {
  override def triangulate = List(this)

  def asClockwise = if (isClockwise) this else reverse
  def asCounterClockwise = if (!isClockwise) this else reverse
  def reverse = Triangle(p3, p2, p1)

  override def isClockwise = FastMath.counterClockwise(
    new Vector2f(p1.x, p1.y),
    new Vector2f(p2.x, p2.y),
    new Vector2f(p3.x, p3.y)) == -1
}
