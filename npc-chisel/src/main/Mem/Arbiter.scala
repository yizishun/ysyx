package npc.mem

import chisel3._
import chisel3.util._
import npc._

class arbiterIO(xlen: Int) extends Bundle{
  val imem = new memIO(xlen)
  val dmem = new memIO(xlen)
  val mem = Flipped(new memIO(xlen))
}

class Arbiter(val coreConfig: CoreConfig) extends Module{
  val io = IO(new arbiterIO(coreConfig.xlen))
  io.mem.clk := clock
  io.mem.rst := reset

  //imem reg
  val imem_arready = RegInit(io.imem.arready)
  val imem_rdata = RegInit(io.imem.rdata)
  val imem_rresp = RegInit(io.imem.rresp)
  val imem_rvalid = RegInit(io.imem.rvalid)
  val imem_awready = RegInit(io.imem.awready)
  val imem_wready = RegInit(io.imem.wready)
  val imem_bresp = RegInit(io.imem.bresp)
  val imem_bvalid = RegInit(io.imem.bvalid)
  io.imem.arready := imem_arready
  io.imem.rdata := imem_rdata
  io.imem.rresp := imem_rresp
  io.imem.rvalid := imem_rvalid
  io.imem.awready := imem_awready
  io.imem.wready := imem_wready
  io.imem.bresp := imem_bresp
  io.imem.bvalid := imem_bvalid
  //dmem reg
  val dmem_arready = RegInit(io.dmem.arready)
  val dmem_rdata = RegInit(io.dmem.rdata)
  val dmem_rresp = RegInit(io.dmem.rresp)
  val dmem_rvalid = RegInit(io.dmem.rvalid)
  val dmem_awready = RegInit(io.dmem.awready)
  val dmem_wready = RegInit(io.dmem.wready)
  val dmem_bresp = RegInit(io.dmem.bresp)
  val dmem_bvalid = RegInit(io.dmem.bvalid)
  io.dmem.arready := dmem_arready
  io.dmem.rdata := dmem_rdata
  io.dmem.rresp := dmem_rresp
  io.dmem.rvalid := dmem_rvalid
  io.dmem.awready := dmem_awready
  io.dmem.wready := dmem_wready
  io.dmem.bresp := dmem_bresp
  io.dmem.bvalid := dmem_bvalid
  //mem reg
  val mem_araddr = RegInit(io.mem.araddr)
  val mem_arvalid = RegInit(io.mem.arvalid)
  val mem_rready = RegInit(io.mem.rready)
  val mem_awaddr = RegInit(io.mem.awaddr)
  val mem_awvalid = RegInit(io.mem.awvalid)
  val mem_wdata = RegInit(io.mem.wdata)
  val mem_wstrb = RegInit(io.mem.wstrb)
  val mem_wvalid = RegInit(io.mem.wvalid)
  val mem_bready = RegInit(io.mem.bready)
  io.mem.araddr := mem_araddr
  io.mem.arvalid := mem_arvalid
  io.mem.rready := mem_rready
  io.mem.awaddr := mem_awaddr
  io.mem.awvalid := mem_awvalid
  io.mem.wdata := mem_wdata
  io.mem.wstrb := mem_wstrb
  io.mem.wvalid := mem_wvalid
  io.mem.bready := mem_bready

//--------------------------------------------------------------------------
  //state transition
  val s_slect :: s_worki :: s_workd :: s_worki_1 :: s_workd_1 :: Nil = Enum(5)
  val state = RegInit(s_slect)
  val nextState = WireDefault(s_slect)
  nextState := MuxLookup(state, s_slect)(Seq(
    s_slect -> MuxCase(s_slect, Array(
      (io.imem.arvalid & imem_arready) -> s_worki,
      ((io.dmem.arvalid & dmem_arready) || (io.dmem.awvalid & dmem_awready & io.dmem.wvalid & dmem_wready)).asBool -> s_workd
    )),
    s_worki -> Mux(io.mem.rready & io.mem.rvalid, s_worki_1, s_worki),
    s_worki_1 -> Mux(io.imem.rready & io.imem.rvalid, s_slect, s_worki_1),
    s_workd -> Mux((io.mem.rready & io.mem.rvalid)|(io.mem.bready & io.mem.bvalid), s_workd_1, s_workd),
    s_workd_1 -> Mux((io.dmem.rready & io.dmem.rvalid)|(io.dmem.bready & io.dmem.bvalid), s_slect, s_workd_1)
  ))
  state := nextState

