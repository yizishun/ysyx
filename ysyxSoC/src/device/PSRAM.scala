package ysyx

import chisel3._
import chisel3.util._
import chisel3.experimental.Analog

import freechips.rocketchip.amba.apb._
import org.chipsalliance.cde.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._

class QSPIIO extends Bundle {
  val sck = Output(Bool())
  val ce_n = Output(Bool())
  val dio = Analog(4.W)
}

class psram_top_apb extends BlackBox {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Reset())
    val in = Flipped(new APBBundle(APBBundleParameters(addrBits = 32, dataBits = 32)))
    val qspi = new QSPIIO
  })
}

class psram extends BlackBox {
  val io = IO(Flipped(new QSPIIO))
}

class psramChisel extends RawModule {
  //this code have serious bug,only ensure bahavior is correct(only verilator)
  //because the cmd,addr....reg is regard as wire
  //so I should do something to make bahvior is correct ,even thouth it's logic is incorrect
  //the reason why I do this is maybe verilator is only support two state 
  //so the code the ysyx'material given has z is not suppoted,so it will cause some UB.
  val io = IO(Flipped(new QSPIIO))
  withClockAndReset((~(io.sck | io.ce_n)).asClock, io.ce_n.asAsyncReset){
    val psramCmd = Module(new psramChisel_cmd)
    val counter = withClock((~io.sck).asClock){ RegInit(0.U(5.W)) } //counter is as same as manual
    val qpi = Reg(Bool())
    val cmd = RegInit(0.U(8.W))
    val addr = RegInit(0.U(24.W))
    val rdata = RegInit(0.U(32.W))
    val wdata = RegInit(0.U(32.W))
    //state transition
    val s_cmd :: s_qcmd :: s_addr :: s_wait :: s_rdata_1 :: s_rdata_2 :: s_wdata :: s_err :: s_rfin :: s_wfin ::Nil = Enum(10)
    val nextState = Wire(UInt(4.W))
    val state = withClockAndReset((~io.sck).asClock, io.ce_n.asAsyncReset){Reg(UInt(4.W))}
    nextState := MuxLookup(Mux(counter === 0.U, Mux(qpi, s_qcmd, cmd), state), s_cmd)(Seq( //state can't reset,borrow a sck to reset
      s_cmd  -> Mux(counter === 8.U, 
        Mux(cmd === "h35".U, s_qcmd, s_addr), s_cmd),
      s_qcmd -> Mux(counter === 2.U, s_addr, s_qcmd),
      s_addr -> 
        Mux(counter === Mux(qpi, 8.U, 14.U), 
        Mux(cmd === "hEB".U, s_wait, 
        Mux(cmd === "h38".U, s_wdata, s_err)),s_addr),
      s_wait -> Mux(counter === Mux(qpi, 14.U, 20.U), s_rdata_1, s_wait),
      s_rdata_1 -> s_rdata_2,
      s_rdata_2 -> Mux(counter === Mux(qpi, 21.U, 27.U), s_rfin, s_rdata_1),
      s_wdata -> Mux(counter === Mux(qpi, 16.U, 22.U), s_wfin, s_wdata),
      s_rfin -> Mux(qpi, s_qcmd, s_cmd),
      s_wfin -> Mux(qpi, s_qcmd, s_cmd)
    ))
    when(io.ce_n || cmd === "h35".U){
      nextState := Mux(qpi, s_qcmd, s_cmd) // state can't reset,so reset nextstate
    }
    state := nextState
    assert(nextState =/= s_err, "psram: Only support `EB` , `38` and `35` command,your command is %d",cmd)

    //dio logic
    val dout = Wire(UInt(4.W))
    val outEn = Wire(Bool())
    dout := MuxLookup(nextState, 0.U)(Seq(
      s_rdata_1 -> rdata(31, 28),
      s_rdata_2 -> rdata(27, 24)
    ))
    outEn := (nextState === s_rdata_1) || (nextState === s_rdata_2)
    val di = TriStateInBuf(io.dio, dout, outEn)

    //counter plus logic
    counter := counter + 1.U

    //cmd logic
    when(nextState === s_cmd && counter <= 6.U){
      cmd := Cat(cmd(6, 0), di(0))
      //acually cmd is h35 is correct,but sck is disappear at that moment
      qpi := Mux(qpi, qpi, (cmd === "h1A".U))
    }
    //QPI mode cmd logic
    when(nextState === s_qcmd && counter <= 0.U){
      cmd := Cat(cmd(3, 0), di)
    }
    
    //addr logic
    when(nextState === s_addr && counter <= Mux(qpi, 6.U, 12.U)){
      addr := Cat(addr(19, 0), di)
    }
    //get rdata logic
    when(nextState === s_wait){
      rdata := psramCmd.io.rdata
    }
    //rdata logic
    when(nextState === s_rdata_2){
      rdata := rdata << 8
    }
    //wdata logic
    when(nextState === s_wdata || (counter === Mux(qpi, 7.U, 13.U) && cmd === "h38".U)){
      wdata := Cat(wdata(27, 0), di)
    }
    psramCmd.io.clock := io.sck.asClock
    psramCmd.io.cmd := cmd
    psramCmd.io.addr := addr
    psramCmd.io.ren := (nextState === s_wait)
    psramCmd.io.wen := (nextState === s_wfin) || (counter === Mux(qpi, 15.U, 21.U))
    psramCmd.io.wdata := wdata
  }
}

class psramChisel_cmd extends BlackBox with HasBlackBoxInline{
  val io = IO(new Bundle{
    val clock = Input(Clock())
    val addr = Input(UInt(32.W))
    val cmd = Input(UInt(8.W))
    val ren = Input(Bool())
    val wen = Input(Bool())
    val wdata = Input(UInt(32.W))
    val rdata = Output(UInt(32.W))
  })
  setInline("psramChisel_cmd.v",
  """module psramChisel_cmd(
  |     input        clock,
  |     input [31:0] addr,
  |     input [7:0]  cmd,
  |     input        ren,
  |     input        wen,
  |     input  [31:0]wdata,
  |     output reg[31:0]rdata
  |); 
  |   import "DPI-C" function void psram_read(input int addr, output int data);
  |   import "DPI-C" function void psram_write(input int addr, input int wdata);
  |   always@(*)begin
  |     rdata = 32'h0;
  |     if(cmd == 8'hEB && ren) psram_read(addr, rdata);
  |     else if((wen | ren) && cmd != 8'hEB && cmd != 8'h38)begin
  |      $fwrite(32'h80000002, "Assertion failed: Unsupport command `%xh`, only support `EBh`, `38h` and `35` command\n", cmd);
  |      $fatal;
  |     end
  |   end
  |   always@(posedge clock)begin
  |     if(cmd == 8'h38 && wen) psram_write(addr, wdata);
  |   end
  |endmodule
  """.stripMargin)
}

class APBPSRAM(address: Seq[AddressSet])(implicit p: Parameters) extends LazyModule {
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
    val qspi_bundle = IO(new QSPIIO)

    val mpsram = Module(new psram_top_apb)
    mpsram.io.clock := clock
    mpsram.io.reset := reset
    mpsram.io.in <> in
    qspi_bundle <> mpsram.io.qspi
  }
}
