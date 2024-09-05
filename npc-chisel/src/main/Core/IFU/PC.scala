package npc.core.ifu

import chisel3._
import chisel3.util._

class pcIO extends Bundle{
  val nextpc = Input(UInt(32.W))
  val wen = Input(Bool())
  val pc = Output(UInt(32.W))
}

class PC(val conf: npc.CoreConfig) extends Module{
  val io = IO(new pcIO)
  val pcInit = if(conf.ysyxsoc){ "h3000_0000".U(32.W) }else if(conf.npc){ "h8000_0000".U(32.W)} else { "h0000_0000".U(32.W) }
  val pcReg = RegEnable(io.nextpc, pcInit, io.wen)
  io.pc := pcReg
}