package npc.core

import chisel3._
import chisel3.util._

class ExuPcIO extends Bundle{
  val PcPlusImm = Output(UInt(32.W))
  val PcPlusRs2 = Output(UInt(32.W))
  val PCSrc = Output(UInt(3.W))
}

class ExuOutIO extends Bundle{
  val signals = new Bundle{
    val lsu = new idu.LSUSignals
    val wbu = new idu.WBUSignals
    val irq = Output(Bool())
  }
  val aluresult = Output(UInt(32.W))
  val crd1 = Output(UInt(32.W))
  val pc = Output(UInt(32.W))
  val immext = Output(UInt(32.W))
  val PcPlus4 = Output(UInt(32.W))
  val rd1 = Output(UInt(32.W))
  val rd2 = Output(UInt(32.W))
  val rw = Output(UInt(5.W))
	val crw = Output(UInt(12.W))
}

class ExuIO extends Bundle{
  val in = Flipped(Decoupled(new IduOutIO))
  val out = Decoupled(new ExuOutIO)
  val pc = new ExuPcIO
}

class EXU(val conf: npc.CoreConfig) extends Module{
  val io = IO(new ExuIO)
  io.out.valid := 1.U
  io.in.ready := 1.U

//place modules
  val alu = Module(new exu.Alu(conf.xlen))
  val idupc = Module(new exu.IduPC)
  val jumpPc = Module(new exu.JumpPc)

//place mux
  import idu.Control._
  val alusrcA = Wire(UInt(32.W))
  val alusrcB = Wire(UInt(32.W))
  alusrcA := MuxLookup(io.in.bits.signals.exu.AluSrcA, io.in.bits.rd1)(Seq(
    ALUaPC -> io.in.bits.pc,
    ALUaRD1-> io.in.bits.rd1
  ))

  alusrcB := MuxLookup(io.in.bits.signals.exu.AluSrcB, io.in.bits.rd2)(Seq(
    ALUbimm -> io.in.bits.immext,
    ALUbRD2 -> io.in.bits.rd2
  ))

//place wires
  //Alu module
  alu.io.A := alusrcA
  alu.io.B := alusrcB
  alu.io.AluControl := io.in.bits.signals.exu.alucontrol

  //IduPc module
  idupc.io.Jump := io.in.bits.signals.exu.Jump
  idupc.io.zf := alu.io.Zf
  idupc.io.cmp := alu.io.result(0)

  //JumpPc module
  jumpPc.io.pc := io.in.bits.pc
  jumpPc.io.imm := io.in.bits.immext

  //EXU module(wrapper)
    //pc values back to IFU
  io.pc.PcPlusImm := jumpPc.io.nextpc
  io.pc.PcPlusRs2 := alu.io.result & (~1.U(32.W))
  io.pc.PCSrc := idupc.io.PCSrc
    //out to LSU
  io.out.bits.signals := io.in.bits.signals
  io.out.bits.aluresult := alu.io.result
  io.out.bits.crd1 := io.in.bits.crd1
  io.out.bits.pc := io.in.bits.pc
  io.out.bits.immext := io.in.bits.immext
  io.out.bits.PcPlus4 := io.in.bits.PcPlus4
  io.out.bits.rd1 := io.in.bits.rd1
  io.out.bits.rd2 := io.in.bits.rd2
  io.out.bits.rw := io.in.bits.rw
	io.out.bits.crw := io.in.bits.crw
  
}