package com.stovokor.editor.model.creation

import com.stovokor.editor.model.Sector
import com.stovokor.editor.model.Sector
import com.stovokor.editor.model.Line
import com.stovokor.editor.model.Border
import com.stovokor.editor.model.Surface
import com.stovokor.editor.model.SurfaceTexture

object BorderFactory {

  def createBorders(fromId: Long, from: Sector, borders: List[(Long, Sector, List[Line])]): List[Border] = {
    borders.flatMap(bor => bor match {
      case (otherId, other, lines) => {
        createBorders(fromId, from, otherId, other, lines)
      }
    })
  }

  def createBorder(fromId: Long, from: Sector, otherId: Long, other: Sector, line: Line) = {
    createBorders(fromId, from, otherId, other, List(line)).head
  }

  def createBorders(fromId: Long, from: Sector, otherId: Long, other: Sector, lines: List[Line]): List[Border] = {
    lines.flatMap(line => {
      val textureAB = findTexture(from, line)
      val textureBA = findTexture(other, line)
      println(s"creating border $fromId -> $otherId = $line")
      List(
        Border(fromId, otherId, line,
          Surface(0f, textureAB), Surface(0f, textureAB))
          .updateHeights(from, other),
        Border(otherId, fromId, line.reverse,
          Surface(0f, textureBA), Surface(0f, textureBA))
          .updateHeights(other, from))
    })
  }

  def findTexture(sector: Sector, line: Line) = {
    val found = sector.openWalls
      .find(w => w.line == line || w.line.reverse == line)
      .map(_.texture)
    if (found.isDefined) found.get
    else SurfaceTexture()
  }
}