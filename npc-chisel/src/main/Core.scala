package npc

import chisel3._
import chisel3.util._

class CoreIO extends Bundle{
  val imem = Flipped(new imemIO)
  val dmem = Flipped(new dmemIO)
}

class Core extends Module {
  val io = IO(new CoreIO)
  val ifu = Module(new IFU)
  val idu = Module(new IDU)
  val exu = Module(new EXU)
  val lsu = Module(new LSU)
  val wbu = Module(new WBU)

  //"state" element in npc core
  val gpr = Module(new gpr)
  val csr = Module(new csr)

  StageConnect(ifu.io.out, idu.io.in)
  StageConnect(idu.io.out, exu.io.in)
  StageConnect(exu.io.out, lsu.io.in)
  StageConnect(lsu.io.out, wbu.io.in)

  idu.io.imem <> io.imem
  lsu.io.dmem <> io.dmem

  ifu.io.pcFromIdu := idu.io.pcToIfu
  ifu.io.pcFromExu := exu.io.pcToIfu
  ifu.io.pcSrcFromIdu := idu.io.pcSrcToIfu

  idu.io.gpr <> gpr.io.read
  wbu.io.gpr <> gpr.io.write
  idu.io.csr <> csr.io.read
  wbu.io.csr <> csr.io.write
}

object StageConnect {
  def apply[T <: Data](left: DecoupledIO[T], right: DecoupledIO[T]) = {
    val arch = "single"
    if      (arch == "single")   { right.bits := left.bits }
    else if (arch == "multi")    { right <> left }
    else if (arch == "pipeline") { right <> RegEnable(left, left.fire) }
    else if (arch == "ooo")      { right <> Queue(left, 16) }
  }
}