package com.stovokor.editor.factory

import com.jme3.asset.AssetManager
import com.jme3.material.RenderState.BlendMode
import com.jme3.math.Vector2f
import com.jme3.math.Vector3f
import com.jme3.renderer.queue.RenderQueue.Bucket
import com.jme3.scene.Geometry
import com.jme3.scene.Mesh
import com.jme3.scene.VertexBuffer.Type
import com.jme3.util.BufferUtils
import com.stovokor.editor.tiles.SpriteSheet

class SpriteFactory(val assetManager: AssetManager) extends MaterialFactoryClient {

  def create(assetManager: AssetManager, name: String, sheet: SpriteSheet, width: Float, height: Float, pos: Vector3f, transparent: Boolean = true) = {
    val mesh = createMesh(sheet, width, height)
    val sprite = new Geometry(name, mesh)
    sprite.setLocalTranslation(pos)

    val mat = texture(sheet.file)
    sprite.setMaterial(mat)
    if (transparent) {
      mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha)
      mat.getAdditionalRenderState().setDepthWrite(false)
      sprite.setQueueBucket(Bucket.Transparent)
    }
    sprite
  }

  def createMesh(sheet: SpriteSheet, width: Float, height: Float) = {
    val m = new Mesh
    m.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(
      new Vector3f(-width / 2f, -height / 2f, 0f),
      new Vector3f(width / 2f, -height / 2f, 0f),
      new Vector3f(-width / 2f, height / 2f, 0f),
      new Vector3f(width / 2f, height / 2f, 0f)))

    val (xo, xe, yo, ye) = sheet.getCoords(0)
    m.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(
      new Vector2f(xo, yo),
      new Vector2f(xe, yo),
      new Vector2f(xo, ye),
      new Vector2f(xe, ye)))

    m.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(2, 0, 1, 1, 3, 2))
    val normal = Vector3f.UNIT_Z
    m.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(
      normal, normal, normal, normal))
    m.updateBound()
    m
  }
}