package npc.core

import chisel3._
import chisel3.util._
import npc._

class WbuOutIO extends Bundle{

}

class WbuIO(xlen: Int) extends Bundle{
  val in = Flipped(Decoupled(new LsuOutIO))
  val out = Decoupled(new WbuOutIO)
  //connect to the external "state" elements(i.e.gpr,csr)
  val gpr = Flipped(new gprWriteIO(xlen))
  val csr = Flipped(new csrWriteIO(xlen))
  val statw = Output(new Stat)
  val statEn = Output(Bool())
  //read the stat reg
  val statr = Input(new Stat)
}

class WBU(val conf: npc.CoreConfig) extends Module{
  val io = IO(new WbuIO(conf.xlen))

  val in_ready = RegInit(io.in.ready)
  val out_valid = RegInit(io.out.valid)
  io.in.ready := in_ready
  io.out.valid := out_valid

  val s_BeforeFire1 :: s_BetweenFire12 :: Nil = Enum(2)
  val state = RegInit(s_BeforeFire1)
  val nextState = WireDefault(state)
  nextState := MuxLookup(state, s_BeforeFire1)(Seq(
      s_BeforeFire1   -> Mux(io.in.fire, s_BetweenFire12, s_BeforeFire1),
      s_BetweenFire12 -> Mux(io.out.fire, s_BeforeFire1, s_BetweenFire12)
  ))
  state := nextState

  SetupWBU()
  SetupIRQ()
  import npc.EVENT._
  PerformanceProbe(clock, IDUFinDec, 0.U, io.in.bits.perfSubType, false.B, nextState === s_BetweenFire12, false.B)

  //default,it will error if no do this
  in_ready := false.B
  out_valid := false.B
  
  switch(nextState){
    is(s_BeforeFire1){
      in_ready := true.B
      out_valid := false.B
      //disable all sequential logic
      io.gpr.wen := false.B
      io.csr.wen := false.B
    }
    is(s_BetweenFire12){
      in_ready := false.B
      out_valid := true.B
      //save all output to regs
    }
  }


  //------------------------------------------------------------------------------------------------------
  def SetupWBU():Unit = {
  //place mux
    import idu.Control._
    val CSRWriteD = Wire(UInt(2.W))
    io.gpr.wdata := MuxLookup(io.in.bits.signals.wbu.RegwriteD, io.in.bits.aluresult)(Seq(
      RAluresult -> io.in.bits.aluresult,
      RImm -> io.in.bits.immext,
      RPcPlus4 -> io.in.bits.PcPlus4,
      RMemRD -> io.in.bits.MemR,
      RCSR -> io.in.bits.crd1
    ))
  
    CSRWriteD := io.in.bits.signals.wbu.CSRWriteD
  
    io.csr.wdata := MuxLookup(CSRWriteD, io.in.bits.rd1)(Seq(
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
    //WBU(wrapper)

  }
  def SetupIRQ():Unit = {
    io.statw := io.in.bits.stat
    io.statEn := (nextState === s_BetweenFire12) || io.statr.stat
    //mech
    when(io.statr.stat){
      io.csr.wdata := io.in.bits.pc
    }
  }
}