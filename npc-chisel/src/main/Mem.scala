package npc

import chisel3._
import chisel3.util._

class imemIO extends Bundle{

}
class dmemIO extends Bundle{

}

class Imem extends Module{
  val io = IO(new imemIO)
}
class Dmem extends Module{
  val io = IO(new dmemIO)
} 