package npc.core

import chisel3._
import chisel3.util._
import npc.dev._
import npc._
import npc.bus._

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

  val icache = Module(new ICache(2, 1, 16, conf))

  //"state" elements in npc core
  //val stat = RegEnable(wbu.io.statw, 0.U.asTypeOf(new Stat), wbu.io.statEn)
  val gpr = Module(new gpr(conf))
  val csr = Module(new csr(conf))

  pipelineConnectIFU(ifu.io.pc , ifu.io.in)
  pipelineConnect(ifu.io.out, idu.io.in)
  pipelineConnect(idu.io.out, exu.io.in)
  pipelineConnect(exu.io.out, lsu.io.in)
  pipelineConnect(lsu.io.out, wbu.io.in)
  when(ifu.io.isFlush){ ifu.io.in.valid := false.B }
  when(idu.io.isFlush){ idu.io.in.valid := false.B }
  when(exu.io.isFlush){ exu.io.in.valid := false.B }

  //data conflict detection
  def dataConflict(rs: UInt, rd: UInt) = (rs === rd)
  def dataConflictWithStage(stageID: IduIO, rd: UInt, is_write: Bool, stageIsWorking: Bool) = {
    val rs1 = stageID.gpr.raddr1
    val rs2 = stageID.gpr.raddr2
    val stagesIsWorking = (stageID.in.valid | ~stageID.in.ready) & stageIsWorking
    val rs1IsNotZero = rs1 =/= 0.U
    val rs2IsNotZero = rs2 =/= 0.U
    val rs1ren = stageID.rs1ren
    val rs2ren = stageID.rs2ren
    ((rs1ren & rs1IsNotZero & dataConflict(rs1, rd)) || (rs2ren & rs2IsNotZero & dataConflict(rs2, rd))) && is_write && stageIsWorking
  }
  val isRAW = dataConflictWithStage(idu.io, exu.io.in.bits.rw, exu.io.in.bits.signals.wbu.RegwriteE, exu.io.in.valid | ~exu.io.in.ready) ||
              dataConflictWithStage(idu.io, lsu.io.in.bits.rw, lsu.io.in.bits.signals.wbu.RegwriteE, lsu.io.in.valid | ~lsu.io.in.ready) ||
              dataConflictWithStage(idu.io, wbu.io.in.bits.rw, wbu.io.in.bits.signals.wbu.RegwriteE, wbu.io.in.valid | ~wbu.io.in.ready)
  idu.io.isRaw := isRAW
  //control hazard detection
  //place mux
  import npc.core.idu.Control._
  val correctedPC = Wire(UInt(32.W))
  val PCSrc = exu.io.pc.bits.PCSrc
  correctedPC := MuxLookup(PCSrc, exu.io.pc.bits.PcPlus4)(Seq(
    PcPlus4 -> exu.io.pc.bits.PcPlus4,
    PcPlusImm -> exu.io.pc.bits.PcPlusImm,
    PcPlusRs2 -> exu.io.pc.bits.PcPlusRs2,
    //Mtvec -> io.in.iduPC.bits.mtvec,
    //Mepc -> io.in.iduPC.bits.mepc
  ))
  val iduIsWorking = idu.io.in.valid
  val isJump = exu.io.in.bits.signals.exu.Jump =/= NJump & exu.io.pc.valid
  val isCH = dontTouch(Wire(Bool()))
  exu.io.pc.ready := true.B
  ifu.io.correctedPC := RegNext(correctedPC)
  val isCH_r = RegNext(isCH)
  ifu.io.isFlush := isCH_r
  idu.io.isFlush := isCH_r
  exu.io.isFlush := isCH_r
  isCH := Mux(isJump, PCSrc =/= PcPlus4, 0.U) 


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