package npc.core

import chisel3._
import chisel3.util._
import npc.dev._
import npc._
import npc.bus._
import npc.core.idu.Control._

class Stat extends Bundle{
  val stat = Bool()
  val statNum = UInt(8.W)
}

class CoreIO(xlen : Int) extends Bundle{
  val imem = new AXI4Master
  val dmem = new AXI4Master
}

class Core(val conf : CoreConfig) extends Module {
  val io = IO(new CoreIO(conf.xlen))
  val ifu = Module(new IFU(conf))
  val idu = Module(new IDU(conf))
  val exu = Module(new EXU(conf))
  val lsu = Module(new LSU(conf))
  val wbu = Module(new WBU(conf))

  val icache = Module(new ICache(4, 1, 8, conf))

  //"state" elements in npc core
  import npc.core.idu.Control._
  val gpr = Module(new gpr(conf))
  val csr = Module(new csr(conf))
  //irq logic
  val stat = RegEnable(wbu.io.statCoreW, 0.U.asTypeOf(new Stat), wbu.io.statCoreWEn)
  ifu.io.statCore := stat
  idu.io.statCore := stat
  exu.io.statCore := stat
  lsu.io.statCore := stat
  wbu.io.statCoreR := stat
  val isIRQ = RegNext(wbu.io.statCoreWEn && (wbu.io.statCoreW.stat))
  csr.io.irq := isIRQ
  csr.io.irq_no := RegNext(wbu.io.statCoreW.statNum)
  csr.io.irq_pc := RegNext(wbu.io.in.bits.pc)

  //pipline connect
  pipelineConnectIFU(ifu.io.pc , ifu.io.in)
  pipelineConnect(ifu.io.out, idu.io.in)
  pipelineConnect(idu.io.out, exu.io.in)
  pipelineConnect(exu.io.out, lsu.io.in)
  pipelineConnect(lsu.io.out, wbu.io.in)
  when(ifu.io.isFlush){ ifu.io.in.valid := false.B }
  when(idu.io.isFlush){ idu.io.in.valid := false.B }
  when(exu.io.isFlush){ exu.io.in.valid := false.B }
  when{lsu.io.isFlush}{ lsu.io.in.valid := false.B }
  when(wbu.io.isFlush){ wbu.io.in.valid := false.B }

