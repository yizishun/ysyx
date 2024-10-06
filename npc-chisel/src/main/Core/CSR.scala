package npc.core

import chisel3._
import chisel3.util._
import npc._

object CsrReg{
  val mstatus = 0x300.U(12.W)
  val mtvec = 0x305.U(12.W)
  val mepc = 0x341.U(12.W)
  val mcause = 0x342.U(12.W)
  val mvendorid = 0xF11.U(12.W)
  val marchid = 0xF12.U(12.W)
  //map
  val csrxx = 0.U(3.W)
  val mstatusIn = 0.U(3.W)
  val mtvecIn = 1.U(3.W)
  val mepcIn = 2.U(3.W)
  val mcauseIn = 3.U(3.W)
  val mvendoridIn = 4.U(3.W)
  val marchidIn = 5.U(3.W)
}

class csrReadIO(xlen: Int) extends Bundle{
  val raddr1 = Input(UInt(12.W))
	val rdata1 = Output(UInt(xlen.W))
  val mtvec = Output(UInt(xlen.W))
  val mepc = Output(UInt(xlen.W))
}

class csrWriteIO(xlen: Int) extends Bundle{
  val wdata = Input(UInt(xlen.W))
  val waddr = Input(UInt(12.W))
  val wen = Input(Bool())
}

class csrIO(xlen: Int) extends Bundle{
  val irq = Input(Bool())
  val irq_no = Input(UInt(8.W))
  val irq_pc = Input(UInt(32.W))
  val read = new csrReadIO(xlen)
  val write = new csrWriteIO(xlen)
}

class csr(conf: CoreConfig) extends Module{
  val io = IO(new csrIO(conf.xlen))
  //val rf = RegInit(VecInit(Seq.fill(4)(0.U(32.W))))
  val rf = Reg(Vec(6, UInt(conf.xlen.W)))
  import CsrReg._
  val waddr_in = Wire(UInt(3.W))
  val raddr_in = Wire(UInt(3.W))
  waddr_in := MuxLookup(io.write.waddr, csrxx)(Seq(
    mstatus -> mstatusIn,
    mtvec -> mtvecIn,
    mepc -> mepcIn,
    mcause -> mcauseIn,
    mvendorid -> mvendoridIn,
    marchid -> marchidIn
  ))
  raddr_in := MuxLookup(io.read.raddr1, csrxx)(Seq(
    mstatus -> mstatusIn,
    mtvec -> mtvecIn,
    mepc -> mepcIn,
    mcause -> mcauseIn,
    mvendorid -> mvendoridIn,
    marchid -> marchidIn
  ))
  io.read.rdata1 := rf(raddr_in)
  io.read.mtvec := rf(mtvecIn)
  io.read.mepc := rf(mepcIn)
  rf(mstatusIn) := 0x1800.U
  rf(mvendoridIn) := "h79737978".U
  rf(marchidIn) := 23060171.U
  when(io.write.wen & ~(io.irq)){
    rf(waddr_in) := io.write.wdata
  }
  when(io.irq){
    rf(mcauseIn) := io.irq_no.asUInt
    rf(mepcIn) := io.irq_pc
  }
}
