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
  val arb = Module(new npc.mem.Arbiter(coreConfig))

  core.io.imem :<>= arb.io.imem
  core.io.dmem :<>= arb.io.dmem
  arb.io.mem :<>= mem.io

//  //backbox must do that(i don kown how to do 555)
//  //AR
//  imem.io.araddr := core.io.imem.araddr
//  imem.io.arvalid := core.io.imem.arvalid
//  core.io.imem.arready := imem.io.arready
//  //R
//  imem.io.rready := core.io.imem.rready 
//  core.io.imem.rdata := imem.io.rdata
//  core.io.imem.rvalid := imem.io.rvalid
//  core.io.imem.rresp := imem.io.rresp
//  //AW
//  imem.io.awaddr := core.io.imem.awaddr
//  imem.io.awvalid := core.io.imem.awvalid
//  core.io.imem.awready := imem.io.awready
//  //W
//  imem.io.wdata := core.io.imem.wdata
//  imem.io.wstrb := core.io.imem.wstrb
//  imem.io.wvalid := core.io.imem.wvalid
//  core.io.imem.wready := imem.io.wready
//  //B
//  imem.io.bready := core.io.imem.bready
//  core.io.imem.bresp := imem.io.bresp
//  core.io.imem.bvalid := imem.io.bvalid
//  //ACLK Areset
//  imem.io.clk := clock
//  imem.io.rst := reset
//
//  //AR
//  dmem.io.araddr := core.io.dmem.araddr
//  dmem.io.arvalid := core.io.dmem.arvalid
//  core.io.dmem.arready := dmem.io.arready
//  //R
//  dmem.io.rready := core.io.dmem.rready 
//  core.io.dmem.rdata := dmem.io.rdata
//  core.io.dmem.rvalid := dmem.io.rvalid
//  core.io.dmem.rresp := dmem.io.rresp
//  //AW
//  dmem.io.awaddr := core.io.dmem.awaddr
//  dmem.io.awvalid := core.io.dmem.awvalid
//  core.io.dmem.awready := dmem.io.awready
//  //W
//  dmem.io.wdata := core.io.dmem.wdata
//  dmem.io.wstrb := core.io.dmem.wstrb
//  dmem.io.wvalid := core.io.dmem.wvalid
//  core.io.dmem.wready := dmem.io.wready
//  //B
//  dmem.io.bready := core.io.dmem.bready
//  core.io.dmem.bresp := dmem.io.bresp
//  core.io.dmem.bvalid := dmem.io.bvalid
//  //ACLK Areset
//  dmem.io.clk := clock
//  dmem.io.rst := reset
}

