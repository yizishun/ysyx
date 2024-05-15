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

  core.io.imem :<>= imem.io
  //backbox must do that(i don kown what to do 555)
  dmem.io.wen := core.io.dmem.wen 
  dmem.io.valid := core.io.dmem.valid
  dmem.io.raddr := core.io.dmem.raddr 
  dmem.io.waddr := core.io.dmem.waddr
  dmem.io.wdata := core.io.dmem.wdata
  dmem.io.wmask := core.io.dmem.wmask
  core.io.dmem.rdata := dmem.io.rdata 
  dmem.io.clk := clock
}

