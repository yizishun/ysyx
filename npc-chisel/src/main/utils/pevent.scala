package npc

import chisel3._
import chisel3.util._
object EVENT{
  val IFUGetInst = 0.U;
  val LSUGetData = 1.U;
  val EXUFinCal  = 2.U;
  val IDUFinDec  = 3.U;
}

class PerformanceProbe(bits: Int) extends BlackBox(Map("width" -> bits)) with HasBlackBoxInline{
  val io = IO(new Bundle{
    val clock = Input(Clock())
    val eventType = Input(UInt(4.W))
    val inc = Input(UInt(bits.W))
    val subType = Input(UInt(8.W))
    val start = Input(Bool())
    val e = Input(Bool())
    val timeEn = Input(Bool())
  })
  setInline("PerformanceProbe.v",
    """module PerformanceProbe #(
      |  parameter width = 1
      |)(
      |    input            clock,
      |    input  [3:0] eventType,
      |    input  [width-1:0] inc,
      |    input  [7:0]   subType,
      |    input            start,
      |    input            e,
      |    input            timeEn
      |);
      |  import "DPI-C" function void peventWrapper(input int eventType, input int inc, input int subtype, input int start, input int e, input int timeEn);
      |  wire [31:0]_eventType = {{28'b0}, eventType};
      |  wire [31:0] _inc = {{(32-width){1'b0}}, inc};
      |  wire [31:0]_subType = {{24'b0}, subType};
      |  wire [31:0]_start = {{31'b0}, start};
      |  wire [31:0]_end = {{31'b0}, e};
      |  wire [31:0]_timeEn = {{31'b0}, timeEn};
      |  always @(posedge clock)begin
      |     peventWrapper(_eventType, _inc, _subType, _start, _end, _timeEn);
      |  end
      |endmodule
    """.stripMargin)
}

object PerformanceProbe {
  def apply(clock: Clock, eventType: UInt, inc: UInt, subType: UInt, start: Bool = false.B, end: Bool = false.B, timeEn: Bool = true.B) = {
    val probe = Module(new PerformanceProbe(inc.getWidth))
    probe.io.clock := clock
    probe.io.eventType := eventType
    probe.io.inc := inc
    probe.io.subType := subType
    probe.io.start := start
    probe.io.e := end
    probe.io.timeEn := timeEn
  }
}