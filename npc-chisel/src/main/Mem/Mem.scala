package npc.mem

import chisel3._
import chisel3.util._
import npc._

class imemIO(xlen: Int) extends Bundle{
  val clk = Input(Clock())
  val rst = Input(Reset())
  val validPC = Input(Bool())
  val pc = Input(UInt(xlen.W))
  val inst = Output(UInt(xlen.W))
  val validInst = Output(Bool())
}
class dmemIO(xlen: Int) extends Bundle{
  val clk = Input(Clock())
  val rst = Input(Reset())
  val wen = Input(Bool())
  val valid = Input(Bool())
  val raddr = Input(UInt(xlen.W))
  val waddr = Input(UInt(xlen.W))
  val wdata = Input(UInt(xlen.W)) 
  val wmask = Input(UInt(8.W))
  val validData = Output(Bool())
  val rdata = Output(UInt(xlen.W))
}

class Imem(coreConfig: CoreConfig) extends BlackBox with HasBlackBoxPath{
  val io = IO(new imemIO(coreConfig.xlen))
  addPath("/Users/yizishun/ysyx-workbench/npc-chisel/src/main/mem/Mem.sv")
}
class Dmem(coreConfig: CoreConfig) extends BlackBox with HasBlackBoxPath{
  val io = IO(new dmemIO(coreConfig.xlen))
  addPath("/Users/yizishun/ysyx-workbench/npc-chisel/src/main/mem/Mem.sv")
} 