package npc.bus

import chisel3._
import chisel3.util._
import npc._
import npc.dev._

class xbar3IO extends Bundle{
  val imem = new AXI4Slave
  val dmem = new AXI4Slave
  val soc = new AXI4Master
  val clint = new AXI4Master
}

class Xbar3 extends Module{
  val io = IO(new xbar3IO)

//--------------------------------------------------------------------------------------------------------------------------------------
  val s_select :: s_i_WaitSoCRL :: s_d_WaitSoCRV :: s_d_WaitClintRV :: Nil = Enum(4)
  val w_clint = Wire(Bool())
  val r_clint = Wire(Bool())
  w_clint := io.dmem.awaddr >= "h0200_0000".U(32.W) && io.dmem.awaddr <= "h0200_ffff".U(32.W)
  r_clint := io.dmem.araddr >= "h0200_0000".U(32.W) && io.dmem.araddr <= "h0200_ffff".U(32.W)
  //state transition
  val state = RegInit(s_select)
  val nextState = WireDefault(s_select)
  val is_dmem = dontTouch(Wire(Bool()))
  val is_imem = dontTouch(Wire(Bool()))
  is_dmem := Mux(io.dmem.arvalid | io.dmem.awvalid, true.B, false.B)
  is_imem := Mux(io.imem.arvalid, Mux(is_dmem || state === s_d_WaitClintRV || state === s_d_WaitSoCRV, false.B, true.B), false.B)
  nextState := MuxLookup(state, s_select)(Seq(
    s_select -> MuxCase(s_select, Seq(
      (is_imem) -> s_i_WaitSoCRL,
      (is_dmem & ~w_clint & ~r_clint) -> s_d_WaitSoCRV,
      (is_dmem & (w_clint | r_clint)) -> s_d_WaitClintRV
    )),
    s_i_WaitSoCRL   -> Mux((io.soc.rvalid & io.soc.rlast), Mux(io.imem.arvalid, s_i_WaitSoCRL, s_select), s_i_WaitSoCRL),
    s_d_WaitSoCRV   -> Mux(io.soc.rvalid | io.soc.bvalid, s_select, s_d_WaitSoCRV),
    s_d_WaitClintRV -> Mux(io.clint.rvalid | io.soc.bvalid, s_select, s_d_WaitClintRV),
  ))
  state := nextState
  dontTouch(nextState)
  io.imem.setDefaults()
  io.dmem.setDefaults()
  io.clint.setDefaults()
  io.soc.setDefaults()

  import npc.bus.AXI4InterConnector._
  switch(state){
    is(s_select){
      when(is_imem){
        connectAll(io.imem, io.soc)
      }.elsewhen(is_dmem & ~w_clint & ~r_clint){
        connectAll(io.dmem, io.soc)
      }.elsewhen(is_dmem & (w_clint | r_clint)){
        connectAll(io.dmem, io.clint)
      }
    }
    is(s_i_WaitSoCRL){
      connectAll(io.imem, io.soc)
    }
    is(s_d_WaitClintRV){
      connectAll(io.dmem, io.clint)
    }
    is(s_d_WaitSoCRV){
      connectAll(io.dmem, io.soc)
    }
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