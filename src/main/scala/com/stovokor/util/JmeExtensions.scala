package com.stovokor.util

import com.jme3.scene.Spatial
import com.jme3.scene.SceneGraphVisitor
import com.jme3.scene.control.Control
import com.jme3.scene.Node

object JmeExtensions {

  implicit class SpatialExtensions(s: Spatial) {

    def depthFirst(f: (Spatial => Unit)) {
      s.depthFirstTraversal(new SceneGraphVisitor {
        def visit(s2: Spatial) {
          f(s2)
        }
      })
    }

    def breadthFirst(f: (Spatial => Unit)) {
      s.breadthFirstTraversal(new SceneGraphVisitor {
        def visit(s2: Spatial) {
          f(s2)
        }
      })
    }

    def hasControl(ct: Class[_ <: Control]) = {
      s.getControl(ct) != null
    }

    def asNode = s.asInstanceOf[Node]
    def isNode = s.isInstanceOf[Node]

    def childOption(name: String) =
      if (s.isNode && s.asNode.getChild(name) != null) Some(s.asNode.getChild(name))
      else None
  }

}