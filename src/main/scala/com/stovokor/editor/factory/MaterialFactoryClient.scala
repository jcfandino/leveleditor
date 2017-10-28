package com.stovokor.editor.factory

import com.jme3.asset.AssetManager
import com.jme3.material.Material
import com.jme3.material.MaterialDef
import com.jme3.material.RenderState.BlendMode
import com.jme3.math.ColorRGBA
import com.jme3.texture.Texture
import com.jme3.texture.Texture.WrapMode
import com.stovokor.editor.model.MatDefMaterial
import com.stovokor.editor.model.NullMaterial
import com.stovokor.editor.model.SimpleMaterial
import com.stovokor.editor.model.SurfaceMaterial

trait MaterialFactoryClient {

  def assetManager: AssetManager

  def plainColor(color: ColorRGBA) = MaterialFactory().plainColor(assetManager, color)

  def texture(material: SurfaceMaterial) = MaterialFactory().texture(assetManager, material)
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

  def texture(assetManager: AssetManager, material: SurfaceMaterial) = {
    def unshaded(path: String) = {
      val tex = assetManager.loadTexture(path)
      tex.setWrap(WrapMode.Repeat)
      //      tex.setMagFilter(Texture.MagFilter.Nearest)
      //      tex.setMinFilter(Texture.MinFilter.NearestNoMipMaps)
      //      tex.setAnisotropicFilter(0)

      val mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
      mat.setTexture("ColorMap", tex)
      mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha)
      mat
    }
    material match {
      case NullMaterial()       => unshaded("Textures/Debug3.png")
      case SimpleMaterial(path) => unshaded(path)
      case MatDefMaterial(path) => {
        val mat = assetManager.loadMaterial(path)
        makeTextureRepeatable(mat)
        mat
      }
    }
  }

  // TODO this is a guess, should be configurable
  def repeatableTextures = List(
    // For Lightning
    "DiffuseMap", "NormalMap", "SpecularMap", "ParallaxMap", "AlphaMap", "ColorRamp", "GlowMap", "LightMap",
    //For Unshaded
    "ColorMap")

  def makeTextureRepeatable(mat: Material) = {
    repeatableTextures.foreach(tex => {
      if (mat.getTextureParam(tex) != null) {
        mat.getTextureParam(tex).getTextureValue().setWrap(WrapMode.Repeat)
      }
    })
  }
}