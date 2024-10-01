package npc.core

import chisel3._
import chisel3.util._
import npc.bus._
import npc._

class LsuOutIO extends Bundle{
  val signals = new Bundle{
    val wbu = new idu.WBUSignals
  }

  val rd1 = Output(UInt(32.W))
  val aluresult = Output(UInt(32.W))
  val crd1 = Output(UInt(32.W))
  val pc = Output(UInt(32.W))
  val immext = Output(UInt(32.W))
  val MemR = Output(UInt(32.W))
  val rw = Output(UInt(5.W))
	val crw = Output(UInt(12.W))
  //val stat = Output(new Stat)
  //for performance analysis
  val perfSubType = Output(UInt(8.W))
}

class LsuIO(xlen: Int) extends Bundle{
  val in = Flipped(Decoupled(new ExuOutIO))
  val out = Decoupled(new LsuOutIO)
  //connect to the external "state" elements(i.e.dmem)
  val dmem = new AXI4Master
}

class LSU(val conf: npc.CoreConfig) extends Module{
  val io = IO(new LsuIO(conf.xlen))
  io.dmem.arid := 0.U
  io.dmem.arlen := 0.U
  io.dmem.arburst := 0.U
  io.dmem.awid := 0.U
  io.dmem.awlen := 0.U
  io.dmem.awburst := 0.U
  io.dmem.wlast := true.B

  //isLoad?isStore?
  val isLoad = Wire(Bool())
  val isStore = Wire(Bool())
  val notLS = Wire(Bool())
  isLoad := (~io.in.bits.signals.lsu.MemWriteE & io.in.bits.signals.lsu.MemValid) 
  isStore := (io.in.bits.signals.lsu.MemWriteE & io.in.bits.signals.lsu.MemValid) 
  notLS := ~isLoad & ~isStore

  //LSFR
  val lfsr = RegInit(3.U(4.W))
  lfsr := Cat(lfsr(2,0), lfsr(0)^lfsr(1)^lfsr(2))

  //Delay
  val delay = RegInit(lfsr)

  //state transition
  val s_WaitExuV :: s_WaitDmemAXR :: s_WaitDmemXV :: s_WaitWbuR :: Nil = Enum(4)
  val stateM = RegInit(s_WaitExuV)
  val nextStateM = WireDefault(stateM)
  nextStateM := MuxLookup(stateM, s_WaitExuV)(Seq(
      s_WaitExuV    -> Mux(io.in.valid, 
                       Mux(notLS, 
                       Mux(io.out.ready, s_WaitExuV, s_WaitWbuR), 
                       Mux(io.dmem.arready | (io.dmem.awready & io.dmem.wready), s_WaitDmemXV,s_WaitDmemAXR)), s_WaitExuV),
      s_WaitWbuR    -> Mux(io.out.ready, s_WaitExuV, s_WaitWbuR),
      s_WaitDmemAXR -> Mux((io.dmem.arready) | (io.dmem.awready & io.dmem.wready),s_WaitDmemXV, s_WaitDmemAXR),
      s_WaitDmemXV  -> Mux((io.dmem.rvalid)|(io.dmem.bvalid), Mux(io.out.ready, s_WaitExuV, s_WaitWbuR), s_WaitDmemXV),
  ))
  stateM := nextStateM

  SetupLSU()
  //SetupIRQ()

  //if(conf.useDPIC){
    //import npc.EVENT._
    //PerformanceProbe(clock, LSUGetData, (io.dmem.rvalid & io.dmem.rready).asUInt, 0.U, io.dmem.arvalid & io.dmem.arready, io.dmem.rvalid & io.dmem.rready)
  //}
  io.in.ready := 0.U
  io.out.valid := 0.U

  io.dmem.arvalid := 0.U
  io.dmem.arsize := 0.U
  io.dmem.rready := 0.U
  io.dmem.awvalid := 0.U
  io.dmem.awsize := 0.U
  io.dmem.wvalid := 0.U
  io.dmem.bready := 0.U
  import npc.core.idu.Control._
  val arsize = MuxLookup(io.in.bits.signals.lsu.MemRD, 2.U)(Seq(
        RBYTE   -> 0.U,
        RHALFW  -> 1.U,
        RWORD   -> 2.U,
        RBYTEU  -> 0.U,
        RHALFWU -> 1.U
      ))
  val awsize = MuxLookup(io.in.bits.signals.lsu.MemWmask, 2.U)(Seq(
        WBYTE  -> 0.U,
        WHALFW -> 1.U,
        WWORD  -> 2.U
      ))

