package com.stovokor.editor.factory

import com.jme3.asset.AssetManager
import com.jme3.math.Vector2f
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.Mesh
import com.jme3.scene.VertexBuffer.Type
import com.jme3.util.BufferUtils
import com.stovokor.editor.model.Point
import com.stovokor.editor.model.Sector
import com.jme3.math.ColorRGBA
import com.stovokor.editor.model.Triangle
import com.jme3.scene.Node
import com.stovokor.editor.model.SurfaceTexture
import com.stovokor.editor.model.Surface
import com.stovokor.editor.model.Wall
import com.stovokor.editor.model.Line
import com.stovokor.editor.model.Border
import com.stovokor.editor.control.HighlightControl

object MeshFactory {
  def apply(assetManager: AssetManager) = new MeshFactory(assetManager)
}
class MeshFactory(val assetManager: AssetManager) extends MaterialFactoryClient {

  def defaultTexture(index: Int) = texture(s"Textures/Debug${(index + 1)}.png")

  // Coordinate mapping:
  // 3D | 2D
  //  X = -X
  //  Z =  Y

  def createMesh(id: Long, sec: Sector, borders: List[Border] = List()) = {

    val triangles = sec.triangulate

    val uniquePoints = triangles
      .flatMap(_.pointsSorted)
      .distinct
      .sortBy(p => p.distance(Point(0f, 0f)))

    val node = new Node("sector")
    val floor = createSurface(id, triangles, uniquePoints, sec.floor, true)
    val ceiling = createSurface(id, triangles, uniquePoints, sec.ceiling, false)
    node.attachChild(floor)
    node.attachChild(ceiling)
    sec.closedWalls
      .zipWithIndex
      .map(w => createWall(id, w._1, "wall-" + w._2, sec.floor.height, sec.ceiling.height))
      .foreach(node.attachChild)
    createBordersMesh(node, id, sec, borders)
    node
  }

  def createBordersMesh(node: Node, id: Long, sector: Sector, borders: List[Border]) = {
    borders.foreach(b => println(s"Border found $b"))
    def idx(border: Border) = {
      sector.openWalls
        .zipWithIndex.find(w => w._1.line == border.line)
        .map(_._2).orElse(Some(-1)).get
    }
    def bottomMaybe(border: Border) = {
      if (border.surfaceFloor.height > 0f) {
        Some(createWall(
          id,
          Wall(border.line, border.surfaceFloor.texture),
          "borderLow-" + idx(border),
          sector.floor.height,
          sector.floor.height + border.surfaceFloor.height))
      } else None
    }
    def topMaybe(border: Border) = {
      if (border.surfaceCeiling.height > 0f) {
        Some(createWall(
          id,
          Wall(border.line, border.surfaceCeiling.texture),
          "borderHi-" + idx(border),
          sector.ceiling.height - border.surfaceCeiling.height,
          sector.ceiling.height))
      } else None
    }
    borders
      .zipWithIndex
      .flatMap(b => b match {
        case (border, idx) => List(bottomMaybe(border), topMaybe(border))
      })
      .filter(_.isDefined)
      .map(_.get)
      .foreach(node.attachChild)
  }

  def createSurface(id: Long, triangles: List[Triangle], uniquePoints: List[Point], surface: Surface, faceUp: Boolean) = {
    // we invert the triangle because we are inverting the X coordinate.
    def sortTriangle(t: Triangle) = if (!faceUp) t.asClockwise else t.asCounterClockwise
    def normal = if (faceUp) Vector3f.UNIT_Y else Vector3f.UNIT_Y.negate
    val m = new Mesh
    m.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(
      uniquePoints
        .map(p => new Vector3f(-p.x, surface.height, p.y)) // we flip coords
        .toArray: _*))

    m.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(
      uniquePoints
        .map(p => {
          val surf = surface.texture
          new Vector2f(surf.uvx(p.x), surf.uvy(p.y))
        })
        .toArray: _*))

    m.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(
      triangles
        .map(sortTriangle)
        .flatMap(_.pointsUnsorted)
        .map(uniquePoints.indexOf)
        .toArray: _*))

    m.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(
      uniquePoints.map(_ => normal): _*))
    m.updateBound()
    val name = if (faceUp) "floor" else "ceiling"
    val geom = new Geometry(name, m)
    geom.setMaterial(defaultTexture(surface.texture.index))
    geom.addControl(new HighlightControl(id, name))
    geom
  }

  def createWall(id: Long, wall: Wall, name: String, bottom: Float, top: Float): Geometry = {
    val line = wall.line
    val tex = wall.texture
    val m = new Mesh
    m.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(
      // the X coord is inverted
      new Vector3f(-line.b.x, top, line.b.y),
      new Vector3f(-line.a.x, top, line.a.y),
      new Vector3f(-line.b.x, bottom, line.b.y),
      new Vector3f(-line.a.x, bottom, line.a.y)))

    val (xo, xe, yo, ye) =
      (tex.uvx(0f), tex.uvx(line.length),
        tex.uvy(bottom), tex.uvy(top))

    m.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(
      new Vector2f(xe, ye),
      new Vector2f(xo, ye),
      new Vector2f(xe, yo),
      new Vector2f(xo, yo)))

    //new Vector2(-vector2.Y, vector2.X);
    // TODO check this
    val normal = new Vector3f(-(line.b.y - line.a.y), 0f, line.b.x - line.a.x)

    m.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(2, 0, 1, 1, 3, 2))
    m.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normal, normal, normal, normal))
    m.updateBound()
    val geom = new Geometry(name, m)
    geom.setMaterial(defaultTexture(wall.texture.index))
    geom.addControl(new HighlightControl(id, name))
    geom
  }

}