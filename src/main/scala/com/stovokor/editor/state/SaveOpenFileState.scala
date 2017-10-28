package com.stovokor.editor.state

import com.stovokor.editor.model.repository.SectorRepository
import com.jme3.app.state.AppStateManager
import com.stovokor.util.PointClicked
import com.stovokor.editor.input.InputFunction
import com.stovokor.editor.model.repository.BorderRepository
import com.stovokor.util.ViewModeSwitch
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

class SaveOpenFileState extends BaseState
    with EditorEventListener
    with CanMapInput
    with CanOpenDialog
    with StateFunctionListener {

  val sectorRepository = SectorRepository()
  val borderRepository = BorderRepository()

  var currentFile: Option[String] = None

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    EventBus.subscribeByType(this, classOf[SaveMap])
    EventBus.subscribeByType(this, classOf[OpenMap])
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
    inputMapper.removeStateListener(this, InputFunction.open)
    inputMapper.removeStateListener(this, InputFunction.save)
    inputMapper.removeStateListener(this, InputFunction.saveAs)
  }

  def setupInput {
    inputMapper.addStateListener(this, InputFunction.open)
    inputMapper.addStateListener(this, InputFunction.save)
    inputMapper.addStateListener(this, InputFunction.saveAs)
    inputMapper.activateGroup(InputFunction.files)
  }

  def onEvent(event: EditorEvent) = event match {
    case SaveMap(false) => saveAsNewFile()
    case SaveMap(true)  => saveCurrent()
    case OpenMap()      => openFile()
    case _              =>
  }

  def valueChanged(func: FunctionId, value: InputState, tpf: Double) {
    func match {
      case InputFunction.open   => EventBus.trigger(OpenMap())
      case InputFunction.save   => EventBus.trigger(SaveMap(true))
      case InputFunction.saveAs => EventBus.trigger(SaveMap(false))
      case _                    =>
    }
  }

  def openFile() {
    // TODO Check unsaved state will be lost
    // JOptionPane.showConfirmDialog(frame, "You will lose your progress, etc..")
    val frame = createFrame
    val fileChooser = new JFileChooser
    fileChooser.setFileFilter(new FileFilter() {
      def accept(file: File) = file.isDirectory() || file.getPath.endsWith(".m8")
      def getDescription = "M8 Editor Maps (*.m8)"
    })
    val result = fileChooser.showOpenDialog(frame)
    if (result == JFileChooser.APPROVE_OPTION) {
      val file = fileChooser.getSelectedFile
      openFile(file)
    }
    frame.dispose()
  }

  def openFile(file: File) {
    println(s"opening file: $file")
    val map = JsonFiles.load(file.getAbsolutePath)
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
      .foreach(entry => EventBus.trigger(SectorUpdated(entry._1, entry._2)))
  }

  def saveAsNewFile() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    val frame = createFrame
    val fileChooser = new JFileChooser
    fileChooser.setFileFilter(new FileFilter() {
      def accept(file: File) = file.isDirectory() || file.getPath.endsWith(".m8")
      def getDescription = "M8 Editor Maps (*.m8)"
    })
    val result = fileChooser.showSaveDialog(frame)
    if (result == JFileChooser.APPROVE_OPTION) {
      currentFile = Some(fileChooser.getSelectedFile.getAbsolutePath)
      saveCurrent()
    }
    frame.dispose()
  }

  def saveCurrent() {
    if (currentFile.isEmpty) {
      saveAsNewFile()
    } else {
      val map = MapFile(1, SectorRepository().sectors, BorderRepository().borders)
      JsonFiles.save(currentFile.get, map)
    }
  }

  object JsonFiles {
    import org.json4s._
    import org.json4s.jackson.Serialization
    import org.json4s.jackson.Serialization.{ read, write }

    implicit val formats = Serialization.formats(NoTypeHints)

    def save(path: String, map: MapFile) {
      val json = write(map)
      val file = new File(path)
      println(s"Saving ${file.getAbsolutePath}")
      val bw = new BufferedWriter(new FileWriter(file))
      bw.write(json)
      bw.close()
    }

    def load(path: String) = {
      val file = new File(path)
      val json = Source.fromFile(file).mkString
      read[MapFile](json)
    }
  }

  case class MapFile(version: Int,
                     sectors: Map[Long, Sector],
                     borders: Map[Long, Border]) {
  }

}