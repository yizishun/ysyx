package npc.core

import chisel3._
import chisel3.util._
import scala.math._
import npc._
import npc.bus._

class ICacheIO extends Bundle{
    val in = Flipped(new Ifu2IcacheIO)
    val out = Flipped(new AXI4)
    val fencei = Flipped(Irrevocable(new npc.core.idu.IFUSignals))
}
class ICacheBlock(val tagSize: Int, val block_sz: Int) extends Bundle{
    val valid = Bool()
    val tag = UInt(tagSize.W)
    val data = Vec(block_sz/4, UInt(32.W))
}

class ICacheSet(val tagSize : Int, val block_sz : Int, val way : Int) extends Bundle{
    val set = Vec(way, new ICacheBlock(tagSize, block_sz))
}

class ICache(val set : Int, val way : Int, val block_sz : Int,val conf: CoreConfig) extends Module{
    val io = IO(new ICacheIO)
    val bus_w = 4

    val m = log2(block_sz).toInt
    val n = log2(set).toInt
    val w = log2(way).toInt
    val c = block_sz / 4
    val count = RegInit(c.U(4.W))
    val tagSize = 32 - m - n
    println(tagSize + (block_sz/4)*32 + 1)
    //icache (use dff by default)
    val icache = Mem(set, new ICacheSet(tagSize, block_sz, way))
    val base_addr = dontTouch(Wire(UInt(32.W)))
    val tagA = dontTouch(Wire(UInt(tagSize.W)))
    val index = dontTouch(Wire(UInt(n.W)))
    val offset = dontTouch(Wire(UInt(m.W)))

    val cacheSet = dontTouch(Wire(new ICacheSet(tagSize, block_sz, way)))
    val cacheData = dontTouch(Wire(new ICacheBlock(tagSize, block_sz)))
    val wayHit = dontTouch(Wire(UInt(w.W)))
    val valid = dontTouch(Wire(Bool()))
    val tagC = dontTouch(Wire(UInt(tagSize.W)))
    val data_h = dontTouch(Wire(UInt((bus_w*8).W)))
    val data_m = dontTouch(Wire(UInt((bus_w*8).W)))
    val hit = dontTouch(Wire(Bool()))
    val fenceCnt = RegInit(0.U(n.W))
    val fifoPtr = dontTouch(RegInit(0.U(w.W)))

    //state transition
    val is_sdram = dontTouch(RegEnable(io.in.araddr >= "ha000_0000".U(32.W) && io.in.araddr <= "hbfff_ffff".U(32.W), false.B, io.in.arready & io.in.arvalid))
    val s_WaitUpV :: s_WaitImemARR :: s_WaitImemRV :: s_WaitUpR :: s_fence_i :: Nil = Enum(5)
    val skip_UpR2V = Mux(io.in.rready, s_WaitUpV, s_WaitUpR)
    val state = RegInit(s_WaitUpV)
    val nextState = Wire(UInt(4.W))
    nextState := MuxLookup(state, s_WaitUpV)(Seq(
        s_WaitUpV -> MuxCase(s_WaitUpV, 
                Array((io.in.arvalid) -> Mux(hit, skip_UpR2V, s_WaitImemARR),
                      (io.fencei.valid && io.fencei.bits.is_fencei) -> s_fence_i)),
        s_WaitImemARR -> Mux(io.out.arready, s_WaitImemRV, s_WaitImemARR),
        s_WaitImemRV -> Mux(io.out.rvalid, Mux(count === 0.U, skip_UpR2V, Mux(is_sdram, s_WaitImemRV, s_WaitImemARR)), s_WaitImemRV),
        s_WaitUpR -> Mux(io.in.rready, s_WaitUpV, s_WaitUpR),
        s_fence_i -> Mux(fenceCnt === set.U - 1.U, s_WaitUpV, s_fence_i)
    ))
    state := nextState
    dontTouch(nextState)

