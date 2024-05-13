package npc.mem

import chisel3._
import chisel3.util._
import npc._

class imemIO(xlen: Int) extends Bundle{
  val valid = Input(Bool())
  val pc = Input(UInt(xlen.W))
  val inst = Output(UInt(xlen.W))
}
class dmemIO(xlen: Int) extends Bundle{
  val clk = Input(Clock())
  val wen = Input(Bool())
  val valid = Input(Bool())
  val raddr = Input(UInt(xlen.W))
  val waddr = Input(UInt(xlen.W))
  val wdata = Input(UInt(xlen.W)) 
  val wmask = Input(UInt(8.W))
  val rdata = Output(UInt(xlen.W))
}

class Imem(coreConfig: CoreConfig) extends BlackBox{
  val io = IO(new imemIO(coreConfig.xlen))
}
class Dmem(coreConfig: CoreConfig) extends BlackBox{
  val io = IO(new dmemIO(coreConfig.xlen))
} 