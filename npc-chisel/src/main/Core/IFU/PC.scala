package npc.core.ifu

import chisel3._
import chisel3.util._

class pcIO extends Bundle{
  val nextpc = Input(UInt(32.W))
  val wen = Input(Bool())
  val pc = Output(UInt(32.W))
}

class PC extends Module{
  val io = IO(new pcIO)
  val pcReg = RegEnable(io.nextpc, "h2000_0000".U(32.W), io.wen)
  io.pc := pcReg
}