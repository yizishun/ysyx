package npc.core

import chisel3._
import chisel3.util._
import scala.math._
import npc._
import npc.bus._

class ICacheIO extends Bundle{
    val in = new AXI4
    val out = Flipped(new AXI4)
}

class ICache(val b : Int, val k : Int, val conf: CoreConfig) extends Module{
    val io = IO(new ICacheIO)

    //axi4 requre reg input and output,but i just reg output
    //"Each AXI interface has a single clock signal, ACLK. "
    //"All input signals are sampled on the rising edge of ACLK. "
    //"All output signal changes can only occur after the rising edge of ACLK."
    //"On Manager and Subordinate interfaces, there must be no combinatorial paths between input and output signals."
    //in reg
    val in_arready = RegInit(false.B)
    val in_rdata = RegInit(io.in.rdata)
    val in_rresp = RegInit(io.in.rresp)
    val in_rvalid = RegInit(false.B)
    val in_rlast = RegInit(io.in.rlast)
    val in_rid = RegInit(io.in.rid)
    val in_awready = RegInit(false.B)
    val in_wready = RegInit(false.B)
    val in_bresp = RegInit(false.B)
    val in_bvalid = RegInit(false.B)
    val in_bid = RegInit(io.in.bid)
    io.in.arready := in_arready
    io.in.rdata := in_rdata
    io.in.rresp := in_rresp
    io.in.rvalid := in_rvalid
    io.in.rlast := in_rlast
    io.in.rid := in_rid
    io.in.awready := in_awready
    io.in.wready := in_wready
    io.in.bresp := in_bresp
    io.in.bvalid := in_bvalid
    io.in.bid := in_bid
    //out reg
    val out_araddr = RegInit(io.out.araddr)
    val out_arvalid = RegInit(false.B)
    val out_arid = RegInit(io.out.arid)
    val out_arlen = RegInit(io.out.arlen)
    val out_arsize = RegInit(io.out.arsize)
    val out_arburst = RegInit(io.out.arburst)
    val out_rready = RegInit(false.B)
    val out_awaddr = RegInit(io.out.awaddr)
    val out_awvalid = RegInit(io.out.awvalid)
    val out_awid = RegInit(io.out.awid)
    val out_awlen = RegInit(io.out.awlen)
    val out_awsize = RegInit(io.out.awsize)
    val out_awburst = RegInit(io.out.awburst)
    val out_wdata = RegInit(io.out.wdata)
    val out_wstrb = RegInit(io.out.wstrb)
    val out_wvalid = RegInit(io.out.wvalid)
    val out_wlast = RegInit(io.out.wlast)
    val out_bready = RegInit(io.out.bready)
    io.out.araddr := out_araddr
    io.out.arvalid := out_arvalid
    io.out.arid := out_arid
    io.out.arlen := out_arlen
    io.out.arsize := out_arsize
    io.out.arburst := out_arburst
    io.out.rready := out_rready
    io.out.awaddr := out_awaddr
    io.out.awvalid := out_awvalid
    io.out.awid := out_awid
    io.out.awlen := out_awlen
    io.out.awsize := out_awsize
    io.out.awburst := out_awburst
    io.out.wdata := out_wdata
    io.out.wstrb := out_wstrb
    io.out.wvalid := out_wvalid
    io.out.wlast := out_wlast
    io.out.bready := out_bready

    val m = sqrt(b).toInt
    val n = sqrt(k).toInt
    val tagSize = 32 - m - n
    //icache (use dff default)
    val icache = Mem(k, UInt((1 + tagSize + b*8).W))
    // 初始化所有地址的值为0
    // 在仿真阶段初始化
    when (reset.asBool) {
        for (i <- 0 until k+1) {
            icache.write(i.U, 0.U)
        }
    }
    val tagA = Wire(UInt(tagSize.W))
    val index = Wire(UInt(n.W))
    val offset = Wire(UInt(m.W))

    val cacheData = Wire(UInt((1 + tagSize + b*8).W))
    val valid = Wire(Bool())
    val tagC = Wire(UInt(tagSize.W))
    val data = Wire(UInt((b*8).W))
    val hit = Wire(Bool())

