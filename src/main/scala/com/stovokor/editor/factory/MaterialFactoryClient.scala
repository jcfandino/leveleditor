package com.stovokor.editor.factory

import com.jme3.material.Material
import com.jme3.asset.AssetManager
import com.jme3.material.RenderState.BlendMode
import com.jme3.texture.Texture.WrapMode
import com.jme3.texture.Texture
import com.jme3.math.ColorRGBA

trait MaterialFactoryClient {

  def assetManager: AssetManager

  def plainColor(color: ColorRGBA) = MaterialFactory().plainColor(assetManager, color)

  def texture(file: String) = MaterialFactory().texture(assetManager, file)
}

object MaterialFactory {
  private var instance = new MaterialFactory

  def setInstance(mf: MaterialFactory) { instance = mf }

  def apply() = instance
}

class MaterialFactory {

  def plainColor(assetManager: AssetManager, color: ColorRGBA): Material = {
    var mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
    mat.setColor("Color", color)
    mat
  }

  def texture(assetManager: AssetManager, file: String): Material = {
    val tex = assetManager.loadTexture(file)
    tex.setWrap(WrapMode.Repeat)

    val mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
    mat.setTexture("ColorMap", tex)
    mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha)
    mat
  }
}