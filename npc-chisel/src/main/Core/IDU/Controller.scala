package npc.core.idu

import chisel3.util.BitPat
import chisel3._
import chisel3.util._
import npc._

object Instructions {
  // Loads
  def LB   =  BitPat("b?????????????????000?????0000011")
  def LH   =  BitPat("b?????????????????001?????0000011")
  def LW   =  BitPat("b?????????????????010?????0000011")
  def LBU  =  BitPat("b?????????????????100?????0000011")
  def LHU  =  BitPat("b?????????????????101?????0000011")
  // Stores
  def SB   =  BitPat("b?????????????????000?????0100011")
  def SH   =  BitPat("b?????????????????001?????0100011")
  def SW   =  BitPat("b?????????????????010?????0100011")
  // Shifts
  def SLL  =  BitPat("b0000000??????????001?????0110011")
  def SLLI =  BitPat("b0000000??????????001?????0010011")
  def SRL  =  BitPat("b0000000??????????101?????0110011")
  def SRLI =  BitPat("b0000000??????????101?????0010011")
  def SRA  =  BitPat("b0100000??????????101?????0110011")
  def SRAI =  BitPat("b0100000??????????101?????0010011")
  // Arithmetic
  def ADD  =  BitPat("b0000000??????????000?????0110011")
  def ADDI =  BitPat("b?????????????????000?????0010011")
  def SUB  =  BitPat("b0100000??????????000?????0110011")
  def LUI  =  BitPat("b?????????????????????????0110111")
  def AUIPC=  BitPat("b?????????????????????????0010111")
  // Logical
  def XOR  =  BitPat("b0000000??????????100?????0110011")
  def XORI =  BitPat("b?????????????????100?????0010011")
  def OR   =  BitPat("b0000000??????????110?????0110011")
  def ORI  =  BitPat("b?????????????????110?????0010011")
  def AND  =  BitPat("b0000000??????????111?????0110011")
  def ANDI =  BitPat("b?????????????????111?????0010011")
  // Compare
  def SLT  =  BitPat("b0000000??????????010?????0110011")
  def SLTI =  BitPat("b?????????????????010?????0010011")
  def SLTU =  BitPat("b0000000??????????011?????0110011")
  def SLTIU=  BitPat("b?????????????????011?????0010011")
  // Branches
  def BEQ  =  BitPat("b?????????????????000?????1100011")
  def BNE  =  BitPat("b?????????????????001?????1100011")
  def BLT  =  BitPat("b?????????????????100?????1100011")
  def BGE  =  BitPat("b?????????????????101?????1100011")
  def BLTU =  BitPat("b?????????????????110?????1100011")
  def BGEU =  BitPat("b?????????????????111?????1100011")
  // Jump & Link
  def JAL  =  BitPat("b?????????????????????????1101111")
  def JALR =  BitPat("b?????????????????000?????1100111")
  // CSR Access
  def CSRRW = BitPat("b?????????????????001?????1110011")
  def CSRRS = BitPat("b?????????????????010?????1110011")
  // Change Level
  def ECALL = BitPat("b00000000000000000000000001110011")
  def EBREAK= BitPat("b00000000000100000000000001110011")
  def MRET  = BitPat("b00110000001000000000000001110011")
  //fence.i
  def FENCEI = BitPat("b000000000000_00000_001_00000_0001111")

  def NOP   = BitPat.bitPatToUInt(BitPat("b00000000000000000000000000010011"))
}

object Control {
  // MemRD
  val RXXXX    = 0.U(3.W)
  val RBYTE    = 0.U(3.W)
  val RHALFW   = 1.U(3.W)
  val RWORD    = 2.U(3.W)
  val RBYTEU   = 3.U(3.W)
  val RHALFWU  = 4.U(3.W)
  
  // MemWriteE
  val MWRITE   = true.B
  val MNWRITE  = false.B
  
  // MemWmask
  val WXXXX    = "b00000001".U(8.W)
  val WBYTE    = "b00000001".U(8.W)
  val WHALFW   = "b00000011".U(8.W)
  val WWORD    = "b00001111".U(8.W)
  
  // MemValid
  val VALID    = true.B
  val INVALID  = false.B
  
  // CSRWriteE
  val CSRWRITE = true.B
  val CSRNWRITE= false.B
  
  // RegwriteE
  val RWRITE   = true.B
  val RNWRITE  = false.B
  
  // RegwriteD
  val RWDXXX      = 0.U(3.W)
  val RAluresult   = 0.U(3.W)
  val RImm         = 1.U(3.W)
  val RPcPlus4     = 2.U(3.W)
  val RMemRD       = 3.U(3.W)
  val RCSR         = 4.U(3.W)
  
