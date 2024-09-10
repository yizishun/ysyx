// See README.md for license details.

package npc
import npc.core._
import npc.bus._
import chisel3._
import chisel3.util._
import chiseltest._
import chiseltest.formal._
import org.scalatest.flatspec.AnyFlatSpec

class CacheTest extends Module {
  val io = IO(new Bundle {
    val req = new AXI4
    val block = Input(Bool())
  })

  val memSize = 2048  // byte
  val mem = Mem(memSize / 4, UInt(32.W))
  val dut = Module(new ICache(4, 16, MiniConfig()))

  dut.io.in <> io.req
  io.req.wready := false.B
  io.req.awready := false.B
  io.req.bvalid := false.B
  //axi
  val s_bfF1 :: s_btF12 :: Nil = Enum(2)
  val state = RegInit(s_bfF1)
  val nextState = Wire(UInt(2.W))
  nextState := MuxLookup(state, s_bfF1)(Seq(
    s_bfF1 -> Mux(dut.io.out.arvalid && dut.io.out.arready, s_btF12, s_bfF1),
    s_btF12 -> Mux(dut.io.out.rvalid && dut.io.out.rready, s_bfF1, s_btF12)
  ))
  state := nextState
  /*val arready = Output(Bool())
  val rdata = Output(UInt(32.W))
  val rresp = Output(UInt(2.W))
  val rvalid = Output(Bool())
  val rlast = Output(Bool())
  val rid = Output(UInt(4.W))
  val awready = Output(Bool())
  val wready = Output(Bool())
  val bresp = Output(UInt(2.W))
  val bvalid = Output(Bool())
  val bid = Output(UInt(4.W))*/
  dut.io.out.rresp := true.B
  dut.io.out.rlast := true.B
  dut.io.out.rid := 0.U
  dut.io.out.awready := false.B
  dut.io.out.wready := false.B
  dut.io.out.bresp := 0.U
  dut.io.out.bvalid := false.B
  dut.io.out.bid := 0.U
  dut.io.out.arready := false.B
  dut.io.out.rdata := 0.U
  dut.io.out.rvalid := false.B 
  val out_arready = RegInit(false.B)
  val out_rvalid = RegInit(false.B)
  val out_rdata = RegInit(0.U(32.W))
  dut.io.out.arready := out_arready
  dut.io.out.rvalid := out_rvalid
  dut.io.out.rdata := out_rdata
  switch(nextState){
    is(s_bfF1){
      out_arready := true.B
      out_rdata := 0.U
      out_rvalid := false.B
    }
    is(s_btF12){
      out_arready := false.B
      out_rdata := mem(io.req.araddr)
      out_rvalid := true.B
    }
  }

  val dutData = dut.io.in.rdata
  val refData = mem(io.req.araddr)
  dontTouch(dutData)
  dontTouch(refData)
  dontTouch(nextState)
  when (io.req.rvalid) {
    assert(dutData === refData)
  }
}
class FormalTest extends AnyFlatSpec with ChiselScalatestTester with Formal {
  "Test" should "pass" in {
    verify(new CacheTest, Seq(BoundedCheck(100), BtormcEngineAnnotation))
  }
}