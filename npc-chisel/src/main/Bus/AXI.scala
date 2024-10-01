package npc.bus

import chisel3._
import chisel3.util._

// AXI4 Master Interface
class AXI4Master extends Bundle {
  // Read Address Channel (AR)
  val araddr   = Output(UInt(32.W))
  val arvalid  = Output(Bool())
  val arid     = Output(UInt(4.W))
  val arlen    = Output(UInt(8.W))
  val arsize   = Output(UInt(3.W))
  val arburst  = Output(UInt(2.W))
  val arready  = Input(Bool())

  // Read Data Channel (R)
  val rdata    = Input(UInt(32.W))
  val rresp    = Input(UInt(2.W))
  val rvalid   = Input(Bool())
  val rlast    = Input(Bool())
  val rid      = Input(UInt(4.W))
  val rready   = Output(Bool())

  // Write Address Channel (AW)
  val awaddr   = Output(UInt(32.W))
  val awvalid  = Output(Bool())
  val awid     = Output(UInt(4.W))
  val awlen    = Output(UInt(8.W))
  val awsize   = Output(UInt(3.W))
  val awburst  = Output(UInt(2.W))
  val awready  = Input(Bool())

  // Write Data Channel (W)
  val wdata    = Output(UInt(32.W))
  val wstrb    = Output(UInt(4.W))
  val wvalid   = Output(Bool())
  val wlast    = Output(Bool())
  val wready   = Input(Bool())

  // Write Response Channel (B)
  val bresp    = Input(UInt(2.W))
  val bvalid   = Input(Bool())
  val bid      = Input(UInt(4.W))
  val bready   = Output(Bool())
   def setDefaults(): Unit = {
    // Set Output signals to default values
    araddr   := 0.U
    arvalid  := false.B
    arid     := 0.U
    arlen    := 0.U
    arsize   := 0.U
    arburst  := 0.U
    rready   := false.B

    awaddr   := 0.U
    awvalid  := false.B
    awid     := 0.U
    awlen    := 0.U
    awsize   := 0.U
    awburst  := 0.U
    bready   := false.B

    wdata    := 0.U
    wstrb    := 0.U
    wvalid   := false.B
    wlast    := false.B
  }
}

// AXI4 Slave Interface
class AXI4Slave extends Bundle {
  // Read Address Channel (AR)
  val araddr   = Input(UInt(32.W))
  val arvalid  = Input(Bool())
  val arid     = Input(UInt(4.W))
  val arlen    = Input(UInt(8.W))
  val arsize   = Input(UInt(3.W))
  val arburst  = Input(UInt(2.W))
  val arready  = Output(Bool())

  // Read Data Channel (R)
  val rdata    = Output(UInt(32.W))
  val rresp    = Output(UInt(2.W))
  val rvalid   = Output(Bool())
  val rlast    = Output(Bool())
  val rid      = Output(UInt(4.W))
  val rready   = Input(Bool())

  // Write Address Channel (AW)
  val awaddr   = Input(UInt(32.W))
  val awvalid  = Input(Bool())
  val awid     = Input(UInt(4.W))
  val awlen    = Input(UInt(8.W))
  val awsize   = Input(UInt(3.W))
  val awburst  = Input(UInt(2.W))
  val awready  = Output(Bool())

  // Write Data Channel (W)
  val wdata    = Input(UInt(32.W))
  val wstrb    = Input(UInt(4.W))
  val wvalid   = Input(Bool())
  val wlast    = Input(Bool())
  val wready   = Output(Bool())

  // Write Response Channel (B)
  val bresp    = Output(UInt(2.W))
  val bvalid   = Output(Bool())
  val bid      = Output(UInt(4.W))
  val bready   = Input(Bool())
  // Function to set default values
  def setDefaults(): Unit = {
    // Set Output signals to default values
    arready  := false.B

    rdata    := 0.U
    rresp    := 0.U
    rvalid   := false.B
    rlast    := false.B
    rid      := 0.U

    awready  := false.B

    wready   := false.B

    bresp    := 0.U
    bvalid   := false.B
    bid      := 0.U
  }
}

