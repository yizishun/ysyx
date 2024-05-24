package npc.mem

import chisel3._
import chisel3.util._
import npc._
import javax.sound.sampled.AudioInputStream

class xbarIO(xlen: Int) extends Bundle{
  val arb = new memIO(xlen)
  val uart = Flipped(new memIO(xlen))
  val sram = Flipped(new memIO(xlen))
}

class Xbar(val coreConfig: CoreConfig) extends Module{
  val io = IO(new xbarIO(coreConfig.xlen))
  io.uart.clk := clock
  io.uart.rst := reset
  io.sram.clk := clock
  io.sram.rst := reset

  //arb reg
  val arb_arready = RegInit(io.arb.arready)
  val arb_rdata = RegInit(io.arb.rdata)
  val arb_rresp = RegInit(io.arb.rresp)
  val arb_rvalid = RegInit(io.arb.rvalid)
  val arb_awready = RegInit(io.arb.awready)
  val arb_wready = RegInit(io.arb.wready)
  val arb_bresp = RegInit(io.arb.bresp)
  val arb_bvalid = RegInit(io.arb.bvalid)
  io.arb.arready := arb_arready
  io.arb.rdata := arb_rdata
  io.arb.rresp := arb_rresp
  io.arb.rvalid := arb_rvalid
  io.arb.awready := arb_awready
  io.arb.wready := arb_wready
  io.arb.bresp := arb_bresp
  io.arb.bvalid := arb_bvalid
  //uart reg
  val uart_araddr = RegInit(io.uart.araddr)
  val uart_arvalid = RegInit(io.uart.arvalid)
  val uart_rready = RegInit(io.uart.rready)
  val uart_awaddr = RegInit(io.uart.awaddr)
  val uart_awvalid = RegInit(io.uart.awvalid)
  val uart_wdata = RegInit(io.uart.wdata)
  val uart_wstrb = RegInit(io.uart.wstrb)
  val uart_wvalid = RegInit(io.uart.wvalid)
  val uart_bready = RegInit(io.uart.bready)
  io.uart.araddr := uart_araddr
  io.uart.arvalid := uart_arvalid
  io.uart.rready := uart_rready
  io.uart.awaddr := uart_awaddr
  io.uart.awvalid := uart_awvalid
  io.uart.wdata := uart_wdata
  io.uart.wstrb := uart_wstrb
  io.uart.wvalid := uart_wvalid
  io.uart.bready := uart_bready
  //sram reg
  val sram_araddr = RegInit(io.sram.araddr)
  val sram_arvalid = RegInit(io.sram.arvalid)
  val sram_rready = RegInit(io.sram.rready)
  val sram_awaddr = RegInit(io.sram.awaddr)
  val sram_awvalid = RegInit(io.sram.awvalid)
  val sram_wdata = RegInit(io.sram.wdata)
  val sram_wstrb = RegInit(io.sram.wstrb)
  val sram_wvalid = RegInit(io.sram.wvalid)
  val sram_bready = RegInit(io.sram.bready)
  io.sram.araddr := sram_araddr
  io.sram.arvalid := sram_arvalid
  io.sram.rready := sram_rready
  io.sram.awaddr := sram_awaddr
  io.sram.awvalid := sram_awvalid
  io.sram.wdata := sram_wdata
  io.sram.wstrb := sram_wstrb
  io.sram.wvalid := sram_wvalid
  io.sram.bready := sram_bready
//--------------------------------------------------------------------------------------------------------------------------------------
  //address transition
  val auart :: asram :: ainvalid ::Nil = Enum(3)
  val aw = Wire(UInt(2.W))
  val ar = Wire(UInt(2.W))
  aw := MuxCase(ainvalid, Seq(
    (io.arb.awaddr >= "h8000_0000".U(32.W) && io.arb.awaddr <= "h87ff_ffff".U(32.W)) -> asram,
    (io.arb.awaddr === "ha000_03f8".U(32.W)) -> auart
  ))
  ar := MuxCase(ainvalid, Seq(
    (io.arb.araddr >= "h8000_0000".U(32.W) && io.arb.araddr <= "h87ff_ffff".U(32.W)) -> asram,
    (io.arb.araddr === "ha000_03f8".U(32.W)) -> auart
  ))

