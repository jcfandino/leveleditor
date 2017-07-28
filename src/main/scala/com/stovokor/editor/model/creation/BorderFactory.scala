package com.stovokor.editor.model.creation

import com.stovokor.editor.model.Sector
import com.stovokor.editor.model.Sector
import com.stovokor.editor.model.Line
import com.stovokor.editor.model.Border
import com.stovokor.editor.model.Surface
import com.stovokor.editor.model.SurfaceTexture

object BorderFactory {

  def createBorders(fromId: Long, from: Sector, borders: List[(Long, Sector, List[Line])]) = {
    borders.flatMap(bor => bor match {
      case (otherId, other, lines) => {
        val lowHeightAB = Math.max(0f, other.floor.height - from.floor.height)
        val highHeightAB = Math.max(0f, from.ceiling.height - other.ceiling.height)
        // these are negative
        val lowHeightBA = -Math.max(0f, from.floor.height - other.floor.height)
        val highHeightBA = -Math.max(0f, other.ceiling.height - from.ceiling.height)
        lines.flatMap(line => {
          val textureAB = findTexture(from, line)
          val textureBA = findTexture(other, line)
          println(s"creating border $fromId -> $otherId = $line")
          List(
            Border(fromId, otherId, line,
              Surface(lowHeightAB, textureAB), Surface(highHeightAB, textureAB)),
            Border(fromId, otherId, line.reverse,
              Surface(lowHeightBA, textureBA), Surface(highHeightBA, textureBA)))
        })
      }
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