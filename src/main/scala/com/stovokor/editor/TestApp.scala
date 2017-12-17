package com.stovokor.editor

import java.io.File

import com.jme3.app.SimpleApplication
import com.jme3.asset.plugins.FileLocator
import com.jme3.system.AppSettings
import com.stovokor.editor.state.CanOpenDialog

import javax.swing.JFileChooser
import javax.swing.filechooser.FileFilter

object TestApp extends SimpleApplication
    with CanOpenDialog {

  def main(args: Array[String]) {
    val sets = new AppSettings(true)
    //    sets.setSettingsDialogImage("/Interface/logo.png")
    sets.setWidth(1024)
    sets.setHeight(768)
    sets.setGammaCorrection(false)
    sets.setTitle("M8 Test app")
    setSettings(sets)
    setDisplayFps(false)
    setDisplayStatView(false)

    TestApp.start
  }

  def simpleInitApp() = {
    val frame = getSwingFrame
    val fileChooser = new JFileChooser
    fileChooser.setFileFilter(new FileFilter() {
      def accept(file: File) = file.isDirectory() || file.getPath.endsWith(".j3o")
      def getDescription = ".j3o"
    })
    val result = fileChooser.showOpenDialog(frame)
    if (result == JFileChooser.APPROVE_OPTION) {
      val file = fileChooser.getSelectedFile

      assetManager.registerLocator(file.getParent, classOf[FileLocator])
      val loadedNode = assetManager.loadModel(file.getName)
      rootNode.attachChild(loadedNode)
      getFlyByCamera.setMoveSpeed(10f)
      getFlyByCamera.setRotationSpeed(2f)
    } else {
      stop()
    }
  }
}
