package npc.core.exu

import chisel3._
import chisel3.util._

class JumpPcIO extends Bundle{
  val pc = Input(UInt(32.W))
  val imm = Input(UInt(32.W))
  val nextpc = Output(UInt(32.W))
}

class JumpPc extends Module{
  val io = IO(new JumpPcIO)
  io.nextpc := (io.pc + io.imm)&(1.U(32.W))
}