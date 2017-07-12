package com.stovokor.editor.state

import com.simsilica.lemur.GuiGlobals

trait CanMapInput {
  def inputMapper = GuiGlobals.getInstance.getInputMapper
}