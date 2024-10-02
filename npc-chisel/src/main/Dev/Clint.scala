package npc.dev

import chisel3._
import chisel3.util._
import npc._
import npc.bus._

class Clint(coreConfig: CoreConfig) extends Module{
  val io = IO(new AXI4Slave)
  io.setDefaults()
  val rdata = RegInit(0.U(32.W))
  io.rdata := rdata
  val ADDR = "h02000000".U
  val mtime = RegInit(0.U(64.W))
  mtime := mtime + 1.U
  //state transition(mearly)
  val s_WaitARV :: s_WaitRR :: Nil = Enum(2)
  val stateC = RegInit(s_WaitARV)
  val nextStateC = WireDefault(stateC)
  nextStateC := MuxLookup(stateC, s_WaitARV)(Seq(
      s_WaitARV   -> Mux(io.arvalid, s_WaitRR, s_WaitARV),
      s_WaitRR    -> Mux(io.rready, s_WaitARV, s_WaitRR)
  ))

  stateC := nextStateC
  dontTouch(nextStateC)
  io.arready := false.B
  rdata := Mux(io.araddr === ADDR, mtime(31, 0), mtime(63, 32))
  switch(stateC){
    is(s_WaitARV){
      io.arready := true.B
      io.rvalid := false.B
    }
    is(s_WaitRR){
      io.arready := false.B
      io.rvalid := true.B
    }
  }
}