package npc.core

import chisel3._
import chisel3.util._
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
}

class IfuIO(xlen: Int) extends Bundle{
  val in = Flipped(Decoupled(new WbuOutIO))
  val out = Decoupled(new IfuOutIO)
  //pc value from IDU and EXU
  val pc = new pcIO
  //Connext to the external imem
  val imem = Flipped(new AXI4)
  val irq = new IrqIO
}


class IFU(val conf: npc.CoreConfig) extends Module{
  val io = IO(new IfuIO(conf.xlen))
  val irq = Wire(Bool())
  val irqR = RegInit(false.B)
  val irqNoR = RegInit(0.U)
  io.irq.irqOut := irqR
  io.irq.irqOutNo := irqNoR
  irq := io.irq.irqIn.reduce(_ | _) | io.irq.irqOut
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
  when(irq){
    nextStateC := sc_BetweenFire12_1_1
  }
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
      imem_arsize := 2.U
      //disable all sequential logic
    }
    is(sc_BetweenFire12_1_1){
      //between modules
      in_ready := false.B
      out_valid := io.imem.rvalid & (io.imem.rresp === 0.U)
      //AXI4-Lite
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
      when(io.imem.rvalid & (io.imem.rresp =/= 0.U)){
        irqR := true.B
        irqNoR := IRQ_IAF
      }
      //AXI4-Lite
      imem_arvalid := false.B
      imem_rready := true.B
      imem_arsize := 2.U

    }
    is(sc_BetweenFire12_2){
      //between modules
      in_ready := false.B
      out_valid := io.imem.rvalid & (io.imem.rresp === 0.U)
      //AXI4-Lite
      imem_arvalid := false.B
      imem_rready := false.B
      imem_arsize := 2.U
      //save all output into regs
    }
  }

  
//-----------------------------------------------------------------------------------
  def SetupIFU():Unit = {

  //place mux
    val PCSrc = Wire(UInt(3.W))
    val nextpc = Wire(UInt(32.W))
    PCSrc := MuxLookup(irq, PcXXXXXXX)(Seq(
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
    imem_araddr := pc.io.pc
    pc.io.wen := Mux(irq, true.B, io.in.valid)
  
    //IFU module(wrapper)
    io.irq.irqOut := false.B
    io.irq.irqOutNo := DontCare
    io.out.bits.PcPlus4 := addpc.io.nextpc
    io.out.bits.pc := pc.io.pc
    //fit to 64-bit bus
    val tempmaddr = Wire(UInt(32.W))
    val dataplace = Wire(UInt(32.W))
    tempmaddr := imem_araddr & (~7.U(32.W))
    dataplace := imem_araddr - tempmaddr
    io.out.bits.inst := io.imem.rdata >> (dataplace(2, 0) << 3)

  }
}
