package com.stovokor.editor.model

abstract class SurfaceMaterial(val path: String) {

}

case class SimpleMaterial(p: String) extends SurfaceMaterial(p) {

}

case class MatDefMaterial(p: String) extends SurfaceMaterial(p) {

}

case class NullMaterial() extends SurfaceMaterial("") {

}