  switch(nextState){
    is(s_slect){
      DefaultMem()
      DefaultDmem()
      DefaultImem()
    }
    is(s_worki){
      ConnectImem()
      DefaultDmem()
      dmem_arready := false.B
      dmem_wready := false.B
      dmem_awready := false.B
    }
    is(s_worki_1){
      ConnectImem()
      DefaultDmem()
      //if handshake hapen,it will go into this state,so we should close connection to memory
      mem_arvalid := false.B
      mem_rready := false.B
      //block
      dmem_arready := false.B
      dmem_wready := false.B
      dmem_awready := false.B
    }
    is(s_workd){
      ConnectDmem()
      DefaultImem()
      imem_arready := false.B
    }
    is(s_workd_1){
      ConnectDmem()
      DefaultImem()
      //close connection to memory
      mem_arvalid := false.B
      mem_rready := false.B
      mem_awvalid := false.B
      mem_wvalid := false.B
      mem_bready := false.B
      //block
      imem_arready := false.B

    }
  }
//--------------------------------------------------------------------------------------------------------------------------
def ConnectImem(): Unit = {
  imem_arready := io.mem.arready
  imem_rdata := io.mem.rdata
  imem_rresp := io.mem.rresp
  imem_rvalid := io.mem.rvalid
  imem_awready := io.mem.awready
  imem_wready := io.mem.wready
  imem_bresp := io.mem.bresp
  imem_bvalid := io.mem.bvalid

  mem_araddr := io.imem.araddr
  mem_arvalid := io.imem.arvalid
  mem_rready := io.imem.rready
  mem_awaddr := io.imem.awaddr
  mem_awvalid := io.imem.awvalid
  mem_wdata := io.imem.wdata
  mem_wstrb := io.imem.wstrb
  mem_wvalid := io.imem.wvalid
  mem_bready := io.imem.bready
  
}
def ConnectDmem(): Unit = {
  dmem_arready := io.mem.arready
  dmem_rdata := io.mem.rdata
  dmem_rresp := io.mem.rresp
  dmem_rvalid := io.mem.rvalid
  dmem_awready := io.mem.awready
  dmem_wready := io.mem.wready
  dmem_bresp := io.mem.bresp
  dmem_bvalid := io.mem.bvalid

  mem_araddr := io.dmem.araddr
  mem_arvalid := io.dmem.arvalid
  mem_rready := io.dmem.rready
  mem_awaddr := io.dmem.awaddr
  mem_awvalid := io.dmem.awvalid
  mem_wdata := io.dmem.wdata
  mem_wstrb := io.dmem.wstrb
  mem_wvalid := io.dmem.wvalid
  mem_bready := io.dmem.bready

}
def DefaultMem(): Unit = {
  mem_araddr := DontCare
  mem_arvalid := false.B
  mem_rready := false.B
  mem_awaddr := false.B
  mem_awvalid := false.B
  mem_wdata := DontCare
  mem_wstrb := 0.U
  mem_wvalid := false.B
  mem_bready := false.B
}
def DefaultImem(): Unit = {
  imem_arready := true.B
  imem_rdata := DontCare
  imem_rresp := 0.U
  imem_rvalid := false.B
  imem_awready := false.B
  imem_wready := false.B
  imem_bresp := false.B
  imem_bvalid := false.B
}
def DefaultDmem(): Unit = {
  dmem_arready := true.B
  dmem_rdata := DontCare
  dmem_rresp := 0.U
  dmem_rvalid := false.B
  dmem_awready := true.B
  dmem_wready := true.B
  dmem_bresp := false.B
  dmem_bvalid := false.B
}
}


/* 
+-----+      +---------+      +-----+
| IFU | ---> |         |      |     |
+-----+      |         |      |     |
             | Arbiter | ---> | Mem |
+-----+      |         |      |     |
| LSU | ---> |         |      |     |
+-----+      +---------+      +-----+  
 */