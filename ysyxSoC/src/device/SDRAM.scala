package ysyx

import chisel3._
import chisel3.util._
import chisel3.experimental.Analog

import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.amba.apb._
import org.chipsalliance.cde.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._
import freechips.rocketchip.rocket.PRV.U

object sdramCmd{
  val ACTIVE = 3.U
  val PRECHARGE = 2.U
  val READ = 5.U
  val WRITE = 4.U
  val NOP = 7.U
  val LOADMODE = 0.U
  val BURSTTERMINATE = 6.U
}

class SDRAMIO extends Bundle {
  val clk = Output(Bool())
  val cke = Output(Bool())
  val cs  = Output(Bool())
  val ras = Output(Bool())
  val cas = Output(Bool())
  val we  = Output(Bool())
  val a   = Output(UInt(13.W))
  val ba  = Output(UInt(2.W))
  val dqm = Output(UInt(2.W))
  val dq  = Analog(16.W)
}

class sdram_top_axi extends BlackBox {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Bool())
    val in = Flipped(new AXI4Bundle(AXI4BundleParameters(addrBits = 32, dataBits = 32, idBits = 4)))
    val sdram = new SDRAMIO
  })
}

class sdram_top_apb extends BlackBox {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Bool())
    val in = Flipped(new APBBundle(APBBundleParameters(addrBits = 32, dataBits = 32)))
    val sdram = new SDRAMIO
  })
}

class sdram extends BlackBox {
  val io = IO(Flipped(new SDRAMIO))
}

class sdramChisel extends RawModule {
  val io = IO(Flipped(new SDRAMIO))
  withClockAndReset(io.clk.asClock, (~io.cke).asAsyncReset){
    import sdramCmd._
    val sdramArray = Module(new sdramChisel_array)
    val cmd = RegInit(0.U(4.W))
    val active = RegInit(0.U(4.W))
    val bankAddr = RegInit(0.U(2.W))
    val rowAddr = Reg(Vec(4, UInt(13.W)))
    val colAddr = Reg(Vec(4, UInt(9.W)))
    val mode = RegInit(0.U(13.W))
    val dataI = RegInit(0.U(16.W))
    val dataO = RegInit(0.U(16.W))
    
    bankAddr := Mux(cmd === ACTIVE || cmd === READ || cmd === WRITE || cmd === PRECHARGE, io.ba, bankAddr)
    cmd := Cat(Cat(io.cs, io.ras), Cat(io.cas, io.we))
    //state
    val s_idle :: s_read :: s_readWaitCas :: s_write :: s_writeWaitCas :: Nil = Enum(5)
    val state = RegInit(s_idle)
    val nextState = Wire(UInt(3.W))
    val burstCounter = RegInit(0.U(3.W))
    val casLantency = RegInit(0.U(3.W))
    burstCounter := Mux(state === s_idle || state =/= nextState, mode(2, 0), Mux(cmd === BURSTTERMINATE, 0.U, burstCounter - 1.U)) 
    casLantency := Mux(state === s_readWaitCas || state === s_writeWaitCas, casLantency - 1.U, mode(6, 4))
    state := nextState
    nextState := MuxLookup(state, s_idle)(Seq(
      s_idle -> Mux(cmd === READ, s_readWaitCas, s_idle),
      s_readWaitCas -> Mux(casLantency === 0.U, s_read, s_readWaitCas),
      s_read -> Mux(burstCounter === 0.U, Mux(cmd === WRITE, s_writeWaitCas, s_idle), s_read),
      s_writeWaitCas -> Mux(casLantency === 0.U, s_write, s_writeWaitCas),
      s_write -> Mux(burstCounter === 0.U, Mux(cmd === READ, s_readWaitCas, s_idle), s_write)
    ))
    
    
    active(bankAddr) := Mux(cmd === ACTIVE, 1.U, Mux(cmd === PRECHARGE, 0.U, active(bankAddr)))
    rowAddr(bankAddr) := Mux(cmd === ACTIVE, io.a, rowAddr(bankAddr))
    colAddr(bankAddr) := io.a(8, 0)
    mode := Mux(cmd === LOADMODE, io.a, mode)

    sdramArray.io.clock := io.clk
    sdramArray.io.active := active
    sdramArray.io.ba := bankAddr
    sdramArray.io.ra := rowAddr(bankAddr)
    sdramArray.io.ca := colAddr(bankAddr)
    sdramArray.io.ren := (state === s_read)
    sdramArray.io.wen := (state === s_write)
    sdramArray.io.dqwrite := dataI
    dataO := sdramArray.io.dqread
    sdramArray.io.dqm := io.dqm
  }
}

