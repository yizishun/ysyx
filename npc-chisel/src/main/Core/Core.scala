package npc.core

import chisel3._
import chisel3.util._
import npc.dev._
import npc._
import npc.bus.AXI4

class Stat extends Bundle{
  val stat = Bool()
  val statNum = UInt(8.W)
}

class CoreIO(xlen : Int) extends Bundle{
  val imem = Flipped(new AXI4)
  val dmem = Flipped(new AXI4)
}

class Core(val conf : CoreConfig) extends Module {
  val io = IO(new CoreIO(conf.xlen))
  val ifu = Module(new IFU(conf))
  val idu = Module(new IDU(conf))
  val exu = Module(new EXU(conf))
  val lsu = Module(new LSU(conf))
  val wbu = Module(new WBU(conf))

  val icache = Module(new ICache(4, 16, conf))

  //"state" elements in npc core
  val stat = RegEnable(wbu.io.statw, 0.U.asTypeOf(new Stat), wbu.io.statEn)
  val gpr = Module(new gpr(conf))
  val csr = Module(new csr(conf))

  StageConnect(ifu.io.out, idu.io.in)
  StageConnect(idu.io.out, exu.io.in)
  StageConnect(exu.io.out, lsu.io.in)
  StageConnect(lsu.io.out, wbu.io.in)
  StageConnect(wbu.io.out, ifu.io.in)

  ifu.io.pc.idu := idu.io.pc
  ifu.io.pc.exu := exu.io.pc

  //Connect to the "state" elements in npc
  ifu.io.imem :<>= icache.io.in
  icache.io.out :<>= io.imem
  idu.io.gpr :<>= gpr.io.read
  idu.io.csr :<>= csr.io.read

  lsu.io.dmem :<>= io.dmem
  wbu.io.gpr :<>= gpr.io.write
  wbu.io.csr :<>= csr.io.write

  wbu.io.statr := stat
  ifu.io.statr := stat
  idu.io.statr := stat
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