  // CSRWriteD
  val CRWDXX  = 0.U(2.W)
  val CRD1     = 0.U(2.W)
  val CRD1OR  = 1.U(2.W)
  val CPC      = 2.U(2.W)
  
  // immtype
  val ImmX    = 0.U(3.W)
  val ImmI    = 0.U(3.W)
  val ImmU    = 1.U(3.W)
  val ImmJ    = 2.U(3.W)
  val ImmS    = 3.U(3.W)
  val ImmB    = 4.U(3.W)
  
  // AluSrcA
  val ALUaXX  = 0.U(1.W)
  val ALUaPC  = 1.U(1.W)
  val ALUaRD1 = 0.U(1.W)
  
  // AluSrcB
  val ALUbXX  = 0.U(1.W)
  val ALUbimm = 1.U(1.W)
  val ALUbRD2 = 0.U(1.W)
  
  // Jump
  val NJump   = "b1111".U(4.W)
  val Jumpbeq = "b0000".U(4.W)
  val Jumpbne = "b0001".U(4.W)
  val Jumpblt = "b0100".U(4.W)
  val Jumpbge = "b0101".U(4.W)
  val Jumpbltu= "b0110".U(4.W)
  val Jumpbgeu= "b0111".U(4.W)
  val Jumpjal = "b1000".U(4.W)
  val Jumpjalr= "b1001".U(4.W)
  val Jumpmret= "b1010".U(4.W)
  
  //PCSrc
  val PcXXXXXXX = 0.U(3.W)
  val PcPlus4 = 0.U(3.W)
  val PcPlusImm = 1.U(3.W)
  val PcPlusRs2 = 2.U(3.W)
  val Mtvec = 3.U(3.W)
  val Mepc = 4.U(3.W)
  
  // Interrupt Request (IRQ)
  val NIRQ   = false.B
  val IRQ    = true.B
  
  // IRQ Number
  val IRQ_XXXXXX = 0.U(8.W)
  val IRQ_MECALL = "b00001011".U(8.W)
  val IRQ_IAF    = "b00000001".U(8.W)
  val IRQ_LAF    = "b00000101".U(8.W)
  val IRQ_SAF    = "b00000111".U(8.W)

  //fence.i
  val IS_FENCEI = true.B
  val NO_FENCEI = false.B
  
  import npc.core.exu.AluOp._
  import Instructions._

