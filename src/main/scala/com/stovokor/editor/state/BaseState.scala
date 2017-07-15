package com.stovokor.editor.state

import com.jme3.app.Application
import com.jme3.app.SimpleApplication
import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import com.jme3.asset.AssetManager
import com.jme3.scene.Node
import com.jme3.math.ColorRGBA
import com.jme3.material.Material
import com.jme3.texture.Texture.WrapMode
import com.jme3.material.RenderState.BlendMode

class BaseState extends AbstractAppState {

  var app: SimpleApplication = null
  def stateManager = app.getStateManager
  def guiNode = app.getGuiNode
  def rootNode = app.getRootNode
  def assetManager = app.getAssetManager

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    app = simpleApp.asInstanceOf[SimpleApplication]
  }

  override def update(tpf: Float) {
  }

  def get2DNode = getOrCreateNode("2d")
  def get3DNode = getOrCreateNode("3d")

  def getOrCreateNode(id: String) = {
    var node = rootNode.getChild(id)
    if (node == null) {
      node = new Node(id)
      rootNode.attachChild(node)
    }
    node.asInstanceOf[Node]
  }
}

