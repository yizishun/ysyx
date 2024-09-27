package npc.core

import chisel3._
import chisel3.util._
import npc._

class PfuInIO extends Bundle{
    val ifuPC = Flipped(Decoupled(new IfuPcIO))
    val iduPC = Flipped(Decoupled(new IduPcIO))
    val exuPC = Flipped(Decoupled(new ExuPcIO))
}

class PfuOutIO extends Bundle{
    val nextPC = Output(UInt(32.W))
}

class PfuIO(xlen: Int) extends Bundle{
    val in = new PfuInIO
    val out = Decoupled(new PfuOutIO)
}

class PFU(val conf: npc.CoreConfig) extends Module{
    val io = IO(new PfuIO(conf.xlen))
    io.in.ifuPC.ready := true.B
    io.in.iduPC.ready := true.B
    io.in.exuPC.ready := true.B

//    //state transition
//    val s_BeforeFire1 :: s_BetweenFire12 :: Nil= Enum(2)
//    val state = RegInit(s_BetweenFire12)
//    val nextState = WireDefault(state)
//    nextState := MuxLookup(state, s_BeforeFire1)(Seq(
//        sc_BeforeFire1   -> Mux(io.in.fire, s_BetweenFire12_1_1, s_BeforeFire1),
//        sc_BetweenFire12 -> Mux(io.imem.arvalid & io.imem.arready, s_BeforeFire1, s_BetweenFire12),
//    ))
//    state := nextState
//    dontTouch(nextState)

    import npc.core.idu.Control._
    //place mux
    val PCSrc = Wire(UInt(3.W))
    val nextpc = Wire(UInt(32.W))
    PCSrc := io.in.exuPC.bits.PCSrc
  
    nextpc := MuxLookup(PCSrc, io.in.ifuPC.bits.speculativePC)(Seq(
      PcPlus4 -> io.in.ifuPC.bits.PcPlus4,
      PcPlusImm -> io.in.exuPC.bits.PcPlusImm,
      PcPlusRs2 -> io.in.exuPC.bits.PcPlusRs2,
      Mtvec -> io.in.iduPC.bits.mtvec,
      Mepc -> io.in.iduPC.bits.mepc
    ))
    io.out.bits.nextPC := nextpc
    io.out.valid := io.in.exuPC.fire
}