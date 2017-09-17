package com.stovokor.editor.model

import earcut4j.Earcut
import scala.collection.JavaConversions._

object Triangulator {

  def triangulate(polygon: Polygon, holes: Set[Polygon] = Set()): List[Triangle] = {
    // vertex coords in pairs of x,y flattened into a single array
    val polygonVerts = polygon.pointsSorted
      .flatMap(p => List(p.x, p.y))
      .map(_.toDouble)

    // vertex coords for the holes
    val holesVerts = holes
      .toList
      .flatMap(p => p.pointsSorted)
      .flatMap(p => List(p.x, p.y))
      .map(_.toDouble)

    // all vertices, first external, then holes
    val vertices = (polygonVerts ++ holesVerts).toArray

    // an array with one number per hole, each being the index in the array of points where that hole starts
    val holeIndices = holes
      .foldLeft(List[Int](polygon.pointsSorted.size))((l, h) => l ++ List(l.last + h.pointsSorted.size))
      .dropRight(1)
      .toArray

    def px(i: Int) = vertices(2 * i)
    def py(i: Int) = vertices(2 * i + 1)

    // the result is a list of indexes pointing to the original array
    val result = Earcut.earcut(vertices, holeIndices, 2)
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

}