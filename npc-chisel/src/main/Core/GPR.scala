package npc.core

import chisel3._
import chisel3.util._
import npc._

class gprReadIO(xlen: Int) extends Bundle{
	val raddr1 = Input(UInt(5.W))
	val raddr2 = Input(UInt(5.W))
	val rdata1 = Output(UInt(xlen.W))
	val rdata2 = Output(UInt(xlen.W))
}

class gprWriteIO(xlen: Int) extends Bundle{
  val wdata = Input(UInt(xlen.W))
  val waddr = Input(UInt(5.W))
  val wen = Input(Bool())
}

class gprIO(xlen: Int) extends Bundle{
  val read = new gprReadIO(xlen)
  val write = new gprWriteIO(xlen)
}

class gpr(conf: CoreConfig) extends Module{
  val io = IO(new gprIO(conf.xlen))
  //val rf = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))
  val rf = Mem(16, UInt(conf.xlen.W))
  io.read.rdata1 := Mux(io.read.raddr1.orR, rf(io.read.raddr1), 0.U)
  io.read.rdata2 := Mux(io.read.raddr2.orR, rf(io.read.raddr2), 0.U)
  when(io.write.wen & io.write.waddr.orR){
    rf(io.write.waddr) := io.write.wdata
  }
}