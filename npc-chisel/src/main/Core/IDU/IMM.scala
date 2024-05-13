package npc.core.idu

import chisel3._
import chisel3.util._
import Control._

class ImmIO extends Bundle{
  val inst = Input(UInt(32.W))
  val immtype = Input(UInt(3.W))
  val immext = Output(UInt(32.W))
}

class Imm extends Module{
  val io = IO(new ImmIO)

  val immI = io.inst(31, 12).asSInt
  val immU = Cat(io.inst(31, 12), 0.U(12.W)).asSInt
  val immJ = Cat(io.inst(31), io.inst(19, 12), io.inst(20), io.inst(30, 21), 0.U(1.W)).asSInt
  val immS = Cat(io.inst(31, 25), io.inst(11, 7)).asSInt
  val immB = Cat(io.inst(31), io.inst(7), io.inst(30, 25), io.inst(11, 8), 0.U(1.W)).asSInt

  io.immext := MuxLookup(io.immtype, immI)(Seq(
    ImmI -> immI,
    ImmU -> immU,
    ImmJ -> immJ,
    ImmS -> immS,
    ImmB -> immB
  )).asUInt
}