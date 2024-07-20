package ysyx

import chisel3._
import chisel3.util._

import freechips.rocketchip.amba.apb._
import org.chipsalliance.cde.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._

object SEG{
  val num0 = ~"b11111101".U
  val num1 = ~"b01100000".asUInt(8.W)
  val num2 = ~"b11011010".U
  val num3 = ~"b11110010".U
  val num4 = ~"b01100110".asUInt(8.W)
  val num5 = ~"b10110110".U
  val num6 = ~"b10111110".U
  val num7 = ~"b11100000".U
  val num8 = ~"b11111110".U
  val num9 = ~"b11110110".U
  val numa = ~"b11101110".U
  val numb = ~"b00111110".U
  val numc = ~"b10011100".U
  val numd = ~"b01111010".U
  val nume = ~"b10011110".U
  val numf = ~"b10001110".U
}

class GPIOIO extends Bundle {
  val out = Output(UInt(16.W)) //led
  val in = Input(UInt(16.W)) //switch
  val seg = Output(Vec(8, UInt(8.W)))
}

class GPIOCtrlIO extends Bundle {
  val clock = Input(Clock())
  val reset = Input(Reset())
  val in = Flipped(new APBBundle(APBBundleParameters(addrBits = 32, dataBits = 32)))
  val gpio = new GPIOIO
}

class gpio_top_apb extends BlackBox {
  val io = IO(new GPIOCtrlIO)
}

class gpioChisel extends Module {
  val io = IO(new GPIOCtrlIO)
  val gpioLed = RegInit(0.U(16.W))
  val gpioSwitch = RegInit(0.U(16.W))
  val gpioSeg = RegInit(0.U(32.W))
//with gpio
  import SEG._
  io.gpio.out := gpioLed
  gpioSwitch := io.gpio.in
  for(i <- 0 until 8){
    io.gpio.seg(i) := MuxLookup(gpioSeg(4*i+3, 4*i), num0)(Seq(
      0.U -> num0,
      1.U -> num1,
      2.U -> num2,
      3.U -> num3,
      4.U -> num4,
      5.U -> num5,
      6.U -> num6,
      7.U -> num7,
      8.U -> num8,
      9.U -> num9,
      10.U -> numa,
      11.U -> numb,
      12.U -> numc,
      13.U -> numd,
      14.U -> nume,
      15.U -> numf
    ))
  }
//with APB
  val addr = io.in.paddr(3, 0)
  io.in.pready := io.in.penable
  io.in.pslverr := 0.U
  io.in.prdata := 0.U
  when(io.in.psel){
    //Write
    gpioLed := Mux(addr ===  0.U && io.in.pwrite, io.in.pwdata, gpioLed)
    gpioSeg := Mux(addr === 8.U && io.in.pwrite, io.in.pwdata, gpioSeg)
    //READ
    io.in.prdata := Mux(addr === 4.U && ~io.in.pwrite, gpioSwitch, 0.U)
  }
}

class APBGPIO(address: Seq[AddressSet])(implicit p: Parameters) extends LazyModule {
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
    val gpio_bundle = IO(new GPIOIO)

    val mgpio = Module(new gpioChisel)
    mgpio.io.clock := clock
    mgpio.io.reset := reset
    mgpio.io.in <> in
    gpio_bundle <> mgpio.io.gpio
  }
}
