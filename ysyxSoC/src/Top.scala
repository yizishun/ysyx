package ysyx

import chisel3._
import org.chipsalliance.cde.config.Parameters
import freechips.rocketchip.system.DefaultConfig
import freechips.rocketchip.diplomacy.LazyModule

object Config {
  def hasChipLink: Boolean = false
  def sdramUseAXI: Boolean = false
}

class ysyxSoCTop extends Module {
  implicit val config: Parameters = new DefaultConfig

  val io = IO(new Bundle { })
  val dut = LazyModule(new ysyxSoCFull)
  val mdut = Module(dut.module)
  mdut.dontTouchPorts()
  mdut.externalPins := DontCare
}

object Elaborate extends App {
  val firtoolOptions = Array("--disable-annotation-unknown")
  circt.stage.ChiselStage.emitSystemVerilogFile(new ysyxSoCTop, args, firtoolOptions)
}
