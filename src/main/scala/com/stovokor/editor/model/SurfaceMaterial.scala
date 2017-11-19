package com.stovokor.editor.model

import java.util.Objects

abstract class SurfaceMaterial(val path: String) {

  override lazy val hashCode = Objects.hash(path)

}

case class SimpleMaterial(p: String) extends SurfaceMaterial(p) {

}

case class MatDefMaterial(p: String) extends SurfaceMaterial(p) {

}

case class MissingMaterial() extends SurfaceMaterial("") {

}

case class NullMaterial() extends SurfaceMaterial("") {

}
