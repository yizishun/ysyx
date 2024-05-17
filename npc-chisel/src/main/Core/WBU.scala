package npc.core

import chisel3._
import chisel3.util._

class WbuOutIO extends Bundle{}

class WbuIO(xlen: Int) extends Bundle{
  val in = Flipped(Decoupled(new LsuOutIO))
  val out = Decoupled(new WbuOutIO)
  //connect to the external "state" elements(i.e.gpr,csr)
  val gpr = Flipped(new gprWriteIO(xlen))
  val csr = Flipped(new csrWriteIO(xlen))
}

class WBU(val conf: npc.CoreConfig) extends Module{
  val io = IO(new WbuIO(conf.xlen))

  val s_BeforeFire1 :: s_BetweenFire12 :: Nil = Enum(2)
  val state = RegInit(s_BeforeFire1)
  state := MuxLookup(state, s_BeforeFire1)(Seq(
      s_BeforeFire1   -> Mux(io.in.fire, s_BetweenFire12, s_BeforeFire1),
      s_BetweenFire12 -> Mux(io.out.fire, s_BeforeFire1, s_BetweenFire12)
  ))

  SetupWBU()

  //default,it will error if no do this
  io.in.ready := false.B
  io.out.valid := false.B
  
  switch(state){
    is(s_BeforeFire1){
      io.in.ready := true.B
      io.out.valid := false.B
      //disable all sequential logic
      io.gpr.wen := false.B
      io.csr.wen := false.B
    }
    is(s_BetweenFire12){
      io.in.ready := false.B
      io.out.valid := true.B
      //save all output to regs
    }
  }


  //------------------------------------------------------------------------------------------------------
  def SetupWBU():Unit = {
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
}