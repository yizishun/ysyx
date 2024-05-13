package npc.core.exu

import chisel3._
import chisel3.util._

object AluOp{
  val ALU_ADD = 0.U(4.W)
  val ALU_SUB = 1.U(4.W)
  val ALU_AND = 2.U(4.W)
  val ALU_CMP = 3.U(4.W)
  val ALU_XOR = 4.U(4.W)
  val ALU_CMPU= 5.U(4.W)
  val ALU_OR  = 6.U(4.W)
  val ALU_SRA = 7.U(4.W)
  val ALU_SRL = 8.U(4.W)
  val ALU_SLL = 9.U(4.W)
  val ALU_XXX =10.U(4.W)
}

class AluIO(width: Int) extends Bundle{
  val A = Input(UInt(width.W))
  val B = Input(UInt(width.W))
  val AluControl = Input(UInt(4.W))
  val result = Output(UInt(width.W))
  val Of = Output(Bool())
  val Zf = Output(Bool())
  val Nf = Output(Bool())
  val Cf = Output(Bool())
}


class Alu(val width: Int) extends Module{
  val io = IO(new AluIO(width))

  val shamt = io.B(4, 0).asUInt
  val reAddSub = io.A +& Mux(io.AluControl(0), -io.B, io.B)
  val reAnd = io.A & io.B
  val reOr = io.A | io.B
  val reXor = io.A ^ io.B
  val reCmp = io.A.asSInt < io.B.asSInt
  val reCmpu = io.A < io.B
  val reSra = (io.A.asSInt >> shamt).asUInt
  val reSrl = io.A >> shamt
  val reSll = io.A << shamt

  import AluOp._
  io.Cf := reAddSub(width)
  io.Of := reAddSub(width)
  io.result := MuxLookup(io.AluControl, io.B)(
    Seq(
      ALU_ADD -> reAddSub,
      ALU_SUB -> reAddSub,
      ALU_AND -> reAnd,
      ALU_OR  -> reOr,
      ALU_XOR -> reXor,
      ALU_CMP -> reCmp,
      ALU_CMPU-> reCmpu,
      ALU_SRA -> reSra,
      ALU_SRL -> reSrl,
      ALU_SLL -> reSll
    )
  )
  io.Zf := (io.result === 0.U)
  io.Nf := io.result(width - 1)
}