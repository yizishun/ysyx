package npc.core

import chisel3._
import chisel3.util._
import npc._
import npc.bus.AXI4
import npc.core.idu.Control._

class IfuPcIO extends Bundle{
  val speculativePC = Output(UInt(32.W))
  val PcPlus4 = Output(UInt(32.W))
}

class IfuOutIO extends Bundle{
  val inst = Output(UInt(32.W))
  val pc = Output(UInt(32.W))
  //val stat = Output(new Stat)
}

class Ifu2IcacheIO extends Bundle{
  val araddr = Output(UInt(32.W))
  val arready = Input(Bool())
  val arvalid = Output(Bool())
  val rvalid = Input(Bool())
  val rready = Output(Bool())
  val rdata = Input(UInt(32.W))
}

class IfuIO(xlen: Int) extends Bundle{
  val in = Flipped(Decoupled(new PfuOutIO))
  val out = Decoupled(new IfuOutIO)
  //coonect to the pfu
  val pf = Decoupled(new IfuPcIO)
  //Connect to the external imem
  val imem = new Ifu2IcacheIO
  //val statr = Input(new Stat)
}


class IFU(val conf: npc.CoreConfig) extends Module{
  val io = IO(new IfuIO(conf.xlen))
  val imem_arvalid = RegInit(false.B)
  val imem_rready = RegInit(false.B)
  io.imem.arvalid := imem_arvalid
  io.imem.rready := imem_rready
  //place modules
  val pcInit = if(conf.ysyxsoc){ "h3000_0000".U(32.W) }else if(conf.npc){ "h8000_0000".U(32.W)} else { "h0000_0000".U(32.W) }
  val pcReg = RegEnable(io.in.bits.nextPC, pcInit, io.in.valid)
  val PcPlus4 = Wire(UInt(32.W))
  PcPlus4 := pcReg + 4.U

  //LSFR
  val lfsr = RegInit(3.U(4.W))
  lfsr := Cat(lfsr(2,0), lfsr(0)^lfsr(1)^lfsr(2))

  //Delay
  val delay = RegInit(lfsr)

  //state transition(mearly)
  val s_WaitPfuV :: s_WaitImemARR :: s_WaitImemRV :: s_WaitIduR :: Nil = Enum(4)
  val stateF = RegInit(s_WaitImemARR)
  val nextStateF = WireDefault(stateF)
  nextStateF := MuxLookup(stateF, s_WaitPfuV)(Seq(
      s_WaitPfuV      -> Mux(io.in.valid, Mux(io.imem.arready, s_WaitImemRV,s_WaitImemARR), s_WaitPfuV),
      s_WaitImemARR   -> Mux(io.imem.arready, s_WaitImemRV, s_WaitImemARR),
      s_WaitImemRV    -> Mux(io.imem.rvalid, Mux(io.out.ready, s_WaitPfuV,s_WaitIduR), s_WaitImemRV),
      s_WaitIduR      -> Mux(io.out.ready, s_WaitPfuV, s_WaitIduR)
  ))

  stateF := nextStateF
  dontTouch(nextStateF)

  SetupIFU()
  //SetupIRQ()

  //if(conf.useDPIC){
    //import npc.EVENT._
    //PerformanceProbe(clock, IFUGetInst, (io.imem.rvalid & io.imem.rready).asUInt, 0.U, io.imem.arvalid & io.imem.arready, io.imem.rvalid & io.imem.rready)
  //}
  //handshake
  //output logic
  io.out.valid := false.B
  io.in.ready := false.B
  io.imem.arvalid := false.B
  io.imem.rready := false.B
  switch(stateF){
    is(s_WaitPfuV){
      //between modules
      io.out.valid := Mux(io.imem.arready & io.imem.rvalid, true.B, false.B)
      io.in.ready := true.B
      //delay
      if(conf.useLFSR){
      delay := lfsr
      }
      //AXI4
      io.imem.arvalid := false.B
      io.imem.araddr := 0.U
      io.imem.rready := false.B
    }
    is(s_WaitImemARR){
      //between modules
      io.in.ready := false.B
      io.out.valid := false.B
      //AXI4
      if(conf.useLFSR){
      when(delay === 0.U){
        io.imem.arvalid := true.B
        io.imem.rready := false.B
      }.otherwise{
        delay := delay - 1.U
        io.imem.arvalid := false.B
        io.imem.rready := false.B
      }
      }
      else{
      io.imem.arvalid := true.B
      io.imem.rready := Mux(io.imem.arready, true.B, false.B) //!!
      }
    }
    is(s_WaitImemRV){
      //between modules
      io.in.ready := false.B
      io.out.valid := io.imem.rvalid
      //AXI4
      io.imem.araddr := pcReg
      io.imem.arvalid := true.B
      io.imem.rready := true.B
    }
    is(s_WaitIduR){
      //between modules
      io.in.ready := false.B
      io.out.valid := io.imem.rvalid
      //AXI4
      io.imem.arvalid := false.B
      io.imem.rready := false.B
    }
  }

  //data path
//-----------------------------------------------------------------------------------
  def SetupIFU():Unit = {

  //place wires
    //Imem module(external)
    io.imem.araddr := pcReg
  
    //IFU module(wrapper)
    io.out.bits.pc := pcReg
    io.out.bits.inst := io.imem.rdata

    //PFU module
    io.pf.bits.PcPlus4 := PcPlus4
    io.pf.bits.speculativePC := PcPlus4
    io.pf.valid := io.out.valid

  }
  //stat logic
//-----------------------------------------------------------------------------------
  //def SetupIRQ():Unit = {
    //val hasIrq = (nextStateC === sc_BetweenFire12_1_2) && io.imem.rvalid && (io.imem.rresp =/= 0.U)
    //io.out.bits.stat.stat := Mux(hasIrq, true.B, false.B)
    //io.out.bits.stat.statNum := Mux(hasIrq, IRQ_IAF, 0.U)
    //when(io.statr.stat){
      //pc.io.nextpc := io.pc.idu.mtvec
      //io.out.bits.stat.stat := false.B
    //}
  //}
}
