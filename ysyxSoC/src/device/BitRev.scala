package ysyx

import chisel3._
import chisel3.util._

class bitrev extends BlackBox {
  val io = IO(Flipped(new SPIIO(1)))
}

class bitrevChisel extends RawModule { // we do not need clock and reset
  val io = IO(Flipped(new SPIIO(1)))
  io.miso := true.B
  withClockAndReset(io.sck.asClock, io.ss.asBool.asAsyncReset){
    val sr = RegInit(0.U(8.W))
    val counter = RegInit(9.U)
    when(io.ss === true.B){
      io.miso := true.B
    }.elsewhen(io.ss === false.B && counter === 0.U){
      sr := Cat(sr(6, 0), 0.U)
      io.miso := sr(7)
    }.elsewhen(io.ss === false.B && counter =/= 0.U){
      sr := Cat(sr(6, 0), io.mosi.asUInt)
      io.miso := false.B
      counter := counter - 1.U
    }
  }
}
