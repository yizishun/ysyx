package npc.core

import chisel3._
import chisel3.util._

class IduPcIO extends Bundle{
  val mepc = Output(UInt(32.W))
  val mtvec = Output(UInt(32.W))
  val irq = Output(Bool())
}

class IduOutIO extends Bundle{
  val signals = new idu.Signals
  val rd1 = Output(UInt(32.W))
  val rd2 = Output(UInt(32.W))
  val crd1 = Output(UInt(32.W))
  val pc = Output(UInt(32.W))
  val PcPlus4 = Output(UInt(32.W))
  val immext = Output(UInt(32.W))
	val rw = Output(UInt(5.W))
	val crw = Output(UInt(12.W))
}

class IduIO(xlen: Int) extends Bundle{
  val in = Flipped(Decoupled(new IfuOutIO))
  val out = Decoupled(new IduOutIO)
  //pc value to IFU
  val pc = new IduPcIO
  //connect to the external "state" elements(i.e.gpr,csr,imem)
  val gpr = Flipped(new gprReadIO(xlen))
  val csr = Flipped(new csrReadIO(xlen))
}

class IDU(val conf: npc.CoreConfig) extends Module{
  val io = IO(new IduIO(conf.xlen))
//place modules
  val controller = Module(new idu.Controller)
  val imm = Module(new idu.Imm)

  val s_BeforeFire1 :: s_BetweenFire12 :: Nil = Enum(2)
  val state = RegInit(s_BeforeFire1)
  state := MuxLookup(state, s_BeforeFire1)(Seq(
      s_BeforeFire1   -> Mux(io.in.fire, s_BetweenFire12, s_BeforeFire1),
      s_BetweenFire12 -> Mux(io.out.fire, s_BeforeFire1, s_BetweenFire12)
  ))

  SetupIDU()

  //default,it will error if no do this
  io.in.ready := false.B
  io.out.valid := false.B

  switch(state){
    is(s_BeforeFire1){
      io.in.ready := true.B
      io.out.valid := false.B
      //disable all sequential logic
    }
    is(s_BetweenFire12){
      io.in.ready := false.B
      io.out.valid := true.B
      //save all output into regs
    }
  }

//-----------------------------------------------------------------------
  def SetupIDU():Unit ={
  //place wires
    //Controller module
    controller.io.inst := io.in.bits.inst
  
    //Imm module
    imm.io.inst := io.in.bits.inst
    imm.io.immtype := controller.io.signals.idu.immtype
  
    //GPR module(external)
    io.gpr.raddr1 := io.in.bits.inst(19, 15)
    io.gpr.raddr2 := io.in.bits.inst(24, 20)
  
    //CSR module(external)
    io.csr.irq := controller.io.signals.irq
    io.csr.irq_no := controller.io.signals.irq_no
    io.csr.raddr1 := io.in.bits.inst(31, 20)
  
    //IFU module(wrapper)
      //pc value back to IFU
    io.pc.mepc := io.csr.mepc
    io.pc.mtvec := io.csr.mtvec
    io.pc.irq := controller.io.signals.irq
      //out to EXU
    io.out.bits.signals := controller.io.signals
    io.out.bits.rd1 := io.gpr.rdata1
    io.out.bits.rd2 := io.gpr.rdata2
    io.out.bits.crd1 := io.csr.rdata1
    io.out.bits.pc := io.in.bits.pc
    io.out.bits.PcPlus4 := io.in.bits.PcPlus4
    io.out.bits.immext := imm.io.immext
    io.out.bits.rw := io.in.bits.inst(11, 7)
    io.out.bits.crw := io.in.bits.inst(31, 20)
  }
}