    if(conf.useDPIC){
        import npc.EVENT._
        val hitState = RegEnable(hit, false.B, state === s_WaitUpV)
        PerformanceProbe(clock, ICacheHit, hit, 0.U, io.in.arready & io.in.arvalid & hit, io.in.rready & io.in.rvalid & hitState)
        //PerformanceProbe(clock, ICacheMiss, state === s_btF12_afF12 && io.in.araddr >= "ha000_0000".U(32.W) && io.in.araddr <= "hbfff_ffff".U(32.W), 0.U, io.in.arready & io.in.arvalid & ~hit & io.in.araddr >= "ha000_0000".U(32.W) && io.in.araddr <= "hbfff_ffff".U(32.W), io.in.rready & io.in.rvalid & ~hitState & io.in.araddr >= "ha000_0000".U(32.W) && io.in.araddr <= "hbfff_ffff".U(32.W))
    }
//fence_i logic
    when(io.fencei.valid & io.fencei.bits.is_fencei){
        fenceCnt := fenceCnt + 1.U
    } otherwise {
        fenceCnt := 0.U
    }
    io.fencei.ready := fenceCnt === set.U - 1.U
    when(io.fencei.valid & io.fencei.bits.is_fencei){
        icache.write(fenceCnt, 0.U.asTypeOf(new ICacheSet(tagSize, block_sz, way)))
    }

//addr decode
    //b=4 k=16 m=2 n=4 tagSize=26
    tagA := io.in.araddr(31, m+n)
    index := io.in.araddr(m+n-1, m)
    offset := io.in.araddr(m-1, 0)
    base_addr := io.in.araddr - offset
//get and decode cacheData default
    cacheData := 0.U.asTypeOf(new ICacheBlock(tagSize, block_sz))
    data_h := 0.U
    wayHit := 0.U
//hit logic
    hit := false.B
    valid := false.B
    tagC := 0.U
    cacheSet := icache(index) //first read port
    for(i <- 0 until way){
        val cacheDataTemp = dontTouch(Wire(new ICacheBlock(tagSize, block_sz)))
        val validTemp = dontTouch(Wire(Bool()))
        val tagCTemp = dontTouch(Wire(UInt(tagSize.W)))
        cacheDataTemp := cacheSet.set(i)
        tagCTemp := cacheDataTemp.tag
        validTemp := cacheDataTemp.valid
        //hit!
        when(validTemp && (tagA === tagCTemp)){
            valid := validTemp
            tagC := tagCTemp
            hit := true.B
            data_h := cacheDataTemp.data(offset >> 2)
            wayHit := i.U
        }
    }

    DefaultConnect()

    switch(state){
        is(s_WaitUpV){
            io.in.arready := true.B
            io.in.rvalid := Mux(hit, true.B, false.B)

            io.out.arvalid := false.B
            io.out.rready := false.B
            io.out.araddr := 0.U
        }
        is(s_WaitUpR){
            io.in.arready := false.B
            io.in.rvalid := true.B

            io.out.arvalid := false.B
            io.out.rready := false.B
            io.out.araddr := 0.U
        }
        is(s_WaitImemARR){
            io.in.arready := false.B
            io.in.rvalid := false.B

            io.out.arvalid := true.B
            io.out.rready := false.B
            io.out.araddr := Mux(is_sdram, base_addr, ((c.U-(count)) << 2) + base_addr)
            io.out.arburst := "b01".U
            io.out.arlen := Mux(is_sdram, (c - 1).U, 0.U)
            io.out.arsize := "b10".U
        }
        is(s_WaitImemRV){
            io.in.arready := false.B
            io.in.rvalid := Mux(count === 0.U & io.out.rvalid, true.B, false.B)

            io.out.arvalid := false.B
            io.out.rready := true.B
            io.out.araddr := 0.U
        }
    }
    io.in.rdata := Mux(hit, data_h, 
                Mux(count === 0.U, data_m, 0.U))
    //data tag valid
//miss logic
    data_m := 0.U
    val wayMiss = dontTouch(Wire(UInt(w.W)))
    wayMiss := fifoPtr
    cacheData := cacheSet.set(wayMiss)
    when(io.out.rvalid) {
        val newBlock = Wire(new ICacheBlock(tagSize, block_sz)) // 创建一个临时块来执行写操作
        newBlock.valid := true.B
        newBlock.tag := tagA
        newBlock.data := cacheSet.set(wayMiss).data // 首先复制原来的数据
        newBlock.data(c.U - (count + 1.U)) := io.out.rdata      // 更新需要修改的部分
        data_m := newBlock.data(offset >> 2).asUInt

        val newCacheSet = Wire(cacheSet.set.cloneType) // 创建一个新的 CacheSet
        newCacheSet := cacheSet.set              // 复制原来的 CacheSet
        // 更新特定的 way
        newCacheSet(wayMiss) := newBlock
        // 将更新后的 CacheSet 写回
        when(io.out.rvalid & count === 0.U){
            fifoPtr := fifoPtr + 1.U
        }
        icache(index).set := newCacheSet
    }
    //count logic
    when(state === s_WaitUpV){
        count := (c-1).U
    }.elsewhen(count =/= 0.U && (io.out.rvalid)){
        count := count - 1.U
    }

//-----------------------------------------------------------------------------------------------------------------------------------
    def log2(x: Int): Double = {
        math.log(x) / math.log(2)
    }
    def DefaultConnect(): Unit = {
        // Set default values for internal signals
        io.in.arready := false.B
        io.in.rdata   := 0.U
        io.in.rvalid  := false.B

        io.out.araddr  := 0.U
        io.out.arvalid := false.B
        io.out.arid    := 0.U
        io.out.arlen   := 0.U
        io.out.arsize  := 0.U
        io.out.arburst := 0.U
        io.out.rready  := false.B
        io.out.awaddr  := 0.U
        io.out.awvalid := false.B
        io.out.awid    := 0.U
        io.out.awlen   := 0.U
        io.out.awsize  := 0.U
        io.out.awburst := 0.U
        io.out.wdata   := 0.U
        io.out.wstrb   := 0.U
        io.out.wvalid  := false.B
        io.out.wlast   := false.B
        io.out.bready  := false.B
}

}
