package npc

import chisel3._
import chisel3.util._
import npc.core._
import npc.bus._
import npc.bus.AXI4Connector._
import chisel3.SpecifiedDirection.Flip

object NPC{
  def apply(config : CoreConfig) : NPC = {new NPC(config)}
}

class npcIO extends Bundle{
  val interrupt = Input(Bool())
  val master = new AXI4Master
  val slave = new AXI4Slave
}

class NPC(val coreConfig : CoreConfig) extends Module {
  val io = IO(new npcIO)
  io.master.setDefaults()
  io.slave.setDefaults()

  if(coreConfig.ysyxsoc){
    val core = Module(new Core(coreConfig))
    val xbar = Module(new npc.bus.Xbar3)
    val clint = Module(new npc.dev.Clint(coreConfig))

    connectAll(core.io.imem ,xbar.io.imem)
    connectAll(core.io.dmem ,xbar.io.dmem)
    connectAll(xbar.io.clint ,clint.io)
    xbar.io.soc <> io.master
  }
//  else if(coreConfig.npc){
//    val core = Module(new Core(coreConfig))
//    val xbar = Module(new npc.bus.Xbar2(coreConfig))
//    val arb = Module(new npc.bus.Arbiter(coreConfig))
//    val clint = Module(new npc.dev.Clint(coreConfig))
//    val mem = Module(new npc.dev.Mem(coreConfig))
//    val uart = Module(new npc.dev.Uart(coreConfig))
//
//    core.io.imem :<>= arb.io.imem
//    core.io.dmem :<>= arb.io.dmem
//    arb.io.mem <> xbar.io.arb
//    xbar.io.clint <> clint.io.axi
//    xbar.io.sram <> mem.io
//    xbar.io.uart <> uart.io
//    clint.io.clk := clock
//    clint.io.rst := reset
//  }

}

