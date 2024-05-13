package npc.core.exu

import chisel3._
import chisel3.util._

class IduPCIO extends Bundle{
  val Jump = Input(UInt(4.W))
  val zf = Input(UInt(1.W))
  val cmp = Input(UInt(1.W))
  val PCSrc = Output(UInt(3.W))
}

import npc.core.idu.Control._
class IduPC extends Module{
  val io = IO(new IduPCIO)
  io.PCSrc := MuxCase(PcXXXXXXX, Array(
    (io.Jump === Jumpbeq) -> Mux(io.zf.asBool, PcPlusImm, PcPlus4),
    (io.Jump === Jumpbne) -> Mux(io.zf.asBool, PcPlus4, PcPlusImm),
    (io.Jump === Jumpbge) -> Mux(io.cmp.asBool, PcPlus4, PcPlusImm),
    (io.Jump === Jumpblt) -> Mux(io.cmp.asBool, PcPlusImm, PcPlus4),
    (io.Jump === Jumpbltu)-> Mux(io.cmp.asBool, PcPlusImm, PcPlus4),
    (io.Jump === Jumpbgeu)-> Mux(io.cmp.asBool, PcPlus4, PcPlusImm),
    (io.Jump === Jumpjal) -> PcPlusImm,
    (io.Jump === Jumpjalr)-> PcPlusRs2,
    (io.Jump === Jumpmret)-> Mepc
  ))
}