  //data conflict detection
  val exuIsRaw = Wire(Bool())
  val lsuIsRaw = Wire(Bool())
  val wbuIsRaw = Wire(Bool())
  val isRAW =  exuIsRaw || lsuIsRaw || wbuIsRaw
  val exuCanFwd2Rd1 = Wire(Bool())
  val exuCanFwd2Rd2 = Wire(Bool())
  val exuCanFwd = exuCanFwd2Rd1 || exuCanFwd2Rd2
  val lsuCanFwd2Rd1 = Wire(Bool())
  val lsuCanFwd2Rd2 = Wire(Bool())
  val lsuCanFwd = lsuCanFwd2Rd1 || lsuCanFwd2Rd2
  val wbuCanFwd2Rd1 = Wire(Bool())
  val wbuCanFwd2Rd2 = Wire(Bool())
  val wbuCanFwd = wbuCanFwd2Rd1 || wbuCanFwd2Rd2
  val exuFwdData = Wire(UInt(32.W))
  val lsuFwdData = Wire(UInt(32.W))
  val wbuFwdData = Wire(UInt(32.W))
  val rd1FwdEn = Wire(Bool())
  val rd2FwdEn = Wire(Bool())
  val rd1FwdData = Wire(UInt(32.W))
  val rd2FwdData = Wire(UInt(32.W))
  exuIsRaw := dataConflictWithStage(idu.io, exu.io.in.bits.rw, exu.io.in.bits.signals.wbu.RegwriteE, exu.io.in.valid) 
  lsuIsRaw := dataConflictWithStage(idu.io, lsu.io.in.bits.rw, lsu.io.in.bits.signals.wbu.RegwriteE, lsu.io.in.valid)
  wbuIsRaw := dataConflictWithStage(idu.io, wbu.io.in.bits.rw, wbu.io.in.bits.signals.wbu.RegwriteE, wbu.io.in.valid)
  exuCanFwd2Rd1 := exuIsRaw && (exu.io.in.bits.signals.wbu.RegwriteD =/= RMemRD) && dataConflict(exu.io.in.bits.rw, idu.io.gpr.raddr1)
  exuCanFwd2Rd2 := exuIsRaw && (exu.io.in.bits.signals.wbu.RegwriteD =/= RMemRD) && dataConflict(exu.io.in.bits.rw, idu.io.gpr.raddr2)
  lsuCanFwd2Rd1 := lsuIsRaw && (lsu.io.in.bits.signals.wbu.RegwriteD =/= RMemRD || lsu.io.dmem.rvalid) && dataConflict(lsu.io.in.bits.rw, idu.io.gpr.raddr1)
  lsuCanFwd2Rd2 := lsuIsRaw && (lsu.io.in.bits.signals.wbu.RegwriteD =/= RMemRD || lsu.io.dmem.rvalid) && dataConflict(lsu.io.in.bits.rw, idu.io.gpr.raddr2)
  wbuCanFwd2Rd1 := wbuIsRaw && dataConflict(wbu.io.in.bits.rw, idu.io.gpr.raddr1)
  wbuCanFwd2Rd2 := wbuIsRaw && dataConflict(wbu.io.in.bits.rw, idu.io.gpr.raddr2)
  exuFwdData := MuxLookup(exu.io.in.bits.signals.wbu.RegwriteD, 0.U)(Seq(
    RAluresult -> exu.io.out.bits.aluresult,
    RImm       -> exu.io.out.bits.immext,
    RPcPlus4   -> (exu.io.out.bits.pc + 4.U),
    RCSR       -> exu.io.out.bits.crd1
  ))
  lsuFwdData := MuxLookup(lsu.io.in.bits.signals.wbu.RegwriteD, 0.U)(Seq(
    RAluresult -> lsu.io.out.bits.aluresult,
    RImm       -> lsu.io.out.bits.immext,
    RPcPlus4   -> (lsu.io.out.bits.pc + 4.U),
    RCSR       -> lsu.io.out.bits.crd1,
    RMemRD     -> lsu.io.out.bits.MemR
  ))
  wbuFwdData := wbu.io.gpr.wdata
  rd1FwdEn := exuCanFwd2Rd1 || lsuCanFwd2Rd1 || wbuCanFwd2Rd1
  rd2FwdEn := exuCanFwd2Rd2 || lsuCanFwd2Rd2 || wbuCanFwd2Rd2
  rd1FwdData := Mux(rd1FwdEn, MuxCase(0.U, Seq(
      exuCanFwd2Rd1 -> exuFwdData,
      lsuCanFwd2Rd1 -> lsuFwdData,
      wbuCanFwd2Rd1 -> wbuFwdData
    )), idu.io.out.bits.rd1)
  rd2FwdData := Mux(rd2FwdEn, MuxCase(0.U, Seq(
      exuCanFwd2Rd2 -> exuFwdData,
      lsuCanFwd2Rd2 -> lsuFwdData,
      wbuCanFwd2Rd2 -> wbuFwdData
    )), idu.io.out.bits.rd2)
  exu.io.in.bits.rd1 := RegEnable(rd1FwdData, idu.io.out.valid && exu.io.in.ready)
  exu.io.in.bits.rd2 := RegEnable(rd2FwdData, idu.io.out.valid && exu.io.in.ready)
  idu.io.isStall := isRAW && (!exuCanFwd && !lsuCanFwd && !wbuCanFwd)
  //control hazard detection
  //place mux
  val correctedPC = Wire(UInt(32.W))
  val PCSrc = exu.io.pc.bits.PCSrc
  correctedPC := MuxLookup(PCSrc, exu.io.pc.bits.PcPlus4)(Seq(
    PcPlus4 -> exu.io.pc.bits.PcPlus4,
    PcPlusImm -> exu.io.pc.bits.PcPlusImm,
    PcPlusRs2 -> exu.io.pc.bits.PcPlusRs2,
    //Mtvec -> io.in.iduPC.bits.mtvec,
    Mepc -> csr.io.read.mepc
  ))
  val iduIsWorking = idu.io.in.valid
  val isJump = exu.io.in.bits.signals.exu.Jump =/= NJump & exu.io.pc.valid
  val isCH = dontTouch(Wire(Bool()))
  exu.io.pc.ready := true.B
  val isCH_r = RegNext(isCH)
  isCH := Mux(isJump, PCSrc =/= PcPlus4, 0.U)

