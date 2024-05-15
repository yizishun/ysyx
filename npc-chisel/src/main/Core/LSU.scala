package npc.core

import chisel3._
import chisel3.util._

class LsuOutIO extends Bundle{
  val signals = new Bundle{
    val wbu = new idu.WBUSignals
    val irq = Output(Bool())
  }

  val rd1 = Output(UInt(32.W))
  val aluresult = Output(UInt(32.W))
  val crd1 = Output(UInt(32.W))
  val pc = Output(UInt(32.W))
  val immext = Output(UInt(32.W))
  val PcPlus4 = Output(UInt(32.W))
  val MemR = Output(UInt(32.W))
  val rw = Output(UInt(5.W))
	val crw = Output(UInt(12.W))
}

class LsuIO(xlen: Int) extends Bundle{
  val in = Flipped(Decoupled(new ExuOutIO))
  val out = Decoupled(new LsuOutIO)
  //connect to the external "state" elements(i.e.dmem)
  val dmem = Flipped(new npc.mem.dmemIO(xlen))
}

class LSU(val conf: npc.CoreConfig) extends Module{
  val io = IO(new LsuIO(conf.xlen))
  io.out.valid := 1.U
  io.in.ready := 1.U

//place wires
  val maddr = Wire(UInt(32.W))
  val place = Wire(UInt(32.W))
  val RealMemWmask = Wire(UInt(8.W))
  val rdplace = Wire(UInt(32.W))
  //some operation before connect to dmem
	maddr := io.in.bits.aluresult & (~3.U(32.W))
	place := io.in.bits.aluresult - maddr
	RealMemWmask := io.in.bits.signals.lsu.MemWmask << place(2, 0)
  //Dmem module(external)
  io.dmem.clk := clock
  io.dmem.wen := io.in.bits.signals.lsu.MemWriteE
  io.dmem.valid := io.in.bits.signals.lsu.MemValid
  io.dmem.raddr := maddr
  io.dmem.waddr := maddr
  io.dmem.wdata := io.in.bits.rd2
  io.dmem.wmask := RealMemWmask
  //some operation to read data
  rdplace := io.dmem.rdata >> (place << 3)
  //LSU module(wrapper)
  io.out.bits.signals := io.in.bits.signals
  io.out.bits.rd1 := io.in.bits.rd1
  io.out.bits.aluresult := io.in.bits.aluresult
  io.out.bits.crd1 := io.in.bits.crd1
  io.out.bits.pc := io.in.bits.pc
  io.out.bits.immext := io.in.bits.immext
  io.out.bits.PcPlus4 := io.in.bits.PcPlus4
  io.out.bits.rw := io.in.bits.rw
	io.out.bits.crw := io.in.bits.crw

//place mux
  import idu.Control._
  val rbyte = Cat(io.dmem.rdata(7), rdplace(7, 0)).asSInt
  val rhalfw = Cat(io.dmem.rdata(15), rdplace(15, 0)).asSInt
  val rword = io.dmem.rdata.asSInt
  val rbyteu = Cat(0.U, rdplace(7, 0)).asSInt 
  val rhalfwu = Cat(0.U, rdplace(15, 0)).asSInt
  io.out.bits.MemR := MuxLookup(io.in.bits.signals.lsu.MemRD, rbyte)(Seq(
    RBYTE -> rbyte,
    RHALFW -> rhalfw,
    RWORD -> rword,
    RBYTEU -> rbyteu,
    RHALFWU -> rhalfwu
  )).asUInt
}