class sdramChisel_array extends BlackBox with HasBlackBoxInline{
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val active = Input(UInt(4.W))
    val ba = Input(UInt(2.W))
    val ra = Input(UInt(13.W))
    val ca = Input(UInt(9.W))
    val wen = Input(Bool()) //corresponding to burst lenth
    val ren = Input(Bool())
    val dqwrite = Input(UInt(16.W))
    val dqread = Output(UInt(16.W))
    val dqm = Input(UInt(2.W))
  })
  setInline("sdramChisel_array.v", 
  """module sdramChisel_array(
 |    input clock,
 |    input [3:0] active,
 |    input [1:0] ba,
 |    input [12:0] ra,
 |    input [8:0] ca,
 |    input wen,
 |    input ren,
 |    input [15:0] dqwrite,
 |    input [1:0] dqm,
 |    output [15:0] dqread
 |);
 |    import "DPI-C" function void sdram_read(input int ba, input int ra, input int ca, output int rdata);
 |    import "DPI-C" function void sdram_write(input int ba, input int ra, input int ca, input int wdata, input int wstrb);
 |    wire [31:0] baddr = {30'b0, ba};
 |    wire [31:0] raddr = {19'b0, ra};
 |    wire [31:0] caddr = {23'b0, ca};
 |    wire [31:0] rdata = {16'b0, dqread};
 |    wire [31:0] wdata = {16'b0, dqwrite};
 |    wire [31:0] wstrb = {16'b0, 8{dqm[1]}, 8{dqm[0]}};
 |    always @(*)begin
 |      if(active[ba])begin
 |        if(ren) sdram_read(baddr, raddr, caddr, rdata);
 |      end
 |    end
 |    always @(posedge clock)begin
 |      if(active[ba])begin
 |        if(wen) sdram_write(baddr, raddr, caddr, wdata, wstrb);
 |      end
 |    end
  """.stripMargin)
}

class AXI4SDRAM(address: Seq[AddressSet])(implicit p: Parameters) extends LazyModule {
  val beatBytes = 8
  val node = AXI4SlaveNode(Seq(AXI4SlavePortParameters(
    Seq(AXI4SlaveParameters(
        address       = address,
        executable    = true,
        supportsWrite = TransferSizes(1, beatBytes),
        supportsRead  = TransferSizes(1, beatBytes),
        interleavedId = Some(0))
    ),
    beatBytes  = beatBytes)))

  lazy val module = new Impl
  class Impl extends LazyModuleImp(this) {
    val (in, _) = node.in(0)
    val sdram_bundle = IO(new SDRAMIO)

    val converter = Module(new AXI4DataWidthConverter64to32)
    converter.io.clock := clock
    converter.io.reset := reset.asBool
    converter.io.in <> in

    val msdram = Module(new sdram_top_axi)
    msdram.io.clock := clock
    msdram.io.reset := reset.asBool
    msdram.io.in <> converter.io.out
    sdram_bundle <> msdram.io.sdram
  }
}

class APBSDRAM(address: Seq[AddressSet])(implicit p: Parameters) extends LazyModule {
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
    val sdram_bundle = IO(new SDRAMIO)

    val msdram = Module(new sdram_top_apb)
    msdram.io.clock := clock
    msdram.io.reset := reset.asBool
    msdram.io.in <> in
    sdram_bundle <> msdram.io.sdram
  }
}
