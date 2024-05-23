package npc.core

import chisel3._
import chisel3.util._

class LsuOutIO extends Bundle{
  val signals = new Bundle{
    val wbu = new idu.WBUSignals
    val irq = Output(Bool())
  }

  val rd1 = Output(UInt(32.W))
  val aluresult = Output(UInt(32.W))
  val crd1 = Output(UInt(32.W))
  val pc = Output(UInt(32.W))
  val immext = Output(UInt(32.W))
  val PcPlus4 = Output(UInt(32.W))
  val MemR = Output(UInt(32.W))
  val rw = Output(UInt(5.W))
	val crw = Output(UInt(12.W))
}

class LsuIO(xlen: Int) extends Bundle{
  val in = Flipped(Decoupled(new ExuOutIO))
  val out = Decoupled(new LsuOutIO)
  //connect to the external "state" elements(i.e.dmem)
  val dmem = Flipped(new npc.mem.memIO(xlen))
}

class LSU(val conf: npc.CoreConfig) extends Module{
  val io = IO(new LsuIO(conf.xlen))

  //Between Modules handshake reg
  val in_ready = RegInit(io.in.ready)
  val out_valid = RegInit(io.out.valid)
  io.in.ready := in_ready
  io.out.valid := out_valid

  //AXI handshake reg
  val dmem_arvalid = RegInit(io.dmem.arvalid)
  val dmem_rready = RegInit(io.dmem.rready)
  val dmem_awvalid = RegInit(io.dmem.awvalid)
  val dmem_wvalid = RegInit(io.dmem.wvalid)
  val dmem_bready = RegInit(io.dmem.bready)
  io.dmem.arvalid := dmem_arvalid
  io.dmem.rready := dmem_rready
  io.dmem.awvalid := dmem_awvalid
  io.dmem.wvalid := dmem_wvalid
  io.dmem.bready := dmem_bready

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
  val s_BeforeFire1 :: s_BetweenFire12 :: s_BetweenFire12_1 :: s_BetweenFire12_2 ::Nil = Enum(4)
  val state = RegInit(s_BeforeFire1)
  val nextState = WireDefault(state)
  nextState := MuxLookup(state, s_BeforeFire1)(Seq(
      s_BeforeFire1   -> Mux(io.in.fire, Mux(notLS, s_BetweenFire12, s_BetweenFire12_1), s_BeforeFire1),
      s_BetweenFire12 -> Mux(io.out.fire, s_BeforeFire1, s_BetweenFire12),
      s_BetweenFire12_1 -> Mux((io.dmem.rvalid & dmem_rready)|(io.dmem.bvalid & dmem_bready), s_BetweenFire12_2, s_BetweenFire12_1),
      s_BetweenFire12_2 -> Mux(io.out.fire, s_BeforeFire1, s_BetweenFire12_2)
  ))
  state := nextState

  SetupLSU()
  
  //output logic
  switch(nextState){
    is(s_BeforeFire1){
      //between modules
      in_ready := true.B
      out_valid := false.B
      //disable all sequential logic
      //AXI4-Lite
      delay := lfsr
      dmem_arvalid := false.B
      dmem_rready := false.B
      dmem_awvalid := false.B
      dmem_wvalid := false.B
      dmem_bready := false.B
    }
    is(s_BetweenFire12){
      //between modules
      in_ready := false.B
      out_valid := true.B
      //AXI4-Lite
      dmem_arvalid := false.B
      dmem_rready := false.B
      dmem_awvalid := false.B
      dmem_wvalid := false.B
      dmem_bready := false.B
    }
    is(s_BetweenFire12_1){
      //between modules
      in_ready := false.B
      out_valid := Mux(isLoad, io.dmem.rvalid & (io.dmem.rresp === 0.U), io.dmem.bvalid & (io.dmem.bresp === 0.U))
      //AXI4-Lite
      when(delay === 0.U){
          //AR
        dmem_arvalid := Mux(isLoad, true.B, false.B)
          //R
        dmem_rready := Mux(isLoad, true.B, false.B)
          //AW
        dmem_awvalid := Mux(isStore, true.B, false.B)
          //W
        dmem_wvalid := Mux(isStore, true.B, false.B)
          //B
        dmem_bready := Mux(isStore, true.B, false.B)
      }.otherwise{
        delay := delay - 1.U
        dmem_arvalid := false.B
        dmem_rready := false.B
        dmem_awvalid := false.B
        dmem_wvalid := false.B
        dmem_bready := false.B
      }
      //save all output to regs

    }
    is(s_BetweenFire12_2){
      //between modules
      in_ready := false.B
      out_valid := Mux(isLoad, io.dmem.rvalid & (io.dmem.rresp === 0.U), io.dmem.bvalid & (io.dmem.bresp === 0.U))
      //AXI4-Lite
        //AR
      dmem_arvalid := false.B
        //R
      dmem_rready := false.B
        //AW
      dmem_awvalid := false.B
        //W
      dmem_wvalid := false.B
        //B
      dmem_bready := false.B
      //save all output to regs
      
    }
  }

//------------------------------------------------------------------------------------------------------
  def SetupLSU():Unit = {
  //place wires
    val maddr = Wire(UInt(32.W))
    val place = Wire(UInt(32.W))
    val RealMemWmask = Wire(UInt(4.W))
    val rdplace = Wire(UInt(32.W))
    //some operation before connect to dmem
  	maddr := io.in.bits.aluresult & (~3.U(32.W))
  	place := io.in.bits.aluresult - maddr
  	RealMemWmask := io.in.bits.signals.lsu.MemWmask << place(1, 0)
    //Dmem module(external)
    io.dmem.clk := clock
    io.dmem.rst := reset
    io.dmem.araddr := maddr
    io.dmem.awaddr := maddr
    io.dmem.wdata := io.in.bits.rd2
    io.dmem.wstrb := RealMemWmask
    //some operation to read data
    rdplace := io.dmem.rdata >> (place << 3)
    //LSU module(wrapper)
    io.out.bits.signals := io.in.bits.signals
    io.out.bits.rd1 := io.in.bits.rd1
    io.out.bits.aluresult := io.in.bits.aluresult
    io.out.bits.crd1 := io.in.bits.crd1
    io.out.bits.pc := io.in.bits.pc
    io.out.bits.immext := io.in.bits.immext
    io.out.bits.PcPlus4 := io.in.bits.PcPlus4
    io.out.bits.rw := io.in.bits.rw
  	io.out.bits.crw := io.in.bits.crw
  
  //place mux
    import idu.Control._
    val rbyte = Cat(io.dmem.rdata(7), rdplace(7, 0)).asSInt
    val rhalfw = Cat(io.dmem.rdata(15), rdplace(15, 0)).asSInt
    val rword = io.dmem.rdata.asSInt
    val rbyteu = Cat(0.U, rdplace(7, 0)).asSInt 
    val rhalfwu = Cat(0.U, rdplace(15, 0)).asSInt
    io.out.bits.MemR := MuxLookup(io.in.bits.signals.lsu.MemRD, rbyte)(Seq(
      RBYTE -> rbyte,
      RHALFW -> rhalfw,
      RWORD -> rword,
      RBYTEU -> rbyteu,
      RHALFWU -> rhalfwu
    )).asUInt
  
  }
}