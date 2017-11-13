package com.stovokor.editor.factory

import com.jme3.asset.AssetManager
import com.jme3.math.ColorRGBA
import com.jme3.math.FastMath
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.scene.Spatial.CullHint
import com.jme3.scene.shape.Box
import com.jme3.scene.shape.Line
import com.stovokor.editor.control.ConstantSizeOnScreenControl
import com.stovokor.editor.control.DragControl
import com.stovokor.editor.control.SelectableControl
import com.stovokor.editor.gui.K
import com.stovokor.editor.gui.Mode2DLayers
import com.stovokor.editor.model.Point
import com.stovokor.editor.model.Sector

object Mesh2dFactory {
  def apply(assetManager: AssetManager) = new Mesh2dFactory(assetManager)
}

class Mesh2dFactory(val assetManager: AssetManager) extends MaterialFactoryClient {

  def createMesh(sector: Sector) = {
    val node = new Node("sector2d")
    sector.polygon.pointsSorted.foreach(p => draw2dVertex(node, p))
    sector.openWalls.foreach(w => draw2dLine(node, ColorRGBA.Brown.mult(2), w.line))
    sector.closedWalls.foreach(w => draw2dLine(node, ColorRGBA.LightGray, w.line))
    node
  }

  def draw2dVertex(node: Node, point: Point) {
    val vertex = new Node("point")
    // visual point
    val pointView = new Geometry("point", K.vertexBox)
    pointView.setMaterial(plainColor(ColorRGBA.LightGray))
    pointView.addControl(new SelectableControl(ColorRGBA.LightGray, Set(point)))
    // clickable point
    val clickableRadius = 0.2f
    val clickableVertex = new Geometry("clickablePoint", new Box(clickableRadius, clickableRadius, clickableRadius))
    clickableVertex.setMaterial(plainColor(ColorRGBA.DarkGray))
    clickableVertex.setCullHint(CullHint.Always)

    vertex.attachChild(pointView)
    vertex.attachChild(clickableVertex)
    vertex.setLocalTranslation(point.x, point.y, Mode2DLayers.vertices)
    vertex.addControl(new ConstantSizeOnScreenControl())
    vertex.addControl(new DragControl(point))
    node.attachChild(vertex)
  }

  def draw2dLine(node: Node, color: ColorRGBA, line: com.stovokor.editor.model.Line) {
    val lineNode = new Node("line")
    // a line
    val lineGeo = new Geometry("line", new Line(
      new Vector3f(line.a.x, line.a.y, Mode2DLayers.lines),
      new Vector3f(line.b.x, line.b.y, Mode2DLayers.lines)))
    lineGeo.setMaterial(plainColor(color))
    lineGeo.addControl(new SelectableControl(color, Set(line.a, line.b)))
    lineNode.attachChild(lineGeo)
    // a dot for selecting
    val lineBox = new Geometry("lineBox", K.lineBox)
    lineBox.setMaterial(plainColor(color))
    lineBox.addControl(new SelectableControl(color, Set(line.a, line.b)))
    lineBox.setLocalTranslation((line.b.x + line.a.x) / 2f, (line.b.y + line.a.y) / 2f, 0)
    lineBox.setLocalRotation(new Quaternion().fromAngleNormalAxis(FastMath.QUARTER_PI, Vector3f.UNIT_Z))
    lineBox.addControl(new ConstantSizeOnScreenControl())
    lineNode.attachChild(lineBox)

    node.attachChild(lineNode)
  }

}