package com.stovokor.editor.factory

import com.jme3.asset.AssetManager
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
import com.stovokor.editor.control.LineDragControl
import com.stovokor.editor.control.PointDragControl
import com.stovokor.editor.control.SectorDragControl
import com.stovokor.editor.model.SelectionPoint
import com.stovokor.editor.model.SelectionLine
import com.stovokor.editor.model.SelectionSector
import com.jme3.math.Vector2f
import com.stovokor.editor.gui.Palette
import com.jme3.math.ColorRGBA

object Mesh2dFactory {
  def apply(assetManager: AssetManager) = new Mesh2dFactory(assetManager)
}

class Mesh2dFactory(val assetManager: AssetManager) extends MaterialFactoryClient {

  def createMesh(sectorId: Long, sector: Sector) = {
    val node = new Node("sector2d")
    sector.polygon.pointsSorted.foreach(p => draw2dVertex(node, p))
    sector.holes.flatMap(_.pointsSorted).foreach(p => draw2dVertex(node, p))
    sector.openWalls.filter(w => ascending(w.line)).foreach(w => draw2dLine(node, Palette.lineOpenWall, w.line))
    sector.closedWalls.foreach(w => draw2dLine(node, Palette.lineClosedWall, w.line))
    draw2dSector(node, sectorId: Long, sector)
    node
  }

  def ascending(line: com.stovokor.editor.model.Line) = {
    line.b.x > line.a.x || line.b.y > line.a.y
  }

  def draw2dVertex(node: Node, point: Point) {
    val vertex = new Node("point")
    // visual point
    val pointView = new Geometry("point", K.vertexBox)
    pointView.setMaterial(plainColor(Palette.vertex))
    pointView.addControl(new SelectableControl(Palette.vertex, SelectionPoint(point)))
    // clickable point
    val clickableRadius = 0.2f
    val clickableVertex = new Geometry("clickablePoint", new Box(clickableRadius, clickableRadius, clickableRadius))
    clickableVertex.setMaterial(plainColor(Palette.vertex))
    clickableVertex.setCullHint(CullHint.Always)

    vertex.attachChild(pointView)
    vertex.attachChild(clickableVertex)
    vertex.setLocalTranslation(point.x, point.y, Mode2DLayers.vertices)
    vertex.addControl(new ConstantSizeOnScreenControl())
    vertex.addControl(new PointDragControl(point))
    node.attachChild(vertex)
  }

  def draw2dLine(node: Node, color: ColorRGBA, line: com.stovokor.editor.model.Line) {
    val center = new Vector3f((line.b.x + line.a.x) / 2f, (line.b.y + line.a.y) / 2f, 0)
    val lineNode = new Node("line")
    // a line
    val lineGeo = new Geometry("line", new Line(
      new Vector3f(line.a.x, line.a.y, 0),
      new Vector3f(line.b.x, line.b.y, 0)))
    lineGeo.setMaterial(plainColor(color))
    lineGeo.addControl(new SelectableControl(color, SelectionLine(line)))
    lineNode.attachChild(lineGeo)
    // a dot for selecting
    val lineBox = new Geometry("lineBox", K.lineBox)
    lineBox.setMaterial(plainColor(color))
    lineBox.addControl(new SelectableControl(color, SelectionLine(line)))
    val ang = FastMath.atan((line.b.y - line.a.y) / (line.b.x - line.a.x)) + FastMath.QUARTER_PI
    lineBox.setLocalRotation(new Quaternion().fromAngleNormalAxis(ang, Vector3f.UNIT_Z))
    lineBox.addControl(new ConstantSizeOnScreenControl())
    // clickable control
    val clickableLine = new Geometry("clickableLine", new Box(0.2f, 0.2f, 0.2f))
    clickableLine.setMaterial(plainColor(Palette.lineClosedWall))
    clickableLine.setCullHint(CullHint.Always)
    // vertices
    val lineHalfDist = new Vector2f((line.b.x - line.a.x) / 2f, (line.b.y - line.a.y) / 2f)
    val pointA = new Geometry("linePointA", K.lineBox)
    pointA.setLocalTranslation(-lineHalfDist.x, -lineHalfDist.y, 0f)
    pointA.setMaterial(plainColor(color))
    pointA.addControl(new ConstantSizeOnScreenControl())
    val pointB = new Geometry("linePointB", K.lineBox)
    pointB.setLocalTranslation(lineHalfDist.x, lineHalfDist.y, 0f)
    pointB.setMaterial(plainColor(color))
    pointB.addControl(new ConstantSizeOnScreenControl())
    // also line to show when dragging
    val linePreview = lineGeo.clone()
    linePreview.setLocalTranslation(-center.x, -center.y, 0f)

    val lineSelector = new Node("lineSelector")
    lineSelector.addControl(new LineDragControl(line))
    lineSelector.attachChild(lineBox)
    lineSelector.attachChild(clickableLine)
    lineSelector.attachChild(pointA)
    lineSelector.attachChild(pointB)
    lineSelector.attachChild(linePreview)
    lineSelector.setLocalTranslation(center)

    lineNode.attachChild(lineSelector)
    lineNode.setLocalTranslation(0, 0, Mode2DLayers.lines)
    node.attachChild(lineNode)
  }

  def draw2dSector(node: Node, sectorId: Long, sector: Sector) {
    val area = new Node("sector-area")
    // visual point
    val centerView = new Geometry("sector-center", K.sectorBox)
    centerView.setMaterial(plainColor(Palette.sectorCenter))
    centerView.addControl(new SelectableControl(Palette.sectorCenter, SelectionSector(sectorId, sector)))
    // clickable point
    val clickableRadius = 0.2f
    val clickableCenter = new Geometry("clickableCenter", new Box(clickableRadius, clickableRadius, clickableRadius))
    clickableCenter.setMaterial(plainColor(Palette.sectorCenter))
    clickableCenter.setCullHint(CullHint.Always)

    val pos = sector.polygon.center
    area.attachChild(centerView)
    area.attachChild(clickableCenter)
    area.setLocalTranslation(pos.x, pos.y, Mode2DLayers.vertices)
    area.addControl(new ConstantSizeOnScreenControl())
    area.addControl(new SectorDragControl(sectorId))
    node.attachChild(area)
  }

}