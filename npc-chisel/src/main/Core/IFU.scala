package npc.core

import chisel3._
import chisel3.util._

class pcIO extends Bundle{
  val idu = Flipped(new IduPcIO)
  val exu = Flipped(new ExuPcIO)
}

class IfuOutIO extends Bundle{
  val inst = Output(UInt(32.W))
  val PcPlus4 = Output(UInt(32.W))
  val pc = Output(UInt(32.W))
}

class IfuIO(xlen: Int) extends Bundle{
  val out = Decoupled(new IfuOutIO)
  //pc value from IDU and EXU
  val pc = new pcIO
  //Connext to the external imem
  val imem = Flipped(new npc.mem.imemIO(xlen))
}


class IFU(val conf: npc.CoreConfig) extends Module{
  val io = IO(new IfuIO(conf.xlen))
  io.out.valid := 1.U

//place modules
  val addpc = Module(new ifu.Addpc)
  val pc = Module(new ifu.PC)

//place mux
  import npc.core.idu.Control._
  val PCSrc = Wire(UInt(3.W))
  val nextpc = Wire(UInt(32.W))
  PCSrc := MuxLookup(io.pc.idu.irq, PcXXXXXXX)(Seq(
    false.B -> io.pc.exu.PCSrc,
    true.B -> Mtvec
  ))

  nextpc := MuxLookup(PCSrc, addpc.io.nextpc)(Seq(
    PcPlus4 -> addpc.io.nextpc,
    PcPlusImm -> io.pc.exu.PcPlusImm,
    PcPlusRs2 -> io.pc.exu.PcPlusRs2,
    Mtvec -> io.pc.idu.mtvec,
    Mepc -> io.pc.idu.mepc
  ))

//place wires
  //Addpc module
  addpc.io.pc := pc.io.pc

  //PC module
  pc.io.nextpc := nextpc

  //Imem module(external)
  io.imem.pc := pc.io.pc
  io.imem.valid := ~(clock.asUInt | reset.asUInt)

  //IFU module(wrapper)
  io.out.bits.PcPlus4 := addpc.io.nextpc
  io.out.bits.pc := pc.io.pc
  io.out.bits.inst := io.imem.inst
}