  //state transition
  val s_select :: s_invalid ::s_uart :: s_sram :: s_uart_1 :: s_sram_1 :: Nil = Enum(6)
  val state = RegInit(s_select)
  val nextState = WireDefault(s_select)
  nextState := MuxLookup(state, s_select)(Seq(
    s_select -> Mux((io.arb.awvalid & io.arb.awready)|(io.arb.arvalid & io.arb.arready), Mux(
      io.arb.awvalid & io.arb.awready, Mux(aw === ainvalid, s_invalid, Mux(aw === asram, s_sram, s_uart)), Mux(ar === ainvalid, s_invalid, Mux(ar === asram, s_sram, s_uart))
    ), s_select),
    s_sram -> Mux((io.sram.rready & io.sram.rvalid)|(io.sram.bready & io.sram.bvalid), s_sram_1, s_sram),
    s_sram_1 -> Mux((io.arb.rready & io.arb.rvalid)|(io.arb.bready & io.arb.bvalid), s_select, s_sram_1),
    s_uart -> Mux((io.uart.rready & io.uart.rvalid)|(io.uart.bready & io.uart.bvalid), s_uart_1, s_uart),
    s_uart_1 -> Mux((io.arb.rready & io.arb.rvalid)|(io.arb.bready & io.arb.bvalid), s_select, s_uart_1)
  ))
  state := nextState

  switch(nextState){
    is(s_select){
      DefaultArb()
      DefaultSram()
      DefaultUart()
    }
    is(s_invalid){
      DefaultArb()
      DefaultSram()
      DefaultUart()
      arb_bresp := Mux(aw === ainvalid, 1.U, 0.U)
      arb_rresp := Mux(ar === ainvalid, 1.U, 0.U)
    }
    is(s_sram){
      Connectsram()
      DefaultUart()
    }
    is(s_sram_1){
      Connectsram()
      DefaultUart()
      sram_arvalid := false.B
      sram_rready := false.B
      sram_awvalid := false.B
      sram_wvalid := false.B
      sram_bready := false.B
    }
    is(s_uart){
      ConnectUart()
      DefaultSram()
    }
    is(s_uart_1){
      ConnectUart()
      DefaultSram()
      uart_arvalid := false.B
      uart_rready := false.B
      uart_awvalid := false.B
      uart_wvalid := false.B
      uart_bready := false.B      
    }
  }
//-----------------------------------------------------------------------------------------------------------------------------------
def ConnectUart(): Unit = {
  arb_arready := io.uart.arready
  arb_rdata := io.uart.rdata
  arb_rresp := io.uart.rresp
  arb_rvalid := io.uart.rvalid
  arb_awready := io.uart.awready
  arb_wready := io.uart.wready
  arb_bresp := io.uart.bresp
  arb_bvalid := io.uart.bvalid

  uart_araddr := io.arb.araddr
  uart_arvalid := io.arb.arvalid
  uart_rready := io.arb.rready
  uart_awaddr := io.arb.awaddr
  uart_awvalid := io.arb.awvalid
  uart_wdata := io.arb.wdata
  uart_wstrb := io.arb.wstrb
  uart_wvalid := io.arb.wvalid
  uart_bready := io.arb.bready
  
}
def Connectsram(): Unit = {
  arb_arready := io.sram.arready
  arb_rdata := io.sram.rdata
  arb_rresp := io.sram.rresp
  arb_rvalid := io.sram.rvalid
  arb_awready := io.sram.awready
  arb_wready := io.sram.wready
  arb_bresp := io.sram.bresp
  arb_bvalid := io.sram.bvalid

  sram_araddr := io.arb.araddr
  sram_arvalid := io.arb.arvalid
  sram_rready := io.arb.rready
  sram_awaddr := io.arb.awaddr
  sram_awvalid := io.arb.awvalid
  sram_wdata := io.arb.wdata
  sram_wstrb := io.arb.wstrb
  sram_wvalid := io.arb.wvalid
  sram_bready := io.arb.bready

}
def DefaultUart(): Unit = {
  uart_araddr := DontCare
  uart_arvalid := false.B
  uart_rready := false.B
  uart_awaddr := false.B
  uart_awvalid := false.B
  uart_wdata := DontCare
  uart_wstrb := 0.U
  uart_wvalid := false.B
  uart_bready := false.B
}
def DefaultSram(): Unit = {
  sram_araddr := DontCare
  sram_arvalid := false.B
  sram_rready := false.B
  sram_awaddr := false.B
  sram_awvalid := false.B
  sram_wdata := DontCare
  sram_wstrb := 0.U
  sram_wvalid := false.B
  sram_bready := false.B
}
def DefaultArb(): Unit = {
  arb_arready := true.B
  arb_rdata := DontCare
  arb_rresp := 0.U
  arb_rvalid := false.B
  arb_awready := true.B
  arb_wready := true.B
  arb_bresp := 0.U
  arb_bvalid := false.B
}
}







/* 
+-----+      +---------+      +------+      +------+
| IFU | ---> |         |      |      | ---> | UART |  [0x1000_0000, 0x1000_0fff)
+-----+      |         |      |      |      +------+
             | Arbiter | ---> | Xbar |
+-----+      |         |      |      |      +------+
| LSU | ---> |         |      |      | ---> | SRAM |  [0x8000_0000, 0x80ff_ffff)
+-----+      +---------+      +------+      +------+
 */