    //state transition
    val s_bfF1 :: s_btF12 :: s_btF12_a :: s_btF12_ar :: Nil = Enum(4)
    val state = RegInit(s_bfF1)
    val nextState = Wire(UInt(4.W))
    nextState := MuxLookup(state, s_bfF1)(Seq(
        s_bfF1 -> Mux(io.in.arready & io.in.arvalid, s_btF12, s_bfF1),
        s_btF12 ->  Mux(valid && tagA === tagC && io.in.rready && io.in.rvalid, s_bfF1, 
                    Mux(io.out.arready & io.out.arvalid, s_btF12_a, s_btF12)),
        s_btF12_a -> Mux(io.out.rready & io.out.rvalid, s_btF12_ar, s_btF12_a),
        s_btF12_ar -> Mux(io.in.rready & io.in.rvalid, s_bfF1, s_btF12_ar)
    ))
    state := nextState
    dontTouch(nextState)

    if(conf.useDPIC){
        import npc.EVENT._
        val hitState = RegEnable(hit, false.B, nextState === s_btF12)
        PerformanceProbe(clock, ICacheHit, hit, 0.U, io.in.arready & io.in.arvalid & hit, io.in.rready & io.in.rvalid & hitState)
        PerformanceProbe(clock, ICacheMiss, state === s_btF12_ar, 0.U, io.in.arready & io.in.arvalid & ~hit, io.in.rready & io.in.rvalid & ~hitState)
    }

    //addr decode
    //b=4 k=16 m=2 n=4 tagSize=26
    tagA := io.in.araddr(31, m+n)
    index := io.in.araddr(m+n-1, m)
    offset := io.in.araddr(m-1, 0)

    //get and decode cacheData
    cacheData := icache(index)
    valid := cacheData(0)
    tagC := cacheData(tagSize, 1)
    data := cacheData(b*8+tagSize, tagSize+1)
    hit := valid && (tagA === tagC) && nextState === s_btF12
    dontTouch(hit)
    dontTouch(valid)
    dontTouch(data)

    DefaultConnect()

    switch(nextState){
        is(s_bfF1){
            out_arvalid := false.B
            out_rready := false.B
        } //default connect
        is(s_btF12){
            in_arready := false.B
            in_rvalid := valid && tagA === tagC
            out_arvalid := ~(valid && tagA === tagC)
            out_rready := false.B
        }
        is(s_btF12_a){
            in_arready := false.B
            in_rvalid := false.B
            out_arvalid := false.B
            out_rready := true.B
        }
        is(s_btF12_ar){
            in_arready := false.B
            in_rvalid := true.B
            out_arvalid := false.B
            out_rready := false.B
        }
    }
    in_rdata := Mux(nextState === s_btF12 && valid && tagA === tagC, data, 
                Mux(nextState === s_btF12_ar , io.out.rdata, in_rdata))
    //data tag valid
    icache(index) := Mux(nextState === s_btF12_ar, Cat(Cat(io.out.rdata, tagA), 1.U(1.W)), icache(index))
    

//-----------------------------------------------------------------------------------------------------------------------------------
    def DefaultConnect(): Unit = {
        in_arready := io.out.arready
        in_rdata := io.out.rdata
        in_rresp := io.out.rresp
        in_rvalid := io.out.rvalid
        in_rlast := io.out.rlast
        in_rid := io.out.rid
        in_awready := io.out.awready
        in_wready := io.out.wready
        in_bresp := io.out.bresp
        in_bvalid := io.out.bvalid
        in_bid := io.out.bid

        out_araddr := io.in.araddr
        out_arvalid := io.in.arvalid
        out_arid := io.in.arid
        out_arlen := io.in.arlen
        out_arsize := io.in.arsize
        out_arburst := io.in.arburst
        out_rready := io.in.rready
        out_awaddr := io.in.awaddr
        out_awvalid := io.in.awvalid
        out_awid := io.in.awid
        out_awlen := io.in.awlen
        out_awsize := io.in.awsize
        out_awburst := io.in.awburst
        out_wdata := io.in.wdata
        out_wstrb := io.in.wstrb
        out_wvalid := io.in.wvalid
        out_wlast := io.in.wlast
        out_bready := io.in.bready
    }
}
