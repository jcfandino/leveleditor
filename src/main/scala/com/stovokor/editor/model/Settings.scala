package com.stovokor.editor.model

object Settings {
  def apply() = new Settings(System.getProperty("user.dir"))
}

case class Settings(
    val assetsBasePath: String) {

  def updateAssetBasePath(path: String) = Settings(path)
}