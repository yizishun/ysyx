package npc.core

import chisel3._
import chisel3.util._
import npc._
import npc.bus._
import npc.core.idu.Control._

class IfuPcIO extends Bundle{
  val nextPC = Output(UInt(32.W))
}

class IfuOutIO extends Bundle{
  val inst = Output(UInt(32.W))
  val pc = Output(UInt(32.W))
  //val stat = Output(new Stat)
}

class Ifu2IcacheIO extends Bundle{
  val araddr = Output(UInt(32.W))
  val arready = Input(Bool())
  val arvalid = Output(Bool())
  val rvalid = Input(Bool())
  val rready = Output(Bool())
  val rdata = Input(UInt(32.W))
}

class IfuIO(xlen: Int) extends Bundle{
  val in = Flipped(Decoupled(new IfuPcIO))
  val out = Decoupled(new IfuOutIO)
  //coonect to the pfu
  val pc = Decoupled(new IfuPcIO)
  //Connect to the external imem
  val imem = new Ifu2IcacheIO
  val isFlush = Input(Bool())
  val correctedPC = Input(UInt(32.W))
  //val statr = Input(new Stat)
}


class IFU(val conf: npc.CoreConfig) extends Module{
  val io = IO(new IfuIO(conf.xlen))
  //place modules
  val pcInit = if(conf.ysyxsoc){ "h3000_0000".U(32.W) }else if(conf.npc){ "h8000_0000".U(32.W)} else { "h0000_0000".U(32.W) }
  val pcReg = RegEnable(io.in.bits.nextPC, pcInit, io.in.valid)
  val PcPlus4 = Wire(UInt(32.W))
  PcPlus4 := Mux(io.in.valid, io.in.bits.nextPC + 4.U, pcReg + 4.U)

  //state transition(mearly)
  val start = dontTouch(Wire(Bool()))
  val end = dontTouch(Wire(Bool()))
  val ready_go = dontTouch(Wire(Bool()))
  val s_WaitStart :: s_WaitEnd :: s_WaitFlushEnd ::Nil = Enum(3)
  val stateF = RegInit(s_WaitStart)
  val nextStateF = WireDefault(stateF)
  nextStateF := MuxLookup(stateF, s_WaitStart)(Seq(
      s_WaitStart   -> Mux(start, Mux(end, s_WaitStart, s_WaitEnd), s_WaitStart),
      s_WaitEnd     -> Mux(end, s_WaitStart, Mux(io.isFlush, s_WaitFlushEnd, s_WaitEnd)),
      s_WaitFlushEnd-> Mux(io.imem.rvalid, s_WaitStart, s_WaitFlushEnd)
  ))
  io.imem.arvalid := start && stateF === s_WaitStart
  io.imem.rready := end || (stateF === s_WaitFlushEnd)
  stateF := nextStateF
  dontTouch(nextStateF)

  SetupIFU()
  //SetupIRQ()

  //if(conf.useDPIC){
    //import npc.EVENT._
    //PerformanceProbe(clock, IFUGetInst, (io.imem.rvalid & io.imem.rready).asUInt, 0.U, io.imem.arvalid & io.imem.arready, io.imem.rvalid & io.imem.rready)
  //}
  //handshake
  //output logic
  ready_go := io.imem.rvalid && (stateF =/= s_WaitFlushEnd)
  start := io.in.valid && io.imem.arready && !io.isFlush
  end := ready_go && io.out.ready

  io.in.ready := (!io.in.valid || (ready_go && io.out.ready))
  io.out.valid := io.in.valid && ready_go

  val ready_go_pc = dontTouch(Wire(Bool()))
  ready_go_pc := ~(reset.asBool)
  io.pc.valid := ready_go_pc && io.pc.ready

  //data path
//-----------------------------------------------------------------------------------
  def SetupIFU():Unit = {

  //place wires
    //Imem module(external)
    io.imem.araddr := Mux(io.in.valid, io.in.bits.nextPC, pcReg)
  
    //IFU module(wrapper)
    io.out.bits.pc := Mux(io.in.valid, io.in.bits.nextPC, pcReg)
    io.out.bits.inst := io.imem.rdata

    io.pc.bits.nextPC := Mux(io.isFlush, io.correctedPC, PcPlus4)

  }
  //stat logic
//-----------------------------------------------------------------------------------
  //def SetupIRQ():Unit = {
    //val hasIrq = (nextStateC === sc_BetweenFire12_1_2) && io.imem.rvalid && (io.imem.rresp =/= 0.U)
    //io.out.bits.stat.stat := Mux(hasIrq, true.B, false.B)
    //io.out.bits.stat.statNum := Mux(hasIrq, IRQ_IAF, 0.U)
    //when(io.statr.stat){
      //pc.io.nextpc := io.pc.idu.mtvec
      //io.out.bits.stat.stat := false.B
    //}
  //}
}
