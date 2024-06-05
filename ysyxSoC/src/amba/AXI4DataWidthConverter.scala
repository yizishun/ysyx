package ysyx

import chisel3._
import chisel3.util._

import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.util._

class AXI4DataWidthConverter64to32IO extends Bundle {
  val clock = Input(Clock())
  val reset = Input(Bool())
  val in = Flipped(new AXI4Bundle(AXI4BundleParameters(addrBits = 32, dataBits = 64, idBits = 4)))
  val out = new AXI4Bundle(AXI4BundleParameters(addrBits = 32, dataBits = 32, idBits = 4))
}

class AXI4DataWidthConverter64to32 extends BlackBox {
  val io = IO(new AXI4DataWidthConverter64to32IO)
}

class AXI4DataWidthConverter64to32Chisel extends Module {
  val io = IO(new AXI4DataWidthConverter64to32IO)
  io.out <> io.in
}
