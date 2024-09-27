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
  val rdata = Output(UInt(32.W))
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
  val wdata = Input(UInt(32.W))
  val wstrb = Input(UInt(4.W))
  val wvalid = Input(Bool())
  val wlast = Input(Bool())
  val wready = Output(Bool())
  //B
  val bresp = Output(UInt(2.W))
  val bvalid = Output(Bool())
  val bid = Output(UInt(4.W))
  val bready = Input(Bool())
  // Function to connect AR channel signals
  def connectAR(other_axi: AXI4): Unit = {
    // Connecting input signals
    other_axi.araddr  := this.araddr
    other_axi.arvalid := this.arvalid
    other_axi.arid    := this.arid
    other_axi.arlen   := this.arlen
    other_axi.arsize  := this.arsize
    other_axi.arburst := this.arburst

    // Connecting output signal
    this.arready := other_axi.arready
  }
  // Function to connect AW channel signals
  def connectAW(other_axi: AXI4): Unit = {
    // Connecting input signals
    other_axi.awaddr  := this.awaddr
    other_axi.awvalid := this.awvalid
    other_axi.awid    := this.awid
    other_axi.awlen   := this.awlen
    other_axi.awsize  := this.awsize
    other_axi.awburst := this.awburst

    // Connecting output signal
    this.awready := other_axi.awready
  }

  // Function to connect W channel signals
  def connectW(other_axi: AXI4): Unit = {
    // Connecting input signals
    other_axi.wdata  := this.wdata
    other_axi.wstrb  := this.wstrb
    other_axi.wvalid := this.wvalid
    other_axi.wlast  := this.wlast

    // Connecting output signal
    this.wready := other_axi.wready
  }

  // Function to connect R channel signals
  def connectR(other_axi: AXI4): Unit = {
    // Connecting output signals
    other_axi.rdata  := this.rdata
    other_axi.rresp  := this.rresp
    other_axi.rvalid := this.rvalid
    other_axi.rlast  := this.rlast
    other_axi.rid    := this.rid

    // Connecting input signal
    this.rready := other_axi.rready
  }

  // Function to connect B channel signals
  def connectB(other_axi: AXI4): Unit = {
    // Connecting output signals
    other_axi.bresp  := this.bresp
    other_axi.bvalid := this.bvalid
    other_axi.bid    := this.bid

    // Connecting input signal
    this.bready := other_axi.bready
  }

  // Optional: Function to connect all channels
  def connectAll(other_axi: AXI4): Unit = {
    connectAR(other_axi)
    connectAW(other_axi)
    connectW(other_axi)
    connectR(other_axi)
    connectB(other_axi)
  }
}