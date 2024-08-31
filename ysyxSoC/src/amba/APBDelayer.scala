package ysyx

import chisel3._
import chisel3.util._

import org.chipsalliance.cde.config.Parameters
import freechips.rocketchip.amba._
import freechips.rocketchip.amba.apb._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._

class APBDelayerIO extends Bundle {
  val clock = Input(Clock())
  val reset = Input(Reset())
  val in = Flipped(new APBBundle(APBBundleParameters(addrBits = 32, dataBits = 32)))
  val out = new APBBundle(APBBundleParameters(addrBits = 32, dataBits = 32))
}

class apb_delayer extends BlackBox {
  val io = IO(new APBDelayerIO)
}

class APBDelayerChisel extends Module {
  val io = IO(new APBDelayerIO)
  io.out <> io.in
  val incr = 5.U //* 2
  val delayCnt = RegInit(0.U(32.W))
  val rdata = RegEnable(io.out.prdata, io.out.pready)
  val s_idle :: s_wait :: s_delay :: Nil = Enum(3)
  val state = RegInit(s_idle)
  val nextState = Wire(UInt(32.W))
  nextState := MuxLookup(state, s_idle)(Seq(
    s_idle -> Mux(io.in.psel, s_wait, s_idle),
    s_wait -> Mux(io.out.pready, s_delay, s_wait),
    s_delay -> Mux(io.in.pready, s_idle, s_delay)
  ))
  state := nextState
  dontTouch(nextState)
  delayCnt := Mux(nextState === s_wait, delayCnt + incr,
    Mux(nextState === s_delay, 
    Mux(io.out.psel, (delayCnt >> 1) - 1.U, delayCnt - 1.U), 0.U))

  io.out.psel := Mux(state === s_delay, false.B, io.in.psel)
  
  io.in.pready := Mux(state === s_delay && delayCnt === 0.U, true.B, false.B)
  io.in.prdata := Mux(state === s_delay, rdata, io.out.prdata)
}
class APBDelayerWrapper(implicit p: Parameters) extends LazyModule {
  val node = APBIdentityNode()

  lazy val module = new Impl
  class Impl extends LazyModuleImp(this) {
    (node.in zip node.out) foreach { case ((in, edgeIn), (out, edgeOut)) =>
      val delayer = Module(new APBDelayerChisel)
      delayer.io.clock := clock
      delayer.io.reset := reset
      delayer.io.in <> in
      out <> delayer.io.out
    }
  }
}

object APBDelayer {
  def apply()(implicit p: Parameters): APBNode = {
    val apbdelay = LazyModule(new APBDelayerWrapper)
    apbdelay.node
  }
}
