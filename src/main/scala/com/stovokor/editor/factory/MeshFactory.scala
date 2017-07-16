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

class MeshFactory(val assetManager: AssetManager) extends MaterialFactory {

  def createMesh(sec: Sector) = {

    val triangles = sec.polygon.triangulate

    val uniquePoints = triangles
      .flatMap(_.pointsSorted)
      .distinct
      .sortBy(p => p.distance(Point(0f, 0f)))

    val node = new Node("sector")
    val floor = createSurface(triangles, uniquePoints, sec.floor, true)
    val ceiling = createSurface(triangles, uniquePoints, sec.ceiling, false)
    node.attachChild(floor)
    node.attachChild(ceiling)
    sec.walls
      .map(w => createWall(w, sec.floor.height, sec.ceiling.height))
      .foreach(node.attachChild)
    node
  }

  // TODO sometimes triangles are backward
  def createSurface(triangles: List[Triangle], uniquePoints: List[Point], surface: Surface, faceUp: Boolean) = {
    def sortTriangle(t: Triangle) = if (faceUp) t.asClockwise else t.asCounterClockwise
    def normal = if (faceUp) Vector3f.UNIT_Y else Vector3f.UNIT_Y.negate
    val m = new Mesh
    m.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(
      uniquePoints
        .map(p => new Vector3f(p.x, surface.height, p.y)) // we flip coords
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
    val geom = new Geometry("floor", m)
    geom.setMaterial(texture("Textures/Debug1.png"))
    geom
  }

  def createWall(wall: Wall, bottom: Float, top: Float): Geometry = {
    val line = wall.line
    val tex = wall.texture
    val m = new Mesh
    m.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(
      new Vector3f(line.a.x, top, line.a.y),
      new Vector3f(line.b.x, top, line.b.y),
      new Vector3f(line.a.x, bottom, line.a.y),
      new Vector3f(line.b.x, bottom, line.b.y)))

    //    val (xo, xe, yo, ye) = (0f, 1f, 0f, 1f) 
    val (xo, xe, yo, ye) =
      (tex.uvx(0f), tex.uvx(line.length),
        tex.uvy(bottom), tex.uvy(top))

    m.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(
      new Vector2f(xo, yo),
      new Vector2f(xe, yo),
      new Vector2f(xo, ye),
      new Vector2f(xe, ye))) // Set up from sprite sheet

    //new Vector2(-vector2.Y, vector2.X);
    // TODO check this
    val normal = new Vector3f(-(line.b.y - line.a.y), 0f, line.b.x - line.a.x)

    m.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(2, 0, 1, 1, 3, 2))
    m.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normal, normal, normal, normal))
    m.updateBound()
    val geom = new Geometry("wall", m)
    geom.setMaterial(texture("Textures/Debug1.png"))
    geom
  }

}