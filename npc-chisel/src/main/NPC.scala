package npc

import chisel3._
import chisel3.util._

class NPC extends Module {
  val core = Module(new Core)
  val imem = Module(new Imem)
  val dmem = Module(new Dmem)

  core.io.imem <> imem.io
  core.io.dmem <> dmem.io
}

