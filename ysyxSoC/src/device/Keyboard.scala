package ysyx

import chisel3._
import chisel3.util._

import freechips.rocketchip.amba.apb._
import org.chipsalliance.cde.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._

class PS2IO extends Bundle {
  val clk = Input(Bool())
  val data = Input(Bool())
}

class PS2CtrlIO extends Bundle {
  val clock = Input(Clock())
  val reset = Input(Bool())
  val in = Flipped(new APBBundle(APBBundleParameters(addrBits = 32, dataBits = 32)))
  val ps2 = new PS2IO
}

class ps2_top_apb extends BlackBox {
  val io = IO(new PS2CtrlIO)
}

class ps2Chisel extends Module {
  val io = IO(new PS2CtrlIO)
  //with ps2
  val ps2Receiver = Module(new ps2Receive)
  val fifo = RegInit(0.U(128.W))
  val ptr = RegInit(0.U(8.W))
  for(i <- 0 until 7){
    fifo := fifo.bitSet(ptr+i.U, ps2Receiver.io.data(i).asBool)
  }
  ptr := Mux(ptr <= 120.U, ptr + 8.U, ptr)
  val data = fifo(7, 0)
  dontTouch(data)
  ps2Receiver.io.clk := clock
  ps2Receiver.io.resetn := reset
  ps2Receiver.io.ps2_clk := io.ps2.clk
  ps2Receiver.io.ps2_data := io.ps2.data
  //with apb
  when(io.in.psel){
    assert(~io.in.pwrite)
  }
  io.in.pready := io.in.penable
  io.in.pslverr := 0.U
  io.in.prdata := Mux(io.in.psel && io.in.paddr(0) === 0.U && ~io.in.pwrite, data, 0.U)
  when(io.in.pready && io.in.psel){
    fifo := fifo >> 8
    ptr := Mux(ptr >= 8.U, ptr - 8.U, ptr)
  }
}

class ps2Receive extends BlackBox with HasBlackBoxInline{
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val resetn = Input(Reset())
    val ps2_clk = Input(Bool())
    val ps2_data = Input(Bool())
    val data = Output(UInt(8.W))
    val ready = Output(Bool())
  })
  setInline("ps2Receive.v",
"""module ps2Receive(
|    input clk,
|    input resetn,
|    input ps2_clk,
|    input ps2_data,
|    output reg [7:0]data,
|    output reg ready);
|
|    reg [9:0] buffer;        // ps2_data bits
|    reg [3:0] count;  // count ps2_data bits
|    reg [2:0] ps2_clk_sync;
|
|    always @(posedge clk) begin
|        ps2_clk_sync <=  {ps2_clk_sync[1:0],ps2_clk};
|    end
|
|    wire sampling = ps2_clk_sync[2] & ~ps2_clk_sync[1];
|
|    always @(posedge clk) begin
|        if (resetn) begin // reset
|            count <= 0;
|            ready <= 0;
|        end
|        else begin
|            if (sampling) begin
|              if (count == 4'd10) begin
|                if ((buffer[0] == 0) &&  // start bit
|                    (ps2_data)       &&  // stop bit
|                    (^buffer[9:1])) begin      // odd  parity
|                    data <= buffer[8:1];
|                    ready <= 1;
|                    $display("receive %x", buffer[8:1]);
|               end
|                count <= 0;     // for next
|              end else begin
|                buffer[count] <= ps2_data;  // store ps2_data
|                count <= count + 3'b1;
|                ready <= 0;
|              end
|            end
|           else begin
|               if(count == 0)
|                 ready <= 0;
|           end
|        end
|    end
|
|endmodule
""".stripMargin)
}

class APBKeyboard(address: Seq[AddressSet])(implicit p: Parameters) extends LazyModule {
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
    val ps2_bundle = IO(new PS2IO)

    val mps2 = Module(new ps2Chisel)
    mps2.io.clock := clock
    mps2.io.reset := reset
    mps2.io.in <> in
    ps2_bundle <> mps2.io.ps2
  }
}
