package npc.core

import chisel3._
import chisel3.util._
import npc._

class IduOutIO extends Bundle{
  val signals = new Bundle{
    val exu = new idu.EXUSignals
    val lsu = new idu.LSUSignals
    val wbu = new idu.WBUSignals
  }
  val rd1 = Output(UInt(32.W))
  val rd2 = Output(UInt(32.W))
  val crd1 = Output(UInt(32.W))
  val pc = Output(UInt(32.W))
  val immext = Output(UInt(32.W))
	val rw = Output(UInt(5.W))
	val crw = Output(UInt(12.W))
  val isEbreak = Output(Bool())
  val statInst = Output(new Stat)
  //for performance analysis
  val perfSubType = Output(UInt(8.W))
}

class IduIO(xlen: Int) extends Bundle{
  val in = Flipped(Decoupled(new IfuOutIO))
  val out = Decoupled(new IduOutIO)
  //pc value to IFU
  //val pc = Decoupled(new IduPcIO)
  //signals to IFU
  val ifuSignals = Irrevocable(new idu.IFUSignals)
  //connect to the external "state" elements(i.e.gpr,csr,imem)
  val gpr = Flipped(new gprReadIO(xlen))
  val csr = Flipped(new csrReadIO(xlen))
  val rs1ren = Output(Bool())
  val rs2ren = Output(Bool())
  val isStall = Input(Bool())
  val isFlush = Input(Bool())
  val statCore = Input(new Stat)
}

class IDU(val conf: npc.CoreConfig) extends Module{
  val io = IO(new IduIO(conf.xlen))
//place modules
  val controller = Module(new idu.Controller(conf))
  val imm = Module(new idu.Imm)

  SetupIDU()
  SetupIRQ()
  io.out.bits.perfSubType := 0.U
  //if(conf.useDPIC) Strob()
  //default,it will error if no do this
  val ready_go = dontTouch(Wire(Bool()))
  ready_go := ~io.isStall
  io.in.ready := !io.in.valid || (ready_go && io.out.ready)
  io.out.valid := io.in.valid && ready_go
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
    io.rs1ren := controller.io.signals.idu.Rs1Ren
    io.rs2ren := controller.io.signals.idu.Rs2Ren
  
    //CSR module(external)
    
    io.csr.raddr1 := io.in.bits.inst(31, 20)
  
    //Fence.i
    io.ifuSignals <> controller.io.signals.ifu
      //out to EXU
    io.out.bits.signals := controller.io.signals
    io.out.bits.rd1 := io.gpr.rdata1
    io.out.bits.rd2 := io.gpr.rdata2
    io.out.bits.crd1 := io.csr.rdata1
    io.out.bits.pc := io.in.bits.pc
    io.out.bits.immext := imm.io.immext
    io.out.bits.rw := io.in.bits.inst(11, 7)
    io.out.bits.crw := io.in.bits.inst(31, 20)
    io.out.bits.isEbreak := (io.in.bits.inst === "b00000000000100000000000001110011".U)

  }
  //irq logic--------------------------------------------------------------------------
  def SetupIRQ(): Unit = {
    io.out.bits.statInst.stat := controller.io.irq
    io.out.bits.statInst.statNum := controller.io.irq_no
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
    PerformanceProbe(clock, IDUFinDec, RegNext(io.in.ready) & io.in.valid, subType.asUInt, RegNext(io.in.ready) & io.in.valid, false.B)
  }
}