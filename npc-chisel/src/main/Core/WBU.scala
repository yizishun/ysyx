package npc.core

import chisel3._
import chisel3.util._

class WbuIO(xlen: Int) extends Bundle{
  val in = Flipped(Decoupled(new LsuOutIO))
  //connect to the external "state" elements(i.e.gpr,csr)
  val gpr = Flipped(new gprWriteIO(xlen))
  val csr = Flipped(new csrWriteIO(xlen))
}

class WBU(val conf: npc.CoreConfig) extends Module{
  val io = IO(new WbuIO(conf.xlen))
  io.in.ready := 1.U

//place mux
  import idu.Control._
  val CSRWriteD_irq = Wire(UInt(2.W))
  io.gpr.wdata := MuxLookup(io.in.bits.signals.wbu.RegwriteD, io.in.bits.aluresult)(Seq(
    RAluresult -> io.in.bits.aluresult,
    RImm -> io.in.bits.immext,
    RPcPlus4 -> io.in.bits.PcPlus4,
    RMemRD -> io.in.bits.MemR,
    RCSR -> io.in.bits.crd1
  ))

  CSRWriteD_irq := MuxLookup(io.in.bits.signals.irq, CPC)(Seq(
    false.B -> io.in.bits.signals.wbu.CSRWriteD,
    true.B -> CPC
  ))

  io.csr.wdata := MuxLookup(CSRWriteD_irq, io.in.bits.rd1)(Seq(
    CRD1 -> io.in.bits.rd1,
    CRD1OR -> (io.in.bits.rd1 | io.in.bits.crd1),
    CPC -> io.in.bits.pc
  ))
  
//place wires
  //GPR module(external)
  io.gpr.waddr := io.in.bits.rw
  io.gpr.wen := io.in.bits.signals.wbu.RegwriteE
  //CSR module(external)
  io.csr.waddr := io.in.bits.crw
  io.csr.wen := io.in.bits.signals.wbu.CSRWriteE
}