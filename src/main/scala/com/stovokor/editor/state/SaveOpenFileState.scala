package com.stovokor.editor.state

import com.stovokor.editor.model.repository.SectorRepository
import com.jme3.app.state.AppStateManager
import com.stovokor.util.PointClicked
import com.stovokor.editor.input.InputFunction
import com.stovokor.editor.model.repository.BorderRepository
import com.jme3.scene.Node
import com.stovokor.util.EventBus
import com.jme3.app.Application
import com.stovokor.util.EditorEventListener
import com.simsilica.lemur.input.StateFunctionListener
import com.stovokor.util.SaveMap
import com.stovokor.util.EditorEvent
import com.simsilica.lemur.input.FunctionId
import com.simsilica.lemur.input.InputState
import java.io.FileWriter
import java.io.BufferedWriter
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JFrame
import com.stovokor.util.OpenMap
import com.stovokor.editor.model.Sector
import com.stovokor.editor.model.Border
import javax.swing.UIManager
import javax.swing.JOptionPane
import javax.swing.filechooser.FileFilter
import java.io.FileReader
import java.io.BufferedReader
import scala.io.Source
import com.stovokor.util.SectorDeleted
import com.stovokor.util.SectorUpdated
import com.stovokor.util.JsonFiles
import com.stovokor.editor.model.MapFile
import com.stovokor.util.StartNewMap
import com.stovokor.util.ExitApplication

class SaveOpenFileState extends BaseState
    with EditorEventListener
    with CanMapInput
    with StateFunctionListener {

  val sectorRepository = SectorRepository()
  val borderRepository = BorderRepository()

  var currentFile: Option[String] = None
  var dirty = false

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    EventBus.subscribeByType(this, classOf[SaveMap])
    EventBus.subscribeByType(this, classOf[OpenMap])
    EventBus.subscribeByType(this, classOf[SectorUpdated])
    EventBus.subscribeByType(this, classOf[SectorDeleted])
    EventBus.subscribe(this, StartNewMap())
    EventBus.subscribe(this, ExitApplication())
    setupInput

    // Auto Load test map
    val file = new File(System.getProperty("user.home") + "/test.m8")
    if (file.exists()) {
      openFile(file)
    }
  }

  override def cleanup() {
    super.cleanup
    EventBus.removeFromAll(this)
    inputMapper.removeStateListener(this, InputFunction.newFile)
    inputMapper.removeStateListener(this, InputFunction.open)
    inputMapper.removeStateListener(this, InputFunction.save)
    inputMapper.removeStateListener(this, InputFunction.saveAs)
    inputMapper.removeStateListener(this, InputFunction.exit)
  }

  def setupInput {
    inputMapper.addStateListener(this, InputFunction.newFile)
    inputMapper.addStateListener(this, InputFunction.open)
    inputMapper.addStateListener(this, InputFunction.save)
    inputMapper.addStateListener(this, InputFunction.saveAs)
    inputMapper.addStateListener(this, InputFunction.exit)
    inputMapper.activateGroup(InputFunction.files)
  }

  def onEvent(event: EditorEvent) = event match {
    case StartNewMap()          => startNewMap()
    case OpenMap()              => openFile()
    case SaveMap(false)         => saveAsNewFile()
    case SaveMap(true)          => saveCurrent()
    case ExitApplication()      => exitApp()
    case SectorUpdated(_, _, _) => dirty = true
    case SectorDeleted(_)       => dirty = true
    case _                      =>
  }

  def exitApp() = if (confirmAction("exit editor")) {
    app.stop()
  }

  def valueChanged(func: FunctionId, value: InputState, tpf: Double) {
    if (value == InputState.Positive) func match {
      case InputFunction.newFile => startNewMap()
      case InputFunction.open    => openFile()
      case InputFunction.save    => saveCurrent()
      case InputFunction.saveAs  => saveAsNewFile()
      case InputFunction.exit    => exitApp()
      case _                     =>
    }
  }

  def startNewMap() {
    if (confirmAction("start new map")) {
      for ((id, sec) <- SectorRepository().sectors) {
        EventBus.trigger(SectorDeleted(id))
      }
      SectorRepository().removeAll
      BorderRepository().removeAll
      currentFile = None
      dirty = false
    }
  }

  def openFile() {
    if (confirmAction("open file")) {
      val fileChooser = new JFileChooser
      fileChooser.setFileFilter(new FileFilter() {
        def accept(file: File) = file.isDirectory() || file.getPath.endsWith(".m8")
        def getDescription = "M8 Editor Maps (*.m8)"
      })
      val result = fileChooser.showOpenDialog(null)
      if (result == JFileChooser.APPROVE_OPTION) {
        val file = fileChooser.getSelectedFile
        openFile(file)
      }
    }
  }

  def confirmAction(action: String) = {
    !dirty || JOptionPane.showConfirmDialog(null,
      s"You will lose your progress, $action anyway?",
      action.capitalize, JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION
  }

  def openFile(file: File) {
    println(s"opening file: $file")
    val map: MapFile = JsonFiles.load[MapFile](file.getAbsolutePath)
    currentFile = Some(file.getAbsolutePath)
    // clean and load
    for ((id, sec) <- SectorRepository().sectors) {
      EventBus.trigger(SectorDeleted(id))
    }
    SectorRepository().removeAll
    BorderRepository().removeAll
    val newIds = map.sectors.map(e => e match {
      case (id, sector) => {
        val newId = SectorRepository().add(sector)
        (id, newId)
      }
    })
    map.borders.map(e => e match {
      case (id, border) => {
        val updated = border.updateSectors(
          newIds(border.sectorA),
          newIds(border.sectorB))
        BorderRepository().add(updated)
      }
    })
    SectorRepository().sectors
      .foreach(entry => EventBus.trigger(SectorUpdated(entry._1, entry._2, true)))
    dirty = false
  }

  def saveAsNewFile() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    val fileChooser = new JFileChooser
    fileChooser.setFileFilter(new FileFilter() {
      def accept(file: File) = file.isDirectory() || file.getPath.endsWith(".m8")
      def getDescription = "M8 Editor Maps (*.m8)"
    })
    val result = fileChooser.showSaveDialog(null)
    if (result == JFileChooser.APPROVE_OPTION) {
      currentFile = Some(fileChooser.getSelectedFile.getAbsolutePath)
      saveCurrent()
    }
  }

  def saveCurrent() {
    if (currentFile.isEmpty) {
      saveAsNewFile()
    } else {
      val map = MapFile(1, SectorRepository().sectors, BorderRepository().borders)
      JsonFiles.save(currentFile.get, map)
    }
    dirty = false
  }

}