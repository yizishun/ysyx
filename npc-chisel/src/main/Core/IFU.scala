package npc.core

import chisel3._
import chisel3.util._

class pcIO extends Bundle{
  val idu = Flipped(new IduPcIO)
  val exu = Flipped(new ExuPcIO)
}

class IfuOutIO extends Bundle{
  val inst = Output(UInt(32.W))
  val PcPlus4 = Output(UInt(32.W))
  val pc = Output(UInt(32.W))
}

class IfuIO(xlen: Int) extends Bundle{
  val in = Flipped(Decoupled(new WbuOutIO))
  val out = Decoupled(new IfuOutIO)
  //pc value from IDU and EXU
  val pc = new pcIO
  //Connext to the external imem
  val imem = Flipped(new npc.mem.memIO(xlen))
}


class IFU(val conf: npc.CoreConfig) extends Module{
  val io = IO(new IfuIO(conf.xlen))
  //disable AW W B channel

  io.imem.awaddr := 0.U
  io.imem.awvalid := 0.U
  io.imem.wdata := 0.U
  io.imem.wstrb := 0.U
  io.imem.wvalid := 0.U
  io.imem.bready := 0.U
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
  io.imem.arvalid := imem_arvalid
  io.imem.rready := imem_rready
  io.imem.araddr := imem_araddr

  //LSFR
  val lfsr = RegInit(3.U(4.W))
  lfsr := Cat(lfsr(2,0), lfsr(0)^lfsr(1)^lfsr(2))

  //Delay
  val delay = RegInit(lfsr)

  //state transition
  val sc_BeforeFire1 :: sc_BetweenFire12_1 :: sc_BetweenFire12_2 :: Nil = Enum(3)
  val stateC = RegInit(sc_BetweenFire12_1)
  val nextStateC = WireDefault(stateC)
  nextStateC := MuxLookup(stateC, sc_BeforeFire1)(Seq(
      sc_BeforeFire1   -> Mux(io.in.fire, sc_BetweenFire12_1, sc_BeforeFire1),
      sc_BetweenFire12_1 -> Mux(io.imem.rvalid & imem_rready, sc_BetweenFire12_2, sc_BetweenFire12_1),
      sc_BetweenFire12_2 -> Mux(io.out.fire, sc_BeforeFire1, sc_BetweenFire12_2)
  ))
  stateC := nextStateC

  SetupIFU()

  //output logic
  switch(nextStateC){
    is(sc_BeforeFire1){
      //between modules
      out_valid := false.B
      in_ready := true.B
      //AXI4-Lite
      delay := lfsr
      imem_arvalid := false.B
      imem_rready := false.B
      //disable all sequential logic
    }
    is(sc_BetweenFire12_1){
      //between modules
      in_ready := false.B
      out_valid := io.imem.rvalid & (io.imem.rresp === 0.U)
      //AXI4-Lite
      when(delay === 0.U){
        imem_arvalid := true.B
        imem_rready := true.B
      }.otherwise{
        delay := delay - 1.U
        imem_arvalid := false.B
        imem_rready := false.B
      }
      //save all output into regs
    }
    is(sc_BetweenFire12_2){
      //between modules
      in_ready := false.B
      out_valid := io.imem.rvalid & (io.imem.rresp === 0.U)
      //AXI4-Lite
      imem_arvalid := false.B
      imem_rready := false.B
      //save all output into regs
    }
  }

  
//-----------------------------------------------------------------------------------
  def SetupIFU():Unit = {
  
  //place mux
    import npc.core.idu.Control._
    val PCSrc = Wire(UInt(3.W))
    val nextpc = Wire(UInt(32.W))
    PCSrc := MuxLookup(io.pc.idu.irq, PcXXXXXXX)(Seq(
      false.B -> io.pc.exu.PCSrc,
      true.B -> Mtvec
    ))
  
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
  
    //Imem module(external)
    io.imem.clk := clock
    io.imem.rst := reset
    imem_araddr := pc.io.pc
    pc.io.wen := true.B & io.in.valid
  
    //IFU module(wrapper)
    io.out.bits.PcPlus4 := addpc.io.nextpc
    io.out.bits.pc := pc.io.pc
    io.out.bits.inst := io.imem.rdata

  }
}
