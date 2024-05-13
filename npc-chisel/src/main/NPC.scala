package npc

import chisel3._
import chisel3.util._
import npc.core._
import npc.mem._

object NPC{
  def apply(config : Config) : NPC = {new NPC(config.core)}
}

class NPC(val coreConfig : CoreConfig) extends Module {
  val core = Module(new Core(coreConfig))
  val imem = Module(new Imem(coreConfig))
  val dmem = Module(new Dmem(coreConfig))

  core.io.imem <> imem.io
  core.io.dmem <> dmem.io
  //dmem.io.clk := clock
}

