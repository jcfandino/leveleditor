package com.stovokor.editor.state

import com.simsilica.lemur.input.StateFunctionListener
import com.stovokor.util.EditorEventListener
import com.stovokor.util.EditorEvent
import com.jme3.app.state.AppStateManager
import com.stovokor.editor.input.InputFunction
import com.stovokor.util.ExportMap
import com.stovokor.util.EventBus
import com.jme3.app.Application
import com.stovokor.util.ChangeMaterial
import com.stovokor.editor.model.repository.BorderRepository
import com.stovokor.editor.model.repository.SectorRepository
import com.stovokor.util.SectorSurfaceMutator
import com.stovokor.editor.gui.GuiFactory
import com.simsilica.lemur.Container
import com.stovokor.editor.model.repository.Repositories
import com.stovokor.editor.model.SimpleMaterial
import com.stovokor.editor.model.SurfaceMaterial
import com.stovokor.editor.model.MatDefMaterial
import com.stovokor.util.EditSettings
import com.stovokor.editor.model.Settings
import com.stovokor.util.SettingsUpdated
import java.io.File
import java.io.FileFilter
import com.jme3.asset.plugins.FileLocator

class SettingsLoaderState extends BaseState with EditorEventListener {

  val settingsRepository = Repositories.settingsRepository
  val materialRepository = Repositories.materialRepository

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    EventBus.subscribe(this, SettingsUpdated())
    reloadSettings()
  }

  override def cleanup() {
    super.cleanup
    EventBus.removeFromAll(this)
  }

  def onEvent(event: EditorEvent) = event match {
    case SettingsUpdated() => reloadSettings()
    case _                 =>
  }

  def reloadSettings() {
    val settings = settingsRepository.get()
    reloadMaterials(settings.assetsBasePath)
  }

  def reloadMaterials(path: String) {
    val dir = new File(path)
    if (dir.exists() && dir.isDirectory()) {
      assetManager.registerLocator(dir.getPath, classOf[FileLocator])
      loadSimpleTextures(dir)
      loadMaterialDefinitions(dir)
    } else {
      println(s"Error: cannot load settings $path")
    }
  }

  def loadSimpleTextures(dir: File) {
    // TODO accept more file types
    val extensions = Set(".png", ".jpg", ".bmp")
    def isImage(file: File) = {
      file.isFile() && extensions.find(file.getName.endsWith).isDefined
    }
    val texturesDir = new File(dir, "Textures")
    if (texturesDir.exists() && dir.isDirectory()) {
      texturesDir.listFiles(isImage(_)).foreach(file => {
        val relPath = dir.toPath.relativize(file.toPath)
        materialRepository.add(SimpleMaterial(relPath.toString))
      })
    }
  }

  def loadMaterialDefinitions(dir: File) {
    def isMatDef(file: File) = file.isFile() && file.getName.endsWith(".j3m")
    val texturesDir = new File(dir, "Materials")
    if (texturesDir.exists() && dir.isDirectory()) {
      texturesDir.listFiles(isMatDef(_)).foreach(file => {
        val relPath = dir.toPath.relativize(file.toPath)
        materialRepository.add(MatDefMaterial(relPath.toString))
      })
    }
  }
}
