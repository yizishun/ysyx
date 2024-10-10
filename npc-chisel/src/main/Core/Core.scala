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
  val exuIsRawRs1 = (Wire(Bool()))
  val exuIsRawRs2 = (Wire(Bool()))
  exuIsRawRs1 := exuIsRaw && dataConflict(exu.io.in.bits.rw, idu.io.gpr.raddr1)
  exuIsRawRs2 := exuIsRaw && dataConflict(exu.io.in.bits.rw, idu.io.gpr.raddr2)
  val lsuIsRaw = Wire(Bool())
  val lsuIsRawRs1 = (Wire(Bool()))
  val lsuIsRawRs2 = (Wire(Bool()))
  lsuIsRawRs1 := lsuIsRaw && dataConflict(lsu.io.in.bits.rw, idu.io.gpr.raddr1)
  lsuIsRawRs2 := lsuIsRaw && dataConflict(lsu.io.in.bits.rw, idu.io.gpr.raddr2)
  val wbuIsRaw = Wire(Bool())
  val wbuIsRawRs1 = (Wire(Bool()))
  val wbuIsRawRs2 = (Wire(Bool()))
  wbuIsRawRs1 := wbuIsRaw && dataConflict(wbu.io.in.bits.rw, idu.io.gpr.raddr1)
  wbuIsRawRs2 := wbuIsRaw && dataConflict(wbu.io.in.bits.rw, idu.io.gpr.raddr2)
  val isRAW =  exuIsRaw || lsuIsRaw || wbuIsRaw
  val rs1IsRaw = exuIsRawRs1 || lsuIsRawRs1 || wbuIsRawRs1
  val rs2IsRaw = exuIsRawRs2 || lsuIsRawRs2 || wbuIsRawRs2
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
  val rd1FwdData_r = RegEnable(rd1FwdData, rd1FwdEn)
  val rd2FwdData = Wire(UInt(32.W))
  val rd2FwdData_r = RegEnable(rd2FwdData, rd2FwdEn)
  val fwdCount1 = RegInit(0.U(1.W))
  val fwdCount1_w = Wire(UInt(1.W))
  val fwdCount2 = RegInit(0.U(1.W))
  val fwdCount2_w = Wire(UInt(1.W))
  fwdCount1_w := fwdCount1
  fwdCount2_w := fwdCount2
  when(RegNext(idu.io.in.ready) && idu.io.in.valid){
    fwdCount1_w := (idu.io.rs1ren & rs1IsRaw).asUInt - rd1FwdEn.asUInt
    fwdCount2_w := (idu.io.rs2ren & rs2IsRaw).asUInt - rd2FwdEn.asUInt
    fwdCount1 := fwdCount1_w
    fwdCount2 := fwdCount2_w
  }.elsewhen(idu.io.in.valid){
    when(rd1FwdEn && fwdCount1 =/= 0.U){fwdCount1 := fwdCount1 - 1.U}
    when(rd2FwdEn && fwdCount2 =/= 0.U){fwdCount2 := fwdCount2 - 1.U}
  }
  exuIsRaw := dataConflictWithStage(idu.io, exu.io.in.bits.rw, exu.io.in.bits.signals.wbu.RegwriteE, exu.io.in.valid) 
  lsuIsRaw := dataConflictWithStage(idu.io, lsu.io.in.bits.rw, lsu.io.in.bits.signals.wbu.RegwriteE, lsu.io.in.valid)
  wbuIsRaw := dataConflictWithStage(idu.io, wbu.io.in.bits.rw, wbu.io.in.bits.signals.wbu.RegwriteE, wbu.io.in.valid)
  exuCanFwd2Rd1 := exuIsRawRs1 && (exu.io.in.bits.signals.wbu.RegwriteD =/= RMemRD)
  exuCanFwd2Rd2 := exuIsRawRs2 && (exu.io.in.bits.signals.wbu.RegwriteD =/= RMemRD)
  lsuCanFwd2Rd1 := lsuIsRawRs1 && (lsu.io.in.bits.signals.wbu.RegwriteD =/= RMemRD || lsu.io.dmem.rvalid)
  lsuCanFwd2Rd2 := lsuIsRawRs2 && (lsu.io.in.bits.signals.wbu.RegwriteD =/= RMemRD || lsu.io.dmem.rvalid)
  wbuCanFwd2Rd1 := wbuIsRawRs1
  wbuCanFwd2Rd2 := wbuIsRawRs2
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
  when (exuIsRawRs1) {
    rd1FwdEn := exuCanFwd2Rd1
  }.elsewhen (lsuIsRawRs1) {
    rd1FwdEn := lsuCanFwd2Rd1
  }.elsewhen(wbuIsRawRs1) {
    rd1FwdEn := wbuCanFwd2Rd1
  }.otherwise {
    rd1FwdEn := false.B
  }
  when (exuIsRawRs2) {
    rd2FwdEn := exuCanFwd2Rd2
  }.elsewhen (lsuIsRawRs2) {
    rd2FwdEn := lsuCanFwd2Rd2
  }.elsewhen(wbuIsRawRs2) {
    rd2FwdEn := wbuCanFwd2Rd2
  }.otherwise {
    rd2FwdEn := false.B
  }

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
  exu.io.in.bits.rd1 := RegEnable(Mux(rd1FwdEn || ~rs1IsRaw, rd1FwdData, rd1FwdData_r), idu.io.out.valid && exu.io.in.ready) //rd1FwdData_r fwd
  exu.io.in.bits.rd2 := RegEnable(Mux(rd2FwdEn || ~rs2IsRaw, rd2FwdData, rd2FwdData_r), idu.io.out.valid && exu.io.in.ready) //rd2FwdData_r fwd
  idu.io.isStall := ~(~isRAW || (((fwdCount1_w === 0.U) || (fwdCount1 === 1.U && rd1FwdEn)) && ((fwdCount2_w === 0.U) || (fwdCount2 === 1.U && rd2FwdEn)))) // count fwd
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
  val isCH = Wire(Bool())
  exu.io.pc.ready := true.B
  val isCH_r = RegNext(isCH)
  isCH := isJump && PCSrc =/= PcPlus4

  val isFencei = RegNext(icache.io.fencei.valid & icache.io.fencei.ready)
  ifu.io.correctedPC := Mux(isIRQ, csr.io.read.mtvec, Mux(isCH_r, RegNext(correctedPC), Mux(isFencei, ifu.io.in.bits.nextPC, 0.U)))

  ifu.io.isFlush := isCH_r || isIRQ || isFencei
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