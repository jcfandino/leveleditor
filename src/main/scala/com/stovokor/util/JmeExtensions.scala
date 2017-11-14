package com.stovokor.util

import com.jme3.scene.Spatial
import com.jme3.scene.SceneGraphVisitor
import com.jme3.scene.control.Control
import com.jme3.scene.Node
import com.jme3.scene.Spatial.CullHint
import com.jme3.math.Vector2f
import com.jme3.math.Vector3f

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

    def isVisible = s.getCullHint != CullHint.Always

    def setVisible(v: Boolean) {
      s.setCullHint(if (v) CullHint.Inherit else CullHint.Always)
    }
  }

  implicit class Vector3fExtensions(v: Vector3f) {
    def to2f() = new Vector2f(v.x, v.y)
  }

  implicit class Vector2fExtensions(v: Vector2f) {
    def to3f(implicit z: Float = 0f) = new Vector3f(v.x, v.y, z)
  }
}