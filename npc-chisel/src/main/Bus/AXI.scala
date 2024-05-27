package npc.bus

import chisel3._
import chisel3.util._

class AXI4 extends Bundle{
  //this is slave version
  //AR
  val araddr = Input(UInt(32.W))
  val arvalid = Input(Bool())
  val arid = Input(UInt(4.W))
  val arlen = Input(UInt(8.W))
  val arsize = Input(UInt(3.W))
  val arburst = Input(UInt(2.W))
  val arready = Output(Bool())
  //R
  val rdata = Output(UInt(64.W))
  val rresp = Output(UInt(2.W))
  val rvalid = Output(Bool())
  val rlast = Output(Bool())
  val rid = Output(UInt(4.W))
  val rready = Input(Bool())
  //AW
  val awaddr = Input(UInt(32.W))
  val awvalid = Input(Bool())
  val awid = Input(UInt(4.W))
  val awlen = Input(UInt(8.W))
  val awsize = Input(UInt(3.W))
  val awburst = Input(UInt(2.W))
  val awready = Output(Bool())
  //W
  val wdata = Input(UInt(64.W))
  val wstrb = Input(UInt(8.W))
  val wvalid = Input(Bool())
  val wlast = Input(Bool())
  val wready = Output(Bool())
  //B
  val bresp = Output(UInt(2.W))
  val bvalid = Output(Bool())
  val bid = Output(UInt(4.W))
  val bready = Input(Bool())
}