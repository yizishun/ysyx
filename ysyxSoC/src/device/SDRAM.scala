package ysyx

import chisel3._
import chisel3.util._
import chisel3.experimental.{attach, Analog}

import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.amba.apb._
import org.chipsalliance.cde.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._

object sdramCmd{
  val ACTIVE = 3.U
  val PRECHARGE = 2.U
  val READ = 5.U
  val WRITE = 4.U
  val NOP = 7.U
  val LOADMODE = 0.U
  val BURSTTERMINATE = 6.U
  val AUTOFRESH = 1.U
}

class SDRAMIO extends Bundle {
  val clk = Output(Bool())
  val cke = Output(Bool())
  val cs  = Output(Bool())
  val ras = Output(Bool())
  val cas = Output(Bool())
  val we  = Output(Bool())
  val a   = Output(UInt(14.W))
  val ba  = Output(UInt(2.W))
  val dqm = Output(UInt(4.W))
  val dq  = Analog(32.W)
}

class SDRAMRANKIO extends Bundle {
  val clk = Output(Bool())
  val cke = Output(Bool())
  val cs  = Output(Bool())
  val ras = Output(Bool())
  val cas = Output(Bool())
  val we  = Output(Bool())
  val a   = Output(UInt(13.W))
  val ba  = Output(UInt(2.W))
  val dqm = Output(UInt(4.W))
  val dq  = Analog(32.W)
}

class SDRAMCHIPIO extends Bundle {
  val clk = Output(Bool())
  val cke = Output(Bool())
  val cs  = Output(Bool())
  val ras = Output(Bool())
  val cas = Output(Bool())
  val we  = Output(Bool())
  val a   = Output(UInt(13.W))
  val ba  = Output(UInt(2.W))
  val dqm = Output(UInt(2.W))
  val dqr = Input(UInt(16.W))
  val dqw = Output(UInt(16.W))
  val outEn = Input(Bool())
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

class sdramChisel extends RawModule{
  val io = IO(Flipped(new SDRAMIO))
  val rank0 = Module(new sdramRank(0))
  val rank1 = Module(new sdramRank(2))

  withClockAndReset(io.clk.asClock, (~io.cke).asAsyncReset){
    import sdramCmd._
    val cmdw = Wire(UInt(4.W))
    cmdw := Cat(Cat(io.cs, io.ras), Cat(io.cas, io.we))
    val csR = Reg(Bool())
    val csw = Wire(Bool())
    csw := Mux(io.a(13), true.B, io.cs)
    csR := Mux(cmdw === ACTIVE, csw, csR)

    rank0.io.clk := io.clk
    rank0.io.cke := io.cke
    rank0.io.cs := Mux(cmdw === ACTIVE, csw, csR)
    rank0.io.ras := io.ras
    rank0.io.cas := io.cas
    rank0.io.we := io.we
    rank0.io.a := io.a(12, 0)
    rank0.io.ba := io.ba
    rank0.io.dqm := io.dqm
    attach(rank0.io.dq, io.dq)
  
    rank1.io.clk := io.clk
    rank1.io.cke := io.cke
    rank1.io.cs := Mux(cmdw === LOADMODE, rank0.io.cs, ~rank0.io.cs)
    rank1.io.ras := io.ras
    rank1.io.cas := io.cas
    rank1.io.we := io.we
    rank1.io.a := io.a(12, 0)
    rank1.io.ba := io.ba
    rank1.io.dqm := io.dqm
    attach(rank1.io.dq, io.dq)
  }
}

class sdramRank(id : Int) extends RawModule {
  val io = IO(Flipped(new SDRAMRANKIO))
  val chip0 = Module(new sdramChip(0 + id))
  val chip1 = Module(new sdramChip(1 + id))

  val dataI = Wire(UInt(32.W))
  val dataO = Wire(UInt(32.W))
  dataO := Cat(chip1.io.dqr, chip0.io.dqr)
  //dq logic
  dataI := TriStateInBuf(io.dq, dataO, chip0.io.outEn & chip1.io.outEn)

  chip0.io.clk := io.clk
  chip0.io.cke := io.cke
  chip0.io.cs := io.cs
  chip0.io.ras := io.ras
  chip0.io.cas := io.cas
  chip0.io.we := io.we
  chip0.io.a := io.a
  chip0.io.ba := io.ba
  chip0.io.dqm := io.dqm(1, 0)
  chip0.io.dqw := dataI(15, 0)

  chip1.io.clk := io.clk
  chip1.io.cke := io.cke
  chip1.io.cs := io.cs
  chip1.io.ras := io.ras
  chip1.io.cas := io.cas
  chip1.io.we := io.we
  chip1.io.a := io.a
  chip1.io.ba := io.ba
  chip1.io.dqm := io.dqm(3, 2)
  chip1.io.dqw := dataI(31, 16)
}

class sdramChip(id : Int) extends RawModule{
  val io = IO(Flipped(new SDRAMCHIPIO))
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
    val dataO = Wire(UInt(16.W))
    val dqmask = RegInit(0.U(2.W))
    
