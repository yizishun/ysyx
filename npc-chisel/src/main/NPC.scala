package npc

import chisel3._
import chisel3.util._
import npc.core._
import chisel3.SpecifiedDirection.Flip

object NPC{
  def apply(config : Config) : NPC = {new NPC(config.core)}
}

class npcIO extends Bundle{
  val interrupt = Input(Bool())
  val master = Flipped(new npc.bus.AXI4)
  val slave = new npc.bus.AXI4
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

  val core = Module(new Core(coreConfig))

  val arb = Module(new npc.bus.Arbiter(coreConfig))
  val xbar = Module(new npc.bus.Xbar)

  val clint = Module(new npc.dev.Clint(coreConfig))

  core.io.imem :<>= arb.io.imem
  core.io.dmem :<>= arb.io.dmem
  arb.io.mem <> xbar.io.arb
  clint.io.axi <> xbar.io.clint
  xbar.io.soc <> io.master
//  arb.io.mem <> io.master
  clint.io.clk := clock
  clint.io.rst := reset
  

}

