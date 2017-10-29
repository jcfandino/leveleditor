package com.stovokor.editor.model.repository

import com.stovokor.editor.model.SurfaceMaterial
import com.stovokor.editor.model.NullMaterial
import com.stovokor.editor.model.Settings
import com.stovokor.util.JsonFiles
import java.io.File

object SettingsRepository {
  val instance = new SettingsRepository()
  def apply() = instance
}

// TODO Instead of json, use the com.typesafe:config lib and support defaults
class SettingsRepository {
  val path = System.getProperty("user.home") + "/.m8rc"
  var settings = {
    if (new File(path).exists()) {
      JsonFiles.load[Settings](path)
    } else {
      JsonFiles.save(path, Settings())
    }
  }

  def update(updated: Settings) = {
    settings = JsonFiles.save(path, updated)
  }

  def get() = settings
}
