package com.stovokor.editor.model.repository

import com.stovokor.editor.model.Polygon

class PolygonRepository {

  var polygons: Set[Polygon] = Set()

  def add(polygon: Polygon) {
    polygons = polygons + polygon
  }
}