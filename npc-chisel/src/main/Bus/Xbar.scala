package npc.bus

import chisel3._
import chisel3.util._
import npc._
import npc.dev._

class xbarIO extends Bundle{
  val arb = new AXI4
  val soc = Flipped(new AXI4)
  val clint = Flipped(new AXI4)
}

class Xbar extends Module{
  val io = IO(new xbarIO)

  //arb reg
  val arb_arready = RegInit(io.arb.arready)
  val arb_rdata = RegInit(io.arb.rdata)
  val arb_rresp = RegInit(io.arb.rresp)
  val arb_rvalid = RegInit(io.arb.rvalid)
  val arb_rlast = RegInit(io.arb.rlast)
  val arb_rid = RegInit(io.arb.rid)
  val arb_awready = RegInit(io.arb.awready)
  val arb_wready = RegInit(io.arb.wready)
  val arb_bresp = RegInit(io.arb.bresp)
  val arb_bvalid = RegInit(io.arb.bvalid)
  val arb_bid = RegInit(io.arb.bid)
  io.arb.arready := arb_arready
  io.arb.rdata := arb_rdata
  io.arb.rresp := arb_rresp
  io.arb.rvalid := arb_rvalid
  io.arb.rlast := arb_rlast
  io.arb.rid := arb_rid
  io.arb.awready := arb_awready
  io.arb.wready := arb_wready
  io.arb.bresp := arb_bresp
  io.arb.bvalid := arb_bvalid
  io.arb.bid := arb_bid
  //soc reg
  val soc_araddr = RegInit(io.soc.araddr)
  val soc_arvalid = RegInit(io.soc.arvalid)
  val soc_arid = RegInit(io.soc.arid)
  val soc_arlen = RegInit(io.soc.arlen)
  val soc_arsize = RegInit(io.soc.arsize)
  val soc_arburst = RegInit(io.soc.arburst)
  val soc_rready = RegInit(io.soc.rready)
  val soc_awaddr = RegInit(io.soc.awaddr)
  val soc_awvalid = RegInit(io.soc.awvalid)
  val soc_awid = RegInit(io.soc.awid)
  val soc_awlen = RegInit(io.soc.awlen)
  val soc_awsize = RegInit(io.soc.awsize)
  val soc_awburst = RegInit(io.soc.awburst)
  val soc_wdata = RegInit(io.soc.wdata)
  val soc_wstrb = RegInit(io.soc.wstrb)
  val soc_wvalid = RegInit(io.soc.wvalid)
  val soc_wlast = RegInit(io.soc.wlast)
  val soc_bready = RegInit(io.soc.bready)
  io.soc.araddr := soc_araddr
  io.soc.arvalid := soc_arvalid
  io.soc.arid := soc_arid
  io.soc.arlen := soc_arlen
  io.soc.arsize := soc_arsize
  io.soc.arburst := soc_arburst
  io.soc.rready := soc_rready
  io.soc.awaddr := soc_awaddr
  io.soc.awvalid := soc_awvalid
  io.soc.awid := soc_awid
  io.soc.awlen := soc_awlen
  io.soc.awsize := soc_awsize
  io.soc.awburst := soc_awburst
  io.soc.wdata := soc_wdata
  io.soc.wstrb := soc_wstrb
  io.soc.wvalid := soc_wvalid
  io.soc.wlast := soc_wlast
  io.soc.bready := soc_bready
  //CLINT reg
  val clint_araddr = RegInit(io.clint.araddr)
  val clint_arvalid = RegInit(io.clint.arvalid)
  val clint_arid = RegInit(io.clint.arid)
  val clint_arlen = RegInit(io.clint.arlen)
  val clint_arsize = RegInit(io.clint.arsize)
  val clint_arburst = RegInit(io.clint.arburst)
  val clint_rready = RegInit(io.clint.rready)
  val clint_awaddr = RegInit(io.clint.awaddr)
  val clint_awvalid = RegInit(io.clint.awvalid)
  val clint_awid = RegInit(io.clint.awid)
  val clint_awlen = RegInit(io.clint.awlen)
  val clint_awsize = RegInit(io.clint.awsize)
  val clint_awburst = RegInit(io.clint.awburst)
  val clint_wdata = RegInit(io.clint.wdata)
  val clint_wstrb = RegInit(io.clint.wstrb)
  val clint_wvalid = RegInit(io.clint.wvalid)
  val clint_wlast = RegInit(io.clint.wlast)
  val clint_bready = RegInit(io.clint.bready)
  io.clint.araddr := clint_araddr
  io.clint.arvalid := clint_arvalid
  io.clint.arid := clint_arid
  io.clint.arlen := clint_arlen
  io.clint.arsize := clint_arsize
  io.clint.arburst := clint_arburst
  io.clint.rready := clint_rready
  io.clint.awaddr := clint_awaddr
  io.clint.awvalid := clint_awvalid
  io.clint.awid := clint_awid
  io.clint.awlen := clint_awlen
  io.clint.awsize := clint_awsize
  io.clint.awburst := clint_awburst
  io.clint.wdata := clint_wdata
  io.clint.wstrb := clint_wstrb
  io.clint.wvalid := clint_wvalid
  io.clint.wlast := clint_wlast
  io.clint.bready := clint_bready
//--------------------------------------------------------------------------------------------------------------------------------------
  val s_select :: s_soc :: s_soc_1 :: s_clint :: s_clint_1 :: Nil = Enum(5)
  //address transition
  val aw = Wire(UInt(3.W))
  val ar = Wire(UInt(3.W))
  aw := Mux(io.arb.awaddr >= "h0200_0000".U(32.W) && io.arb.awaddr <= "h0200_ffff".U(32.W), s_clint, s_soc)
  ar := Mux(io.arb.araddr >= "h0200_0000".U(32.W) && io.arb.araddr <= "h0200_ffff".U(32.W), s_clint, s_soc)
  //state transition
  val state = RegInit(s_select)
  val nextState = WireDefault(s_select)
  nextState := MuxLookup(state, s_select)(Seq(
    s_select -> Mux((io.arb.awvalid)|(io.arb.arvalid & io.arb.arready), Mux(
      io.arb.awvalid, aw, ar
    ), s_select),
    s_soc -> Mux((io.soc.rready & io.soc.rvalid)|(io.soc.bready & io.soc.bvalid), s_soc_1, s_soc),
    s_soc_1 -> Mux((io.arb.rready & io.arb.rvalid)|(io.arb.bready & io.arb.bvalid), s_select, s_soc_1),
    s_clint -> Mux((io.clint.rready & io.clint.rvalid)|(io.clint.bready & io.clint.bvalid), s_clint_1, s_clint),
    s_clint_1 -> Mux((io.arb.rready & io.arb.rvalid)|(io.arb.bready & io.arb.bvalid), s_select, s_clint_1)
  ))
  state := nextState
  dontTouch(nextState)

