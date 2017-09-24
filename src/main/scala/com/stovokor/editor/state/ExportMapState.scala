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
import com.stovokor.util.ExportMap
import com.jme3.export.binary.BinaryExporter
import com.stovokor.editor.factory.MeshFactory
import com.stovokor.util.JmeExtensions._
import com.stovokor.editor.control.HighlightControl

class ExportMapState extends BaseState
    with EditorEventListener
    with CanMapInput
    with CanOpenDialog
    with StateFunctionListener {

  val sectorRepository = SectorRepository()
  val borderRepository = BorderRepository()

  var currentFile: Option[String] = None

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    EventBus.subscribe(this, ExportMap())
    setupInput
  }

  override def cleanup() {
    super.cleanup
    EventBus.removeFromAll(this)
    inputMapper.removeStateListener(this, InputFunction.export)
  }

  def setupInput {
    inputMapper.addStateListener(this, InputFunction.export)
    inputMapper.activateGroup(InputFunction.files)
  }

  def onEvent(event: EditorEvent) = event match {
    case ExportMap() => exportAsJ3o()
    case _           =>
  }

  def valueChanged(func: FunctionId, value: InputState, tpf: Double) {
    func match {
      case InputFunction.open => EventBus.trigger(ExportMap())
      case _                  =>
    }
  }

  def exportAsJ3o() {
    // Create a Lemur window with options for the export
    val frame = createFrame
    val fileChooser = new JFileChooser
    fileChooser.setFileFilter(new FileFilter() {
      def accept(file: File) = file.isDirectory() || file.getPath.endsWith(".j3o")
      def getDescription = "jMonkeyEngine3 (*.j3o)"
    })
    if (currentFile.isDefined) {
      fileChooser.setSelectedFile(new File(currentFile.get))
    }
    val result = fileChooser.showSaveDialog(frame)
    if (result == JFileChooser.APPROVE_OPTION) {
      val selected = fileChooser.getSelectedFile.getAbsolutePath
      currentFile = Some(if (selected.endsWith(".j3o")) selected else selected + ".j3o")
      JMEMapExporter.export(currentFile.get)
    }
    frame.dispose()
  }

  trait MapExporter {
    def export(path: String)
  }
  object JMEMapExporter extends MapExporter {
    def export(path: String) {
      val file = new File(path)
      println(s"Exporting to ${file.getAbsolutePath}")
      val exporter = BinaryExporter.getInstance();
      val root = buildNode
      exporter.save(root, file);
    }

    def buildNode = {
      val root = new Node("m8")
      SectorRepository().sectors.foreach(p => p match {
        case (id, sector) => {
          val node = getOrCreateNode(root, "sector-" + id)
          val borders = borderRepository.findFrom(id).map(_._2)
          val meshNode = MeshFactory(assetManager).createMesh(id, sector, borders)
          // TODO improve this, the mesh factory should not add controls in this case.
          meshNode.depthFirst(s => s.removeControl(classOf[HighlightControl]))
          node.attachChild(meshNode)
        }
      })
      root
    }
  }
}