  //output logic
  switch(stateM){
    is(s_WaitExuV){
      //between modules
      io.in.ready := true.B
      io.out.valid := Mux(io.in.valid & notLS, true.B, false.B)
      //disable all sequential logic
      //AXI4-Lite
      delay := lfsr
      io.dmem.arvalid := Mux(io.in.valid & isLoad, true.B, false.B)
      io.dmem.arsize := Mux(io.in.valid & isLoad, arsize, 2.U)
      io.dmem.rready := Mux(io.in.valid & isLoad, false.B, true.B)
      io.dmem.awvalid := Mux(io.in.valid & isStore, true.B, false.B)
      io.dmem.awsize := Mux(io.in.valid & isStore, awsize, 2.U)
      io.dmem.wvalid := Mux(io.in.valid & isStore, true.B, false.B)
      io.dmem.bready := Mux(io.in.valid & isStore, false.B, true.B)
    }
    is(s_WaitWbuR){
      //between modules
      io.in.ready := false.B
      io.out.valid := true.B
      //AXI4-Lite
      io.dmem.arvalid := false.B
      io.dmem.arsize := 2.U
      io.dmem.rready := false.B
      io.dmem.awvalid := false.B
      io.dmem.awsize := 2.U
      io.dmem.wvalid := false.B
      io.dmem.bready := false.B
    }
    is(s_WaitDmemAXR){
      //between modules
      io.in.ready := false.B
      io.out.valid := false.B
      //AXI4-Lite
        //AR
      io.dmem.arvalid := Mux(isLoad, true.B, false.B)
      io.dmem.arsize := arsize
        //R
      io.dmem.rready := Mux(isLoad, false.B, false.B)
        //AW
      io.dmem.awvalid := Mux(isStore, true.B, false.B)
      io.dmem.awsize := awsize
        //W
      io.dmem.wvalid := Mux(isStore, true.B, false.B)
        //B
      io.dmem.bready := Mux(isStore, false.B, false.B)

    }
    is(s_WaitDmemXV){
      //between modules
      io.in.ready := false.B
      io.out.valid := Mux(io.dmem.rvalid | io.dmem.bvalid, true.B, false.B)
      //AXI4-Lite
        //AR
      io.dmem.arvalid := Mux(isLoad, false.B, false.B)
      io.dmem.arsize := arsize
        //R
      io.dmem.rready := Mux(isLoad, true.B, false.B)
        //AW
      io.dmem.awvalid := Mux(isStore, false.B, false.B)
      io.dmem.awsize := awsize
        //W
      io.dmem.wvalid := Mux(isStore, false.B, false.B)
        //B
      io.dmem.bready := Mux(isStore, true.B, false.B)
      //save all output to regs
    }
  }

//------------------------------------------------------------------------------------------------------
  def SetupLSU():Unit = {
  //place wires
    val tempmaddr = Wire(UInt(32.W))
    val dataplace = Wire(UInt(32.W))
    val RealMemWmask = Wire(UInt(4.W))
    val rdplace = Wire(UInt(32.W))
    //some operation before connect to dmem
    tempmaddr := io.in.bits.aluresult & (~3.U(32.W))
    dataplace := io.in.bits.aluresult - tempmaddr
  	RealMemWmask := io.in.bits.signals.lsu.MemWmask << dataplace(2, 0)
    //Dmem module(external)
    io.dmem.araddr := io.in.bits.aluresult
    io.dmem.awaddr := io.in.bits.aluresult
    io.dmem.wdata := io.in.bits.rd2 << (dataplace(2, 0) << 3)
    io.dmem.wstrb := RealMemWmask
    //some operation to read data
    rdplace := io.dmem.rdata >> (dataplace(2, 0) << 3)
    //LSU module(wrapper)
    io.out.bits.signals := io.in.bits.signals
    io.out.bits.rd1 := io.in.bits.rd1
    io.out.bits.aluresult := io.in.bits.aluresult
    io.out.bits.crd1 := io.in.bits.crd1
    io.out.bits.pc := io.in.bits.pc
    io.out.bits.immext := io.in.bits.immext
    io.out.bits.rw := io.in.bits.rw
  	io.out.bits.crw := io.in.bits.crw
  
  //place mux
    import idu.Control._
    val rbyte = Cat(rdplace(7), rdplace(7, 0)).asSInt
    val rhalfw = Cat(rdplace(15), rdplace(15, 0)).asSInt
    val rword = rdplace.asSInt
    val rbyteu = Cat(0.U, rdplace(7, 0)).asSInt 
    val rhalfwu = Cat(0.U, rdplace(15, 0)).asSInt
    io.out.bits.MemR := MuxLookup(io.in.bits.signals.lsu.MemRD, rbyte)(Seq(
      RBYTE -> rbyte,
      RHALFW -> rhalfw,
      RWORD -> rword,
      RBYTEU -> rbyteu,
      RHALFWU -> rhalfwu
    )).asUInt

    io.out.bits.perfSubType := io.in.bits.perfSubType
  
  }
//  def SetupIRQ() :Unit = {
//    import npc.core.idu.Control._
//    val hasLAF = RegEnable(nextState === s_BetweenFire12_1_2 || nextState === s_BetweenFire12, io.dmem.rvalid && (io.dmem.rresp =/= 0.U)) 
//    val hasSAF = RegEnable(nextState === s_BetweenFire12_1_2 || nextState === s_BetweenFire12, io.dmem.bvalid & (io.dmem.bresp =/= 0.U))
//    io.out.bits.stat.stat := Mux(hasSAF || hasLAF, true.B, io.in.bits.stat.stat)
//    io.out.bits.stat.statNum := Mux(hasSAF, IRQ_SAF, Mux(hasLAF, IRQ_LAF, io.in.bits.stat.statNum))
//  }
}