package com.stovokor.editor.state

import com.jme3.app.Application
import com.jme3.app.SimpleApplication
import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppState
import com.jme3.app.state.AppStateManager
import com.jme3.scene.Node

class BaseState extends AbstractAppState {

  var app: SimpleApplication = null
  def stateManager = app.getStateManager
  def guiNode = app.getGuiNode
  def rootNode = app.getRootNode
  def assetManager = app.getAssetManager
  def inputManager = app.getInputManager
  def cam = app.getCamera

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    app = simpleApp.asInstanceOf[SimpleApplication]
  }

  override def update(tpf: Float) {
  }

  def get2DNode = getOrCreateNode(rootNode, "2d")
  def get3DNode = getOrCreateNode(rootNode, "3d")

  def getOrCreateNode(parent: Node, id: String) = {
    var node = parent.getChild(id)
    if (node == null) {
      node = new Node(id)
      parent.attachChild(node)
    }
    node.asInstanceOf[Node]
  }

  def enableStates(classes: Class[_ <: AppState]*) = setEnabledToStates(true, classes: _*)
  def disableStates(classes: Class[_ <: AppState]*) = setEnabledToStates(false, classes: _*)

  def setEnabledToStates(enabled: Boolean, classes: Class[_ <: AppState]*) {
    for (clazz <- classes) {
      val st = stateManager.getState(clazz)
      if (st != null) st.setEnabled(enabled)
    }
  }

  def removeStates(classes: Class[_ <: AppState]*) = {
    for (clazz <- classes) {
      val st = stateManager.getState(clazz)
      if (st != null) stateManager.detach(st)
    }
  }

}