  // format: off
  val default =
  //                                                    
  //              Jump     MemWriteE RegwriteE CSRWriteE AluSrcA  AluSrcB immtype AluCtrl  MemValid  MemRD   MemWmask RegwriteD CSRWriteD IRQ   IRQN       FENCEI
  //               |           |          |         |        |      |        |       |        |        |        |        |           |     |      |           |
             List(NJump   , MNWRITE, RNWRITE,  CSRNWRITE, ALUaXX , ALUbXX , ImmX,  ALU_XXX , INVALID, RXXXX  , WXXXX , RWDXXX    , CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI)
  val map = Array(
    LUI   -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaXX , ALUbXX , ImmU,  ALU_XXX , INVALID, RXXXX  , WXXXX , RImm      , CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    AUIPC -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaPC , ALUbimm, ImmU,  ALU_ADD , INVALID, RXXXX  , WXXXX , RAluresult, CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    JAL   -> List(Jumpjal , MNWRITE, RWRITE ,  CSRNWRITE, ALUaPC , ALUbimm, ImmJ,  ALU_ADD , INVALID, RXXXX  , WXXXX , RPcPlus4  , CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    JALR  -> List(Jumpjalr, MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbimm, ImmI,  ALU_ADD , INVALID, RXXXX  , WXXXX , RPcPlus4  , CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    BEQ   -> List(Jumpbeq , MNWRITE, RNWRITE,  CSRNWRITE, ALUaRD1, ALUbRD2, ImmB,  ALU_SUB , INVALID, RXXXX  , WXXXX , RWDXXX    , CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    BNE   -> List(Jumpbne , MNWRITE, RNWRITE,  CSRNWRITE, ALUaRD1, ALUbRD2, ImmB,  ALU_SUB , INVALID, RXXXX  , WXXXX , RWDXXX    , CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    BLT   -> List(Jumpblt , MNWRITE, RNWRITE,  CSRNWRITE, ALUaRD1, ALUbRD2, ImmB,  ALU_CMP , INVALID, RXXXX  , WXXXX , RWDXXX    , CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    BGE   -> List(Jumpbge , MNWRITE, RNWRITE,  CSRNWRITE, ALUaRD1, ALUbRD2, ImmB,  ALU_CMP , INVALID, RXXXX  , WXXXX , RWDXXX    , CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    BLTU  -> List(Jumpbltu, MNWRITE, RNWRITE,  CSRNWRITE, ALUaRD1, ALUbRD2, ImmB,  ALU_CMPU, INVALID, RXXXX  , WXXXX , RWDXXX    , CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    BGEU  -> List(Jumpbgeu, MNWRITE, RNWRITE,  CSRNWRITE, ALUaRD1, ALUbRD2, ImmB,  ALU_CMPU, INVALID, RXXXX  , WXXXX , RWDXXX    , CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    ADDI  -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbimm, ImmI,  ALU_ADD , INVALID, RXXXX  , WXXXX , RAluresult, CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    SLTI  -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbimm, ImmI,  ALU_CMP , INVALID, RXXXX  , WXXXX , RAluresult, CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    SLTIU -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbimm, ImmI,  ALU_CMPU, INVALID, RXXXX  , WXXXX , RAluresult, CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    XORI  -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbimm, ImmI,  ALU_XOR , INVALID, RXXXX  , WXXXX , RAluresult, CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    ORI   -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbimm, ImmI,  ALU_OR  , INVALID, RXXXX  , WXXXX , RAluresult, CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    ANDI  -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbimm, ImmI,  ALU_AND , INVALID, RXXXX  , WXXXX , RAluresult, CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    SLLI  -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbimm, ImmI,  ALU_SLL , INVALID, RXXXX  , WXXXX , RAluresult, CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    SRLI  -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbimm, ImmI,  ALU_SRL , INVALID, RXXXX  , WXXXX , RAluresult, CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    SRAI  -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbimm, ImmI,  ALU_SRA , INVALID, RXXXX  , WXXXX , RAluresult, CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    ADD   -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbRD2, ImmX,  ALU_ADD , INVALID, RXXXX  , WXXXX , RAluresult, CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    SUB   -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbRD2, ImmX,  ALU_SUB , INVALID, RXXXX  , WXXXX , RAluresult, CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    SLL   -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbRD2, ImmX,  ALU_SLL , INVALID, RXXXX  , WXXXX , RAluresult, CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    SLT   -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbRD2, ImmX,  ALU_CMP , INVALID, RXXXX  , WXXXX , RAluresult, CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    SLTU  -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbRD2, ImmX,  ALU_CMPU, INVALID, RXXXX  , WXXXX , RAluresult, CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    XOR   -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbRD2, ImmX,  ALU_XOR , INVALID, RXXXX  , WXXXX , RAluresult, CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    SRL   -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbRD2, ImmX,  ALU_SRL , INVALID, RXXXX  , WXXXX , RAluresult, CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    SRA   -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbRD2, ImmX,  ALU_SRA , INVALID, RXXXX  , WXXXX , RAluresult, CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    OR    -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbRD2, ImmX,  ALU_OR  , INVALID, RXXXX  , WXXXX , RAluresult, CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    AND   -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbRD2, ImmX,  ALU_AND , INVALID, RXXXX  , WXXXX , RAluresult, CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    SW    -> List(NJump   , MWRITE , RNWRITE,  CSRNWRITE, ALUaRD1, ALUbimm, ImmS,  ALU_ADD , VALID  , RXXXX  , WWORD , RWDXXX    , CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    SH    -> List(NJump   , MWRITE , RNWRITE,  CSRNWRITE, ALUaRD1, ALUbimm, ImmS,  ALU_ADD , VALID  , RXXXX  , WHALFW, RWDXXX    , CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    SB    -> List(NJump   , MWRITE , RNWRITE,  CSRNWRITE, ALUaRD1, ALUbimm, ImmS,  ALU_ADD , VALID  , RXXXX  , WBYTE , RWDXXX    , CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    LW    -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbimm, ImmI,  ALU_ADD , VALID  , RWORD  , WWORD , RMemRD    , CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    LH    -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbimm, ImmI,  ALU_ADD , VALID  , RHALFW , WWORD , RMemRD    , CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    LB    -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbimm, ImmI,  ALU_ADD , VALID  , RBYTE  , WWORD , RMemRD    , CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    LBU   -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbimm, ImmI,  ALU_ADD , VALID  , RBYTEU , WWORD , RMemRD    , CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    LHU   -> List(NJump   , MNWRITE, RWRITE ,  CSRNWRITE, ALUaRD1, ALUbimm, ImmI,  ALU_ADD , VALID  , RHALFWU, WWORD , RMemRD    , CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    ECALL -> List(NJump   , MNWRITE, RNWRITE,  CSRWRITE , ALUaXX , ALUbXX , ImmX,  ALU_XXX , INVALID, RXXXX  , WXXXX , RWDXXX    , CPC   , IRQ , IRQ_MECALL, NO_FENCEI),
    EBREAK-> List(NJump   , MNWRITE, RNWRITE,  CSRNWRITE, ALUaXX , ALUbXX , ImmX,  ALU_XXX , INVALID, RXXXX  , WXXXX , RWDXXX    , CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    MRET  -> List(Jumpmret, MNWRITE, RNWRITE,  CSRNWRITE, ALUaXX , ALUbXX , ImmX,  ALU_XXX , INVALID, RXXXX  , WXXXX , RWDXXX    , CRWDXX, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    CSRRW -> List(NJump   , MNWRITE, RWRITE ,  CSRWRITE , ALUaXX , ALUbXX , ImmX,  ALU_XXX , INVALID, RXXXX  , WXXXX , RCSR      , CRD1   , NIRQ, IRQ_XXXXXX,NO_FENCEI),
    CSRRS -> List(NJump   , MNWRITE, RWRITE ,  CSRWRITE , ALUaXX , ALUbXX , ImmX,  ALU_XXX , INVALID, RXXXX  , WXXXX , RCSR      , CRD1OR, NIRQ, IRQ_XXXXXX, NO_FENCEI),
    FENCEI-> List(NJump   , MNWRITE, RNWRITE,  CSRNWRITE, ALUaXX , ALUbXX , ImmX,  ALU_XXX , INVALID, RXXXX  , WXXXX , RWDXXX    , CRWDXX, NIRQ, IRQ_XXXXXX, IS_FENCEI)
)
  // format: on
}

class Ebreak(conf : npc.CoreConfig) extends BlackBox with HasBlackBoxPath{
  val io = IO(new Bundle{
    val inst = Input(UInt(32.W))
  })
  if(conf.useDPIC)
    addPath("/Users/yizishun/ysyx-workbench/npc-chisel/src/main/core/idu/Ebreak.sv")
}

class IFUSignals extends Bundle{
  val is_fencei = Output(Bool())
}

class IDUSignals extends Bundle{
	val immtype = Output(UInt(3.W))
}

class EXUSignals extends Bundle{
	val alucontrol = Output(UInt(4.W))
	val Jump = Output(UInt(4.W))
	val AluSrcA = Output(UInt(1.W))
	val AluSrcB = Output(UInt(1.W))
}

class LSUSignals extends Bundle{
	val MemWriteE = Output(Bool())
	val MemWmask = Output(UInt(8.W))
	val MemValid = Output(Bool())
	val MemRD = Output(UInt(3.W))
}

class WBUSignals extends Bundle{
	val CSRWriteE = Output(Bool())
	val RegwriteE = Output(Bool())
	val CSRWriteD = Output(UInt(2.W))
	val RegwriteD = Output(UInt(3.W))
}

class Signals extends Bundle{
  val ifu = Irrevocable(new IFUSignals)
  val idu = new IDUSignals
  val exu = new EXUSignals
  val lsu = new LSUSignals
  val wbu = new WBUSignals
}

class ControlIO extends Bundle{
	val irq = Output(Bool())
	val irq_no = Output(UInt(8.W))
  val inst = Input(UInt(32.W))
  val signals = new Signals
}

class Controller(conf : npc.CoreConfig) extends Module{
  val io = IO(new ControlIO)
  val controlsignals = ListLookup(io.inst, Control.default, Control.map)

  io.signals.idu.immtype := controlsignals(6)

  io.signals.exu.alucontrol := controlsignals(7)
  io.signals.exu.Jump := controlsignals(0)
  io.signals.exu.AluSrcA := controlsignals(4)
  io.signals.exu.AluSrcB := controlsignals(5)

  io.signals.lsu.MemRD := controlsignals(9)
  io.signals.lsu.MemValid := controlsignals(8)
  io.signals.lsu.MemWmask := controlsignals(10)
  io.signals.lsu.MemWriteE := controlsignals(1)

  io.signals.wbu.RegwriteE := controlsignals(2)
  io.signals.wbu.CSRWriteE := controlsignals(3)
  io.signals.wbu.RegwriteD := controlsignals(11)
  io.signals.wbu.CSRWriteD := controlsignals(12)

  io.irq := controlsignals(13)
  io.irq_no := controlsignals(14)

  io.signals.ifu.bits.is_fencei := controlsignals(15)
  val fire = RegInit(false.B)
  fire := io.signals.ifu.fire
  io.signals.ifu.valid := Mux(fire, false.B, true.B)

  val ebreak = if(conf.useDPIC) Some(Module(new Ebreak(conf))) else None
  if(conf.useDPIC) ebreak.get.io.inst := io.inst
}