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

  //backbox must do that(i don kown how to do 555)
  imem.io.validPC := core.io.imem.validPC
  imem.io.pc := core.io.imem.pc
  core.io.imem.inst := imem.io.inst
  core.io.imem.validInst := imem.io.validInst
  imem.io.clk := clock
  imem.io.rst := reset

  dmem.io.wen := core.io.dmem.wen 
  dmem.io.valid := core.io.dmem.valid
  dmem.io.raddr := core.io.dmem.raddr 
  dmem.io.waddr := core.io.dmem.waddr
  dmem.io.wdata := core.io.dmem.wdata
  dmem.io.wmask := core.io.dmem.wmask
  core.io.dmem.rdata := dmem.io.rdata
  core.io.dmem.validData := dmem.io.validData 
  dmem.io.clk := clock
  dmem.io.rst := reset
}

