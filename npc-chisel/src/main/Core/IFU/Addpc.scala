package npc.core.ifu

import chisel3._

class AddpcIO extends Bundle{
  val pc = Input(UInt(32.W))
  val nextpc = Output(UInt(32.W))
}

class Addpc extends Module{
  val io = IO(new AddpcIO)
  io.nextpc := io.pc + 4.U
}