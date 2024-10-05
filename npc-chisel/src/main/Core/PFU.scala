package npc.core

import chisel3._
import chisel3.util._
import npc._
import scala.annotation.switch

class PfuInIO extends Bundle{
    val ifuPC = Flipped(Decoupled(new IfuPcIO))
}

class PfuOutIO extends Bundle{
    val nextPC = Output(UInt(32.W))
}

class PfuIO(xlen: Int) extends Bundle{
    val in = new PfuInIO
    val out = Decoupled(new PfuOutIO)
    val isFlush = Input(Bool())
    val correctedPC = Input(UInt(32.W))
}

class PFU(val conf: npc.CoreConfig) extends Module{
    val io = IO(new PfuIO(conf.xlen))
    io.in.ifuPC.ready := true.B

      //state transition(mearly)
    val s_WaitIfuV :: s_WaitIfuR :: Nil = Enum(2)
    val stateP = RegInit(s_WaitIfuV)
    val nextStateP = WireDefault(stateP)
    nextStateP := MuxLookup(stateP, s_WaitIfuV)(Seq(
        s_WaitIfuV -> Mux(io.in.ifuPC.valid, Mux(io.out.ready, s_WaitIfuV, s_WaitIfuR), s_WaitIfuV),
        s_WaitIfuR -> Mux(io.out.ready, s_WaitIfuV, s_WaitIfuR)
    ))
    when(io.isFlush){ nextStateP := s_WaitIfuV }

    stateP := nextStateP
    dontTouch(nextStateP)
    io.out.bits.nextPC := io.in.ifuPC.bits.speculativePC
    io.out.valid := false.B
    io.in.ifuPC.ready := false.B
    switch(stateP){
        is(s_WaitIfuV){
            io.in.ifuPC.ready := true.B
            io.out.valid := Mux(io.in.ifuPC.valid, true.B, false.B)
        }
        is(s_WaitIfuR){
            io.in.ifuPC.ready := Mux(io.out.ready, true.B, false.B)
            io.out.valid := true.B
        }
    }
    when(io.isFlush){
        io.in.ifuPC.ready := true.B
    }
}