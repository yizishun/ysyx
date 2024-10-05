package npc.core

import chisel3._
import chisel3.util._
import npc._
import npc.bus._
import npc.core.idu.Control._

class IfuPcIO extends Bundle{
  val speculativePC = Output(UInt(32.W))
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
  val isFlush = Input(Bool())
  val correctedPC = Input(UInt(32.W))
  //val statr = Input(new Stat)
}


class IFU(val conf: npc.CoreConfig) extends Module{
  val io = IO(new IfuIO(conf.xlen))
  //place modules
  val pcInit = if(conf.ysyxsoc){ "h3000_0000".U(32.W) }else if(conf.npc){ "h8000_0000".U(32.W)} else { "h0000_0000".U(32.W) }
  val pcReg = RegEnable(io.in.bits.nextPC, pcInit, io.in.valid)
  val PcPlus4 = Wire(UInt(32.W))
  PcPlus4 := Mux(io.in.valid, io.in.bits.nextPC + 4.U, pcReg + 4.U)

  //LSFR
  val lfsr = RegInit(3.U(4.W))
  lfsr := Cat(lfsr(2,0), lfsr(0)^lfsr(1)^lfsr(2))

  //Delay
  val delay = RegInit(lfsr)

  //state transition(mearly)
  val s_WaitPfuV :: s_WaitImemARR :: s_WaitImemRV :: s_WaitIduR :: s_WaitFlushAXIARR :: s_WaitFlushAXIRV  ::Nil = Enum(6)
  val stateF = RegInit(s_WaitImemARR)
  val nextStateF = WireDefault(stateF)
  val checkedS_WaitImemARR = Mux(io.isFlush, s_WaitFlushAXIARR, s_WaitImemARR)
  val checkedS_WaitImemRV = Mux(io.isFlush, s_WaitFlushAXIRV, s_WaitImemRV)
  nextStateF := MuxLookup(stateF, s_WaitPfuV)(Seq(
      s_WaitPfuV      -> Mux(io.in.valid, Mux(io.imem.arready, Mux(io.imem.rvalid, Mux(io.out.ready, s_WaitPfuV, s_WaitIduR), checkedS_WaitImemRV),checkedS_WaitImemARR), s_WaitPfuV),
      s_WaitImemARR   -> Mux(io.imem.arready, checkedS_WaitImemRV, checkedS_WaitImemARR),
      s_WaitImemRV    -> Mux(io.imem.rvalid, Mux(io.out.ready, s_WaitPfuV,s_WaitIduR), checkedS_WaitImemRV),
      s_WaitIduR      -> Mux(io.out.ready, s_WaitPfuV, s_WaitIduR),
      s_WaitFlushAXIARR -> Mux(io.imem.arready, s_WaitFlushAXIRV, s_WaitFlushAXIARR),
      s_WaitFlushAXIRV  -> Mux(io.imem.rvalid, s_WaitPfuV, s_WaitFlushAXIRV)
  ))
//  when(io.isFlush){
//    when(stateF === s_WaitImemRV){
//      nextStateF := s_WaitFlushAXIRV
//    }.elsewhen(stateF === s_WaitImemARR){
//      nextStateF := s_WaitFlushAXIARR
//    }.elsewhen(stateF === s_WaitIduR){
//      nextStateF := s_WaitPfuV
//    }.elsewhen(io.imem.arvalid & io.imem.arready){
//      nextStateF := s_WaitFlushAXIRV
//    }.elsewhen(io.imem.arvalid & ~io.imem.arready){
//      nextStateF := s_WaitFlushAXIARR
//    }
//  }

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
      io.out.valid := Mux(io.in.valid & io.imem.arready & io.imem.rvalid, true.B, false.B)
      io.in.ready := Mux(io.out.ready & io.imem.arready & io.imem.rvalid, true.B, false.B)
      when(io.isFlush){io.out.valid := false.B}
      //delay
      if(conf.useLFSR){
      delay := lfsr
      }
      //AXI4
      io.imem.arvalid := Mux(io.in.valid & io.imem.arready, true.B, false.B)
      io.imem.araddr := Mux(io.in.valid & io.imem.arready, io.in.bits.nextPC, 0.U)
      io.imem.rready := Mux(io.in.valid & io.imem.rvalid & io.imem.arready & io.out.ready, true.B, false.B)
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
      io.in.ready := Mux(io.imem.rvalid & io.out.ready, true.B, false.B)
      io.out.valid := io.imem.rvalid
      //AXI4
      io.imem.araddr := pcReg
      io.imem.arvalid := false.B
      io.imem.rready := true.B
    }
    is(s_WaitIduR){
      //between modules
      io.in.ready := Mux(io.out.ready, true.B, false.B)
      io.out.valid := true.B
      //AXI4
      io.imem.arvalid := true.B
      io.imem.rready := Mux(io.out.ready, true.B, false.B)
    }
    is(s_WaitFlushAXIARR){
      io.in.ready := false.B
      io.out.valid := false.B
      //AXI4
      io.imem.arvalid := true.B
      io.imem.rready := Mux(io.imem.arready, true.B, false.B) //!!
    }
    is(s_WaitFlushAXIRV){
      io.in.ready := false.B
      io.out.valid := false.B
      //AXI4
      io.imem.araddr := pcReg
      io.imem.arvalid := false.B
      io.imem.rready := true.B
    }
  }

  //data path
//-----------------------------------------------------------------------------------
  def SetupIFU():Unit = {

  //place wires
    //Imem module(external)
    io.imem.araddr := pcReg
  
    //IFU module(wrapper)
    io.out.bits.pc := Mux(io.in.valid, io.in.bits.nextPC, pcReg)
    io.out.bits.inst := io.imem.rdata

    //PFU module
    io.pf.bits.speculativePC := Mux(io.isFlush, io.correctedPC, PcPlus4)
    io.pf.valid := Mux(io.isFlush, true.B, io.in.ready)

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
