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

class MeshFactory(val assetManager: AssetManager) extends MaterialFactory {

  def createMesh(sec: Sector) = {

    val triangles = sec.polygon.triangulate

    val uniquePoints = triangles
      .flatMap(_.points)
      .distinct
      .sortBy(p => p.distance(Point(0f, 0f)))

    val node = new Node("sector")
    val floor = createSurface(triangles, uniquePoints, sec.floor, true)
    val ceiling = createSurface(triangles, uniquePoints, sec.ceiling, false)
    node.attachChild(floor)
    node.attachChild(ceiling)
    node
  }

  def createSurface(triangles: List[Triangle], uniquePoints: List[Point], surface: Surface, faceUp: Boolean) = {
    def sortTriangle(t: Triangle) = if (faceUp) t.reverse else t
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
        .flatMap(_.points)
        .map(uniquePoints.indexOf)
        .toArray: _*))

    m.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(
      uniquePoints.map(_ => normal): _*))
    m.updateBound()
    val geom = new Geometry("floor", m)
    //    geom.setMaterial(plainColor(ColorRGBA.Orange))
    geom.setMaterial(texture("Textures/Debug1.png"))
    geom
  }

  def createWall(wall: Wall) = {
    //TODO   
  }

}