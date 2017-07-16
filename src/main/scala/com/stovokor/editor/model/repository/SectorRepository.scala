package com.stovokor.editor.model.repository

import com.stovokor.editor.model.Sector
import java.util.concurrent.atomic.AtomicLong

object SectorRepository {
  var instance = new SectorRepository()
  def apply() = instance
}

class SectorRepository {

  var idGenerator = new AtomicLong(0)

  var sectors: Map[Long, Sector] = Map()

  def add(sector: Sector) = {
    val id = idGenerator.getAndIncrement
    sectors = sectors.updated(id, sector)
    id
  }

  def update(id: Long, sector: Sector) = {
    sectors = sectors.updated(id, sector)
    sector // maybe should return old?
  }

  def remove(id: Long) = {
    val old = sectors(id)
    sectors = sectors - id
    old
  }

  def get(id: Long) = {
    sectors(id)
  }
}