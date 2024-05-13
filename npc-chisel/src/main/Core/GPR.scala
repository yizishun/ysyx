package npc.core

import chisel3._
import chisel3.util._
import npc._

class gprIO(xlen: Int) extends Bundle{
  val wdata = Input(UInt(xlen.W))
  val waddr = Input(UInt(5.W))
  val wen = Input(Bool())
	val raddr1 = Input(UInt(5.W))
	val raddr2 = Input(UInt(5.W))
	val rdata1 = Output(UInt(xlen.W))
	val rdata2 = Output(UInt(xlen.W))
}

class gpr(conf: CoreConfig) extends Module{
  val io = IO(new gprIO(conf.xlen))
  //val rf = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))
  val rf = Mem(32, UInt(conf.xlen.W))
  io.rdata1 := Mux(io.raddr1.orR, rf(io.raddr1), 0.U)
  io.rdata2 := Mux(io.raddr2.orR, rf(io.raddr2), 0.U)
  when(io.wen & io.waddr.orR){
    rf(io.waddr) := io.wdata
  }
}