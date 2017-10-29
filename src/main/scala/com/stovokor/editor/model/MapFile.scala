package com.stovokor.editor.model

case class MapFile(version: Int,
                   sectors: Map[Long, Sector],
                   borders: Map[Long, Border]) {
}