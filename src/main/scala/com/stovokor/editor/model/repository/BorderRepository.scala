package com.stovokor.editor.model.repository

import com.stovokor.editor.model.Sector
import java.util.concurrent.atomic.AtomicLong
import com.stovokor.editor.model.Border
import com.stovokor.editor.model.Line
import com.stovokor.editor.model.Point

object BorderRepository {
  val instance = new BorderRepository()
  def apply() = instance
}
class BorderRepository {
  var idGenerator = new AtomicLong(0)

  var borders: Map[Long, Border] = Map()

  def add(border: Border) = {
    val id = idGenerator.getAndIncrement
    borders = borders.updated(id, border)
    //    index.indexSector(id, border)
    id
  }

  def update(id: Long, border: Border) = {
    borders = borders.updated(id, border)
    //    index.indexSector(id, border)
    border // maybe should return old?
  }

  def remove(id: Long) = {
    val old = borders(id)
    borders = borders - id
    //    index.removeSector(id)
    old
  }

  def removeAll {
    borders = Map()
  }

  def get(id: Long) = {
    borders(id)
  }

  def findFrom(sectorId: Long): List[(Long, Border)] = {
    find((id, border) => border.sectorA == sectorId) // Only from sectorId
  }
  def findTo(sectorId: Long): List[(Long, Border)] = {
    find((id, border) => border.sectorB == sectorId) // Only to sectorId
  }

  def find(point: Point): List[(Long, Border)] = {
    find((id, border) => border.line.isEnd(point))
  }

  def find(line: Line): List[(Long, Border)] = {
    find((id, border) => border.line == line) //or should be border.line.alike(line))
  }

  def find(filterFunc: (Long, Border) => Boolean) = {
    borders.filter(pair => pair match { case (i, s) => filterFunc(i, s) }).toList
  }

}