    val cmdw = Wire(UInt(4.W))
    cmdw := Cat(Cat(io.cs, io.ras), Cat(io.cas, io.we))
    cmd := cmdw
    dqmask := io.dqm
    bankAddr := Mux(cmdw === ACTIVE || cmdw === READ || cmdw === WRITE || cmdw === PRECHARGE, io.ba, bankAddr)
    //state
    val s_idle :: s_read :: s_readWaitCas :: s_write :: Nil = Enum(4)
    val state = RegInit(s_idle)
    val nextState = Wire(UInt(3.W))
    val burstCounter = RegInit(0.U(3.W))
    val casLatCounter = RegInit(0.U(3.W))
    val burstLenth = Wire(UInt(8.W))
    val casLat = Wire(UInt(3.W))
    burstLenth := (1.U << mode(2, 0))
    burstCounter := Mux(((nextState === s_read && state === s_readWaitCas) || cmd === WRITE) && burstLenth =/= 0.U, burstLenth - 1.U, Mux(burstCounter =/= burstLenth && burstCounter =/= 0.U, burstCounter - 1.U, burstLenth))
    casLat := mode(6, 4)
    casLatCounter := Mux(cmd === READ && state === s_idle, casLat - 1.U, Mux(casLatCounter =/= casLat && casLatCounter =/= 0.U, casLatCounter - 1.U, casLat))
    state := nextState
    nextState := MuxLookup(state, s_idle)(Seq(
      s_idle -> Mux(cmd === READ, s_readWaitCas, Mux(cmd === WRITE, s_write, s_idle)),
      s_readWaitCas -> Mux(casLatCounter === 1.U, s_read, s_readWaitCas),
      s_read -> Mux(burstCounter === 0.U, Mux(cmd === WRITE, s_write, s_idle), s_read),
      s_write -> Mux(burstCounter === 0.U, Mux(cmd === READ, s_readWaitCas, s_idle), s_write)
    ))
    
    //dq logic
    io.outEn := (nextState === s_read)
    dataI := io.dqw
    io.dqr := dataO
    
    when(cmd === ACTIVE) {
      active := active | (1.U << bankAddr)
    }.elsewhen(cmd === PRECHARGE) {
      active := active & ~(1.U << bankAddr)
    }
    rowAddr(io.ba) := Mux(cmdw === ACTIVE, io.a, rowAddr(io.ba))
    colAddr(io.ba) := Mux(cmdw === READ || cmdw === WRITE, io.a(8, 0), colAddr(io.ba))
    mode := Mux(cmdw === LOADMODE, io.a, mode)

    sdramArray.io.clock := io.clk.asClock
    sdramArray.io.active := active
    sdramArray.io.ba := bankAddr
    sdramArray.io.ra := rowAddr(bankAddr)
    sdramArray.io.ca := Mux(burstCounter =/= burstLenth, colAddr(bankAddr) + (burstLenth - burstCounter), colAddr(bankAddr))
    sdramArray.io.ren := (nextState === s_read)
    sdramArray.io.wen := (nextState === s_write)
    sdramArray.io.dqwrite := dataI
    dataO := sdramArray.io.dqread
    sdramArray.io.dqm := dqmask
    sdramArray.io.id := id.asUInt
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
    val id = Input(UInt(2.W))
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
 |    input [1:0]id,
 |    output reg [15:0] dqread
 |);
 |    import "DPI-C" function void sdram_read(input int id, input int ba, input int ra, input int ca, output int rdata);
 |    import "DPI-C" function void sdram_write(input int id, input int ba, input int ra, input int ca, input int wdata, input int wstrb);
 |    wire [31:0] baddr = {30'b0, ba};
 |    wire [31:0] raddr = {19'b0, ra};
 |    wire [31:0] caddr = {23'b0, ca};
 |    reg [31:0] rdata;
 |    wire [31:0] wdata = {16'b0, dqwrite};
 |    wire [31:0] wstrb = {30'b0, ~dqm[1], ~dqm[0]};
 |    wire [31:0] chipid = {30'b0, id};
 |    always @(*)begin
 |      rdata = 32'h0;
 |      if(active[ba])begin
 |        if(ren) sdram_read(chipid, baddr, raddr, caddr, rdata);
 |      end
 |      dqread = rdata[15:0];
 |    end
 |    always @(posedge clock)begin
 |      if(active[ba])begin
 |        if(wen) sdram_write(chipid, baddr, raddr, caddr, wdata, wstrb);
 |      end
 |    end
 |endmodule
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
