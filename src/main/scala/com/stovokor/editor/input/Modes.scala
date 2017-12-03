package com.stovokor.editor.input

object Modes {

  sealed case class SelectionMode(mode: Int)
  object SelectionMode {
    val None = SelectionMode(0)
    val Point = SelectionMode(1)
    val Line = SelectionMode(2)
    val Sector = SelectionMode(3)
  }

  sealed case class EditMode(mode: Int)
  object EditMode {
    val Select = EditMode(0)
    val Draw = EditMode(1)
    val Fill = EditMode(3)
  }
}