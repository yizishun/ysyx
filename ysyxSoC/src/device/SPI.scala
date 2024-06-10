package ysyx

import chisel3._
import chisel3.util._

import freechips.rocketchip.amba.apb._
import org.chipsalliance.cde.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._
import freechips.rocketchip.rocket.CSRs.satp

class SPIIO(val ssWidth: Int = 8) extends Bundle {
  val sck = Output(Bool())
  val ss = Output(UInt(ssWidth.W))
  val mosi = Output(Bool())
  val miso = Input(Bool())
}

class spi_top_apb extends BlackBox {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Reset())
    val in = Flipped(new APBBundle(APBBundleParameters(addrBits = 32, dataBits = 32)))
    val spi = new SPIIO
    val spi_irq_out = Output(Bool())
  })
}

class flash extends BlackBox {
  val io = IO(Flipped(new SPIIO(1)))
}

class APBSPI(address: Seq[AddressSet])(implicit p: Parameters) extends LazyModule {
  val node = APBSlaveNode(Seq(APBSlavePortParameters(
    Seq(APBSlaveParameters(
      address       = address,
      executable    = true,
      supportsRead  = true,
      supportsWrite = true)),
    beatBytes  = 4)))

  lazy val module = new Impl
  class Impl extends LazyModuleImp(this) {
    val (in, _) = node.in(0)
    val spi_bundle = IO(new SPIIO)

    val mspi = Module(new spi_top_apb)
    mspi.io.clock := clock
    mspi.io.reset := reset
    spi_bundle <> mspi.io.spi
    mspi.io.in <> in
    when(in.paddr <= "h1000_1fff".U(32.W) && in.paddr >= "h1000_1000".U(32.W)){ //SPI master mode
    }.elsewhen(in.paddr >= "h3000_0000".U(32.W) && in.paddr <= "h3fff_ffff".U(32.W)){ //XIP mode
      //state transition
      val s_idle :: s_tx0 :: s_tx1 :: s_div :: s_ctrl :: s_ss :: s_start :: s_poll :: s_get :: Nil = Enum(9)
      val state = RegInit(s_tx0)
      state := MuxLookup(state, s_tx0)(Seq(
        s_tx0  -> Mux(mspi.io.in.pready, s_tx1  , s_tx0  ),
        s_tx1  -> Mux(mspi.io.in.pready, s_div  , s_tx1  ),
        s_div  -> Mux(mspi.io.in.pready, s_ctrl , s_div  ),
        s_ctrl -> Mux(mspi.io.in.pready, s_ss   , s_ctrl ),
        s_ss   -> Mux(mspi.io.in.pready, s_start, s_ss   ),
        s_start-> Mux(mspi.io.in.pready, s_poll , s_start),
        s_poll -> Mux(mspi.io.spi_irq_out, s_get, s_poll),
        s_get  -> Mux(mspi.io.in.pready, s_idle , s_get  ),
        s_idle -> s_tx0
      ))
      val rdata = RegEnable(mspi.io.in.prdata, state === s_get)
      val dataplace = Wire(UInt(32.W))
      val data = Wire(UInt(32.W))
      //To master
      in.pready := (state === s_idle)
      dataplace := ((in.paddr - (in.paddr & (~7.U(32.W)))) << 3) % 32.U
      data := Cat(Cat(rdata(7, 0), rdata(15, 8)), Cat(rdata(23, 16), rdata(31, 24)))
      in.prdata := (data << dataplace(5, 0)) | (data >> (32.U - dataplace(5, 0)))
      //To flash(spi controller)
      mspi.io.in.paddr := MuxLookup(state, 0.U)(Seq(
        s_tx0   -> "h1000_1000".U,
        s_tx1   -> "h1000_1004".U,
        s_div   -> "h1000_1014".U,
        s_ctrl  -> "h1000_1010".U,
        s_ss    -> "h1000_1018".U,
        s_start -> "h1000_1010".U,
        s_get   -> "h1000_1000".U
      ))
      mspi.io.in.pwdata := MuxLookup(state, 0.U)(Seq(
        s_tx0   -> 0.U,
        s_tx1   -> ((3.U << 24) + (in.paddr & "hff_ffff".U)),
        s_div   -> 1.U,
        s_ctrl  -> "b1_1_0_1_0_0_0_1000000".U,
        s_ss    -> 1.U,
        s_start -> "b1_1_0_1_0_1_0_1000000".U
      ))
      mspi.io.in.psel := in.psel && (state =/= s_idle) && (state =/= s_poll)
      mspi.io.in.penable := in.penable && (state =/= s_idle) && (state =/= s_poll)
      mspi.io.in.pwrite := (state === s_tx0) || (state === s_tx1) || (state === s_div) ||
        (state === s_ctrl) || (state === s_ss) || (state === s_start)
      mspi.io.in.pstrb := Mux(mspi.io.in.pwrite, "b1111".U, 0.U)
      assert(!in.pwrite, "do not support write operations")
    }
  }
}
//  input  [31:0] in_paddr, tx0 tx1 div ss start poll
//  input         in_psel, 
//  input         in_penable,
//  input  [2:0]  in_pprot,
//  input         in_pwrite,
//  input  [31:0] in_pwdata,
//  input  [3:0]  in_pstrb,
//  output        in_pready,
//  output [31:0] in_prdata,
//  output        in_pslverr,