  switch(nextState){
    is(s_select){
      DefaultArb()
      DefaultSoc()
      DefaultClint()
    }
    is(s_soc){
      ConnectSoc()
      DefaultClint()
    }
    is(s_soc_1){
      ConnectSoc()
      DefaultClint()
      soc_arvalid := false.B
      soc_rready := false.B
      soc_awvalid := false.B
      soc_wvalid := false.B
      soc_bready := false.B      
    }
    is(s_clint){
      ConnectClint()
      DefaultSoc()
    }
    is(s_clint_1){
      ConnectClint()
      DefaultSoc()
      clint_arvalid := false.B
      clint_rready := false.B
      clint_awvalid := false.B
      clint_wvalid := false.B
      clint_bready := false.B      
    }
  }
//-----------------------------------------------------------------------------------------------------------------------------------
def ConnectSoc(): Unit = {
  arb_arready := io.soc.arready
  arb_rdata := io.soc.rdata
  arb_rresp := io.soc.rresp
  arb_rvalid := io.soc.rvalid
  arb_rlast := io.soc.rlast
  arb_rid := io.soc.rid
  arb_awready := io.soc.awready
  arb_wready := io.soc.wready
  arb_bresp := io.soc.bresp
  arb_bvalid := io.soc.bvalid
  arb_bid := io.soc.bid

  soc_araddr := io.arb.araddr
  soc_arvalid := io.arb.arvalid
  soc_arid := io.arb.arid
  soc_arlen := io.arb.arlen
  soc_arsize := io.arb.arsize
  soc_arburst := io.arb.arburst
  soc_rready := io.arb.rready
  soc_awaddr := io.arb.awaddr
  soc_awvalid := io.arb.awvalid
  soc_awid := io.arb.awid
  soc_awlen := io.arb.awlen
  soc_awsize := io.arb.awsize
  soc_awburst := io.arb.awburst
  soc_wdata := io.arb.wdata
  soc_wstrb := io.arb.wstrb
  soc_wvalid := io.arb.wvalid
  soc_wlast := io.arb.wlast
  soc_bready := io.arb.bready
}
def ConnectClint(): Unit = {
  arb_arready := io.clint.arready
  arb_rdata := io.clint.rdata
  arb_rresp := io.clint.rresp
  arb_rvalid := io.clint.rvalid
  arb_rlast := io.clint.rlast
  arb_rid := io.clint.rid
  arb_awready := io.clint.awready
  arb_wready := io.clint.wready
  arb_bresp := io.clint.bresp
  arb_bvalid := io.clint.bvalid
  arb_bid := io.clint.bid

  clint_araddr := io.arb.araddr
  clint_arvalid := io.arb.arvalid
  clint_arid := io.arb.arid
  clint_arlen := io.arb.arlen
  clint_arsize := io.arb.arsize
  clint_arburst := io.arb.arburst
  clint_rready := io.arb.rready
  clint_awaddr := io.arb.awaddr
  clint_awvalid := io.arb.awvalid
  clint_awid := io.arb.awid
  clint_awlen := io.arb.awlen
  clint_awsize := io.arb.awsize
  clint_awburst := io.arb.awburst
  clint_wdata := io.arb.wdata
  clint_wstrb := io.arb.wstrb
  clint_wvalid := io.arb.wvalid
  clint_wlast := io.arb.wlast
  clint_bready := io.arb.bready
}
def DefaultSoc(): Unit = {
  soc_araddr := 0.U
  soc_arvalid := false.B
  soc_arid := 0.U
  soc_arlen := 0.U
  soc_arsize := 0.U
  soc_arburst := 0.U
  soc_rready := false.B
  soc_awaddr := false.B
  soc_awvalid := false.B
  soc_awid := 0.U
  soc_awlen := 0.U
  soc_awsize := 0.U
  soc_awburst := 0.U
  soc_wdata := 0.U
  soc_wstrb := 0.U
  soc_wvalid := false.B
  soc_wlast := true.B
  soc_bready := false.B
}
def DefaultClint(): Unit = {
  clint_araddr := 0.U
  clint_arvalid := false.B
  clint_arid := 0.U
  clint_arlen := 0.U
  clint_arsize := 0.U
  clint_arburst := 0.U
  clint_rready := false.B
  clint_awaddr := false.B
  clint_awvalid := false.B
  clint_awid := 0.U
  clint_awlen := 0.U
  clint_awsize := 0.U
  clint_awburst := 0.U
  clint_wdata := 0.U
  clint_wstrb := 0.U
  clint_wvalid := false.B
  clint_wlast := DontCare
  clint_bready := false.B
}
def DefaultArb(): Unit = {
  arb_arready := io.soc.arready
  arb_rdata := io.soc.rdata
  arb_rresp := io.soc.rresp
  arb_rvalid := io.soc.rvalid
  arb_rlast := io.soc.rlast
  arb_rid := io.soc.rid
  arb_awready := io.soc.awready
  arb_wready := io.soc.wready
  arb_bresp := io.soc.bresp
  arb_bvalid := io.soc.bvalid
  arb_bid := io.soc.bid
}
}







/* 
+-----+      +---------+      +------+      +------+
| IFU | ---> |         |      |      |-reg->| soc  |  -> socXbar -> ...
+-----+      |         |      |      |      +------+
             | Arbiter |-reg->| Xbar |
+-----+      |         |      |      |      +------+
| LSU | ---> |         |      |      |-reg->| CLINT|  [0x0200_0000, 0x0200_ffff)
+-----+      +---------+      +------+      +------+
 */