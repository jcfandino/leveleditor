package com.stovokor.editor.state

import com.jme3.app.Application
import com.jme3.app.SimpleApplication
import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import com.jme3.asset.AssetManager
import com.jme3.scene.Node
import com.jme3.math.ColorRGBA
import com.jme3.material.Material

class BaseState extends AbstractAppState {

  var app: SimpleApplication = null
  var guiNode: Node = null
  var rootNode: Node = null
  var assetManager: AssetManager = null

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    app = simpleApp.asInstanceOf[SimpleApplication]
    guiNode = app.getGuiNode
    rootNode = app.getRootNode
    assetManager = app.getAssetManager
  }

  override def update(tpf: Float) {
  }
}

trait MaterialFactory {

  def assetManager:AssetManager
  
  def plainColor(color: ColorRGBA): Material = {
    var mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
    mat.setColor("Color", color)
    mat
  }
}