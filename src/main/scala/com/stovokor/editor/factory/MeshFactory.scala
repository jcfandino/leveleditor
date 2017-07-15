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

class MeshFactory(val assetManager: AssetManager) extends MaterialFactory {

  def createMesh(sec: Sector) = {

    val triangles = sec.polygon.triangulate

    val uniquePoints = triangles
      .flatMap(_.points)
      .distinct
      .sortBy(p => p.distance(Point(0f, 0f)))

    val m = new Mesh
    m.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(
      uniquePoints
        .map(p => new Vector3f(p.x, p.y, sec.floor.height))
        .toArray: _*))

    m.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(
      uniquePoints
        .map(p => {
          val surf = sec.floor.texture
          new Vector2f(surf.uvx(p.x), surf.uvy(p.y))
        })
        .toArray: _*))

    m.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(
      triangles
        .flatMap(_.points) // TODO sort
        .map(uniquePoints.indexOf)
        .toArray: _*))

    m.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(
      uniquePoints.map(_ => Vector3f.UNIT_Z): _*))
    m.updateBound()
    val geom = new Geometry("floor", m)
    //    geom.setMaterial(plainColor(ColorRGBA.Orange))
    geom.setMaterial(texture("Textures/Debug1.png"))
    geom
  }

  def uvx(x: Float, scale: Float) = {

  }
}