package com.stovokor.editor.factory

import com.jme3.material.Material
import com.jme3.asset.AssetManager
import com.jme3.material.RenderState.BlendMode
import com.jme3.texture.Texture.WrapMode
import com.jme3.texture.Texture
import com.jme3.math.ColorRGBA

trait MaterialFactory {

  def assetManager: AssetManager

  def plainColor(color: ColorRGBA): Material = {
    var mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
    mat.setColor("Color", color)
    mat
  }

  def texture(file: String): Material = {
    val tex = assetManager.loadTexture(file)
    //    tex.setMagFilter(Texture.MagFilter.Nearest)
    //    tex.setMinFilter(Texture.MinFilter.NearestNoMipMaps)
    //    tex.setAnisotropicFilter(0)
    tex.setWrap(WrapMode.Repeat)

    val mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
    mat.setTexture("ColorMap", tex)
    //    mat.setColor("Color", ColorRGBA.White.mult(2))
    mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha)
    mat
  }
}
