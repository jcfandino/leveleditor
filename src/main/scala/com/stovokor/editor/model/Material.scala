package com.stovokor.editor.model

abstract class Material(val path: String) {

}

case class SimpleMaterial(p: String) extends Material(p) {

}

case class NullMaterial() extends Material("") {

}