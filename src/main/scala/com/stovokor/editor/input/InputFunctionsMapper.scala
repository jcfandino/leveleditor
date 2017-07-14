package com.stovokor.editor.input

import com.simsilica.lemur.input.InputMapper
import com.simsilica.lemur.input.FunctionId
import com.jme3.input.KeyInput
import com.simsilica.lemur.input.InputState
import com.simsilica.lemur.input.Axis

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

  val general = "general"
  val cancel = new FunctionId(general, "cancel")
  val snapToGrid = new FunctionId(general, "snapToGrid")

}