package npc.bus

import chisel3._
import chisel3.util._
import npc._
import npc.dev._

class xbar3IO extends Bundle{
  val imem = new AXI4
  val dmem = new AXI4
  val soc = Flipped(new AXI4)
  val clint = Flipped(new AXI4)
}

class Xbar3 extends Module{
  val io = IO(new xbar3IO)
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
  val soc_rvalid_prev = RegNext(io.soc.rvalid)
  val soc_rready_prev = RegNext(io.soc.rready)
  val soc_bvalid_prev = RegNext(io.soc.bvalid)
  val soc_bready_prev = RegNext(io.soc.bready)
  val clint_rvalid_prev = RegNext(io.clint.rvalid)
  val clint_rready_prev = RegNext(io.clint.rready)
  val clint_bvalid_prev = RegNext(io.clint.bvalid)
  val clint_bready_prev = RegNext(io.clint.bready)

  val burstCnt = dontTouch(RegInit(0.U(8.W)))
 

//--------------------------------------------------------------------------------------------------------------------------------------
  val s_select :: s_i_soc :: s_d_soc :: s_d_clint :: Nil = Enum(4)
  val w_clint = Wire(Bool())
  val r_clint = Wire(Bool())
  w_clint := io.dmem.awaddr >= "h0200_0000".U(32.W) && io.dmem.awaddr <= "h0200_ffff".U(32.W)
  r_clint := io.dmem.araddr >= "h0200_0000".U(32.W) && io.dmem.araddr <= "h0200_ffff".U(32.W)
  //state transition
  val state = RegInit(s_select)
  val nextState = WireDefault(s_select)
  nextState := MuxLookup(state, s_select)(Seq(
    s_select -> MuxCase(s_select, Seq(
      (io.imem.arvalid) -> s_i_soc,
      ((io.dmem.awvalid & !w_clint) || (io.dmem.arvalid & !r_clint)) -> s_d_soc,
      ((io.dmem.arvalid & r_clint) || (io.dmem.awvalid & w_clint)) -> s_d_clint
    )),
    s_i_soc -> Mux((soc_rready & soc_rvalid_prev === 1.U && io.soc.rvalid === 0.U && burstCnt === 0.U), Mux(io.imem.arvalid, s_i_soc, s_select), s_i_soc),
    s_d_soc -> Mux((soc_rready & soc_rvalid_prev === 1.U && io.soc.rvalid === 0.U)|(soc_bready & soc_bvalid_prev === 1.U), s_select, s_d_soc),
    s_d_clint -> Mux((clint_rvalid_prev === 1.U && io.clint.rvalid === 0.U)|(clint_bvalid_prev === 1.U && io.clint.bvalid === 0.U), s_select, s_d_clint),
  ))
  state := nextState
  dontTouch(nextState)
  DefaultImem()
  DefaultDmem()
  DefaultSoc()
  DefaultClint()

  val imem_araddr = RegEnable(io.imem.araddr, io.imem.arvalid)
  val imem_arburst = RegEnable(io.imem.arburst, io.imem.arvalid)
  val imem_arlen = RegEnable(io.imem.arlen, io.imem.arvalid)
  val imem_arsize = RegEnable(io.imem.arsize, io.imem.arvalid)
  val dmem_araddr = RegEnable(io.dmem.araddr, io.dmem.arvalid)
  val dmem_awaddr = RegEnable(io.dmem.awaddr, io.dmem.awvalid)
  switch(nextState){
    is(s_select){
    }
    is(s_i_soc){
      ConnectImem2Soc()
      when(io.soc.arready & io.soc.arvalid){
        soc_arvalid := 0.U
      }.elsewhen(~io.soc.arready & io.soc.arvalid){
        soc_arvalid := 1.U
        soc_araddr := imem_araddr
        soc_arburst := imem_arburst
        soc_arlen := imem_arlen
        soc_arsize := imem_arsize
      }
    }
    is(s_d_clint){
      ConnectDmem2Clint()
    }
    is(s_d_soc){
      ConnectDmem2Soc()
      when(io.soc.arready & io.soc.arvalid){
        soc_arvalid := 0.U
      }.elsewhen(~io.soc.arready & io.soc.arvalid){
        soc_arvalid := 1.U
        soc_araddr := dmem_araddr
      }
      when(io.soc.awready & io.soc.awvalid){
        soc_awvalid := 0.U
      }.elsewhen(~io.soc.awready & io.soc.awvalid){
        soc_awvalid := 1.U
        soc_awaddr := dmem_awaddr
      }
    }
  }

