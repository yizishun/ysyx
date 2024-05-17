package npc.core

import chisel3._
import chisel3.util._

class pcIO extends Bundle{
  val idu = Flipped(new IduPcIO)
  val exu = Flipped(new ExuPcIO)
}

class IfuOutIO extends Bundle{
  val inst = Output(UInt(32.W))
  val PcPlus4 = Output(UInt(32.W))
  val pc = Output(UInt(32.W))
}

class IfuIO(xlen: Int) extends Bundle{
  val in = Flipped(Decoupled(new WbuOutIO))
  val out = Decoupled(new IfuOutIO)
  //pc value from IDU and EXU
  val pc = new pcIO
  //Connext to the external imem
  val imem = Flipped(new npc.mem.imemIO(xlen))
}


class IFU(val conf: npc.CoreConfig) extends Module{
  val io = IO(new IfuIO(conf.xlen))
  //place modules
  val addpc = Module(new ifu.Addpc)
  val pc = Module(new ifu.PC)

  val s_BeforeFire1 :: s_BetweenFire12 :: Nil = Enum(2)
  val state = RegInit(s_BetweenFire12)
  state := MuxLookup(state, s_BeforeFire1)(Seq(
      s_BeforeFire1   -> Mux(io.in.fire, s_BetweenFire12, s_BeforeFire1),
      s_BetweenFire12 -> Mux(io.out.fire, s_BeforeFire1, s_BetweenFire12)
  ))
  val prevState = RegNext(state, 0.U)

  SetupIFU()

  //default,it will error if not do this
  io.in.ready := false.B
  io.out.valid := false.B
  io.imem.validPC := false.B

  switch(state){
    is(s_BeforeFire1){
      io.out.valid := false.B
      io.in.ready := true.B
      //disable all sequential logic
      io.imem.validPC := false.B
    }
    is(s_BetweenFire12){
      io.in.ready := false.B
      io.out.valid := io.imem.validInst
      //save all output into regs
      io.imem.validPC := Mux(state =/= prevState, true.B, false.B)
    }
  }

  
//-----------------------------------------------------------------------------------
  def SetupIFU():Unit = {
  
  //place mux
    import npc.core.idu.Control._
    val PCSrc = Wire(UInt(3.W))
    val nextpc = Wire(UInt(32.W))
    PCSrc := MuxLookup(io.pc.idu.irq, PcXXXXXXX)(Seq(
      false.B -> io.pc.exu.PCSrc,
      true.B -> Mtvec
    ))
  
    nextpc := MuxLookup(PCSrc, addpc.io.nextpc)(Seq(
      PcPlus4 -> addpc.io.nextpc,
      PcPlusImm -> io.pc.exu.PcPlusImm,
      PcPlusRs2 -> io.pc.exu.PcPlusRs2,
      Mtvec -> io.pc.idu.mtvec,
      Mepc -> io.pc.idu.mepc
    ))
  
  //place wires
    //Addpc module
    addpc.io.pc := pc.io.pc
  
    //PC module
    pc.io.nextpc := nextpc
  
    //Imem module(external)
    io.imem.clk := clock
    io.imem.rst := reset
    io.imem.pc := pc.io.pc
    pc.io.wen := true.B & io.in.valid
  
    //IFU module(wrapper)
    io.out.bits.PcPlus4 := addpc.io.nextpc
    io.out.bits.pc := pc.io.pc
    io.out.bits.inst := io.imem.inst

  }
}