// Functions to connect AXI4 Master and Slave interfaces
object AXI4Connector {
  // Connect Read Address Channel
  def connectAR(master: AXI4Master, slave: AXI4Slave): Unit = {
    slave.araddr   := master.araddr
    slave.arvalid  := master.arvalid
    slave.arid     := master.arid
    slave.arlen    := master.arlen
    slave.arsize   := master.arsize
    slave.arburst  := master.arburst
    master.arready := slave.arready
  }

  // Connect Read Data Channel
  def connectR(master: AXI4Master, slave: AXI4Slave): Unit = {
    master.rdata   := slave.rdata
    master.rresp   := slave.rresp
    master.rvalid  := slave.rvalid
    master.rlast   := slave.rlast
    master.rid     := slave.rid
    slave.rready   := master.rready
  }

  // Connect Write Address Channel
  def connectAW(master: AXI4Master, slave: AXI4Slave): Unit = {
    slave.awaddr   := master.awaddr
    slave.awvalid  := master.awvalid
    slave.awid     := master.awid
    slave.awlen    := master.awlen
    slave.awsize   := master.awsize
    slave.awburst  := master.awburst
    master.awready := slave.awready
  }

  // Connect Write Data Channel
  def connectW(master: AXI4Master, slave: AXI4Slave): Unit = {
    slave.wdata    := master.wdata
    slave.wstrb    := master.wstrb
    slave.wvalid   := master.wvalid
    slave.wlast    := master.wlast
    master.wready  := slave.wready
  }

  // Connect Write Response Channel
  def connectB(master: AXI4Master, slave: AXI4Slave): Unit = {
    master.bresp   := slave.bresp
    master.bvalid  := slave.bvalid
    master.bid     := slave.bid
    slave.bready   := master.bready
  }

  // Optional: Connect All Channels
  def connectAll(master: AXI4Master, slave: AXI4Slave): Unit = {
    connectAR(master, slave)
    connectR(master, slave)
    connectAW(master, slave)
    connectW(master, slave)
    connectB(master, slave)
  }
}
object AXI4InterConnector {
  // Connect Read Address Channel (AR)
  def connectAR(slave: AXI4Slave, master: AXI4Master): Unit = {
    master.araddr   := slave.araddr
    master.arvalid  := slave.arvalid
    master.arid     := slave.arid
    master.arlen    := slave.arlen
    master.arsize   := slave.arsize
    master.arburst  := slave.arburst
    slave.arready   := master.arready
  }

  // Connect Read Data Channel (R)
  def connectR(slave: AXI4Slave, master: AXI4Master): Unit = {
    slave.rdata    := master.rdata
    slave.rresp    := master.rresp
    slave.rvalid   := master.rvalid
    slave.rlast    := master.rlast
    slave.rid      := master.rid
    master.rready  := slave.rready
  }

  // Connect Write Address Channel (AW)
  def connectAW(slave: AXI4Slave, master: AXI4Master): Unit = {
    master.awaddr   := slave.awaddr
    master.awvalid  := slave.awvalid
    master.awid     := slave.awid
    master.awlen    := slave.awlen
    master.awsize   := slave.awsize
    master.awburst  := slave.awburst
    slave.awready   := master.awready
  }

  // Connect Write Data Channel (W)
  def connectW(slave: AXI4Slave, master: AXI4Master): Unit = {
    master.wdata    := slave.wdata
    master.wstrb    := slave.wstrb
    master.wvalid   := slave.wvalid
    master.wlast    := slave.wlast
    slave.wready    := master.wready
  }

  // Connect Write Response Channel (B)
  def connectB(slave: AXI4Slave, master: AXI4Master): Unit = {
    slave.bresp    := master.bresp
    slave.bvalid   := master.bvalid
    slave.bid      := master.bid
    master.bready  := slave.bready
  }

  // Connect All Channels
  def connectAll(slave: AXI4Slave, master: AXI4Master): Unit = {
    connectAR(slave, master)
    connectR(slave, master)
    connectAW(slave, master)
    connectW(slave, master)
    connectB(slave, master)
  }
}
