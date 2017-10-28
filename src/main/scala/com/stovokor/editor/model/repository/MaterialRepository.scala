package com.stovokor.editor.model.repository

import com.stovokor.editor.model.Material
import com.stovokor.editor.model.NullMaterial

object MaterialRepository {
  val instance = new MaterialRepository()
  def apply() = instance
}

class MaterialRepository {

  var materials: List[Material] = List()

  def add(material: Material) = {
    materials = materials ++ List(material)
    materials.size - 1
  }

  def update(index: Int, material: Material) = {
    materials = materials.updated(index, material)
    material
  }

  def remove(index: Int) = {
    update(index, NullMaterial())
  }

  def removeAll {
    materials = List()
  }

  def get(index: Int) = materials(index)
}
