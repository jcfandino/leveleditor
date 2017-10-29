package com.stovokor.editor.model.repository

import com.stovokor.editor.model.SurfaceMaterial
import com.stovokor.editor.model.NullMaterial
import com.stovokor.editor.model.MissingMaterial

object MaterialRepository {
  val instance = new MaterialRepository()
  def apply() = instance
}

class MaterialRepository {

  var materials: List[SurfaceMaterial] = List()

  def add(material: SurfaceMaterial) = {
    materials = materials ++ List(material)
    materials.size - 1
  }

  def update(index: Int, material: SurfaceMaterial) = {
    materials = materials.updated(index, material)
    material
  }

  def remove(index: Int) = {
    update(index, NullMaterial())
  }

  def removeAll {
    materials = List()
  }

  def get(index: Int) = {
    if (materials.size > index) {
      materials(index)
    } else {
      //      MissingMaterial()
      NullMaterial()
    }
  }
}
