package npc.core

import chisel3._
import chisel3.util._
import npc._

class IduPcIO extends Bundle{
  val mepc = Output(UInt(32.W))
  val mtvec = Output(UInt(32.W))
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
  val stat = Output(new Stat)
  //for performance analysis
  val perfSubType = Output(UInt(8.W))
}

class IduIO(xlen: Int) extends Bundle{
  val in = Flipped(Decoupled(new IfuOutIO))
  val out = Decoupled(new IduOutIO)
  //pc value to IFU
  val pc = new IduPcIO
  //connect to the external "state" elements(i.e.gpr,csr,imem)
  val gpr = Flipped(new gprReadIO(xlen))
  val csr = Flipped(new csrReadIO(xlen))
  val statr = Input(new Stat)
}

class IDU(val conf: npc.CoreConfig) extends Module{
  val io = IO(new IduIO(conf.xlen))
//place modules
  val controller = Module(new idu.Controller(conf))
  val imm = Module(new idu.Imm)

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

  SetupIDU()
  SetupIRQ()
  io.out.bits.perfSubType := 0.U
  if(conf.useDPIC) Strob()
  //default,it will error if no do this
  in_ready := false.B
  out_valid := false.B

  switch(nextState){
    is(s_BeforeFire1){
      in_ready := true.B
      out_valid := false.B
      //disable all sequential logic
    }
    is(s_BetweenFire12){
      in_ready := false.B
      out_valid := true.B
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
    
    io.csr.raddr1 := io.in.bits.inst(31, 20)
  
    //IFU module(wrapper)
      //pc value back to IFU
    io.pc.mepc := io.csr.mepc
    io.pc.mtvec := io.csr.mtvec
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
  //irq logic
  def SetupIRQ(): Unit = {
    io.out.bits.stat.stat := controller.io.irq
    io.out.bits.stat.statNum := controller.io.irq_no
    //mech
    when(io.statr.stat){
      state := s_BeforeFire1
      io.out.bits.stat.stat := false.B
    }
    io.csr.irq := io.statr.stat
    io.csr.irq_no := io.statr.statNum
  }
  def Strob(): Unit = {
    import idu.Control._
    import npc.core.exu.AluOp._
    import npc.EVENT._
    val isJump = (controller.io.signals.exu.Jump =/= NJump)
    val isStore = (controller.io.signals.lsu.MemWriteE)
    val isLoad = (controller.io.signals.lsu.MemValid & ~controller.io.signals.lsu.MemWriteE)
    val isCal = (controller.io.signals.exu.alucontrol =/= ALU_XXX && ~isLoad && ~isJump && ~isStore)
    val isCsr = (controller.io.signals.wbu.CSRWriteE === CSRWRITE && controller.io.irq === NIRQ)
    val subType = WireInit(VecInit(Seq.fill(8)(0.U(1.W))))
    io.out.bits.perfSubType := subType.asUInt
    subType(0) := isJump.asUInt
    subType(1) := isStore.asUInt
    subType(2) := isLoad.asUInt
    subType(3) := isCal.asUInt
    subType(4) := isCsr.asUInt
    PerformanceProbe(clock, IDUFinDec, nextState === s_BetweenFire12, subType.asUInt, nextState === s_BetweenFire12, false.B)
  }
}