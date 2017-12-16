package com.stovokor.editor.input

import com.simsilica.lemur.input.InputMapper
import com.simsilica.lemur.input.FunctionId
import com.jme3.input.KeyInput
import com.simsilica.lemur.input.InputState
import com.simsilica.lemur.input.Axis
import com.stovokor.util.LemurExtensions._

object InputFunctionsMapper {

  def initialize(inputMapper: InputMapper) {
    inputMapper.map(InputFunction.moveX, InputState.Negative, KeyInput.KEY_LEFT)
    inputMapper.map(InputFunction.moveX, InputState.Negative, KeyInput.KEY_A)
    inputMapper.map(InputFunction.moveX, KeyInput.KEY_RIGHT)
    inputMapper.map(InputFunction.moveX, KeyInput.KEY_D)
    inputMapper.map(InputFunction.moveY, KeyInput.KEY_UP)
    inputMapper.map(InputFunction.moveY, KeyInput.KEY_W)
    inputMapper.map(InputFunction.moveY, InputState.Negative, KeyInput.KEY_DOWN)
    inputMapper.map(InputFunction.moveY, InputState.Negative, KeyInput.KEY_S)
    inputMapper.map(InputFunction.moveZ, InputState.Negative, KeyInput.KEY_PGDN)
    inputMapper.map(InputFunction.moveZ, InputState.Negative, KeyInput.KEY_RBRACKET)
    inputMapper.map(InputFunction.moveZ, KeyInput.KEY_PGUP)
    inputMapper.map(InputFunction.moveZ, KeyInput.KEY_LBRACKET)

    inputMapper.map(InputFunction.mouseX, Axis.MOUSE_X)
    inputMapper.map(InputFunction.mouseY, Axis.MOUSE_Y)
    inputMapper.map(InputFunction.mouseWheel, Axis.MOUSE_WHEEL)

    inputMapper.map(InputFunction.cancel, KeyInput.KEY_ESCAPE)
    inputMapper.map(InputFunction.snapToGrid, KeyInput.KEY_G)
    inputMapper.map(InputFunction.resizeGrid, KeyInput.KEY_R)
    inputMapper.map(InputFunction.switchViewMode, KeyInput.KEY_TAB)
    inputMapper.map(InputFunction.test1, KeyInput.KEY_F1)
    inputMapper.map(InputFunction.test2, KeyInput.KEY_F2)
    inputMapper.map(InputFunction.test3, KeyInput.KEY_F3)
    inputMapper.map(InputFunction.test4, KeyInput.KEY_F4)

    inputMapper.map(InputFunction.open, KeyInput.KEY_O, KeyInput.KEY_LCONTROL)
    inputMapper.map(InputFunction.open, KeyInput.KEY_F9)
    inputMapper.map(InputFunction.save, KeyInput.KEY_S, KeyInput.KEY_LCONTROL)
    inputMapper.map(InputFunction.save, KeyInput.KEY_F5)
    inputMapper.map(InputFunction.saveAs, KeyInput.KEY_S, KeyInput.KEY_LCONTROL, KeyInput.KEY_LSHIFT)

    // TODO try to reuse pgup and pgdn
    inputMapper.map(InputFunction.editHeight, KeyInput.KEY_P)
    inputMapper.map(InputFunction.editHeight, InputState.Negative, KeyInput.KEY_O)
    inputMapper.map(InputFunction.editTextureOffsetX, KeyInput.KEY_L)
    inputMapper.map(InputFunction.editTextureOffsetX, InputState.Negative, KeyInput.KEY_H)
    inputMapper.map(InputFunction.editTextureOffsetY, KeyInput.KEY_K)
    inputMapper.map(InputFunction.editTextureOffsetY, InputState.Negative, KeyInput.KEY_J)

    inputMapper.map(InputFunction.editTextureScaleX, InputState.Positive, KeyInput.KEY_L, KeyInput.KEY_LCONTROL)
    inputMapper.map(InputFunction.editTextureScaleX, InputState.Negative, KeyInput.KEY_H, KeyInput.KEY_LCONTROL)
    inputMapper.map(InputFunction.editTextureScaleY, InputState.Positive, KeyInput.KEY_K, KeyInput.KEY_LCONTROL)
    inputMapper.map(InputFunction.editTextureScaleY, InputState.Negative, KeyInput.KEY_J, KeyInput.KEY_LCONTROL)

    inputMapper.map(InputFunction.changeMaterial, KeyInput.KEY_1)
  }
}

object InputFunction {

  val camera = "camera"
  val moveX = new FunctionId(camera, "camX")
  val moveY = new FunctionId(camera, "camY")
  val moveZ = new FunctionId(camera, "camZ")

  val mouse = "mouse"
  val mouseX = new FunctionId(mouse, "mouseX")
  val mouseY = new FunctionId(mouse, "mouseY")
  val mouseWheel = new FunctionId(mouse, "mouseWheel")

  val files = "files"
  val open = new FunctionId(files, "open")
  val save = new FunctionId(files, "save")
  val saveAs = new FunctionId(files, "saveAs")
  val export = new FunctionId(files, "export")

  val general = "general"
  val cancel = new FunctionId(general, "cancel")
  val snapToGrid = new FunctionId(general, "snapToGrid")
  val resizeGrid = new FunctionId(general, "resizeGrid")
  val switchViewMode = new FunctionId(general, "switchViewMode")
  val test1 = new FunctionId(general, "test1")
  val test2 = new FunctionId(general, "test2")
  val test3 = new FunctionId(general, "test3")
  val test4 = new FunctionId(general, "test4")

  val edit3d = "edit3d"
  val editHeight = new FunctionId(edit3d, "editHeight")
  val editTextureOffsetX = new FunctionId(edit3d, "editTextureOffsetX")
  val editTextureOffsetY = new FunctionId(edit3d, "editTextureOffsetY")
  val editTextureScaleX = new FunctionId(edit3d, "editTextureScaleX")
  val editTextureScaleY = new FunctionId(edit3d, "editTextureScaleY")
  val changeMaterial = new FunctionId(edit3d, "changeMaterial")
}