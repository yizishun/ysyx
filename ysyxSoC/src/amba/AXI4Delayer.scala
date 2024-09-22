package ysyx

import chisel3._
import chisel3.util._

import org.chipsalliance.cde.config.Parameters
import freechips.rocketchip.amba._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._

class AXI4DelayerIO extends Bundle {
  val clock = Input(Clock())
  val reset = Input(Reset())
  val in = Flipped(new AXI4Bundle(AXI4BundleParameters(addrBits = 32, dataBits = 32, idBits = 4)))
  val out = new AXI4Bundle(AXI4BundleParameters(addrBits = 32, dataBits = 32, idBits = 4))
}

class axi4_delayer extends BlackBox {
  val io = IO(new AXI4DelayerIO)
}

class AXI4DelayerChisel extends Module {
  val io = IO(new AXI4DelayerIO)
  io.out <> io.in
  val is_read = RegInit(false.B)
  val is_write = RegInit(false.B)
  val incr = 5.U //* 2
  val delayCnt = RegInit(0.U(16.W))
  val rdata = RegInit(VecInit(Seq.fill(8)(0.U(32.W))))
  val burstR = dontTouch(RegEnable(io.out.ar.bits.burst, io.out.ar.fire))
  val burstLenR = dontTouch(RegEnable(io.out.ar.bits.len, io.out.ar.fire))
  val burstCntOut = dontTouch(RegInit(0.U(8.W)))
  val burstCntIn = dontTouch(RegInit(0.U(8.W)))
  val singleBurstDelay = dontTouch(RegInit(VecInit(Seq.fill(8)(0.U(16.W)))))
  //state transition logic
  val s_idle :: s_waitR :: s_waitW :: s_delay :: Nil = Enum(4)
  val state = RegInit(s_idle)
  val nextState = dontTouch(Wire(UInt(32.W)))
  nextState := MuxLookup(state, s_idle)(Seq(
    s_idle -> Mux(io.in.ar.valid | io.in.aw.valid, Mux(io.in.ar.valid, s_waitR, s_waitW), s_idle),
    s_waitR -> Mux(burstCntOut === burstLenR + 1.U, s_delay, s_waitR),
    s_waitW -> Mux(io.out.b.fire, s_delay, s_waitW),
    s_delay -> Mux(delayCnt === 0.U, s_idle, s_delay)
  ))
  state := nextState
  //out burst counter logic
  when(state === s_idle){
    burstCntOut := 0.U
  }.elsewhen(nextState === s_waitR & io.out.r.fire){
    burstCntOut := burstCntOut + 1.U
    singleBurstDelay(burstCntOut) := delayCnt
    rdata(burstCntOut) := io.out.r.bits.data
  }
  //in burst counter logic
  when(io.in.ar.valid){
    burstCntIn := 0.U
  }.elsewhen(io.in.r.fire){
    burstCntIn := burstCntIn + 1.U
  }
  //delay counter logic
  when(nextState === s_idle){
    delayCnt := 0.U
  }.elsewhen(nextState === s_waitR || nextState === s_waitW){
    delayCnt := delayCnt + incr
  }.elsewhen(nextState === s_delay){
    delayCnt := Mux(state =/= s_delay, (delayCnt >> 1) - 1.U, delayCnt - 1.U)
    for(i <- 0 until 8){
      singleBurstDelay(i) := Mux(state =/= s_delay, (singleBurstDelay(i) >> 1) - 1.U, singleBurstDelay(i) - 1.U)
    }
  }
  when(io.in.ar.valid){
    is_read := true.B
  }.elsewhen(state === s_idle){
    is_read := false.B
  }
  when(io.in.aw.valid){
    is_write := true.B
  }.elsewhen(state === s_idle){
    is_write := false.B
  }
  when(is_write){
    //in write logic
    io.in.b.valid := delayCnt === 0.U && state === s_delay
    //out write logic
  }
  when(is_read){
    //in read logic
    io.in.r.valid := Mux(nextState === s_delay && singleBurstDelay(burstCntIn) === 0.U, true.B, false.B)
    io.in.r.bits.last := burstCntIn === burstLenR
    //out read logic
    //io.out.ar.valid := Mux(nextState === s_delay, false.B, io.in.ar.valid)
    //io.out.r.ready := Mux(nextState === s_delay, false.B, io.in.r.ready)
  }
  io.in.r.bits.data := Mux(state === s_idle, 0.U, Mux(is_read, rdata(burstCntIn), 0.U))
}

class AXI4DelayerWrapper(implicit p: Parameters) extends LazyModule {
  val node = AXI4IdentityNode()

  lazy val module = new Impl
  class Impl extends LazyModuleImp(this) {
    (node.in zip node.out) foreach { case ((in, edgeIn), (out, edgeOut)) =>
      val delayer = Module(new AXI4DelayerChisel)
      delayer.io.clock := clock
      delayer.io.reset := reset
      delayer.io.in <> in
      out <> delayer.io.out
    }
  }
}

object AXI4Delayer {
  def apply()(implicit p: Parameters): AXI4Node = {
    val axi4delay = LazyModule(new AXI4DelayerWrapper)
    axi4delay.node
  }
}
