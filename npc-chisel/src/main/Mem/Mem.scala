package npc.mem

import chisel3._
import chisel3.util._
import npc._

class memIO(xlen: Int) extends Bundle{
  val clk = Input(Clock())
  val rst = Input(Reset())
  //AR
  val araddr = Input(UInt(xlen.W))
  val arvalid = Input(Bool())
  val arready = Output(Bool())
  //R
  val rdata = Output(UInt(xlen.W))
  val rresp = Output(UInt(2.W))
  val rvalid = Output(Bool())
  val rready = Input(Bool())
  //AW
  val awaddr = Input(UInt(xlen.W))
  val awvalid = Input(Bool())
  val awready = Output(Bool())
  //W
  val wdata = Input(UInt(xlen.W))
  val wstrb = Input(UInt((xlen/8).W))
  val wvalid = Input(Bool())
  val wready = Output(Bool())
  //B
  val bresp = Output(UInt(2.W))
  val bvalid = Output(Bool())
  val bready = Input(Bool())
}

class Mem(coreConfig: CoreConfig) extends BlackBox with HasBlackBoxPath{
  val io = IO(new memIO(coreConfig.xlen))
  addPath("/Users/yizishun/ysyx-workbench/npc-chisel/src/main/mem/Mem.sv")
} 

class Uart(coreConfig: CoreConfig) extends BlackBox with HasBlackBoxPath{
  val io = IO(new memIO(coreConfig.xlen))
  addPath("/Users/yizishun/ysyx-workbench/npc-chisel/src/main/Dev/UART.sv")
}

class Clint(coreConfig: CoreConfig) extends BlackBox with HasBlackBoxPath{
  val io = IO(new memIO(coreConfig.xlen))
  addPath("/Users/yizishun/ysyx-workbench/npc-chisel/src/main/Dev/CLINT.sv")
}
