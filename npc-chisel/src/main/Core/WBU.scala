package npc.core

import chisel3._
import chisel3.util._
import npc._
class Ebreak(conf: npc.CoreConfig) extends BlackBox with HasBlackBoxPath{
  val io = IO(new Bundle{
    val isEbreak = Input(Bool())
  })
  if(conf.useDPIC)
    addPath("/Users/yizishun/ysyx-workbench/npc-chisel/src/main/core/idu/Ebreak.sv")
}


class WbuOutIO extends Bundle{

}

class WbuIO(xlen: Int) extends Bundle{
  val in = Flipped(Decoupled(new LsuOutIO))
  //connect to the external "state" elements(i.e.gpr,csr)
  val gpr = Flipped(new gprWriteIO(xlen))
  val csr = Flipped(new csrWriteIO(xlen))
  //val statw = Output(new Stat)
  //val statEn = Output(Bool())
  //read the stat reg
  //val statr = Input(new Stat)
}

class WBU(val conf: npc.CoreConfig) extends Module{
  val io = IO(new WbuIO(conf.xlen))

  SetupWBU()
  val ebreak = if(conf.useDPIC) Some(Module(new Ebreak(conf))) else None
  if(conf.useDPIC) ebreak.get.io.isEbreak := io.in.bits.isEbreak
  //SetupIRQ()

  //if(conf.useDPIC){
    //import npc.EVENT._
    //PerformanceProbe(clock, IDUFinDec, 0.U, io.in.bits.perfSubType, false.B, io.in.valid, false.B)
  //}
  //default,it will error if no do this

  io.in.ready := true.B

  //------------------------------------------------------------------------------------------------------
  def SetupWBU():Unit = {
  //place mux
    import idu.Control._
    val CSRWriteD = Wire(UInt(2.W))
    io.gpr.wdata := MuxLookup(io.in.bits.signals.wbu.RegwriteD, io.in.bits.aluresult)(Seq(
      RAluresult -> io.in.bits.aluresult,
      RImm -> io.in.bits.immext,
      RPcPlus4 -> (io.in.bits.pc + 4.U),
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
    io.gpr.wen := io.in.bits.signals.wbu.RegwriteE & io.in.valid
    //CSR module(external)
    io.csr.waddr := io.in.bits.crw
    io.csr.wen := io.in.bits.signals.wbu.CSRWriteE & io.in.valid
    //WBU(wrapper)

  }
//  def SetupIRQ():Unit = {
//    io.statw := io.in.bits.stat
//    io.statEn := (nextState === s_BetweenFire12) || io.statr.stat
//    //mech
//    when(io.statr.stat){
//      io.csr.wdata := io.in.bits.pc
//    }
//  }
}