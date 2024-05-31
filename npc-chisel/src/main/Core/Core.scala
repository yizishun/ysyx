package npc.core

import chisel3._
import chisel3.util._
import npc.dev._
import npc._
import npc.bus.AXI4

class IrqIO extends Bundle{
  val irqOut = Output(Bool())
  val irqOutNo = Output(UInt(8.W))
  val irqIn = Input(Vec(4, Bool()))
  val irqInNo = Input(Vec(4, UInt(8.W)))
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

  //"state" elements in npc core
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
  ifu.io.imem :<>= io.imem
  idu.io.gpr :<>= gpr.io.read
  idu.io.csr :<>= csr.io.read

  lsu.io.dmem :<>= io.dmem
  wbu.io.gpr :<>= gpr.io.write
  wbu.io.csr :<>= csr.io.write
  //irq directly connect
  ifu.io.irq.irqIn(0) := idu.io.irq.irqOut
  ifu.io.irq.irqIn(1) := exu.io.irq.irqOut
  ifu.io.irq.irqIn(2) := lsu.io.irq.irqOut
  ifu.io.irq.irqIn(3) := wbu.io.irq.irqOut
  ifu.io.irq.irqInNo(0) := idu.io.irq.irqOutNo
  ifu.io.irq.irqInNo(1) := exu.io.irq.irqOutNo
  ifu.io.irq.irqInNo(2) := lsu.io.irq.irqOutNo
  ifu.io.irq.irqInNo(3) := wbu.io.irq.irqOutNo
  idu.io.irq.irqIn(0) := ifu.io.irq.irqOut
  idu.io.irq.irqIn(1) := exu.io.irq.irqOut
  idu.io.irq.irqIn(2) := lsu.io.irq.irqOut
  idu.io.irq.irqIn(3) := wbu.io.irq.irqOut
  idu.io.irq.irqInNo(0) := ifu.io.irq.irqOutNo
  idu.io.irq.irqInNo(1) := exu.io.irq.irqOutNo
  idu.io.irq.irqInNo(2) := lsu.io.irq.irqOutNo
  idu.io.irq.irqInNo(3) := wbu.io.irq.irqOutNo
  exu.io.irq.irqIn(0) := ifu.io.irq.irqOut
  exu.io.irq.irqIn(1) := idu.io.irq.irqOut
  exu.io.irq.irqIn(2) := lsu.io.irq.irqOut
  exu.io.irq.irqIn(3) := wbu.io.irq.irqOut
  exu.io.irq.irqInNo(0) := ifu.io.irq.irqOutNo
  exu.io.irq.irqInNo(1) := idu.io.irq.irqOutNo
  exu.io.irq.irqInNo(2) := lsu.io.irq.irqOutNo
  exu.io.irq.irqInNo(3) := wbu.io.irq.irqOutNo
  lsu.io.irq.irqIn(0) := ifu.io.irq.irqOut
  lsu.io.irq.irqIn(1) := idu.io.irq.irqOut
  lsu.io.irq.irqIn(2) := exu.io.irq.irqOut
  lsu.io.irq.irqIn(3) := wbu.io.irq.irqOut
  lsu.io.irq.irqInNo(0) := ifu.io.irq.irqOutNo
  lsu.io.irq.irqInNo(1) := idu.io.irq.irqOutNo
  lsu.io.irq.irqInNo(2) := exu.io.irq.irqOutNo
  lsu.io.irq.irqInNo(3) := wbu.io.irq.irqOutNo
  wbu.io.irq.irqIn(0) := ifu.io.irq.irqOut
  wbu.io.irq.irqIn(1) := idu.io.irq.irqOut
  wbu.io.irq.irqIn(2) := exu.io.irq.irqOut
  wbu.io.irq.irqIn(3) := lsu.io.irq.irqOut
  wbu.io.irq.irqInNo(0) := ifu.io.irq.irqOutNo
  wbu.io.irq.irqInNo(1) := idu.io.irq.irqOutNo
  wbu.io.irq.irqInNo(2) := exu.io.irq.irqOutNo
  wbu.io.irq.irqInNo(3) := lsu.io.irq.irqOutNo
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