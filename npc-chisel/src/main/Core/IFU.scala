package npc.core

import chisel3._
import chisel3.util._
import npc._
import npc.bus.AXI4
import npc.core.idu.Control._

class pcIO extends Bundle{
  val idu = Flipped(new IduPcIO)
  val exu = Flipped(new ExuPcIO)
}

class IfuOutIO extends Bundle{
  val inst = Output(UInt(32.W))
  val PcPlus4 = Output(UInt(32.W))
  val pc = Output(UInt(32.W))
  val stat = Output(new Stat)
}

class IfuIO(xlen: Int) extends Bundle{
  val in = Flipped(Decoupled(new WbuOutIO))
  val out = Decoupled(new IfuOutIO)
  //pc value from IDU and EXU
  val pc = new pcIO
  //Connext to the external imem
  val imem = Flipped(new AXI4)
  val statr = Input(new Stat)
}


class IFU(val conf: npc.CoreConfig) extends Module{
  val io = IO(new IfuIO(conf.xlen))
  //disable AW W B channel
  io.imem.arid := 0.U
  io.imem.arlen := 0.U
  io.imem.arburst := 0.U
  io.imem.awaddr := DontCare
  io.imem.awvalid := false.B
  io.imem.awid := 0.U
  io.imem.awlen := 0.U
  io.imem.awsize := 0.U
  io.imem.awburst := 0.U
  io.imem.wdata := DontCare
  io.imem.wstrb := 0.U
  io.imem.wvalid := 0.U
  io.imem.wlast := false.B
  io.imem.bready := false.B
  //place modules
  val addpc = Module(new ifu.Addpc)
  val pc = Module(new ifu.PC)

  //Beteen Modules handshake reg
  val in_ready = RegInit(io.in.ready)
  val out_valid = RegInit(io.out.valid)
  io.in.ready := in_ready
  io.out.valid := out_valid

  //AXI handshake reg
  val imem_arvalid = RegInit(io.imem.arvalid)
  val imem_rready = RegInit(io.imem.rready)
  val imem_araddr = RegInit(io.imem.araddr)
  val imem_arsize = RegInit(io.imem.arsize)
  io.imem.arvalid := imem_arvalid
  io.imem.arsize := imem_arsize
  io.imem.rready := imem_rready
  io.imem.araddr := imem_araddr

  //LSFR
  val lfsr = RegInit(3.U(4.W))
  lfsr := Cat(lfsr(2,0), lfsr(0)^lfsr(1)^lfsr(2))

  //Delay
  val delay = RegInit(lfsr)

  //state transition
  val sc_BeforeFire1 :: sc_BetweenFire12_1_1 :: sc_BetweenFire12_1_2 :: sc_BetweenFire12_2 :: Nil = Enum(4)
  val stateC = RegInit(sc_BetweenFire12_1_1)
  val nextStateC = WireDefault(stateC)
  nextStateC := MuxLookup(stateC, sc_BeforeFire1)(Seq(
      sc_BeforeFire1   -> Mux(io.in.fire, sc_BetweenFire12_1_1, sc_BeforeFire1),
      sc_BetweenFire12_1_1 -> Mux(io.imem.arvalid & io.imem.arready, sc_BetweenFire12_1_2, sc_BetweenFire12_1_1),
      sc_BetweenFire12_1_2 -> Mux(io.imem.rvalid & imem_rready, sc_BetweenFire12_2, sc_BetweenFire12_1_2),
      sc_BetweenFire12_2 -> Mux(io.out.fire, sc_BeforeFire1, sc_BetweenFire12_2)
  ))
  stateC := nextStateC
  dontTouch(nextStateC)

  SetupIFU()
  SetupIRQ()

  import npc.EVENT._
  PerformanceProbe(clock, IFUGetInst, (io.imem.rvalid & imem_rready).asUInt, 0.U, io.imem.arvalid & io.imem.arready, io.imem.rvalid & imem_rready)

  //handshake
  //output logic
  switch(nextStateC){
    is(sc_BeforeFire1){
      //between modules
      out_valid := false.B
      in_ready := true.B
      //AXI4
      delay := lfsr
      imem_arvalid := false.B
      imem_rready := false.B
      imem_arsize := 2.U
      //disable all sequential logic
    }
    is(sc_BetweenFire12_1_1){
      //between modules
      in_ready := false.B
      out_valid := io.imem.rvalid & (io.imem.rresp === 0.U)
      //AXI4
      when(delay === 0.U){
        imem_arvalid := true.B
        imem_rready := false.B
        imem_arsize := 2.U
      }.otherwise{
        delay := delay - 1.U
        imem_arvalid := false.B
        imem_rready := false.B
        imem_arsize := 2.U
      }
      //save all output into regs
    }
    is(sc_BetweenFire12_1_2){
      //between modules
      in_ready := false.B
      out_valid := io.imem.rvalid & (io.imem.rresp === 0.U)
      //AXI4
      imem_arvalid := false.B
      imem_rready := true.B
      imem_arsize := 2.U

    }
    is(sc_BetweenFire12_2){
      //between modules
      in_ready := false.B
      out_valid := io.imem.rvalid & (io.imem.rresp === 0.U)
      //AXI4
      imem_arvalid := false.B
      imem_rready := false.B
      imem_arsize := 2.U
      //save all output into regs
    }
  }

  //data path
//-----------------------------------------------------------------------------------
  def SetupIFU():Unit = {

  //place mux
    val PCSrc = Wire(UInt(3.W))
    val nextpc = Wire(UInt(32.W))
    PCSrc := io.pc.exu.PCSrc
  
    nextpc := MuxLookup(PCSrc, addpc.io.nextpc)(Seq(
      PcPlus4 -> addpc.io.nextpc,
      PcPlusImm -> io.pc.exu.PcPlusImm,
      PcPlusRs2 -> io.pc.exu.PcPlusRs2,
      Mtvec -> io.pc.idu.mtvec,
      Mepc -> io.pc.idu.mepc
    ))
  
  //place wires
    //Addpc module
    addpc.io.pc := pc.io.pc
  
    //PC module
    pc.io.nextpc := nextpc
    pc.io.wen := io.in.valid
  
    //Imem module(external)
    imem_araddr := pc.io.pc
  
    //IFU module(wrapper)
    io.out.bits.PcPlus4 := addpc.io.nextpc
    io.out.bits.pc := pc.io.pc
    //fit to 64-bit bus
    val tempmaddr = Wire(UInt(32.W))
    val dataplace = Wire(UInt(32.W))
    tempmaddr := imem_araddr & (~3.U(32.W))
    dataplace := imem_araddr - tempmaddr
    val inst = Reg(UInt(32.W))
    inst := Mux(io.in.valid, 0.U, Mux(io.imem.rvalid, io.imem.rdata >> (dataplace(2, 0) << 3), inst)) //correct save and cancel
    io.out.bits.inst := inst

  }
  //stat logic
//-----------------------------------------------------------------------------------
  def SetupIRQ():Unit = {
    val hasIrq = (nextStateC === sc_BetweenFire12_1_2) && io.imem.rvalid && (io.imem.rresp =/= 0.U)
    io.out.bits.stat.stat := Mux(hasIrq, true.B, false.B)
    io.out.bits.stat.statNum := Mux(hasIrq, IRQ_IAF, 0.U)
    when(io.statr.stat){
      pc.io.nextpc := io.pc.idu.mtvec
      io.out.bits.stat.stat := false.B
    }
  }
}
