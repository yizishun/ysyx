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
  val mem = Module(new npc.mem.Mem(coreConfig))
  val uart = Module(new npc.mem.Uart(coreConfig))
  val arb = Module(new npc.mem.Arbiter(coreConfig))
  val xbar = Module(new npc.mem.Xbar(coreConfig))

  core.io.imem :<>= arb.io.imem
  core.io.dmem :<>= arb.io.dmem
  arb.io.mem :<>= xbar.io.arb
  xbar.io.uart :<>= uart.io
  xbar.io.sram :<>= mem.io
  

}

