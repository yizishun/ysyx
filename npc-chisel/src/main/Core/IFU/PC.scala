package npc.core.ifu

import chisel3._
import chisel3.util._

class pcIO extends Bundle{
  val nextpc = Input(UInt(32.W))
  val pc = Output(UInt(32.W))
}

class pc extends Module{
  val io = IO(new pcIO)
  val pcReg = RegInit("h8000_0000".U(32.W))
  pcReg := io.nextpc
  io.pc := pcReg
}