  ifu.io.correctedPC := Mux(isIRQ, csr.io.read.mtvec, Mux(isCH_r, RegNext(correctedPC), 0.U))

  ifu.io.isFlush := isCH_r || isIRQ
  idu.io.isFlush := isCH_r || isIRQ
  exu.io.isFlush := isCH_r || isIRQ
  lsu.io.isFlush := isIRQ
  wbu.io.isFlush := isIRQ
  //Connect to the "state" elements in npc
  ifu.io.imem <> icache.io.in
  icache.io.out <> io.imem
  icache.io.fencei <> idu.io.ifuSignals
  idu.io.gpr :<>= gpr.io.read
  idu.io.csr :<>= csr.io.read

  lsu.io.dmem <> io.dmem
  wbu.io.gpr :<>= gpr.io.write
  wbu.io.csr :<>= csr.io.write

  //wbu.io.statr := stat
  //ifu.io.statr := stat
  //idu.io.statr := stat

  def pipelineConnect[T <: Data, T2 <: Data](prevOut: DecoupledIO[T], thisIn: DecoupledIO[T]) = {
    prevOut.ready := thisIn.ready
    thisIn.bits := RegEnable(prevOut.bits, prevOut.valid && thisIn.ready)
    thisIn.valid := RegEnable(prevOut.valid, thisIn.ready);
  }
  def pipelineConnectIFU[T <: Data, T2 <: Data](prevOut: DecoupledIO[T], thisIn: DecoupledIO[T]) = {
    prevOut.ready := thisIn.ready
    thisIn.bits := RegEnable(prevOut.bits, "h30000000".U.asTypeOf(new IfuPcIO), prevOut.valid && thisIn.ready)
    thisIn.valid := RegEnable(prevOut.valid, true.B, thisIn.ready);
  }
  def dataConflict(rs: UInt, rd: UInt) = (rs === rd)
  def dataConflictWithStage(stageID: IduIO, rd: UInt, is_write: Bool, stageIsWorking: Bool) = {
    val rs1 = stageID.gpr.raddr1
    val rs2 = stageID.gpr.raddr2
    val stagesIsWorking = (stageID.in.valid) & stageIsWorking
    val rs1IsNotZero = rs1 =/= 0.U
    val rs2IsNotZero = rs2 =/= 0.U
    val rs1ren = stageID.rs1ren
    val rs2ren = stageID.rs2ren
    ((rs1ren & rs1IsNotZero & dataConflict(rs1, rd)) || (rs2ren & rs2IsNotZero & dataConflict(rs2, rd))) && is_write && stageIsWorking
  }
}

object StageConnect {
  def apply[T <: Data](left: DecoupledIO[T], right: DecoupledIO[T]) = {
    val arch = "multi"
    if      (arch == "single")   { right.bits := left.bits }
    else if (arch == "multi")    { right :<>= left }
    else if (arch == "pipeline") { right :<>= RegEnable(left, left.fire) }
    else if (arch == "ooo")      { right :<>= Queue(left, 16) }
  }
}