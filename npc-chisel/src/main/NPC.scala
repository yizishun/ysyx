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
  io.slave.arready := false.B
  io.slave.rdata := DontCare
  io.slave.rresp := 0.U
  io.slave.rid := 0.U
  io.slave.rready := DontCare
  io.slave.rvalid := false.B
  io.slave.rlast := true.B
  io.slave.awready := false.B
  io.slave.wready := false.B
  io.slave.bresp := 0.U
  io.slave.bvalid := false.B
  io.slave.bid := 0.U
  io.master.araddr := DontCare
  io.master.arvalid := false.B
  io.master.arid := 0.U
  io.master.arlen := 0.U
  io.master.arsize := 0.U
  io.master.arburst := 0.U
  io.master.rready := false.B
  io.master.awaddr := DontCare
  io.master.awvalid := false.B
  io.master.awid := 0.U
  io.master.awlen := 0.U
  io.master.awsize := 0.U
  io.master.awburst := 0.U
  io.master.wdata := DontCare
  io.master.wstrb := 0.U
  io.master.wvalid := 0.U
  io.master.wlast := false.B
  io.master.bready := false.B

  if(coreConfig.ysyxsoc){
    val core = Module(new Core(coreConfig))
    val xbar = Module(new npc.bus.Xbar3)
    val clint = Module(new npc.dev.Clint(coreConfig))

    connectAll(core.io.imem ,xbar.io.imem)
    connectAll(core.io.dmem ,xbar.io.dmem)
    connectAll(xbar.io.clint ,clint.io.axi)
    xbar.io.soc <> io.master
    clint.io.clk := clock
    clint.io.rst := reset
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

