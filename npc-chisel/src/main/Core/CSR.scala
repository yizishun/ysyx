package npc.core

import chisel3._
import chisel3.util._
import npc._

object CsrReg{
  val mstatus = 0x300.U(12.W)
  val mtvec = 0x305.U(12.W)
  val mepc = 0x341.U(12.W)
  val mcause = 0x342.U(12.W)
  //map
  val csrxx = 0.U(2.W)
  val mstatusIn = 0.U(2.W)
  val mtvecIn = 1.U(2.W)
  val mepcIn = 2.U(2.W)
  val mcauseIn = 3.U(2.W)
}

class csrIO(xlen: Int) extends Bundle{
  val irq = Input(Bool())
  val irq_no = Input(UInt(8.W))
  val wdata = Input(UInt(xlen.W))
  val waddr = Input(UInt(12.W))
  val wen = Input(Bool())
  val raddr1 = Input(UInt(12.W))
	val rdata1 = Output(UInt(xlen.W))
  val mtvec = Output(UInt(xlen.W))
  val mepc = Output(UInt(xlen.W))
}

class csr(conf: CoreConfig) extends Module{
  val io = IO(new csrIO(conf.xlen))
  //val rf = RegInit(VecInit(Seq.fill(4)(0.U(32.W))))
  val rf = Mem(4, UInt(conf.xlen.W))
  import CsrReg._
  val waddr_in = Wire(UInt(2.W))
  val raddr_in = Wire(UInt(2.W))
  waddr_in := MuxLookup(io.waddr, csrxx)(Seq(
    mstatus -> mstatusIn,
    mtvec -> mtvecIn,
    mepc -> mepcIn,
    mcause -> mcauseIn
  ))
  raddr_in := MuxLookup(io.raddr1, csrxx)(Seq(
    mstatus -> mstatusIn,
    mtvec -> mtvecIn,
    mepc -> mepcIn,
    mcause -> mcauseIn
  ))
  io.rdata1 := rf(raddr_in)
  io.mtvec := rf(mtvecIn)
  io.mepc := rf(mepcIn)
  rf(mstatusIn) := 0x1800.U
  when(io.wen){
    rf(waddr_in) := io.wdata
  }
  when(io.irq){
    rf(mcauseIn) := io.irq_no.asUInt
    rf(mepcIn) := io.wdata
  }
}
