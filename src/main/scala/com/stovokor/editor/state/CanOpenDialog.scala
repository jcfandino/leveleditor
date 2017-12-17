package com.stovokor.editor.state

import javax.swing.JFrame

trait CanOpenDialog {

  lazy val getSwingFrame = {
    val frame = new JFrame()
    frame.setLocationRelativeTo(null)
    frame.setUndecorated(true)
    frame.setVisible(false)
    frame.toFront()
    frame
  }
}