  when(io.soc.arready & io.soc.arvalid){
    burstCnt := io.soc.arlen + 1.U
  }.elsewhen(burstCnt =/= 0.U & io.soc.rready & io.soc.rvalid){
    burstCnt := burstCnt - 1.U
  }
//-----------------------------------------------------------------------------------------------------------------------------------
def ConnectImem2Soc(): Unit = {
  io.imem.arready := io.soc.arready
  io.imem.rdata := io.soc.rdata
  io.imem.rresp := io.soc.rresp
  io.imem.rvalid := io.soc.rvalid
  io.imem.rlast := io.soc.rlast
  io.imem.rid := io.soc.rid
  io.imem.awready := io.soc.awready
  io.imem.wready := io.soc.wready
  io.imem.bresp := io.soc.bresp
  io.imem.bvalid := io.soc.bvalid
  io.imem.bid := io.soc.bid

  soc_araddr := io.imem.araddr
  soc_arvalid := io.imem.arvalid
  soc_arid := io.imem.arid
  soc_arlen := io.imem.arlen
  soc_arsize := io.imem.arsize
  soc_arburst := io.imem.arburst
  soc_rready := io.imem.rready
  soc_awaddr := io.imem.awaddr
  soc_awvalid := io.imem.awvalid
  soc_awid := io.imem.awid
  soc_awlen := io.imem.awlen
  soc_awsize := io.imem.awsize
  soc_awburst := io.imem.awburst
  soc_wdata := io.imem.wdata
  soc_wstrb := io.imem.wstrb
  soc_wvalid := io.imem.wvalid
  soc_wlast := io.imem.wlast
  soc_bready := io.imem.bready
}
def ConnectDmem2Soc(): Unit = {
  io.dmem.arready := io.soc.arready
  io.dmem.rdata := io.soc.rdata
  io.dmem.rresp := io.soc.rresp
  io.dmem.rvalid := io.soc.rvalid
  io.dmem.rlast := io.soc.rlast
  io.dmem.rid := io.soc.rid
  io.dmem.awready := io.soc.awready
  io.dmem.wready := io.soc.wready
  io.dmem.bresp := io.soc.bresp
  io.dmem.bvalid := io.soc.bvalid
  io.dmem.bid := io.soc.bid

  soc_araddr := io.dmem.araddr
  soc_arvalid := io.dmem.arvalid
  soc_arid := io.dmem.arid
  soc_arlen := io.dmem.arlen
  soc_arsize := io.dmem.arsize
  soc_arburst := io.dmem.arburst
  soc_rready := io.dmem.rready
  soc_awaddr := io.dmem.awaddr
  soc_awvalid := io.dmem.awvalid
  soc_awid := io.dmem.awid
  soc_awlen := io.dmem.awlen
  soc_awsize := io.dmem.awsize
  soc_awburst := io.dmem.awburst
  soc_wdata := io.dmem.wdata
  soc_wstrb := io.dmem.wstrb
  soc_wvalid := io.dmem.wvalid
  soc_wlast := io.dmem.wlast
  soc_bready := io.dmem.bready
}
def ConnectDmem2Clint(): Unit = {
  io.dmem.arready := io.clint.arready
  io.dmem.rdata := io.clint.rdata
  io.dmem.rresp := io.clint.rresp
  io.dmem.rvalid := io.clint.rvalid
  io.dmem.rlast := io.clint.rlast
  io.dmem.rid := io.clint.rid
  io.dmem.awready := io.clint.awready
  io.dmem.wready := io.clint.wready
  io.dmem.bresp := io.clint.bresp
  io.dmem.bvalid := io.clint.bvalid
  io.dmem.bid := io.clint.bid

  io.clint.araddr := io.dmem.araddr
  io.clint.arvalid := io.dmem.arvalid
  io.clint.arid := io.dmem.arid
  io.clint.arlen := io.dmem.arlen
  io.clint.arsize := io.dmem.arsize
  io.clint.arburst := io.dmem.arburst
  io.clint.rready := io.dmem.rready
  io.clint.awaddr := io.dmem.awaddr
  io.clint.awvalid := io.dmem.awvalid
  io.clint.awid := io.dmem.awid
  io.clint.awlen := io.dmem.awlen
  io.clint.awsize := io.dmem.awsize
  io.clint.awburst := io.dmem.awburst
  io.clint.wdata := io.dmem.wdata
  io.clint.wstrb := io.dmem.wstrb
  io.clint.wvalid := io.dmem.wvalid
  io.clint.wlast := io.dmem.wlast
  io.clint.bready := io.dmem.bready
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
  io.clint.araddr := 0.U
  io.clint.arvalid := false.B
  io.clint.arid := 0.U
  io.clint.arlen := 0.U
  io.clint.arsize := 0.U
  io.clint.arburst := 0.U
  io.clint.rready := false.B
  io.clint.awaddr := false.B
  io.clint.awvalid := false.B
  io.clint.awid := 0.U
  io.clint.awlen := 0.U
  io.clint.awsize := 0.U
  io.clint.awburst := 0.U
  io.clint.wdata := 0.U
  io.clint.wstrb := 0.U
  io.clint.wvalid := false.B
  io.clint.wlast := DontCare
  io.clint.bready := false.B
}
def DefaultImem(): Unit = {
  io.imem.rid := 0.U
  io.imem.rlast := true.B
  io.imem.bid := 0.U
  io.imem.arready := true.B
  io.imem.rdata := DontCare
  io.imem.rresp := 0.U
  io.imem.rvalid := false.B
  io.imem.awready := false.B
  io.imem.wready := false.B
  io.imem.bresp := false.B
  io.imem.bvalid := false.B
}
def DefaultDmem(): Unit = {
  io.dmem.rid := 0.U
  io.dmem.rlast := true.B
  io.dmem.bid := 0.U
  io.dmem.arready := true.B
  io.dmem.rdata := DontCare
  io.dmem.rresp := 0.U
  io.dmem.rvalid := false.B
  io.dmem.awready := false.B
  io.dmem.wready := true.B
  io.dmem.bresp := false.B
  io.dmem.bvalid := false.B
}
}







/* 
riscve-ysyxsoc
+-----+      +---------------+      +------+
| IFU | ---> |               |----> | soc  |  -> socXbar -> ...
+-----+      |               |      +------+
             |      Xbar3    |
+-----+      |               |      +------+
| LSU | ---> |               |----> | CLINT|  [0x0200_0000, 0x0200_ffff)
+-----+      +---------------+      +------+
*/