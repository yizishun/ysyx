package ysyx

import chisel3._
import chisel3.util._

import freechips.rocketchip.amba.apb._
import org.chipsalliance.cde.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._

class VGAIO extends Bundle {
  val r = Output(UInt(8.W))
  val g = Output(UInt(8.W))
  val b = Output(UInt(8.W))
  val hsync = Output(Bool())
  val vsync = Output(Bool())
  val valid = Output(Bool())
}

class VGACtrlIO extends Bundle {
  val clock = Input(Clock())
  val reset = Input(Bool())
  val in = Flipped(new APBBundle(APBBundleParameters(addrBits = 32, dataBits = 32)))
  val vga = new VGAIO
}

class vga_top_apb extends BlackBox {
  val io = IO(new VGACtrlIO)
}

class vgaChisel extends Module {
  val io = IO(new VGACtrlIO)
  //frame buffer
  val frame = Mem(640 * 480, Vec(4, UInt(8.W)))
  val vgaSender = Module(new vgaSend)
  //APB
  io.in.pready := io.in.penable
  io.in.pslverr := 0.U
  val wdata = Wire(Vec(4, UInt(8.W)))
  val strb = Wire(Vec(4, Bool()))
  for(i <- 0 until 4){
    wdata(i) := io.in.pwdata(i*8 + 7, i*8)
    strb(i) := io.in.pstrb(i).asBool
  }
  io.in.prdata := 0.U
  when(io.in.psel){
    when(io.in.pwrite){
      frame.write(io.in.paddr(23, 0) / 4.U, wdata, strb)
    }.elsewhen(~io.in.pwrite){
      io.in.prdata := frame.read(io.in.paddr).asUInt
    }
  }

  vgaSender.io.pclk := clock
  vgaSender.io.reset := reset
  io.vga.hsync := vgaSender.io.hsync
  io.vga.vsync := vgaSender.io.vsync
  io.vga.valid := vgaSender.io.valid
  io.vga.r := vgaSender.io.vga_r
  io.vga.g := vgaSender.io.vga_g
  io.vga.b := vgaSender.io.vga_b
  
  vgaSender.io.vga_data := frame.read(vgaSender.io.h_addr + vgaSender.io.v_addr * 640.U).asUInt
}

class vgaSend extends BlackBox with HasBlackBoxInline{
  val io = IO(new Bundle {
    val pclk = Input(Clock())
    val reset = Input(Reset())
    val vga_data = Input(UInt(24.W))
    val h_addr = Output(UInt(10.W))
    val v_addr = Output(UInt(10.W))
    //external
    val hsync = Output(Bool())
    val vsync = Output(Bool())
    val valid = Output(Bool())
    val vga_r = Output(UInt(8.W))
    val vga_g = Output(UInt(8.W))
    val vga_b = Output(UInt(8.W))
  })
  setInline("vgaSend.v", 
  """module vgaSend(
  |     input           pclk,     //25MHz时钟
  |     input           reset,    //置位
  |     input  [23:0]   vga_data, //上层模块提供的VGA颜色数据
  |     output [9:0]    h_addr,   //提供给上层模块的当前扫描像素点坐标
  |     output [9:0]    v_addr,
  |     output          hsync,    //行同步和列同步信号
  |     output          vsync,
  |     output          valid,    //消隐信号
  |     output [7:0]    vga_r,    //红绿蓝颜色信号
  |     output [7:0]    vga_g,
  |     output [7:0]    vga_b
  |     );
  | 
  |   //640x480分辨率下的VGA参数设置
  |   parameter    h_frontporch = 96;
  |   parameter    h_active = 144;
  |   parameter    h_backporch = 784;
  |   parameter    h_total = 800;
  | 
  |   parameter    v_frontporch = 2;
  |   parameter    v_active = 35;
  |   parameter    v_backporch = 515;
  |   parameter    v_total = 525;
  | 
  |   //像素计数值
  |   reg [9:0]    x_cnt;
  |   reg [9:0]    y_cnt;
  |   wire         h_valid;
  |   wire         v_valid;
  | 
  |   always @(posedge reset or posedge pclk) //行像素计数
  |       if (reset == 1'b1)
  |         x_cnt <= 1;
  |       else
  |       begin
  |         if (x_cnt == h_total)
  |             x_cnt <= 1;
  |         else
  |             x_cnt <= x_cnt + 10'd1;
  |       end
  | 
  |   always @(posedge pclk)  //列像素计数
  |       if (reset == 1'b1)
  |         y_cnt <= 1;
  |       else
  |       begin
  |         if (y_cnt == v_total & x_cnt == h_total)
  |             y_cnt <= 1;
  |         else if (x_cnt == h_total)
  |             y_cnt <= y_cnt + 10'd1;
  |       end
  |   //生成同步信号
  |   assign hsync = (x_cnt > h_frontporch);
  |   assign vsync = (y_cnt > v_frontporch);
  |   //生成消隐信号
  |   assign h_valid = (x_cnt > h_active) & (x_cnt <= h_backporch);
  |   assign v_valid = (y_cnt > v_active) & (y_cnt <= v_backporch);
  |   assign valid = h_valid & v_valid;
  |   //计算当前有效像素坐标
  |   assign h_addr = h_valid ? (x_cnt - 10'd145) : {10{1'b0}};
  |   assign v_addr = v_valid ? (y_cnt - 10'd36) : {10{1'b0}};
  |   //设置输出的颜色值
  |   assign vga_r = vga_data[23:16];
  |   assign vga_g = vga_data[15:8];
  |   assign vga_b = vga_data[7:0];
  | endmodule
  |
  """.stripMargin)
}


class APBVGA(address: Seq[AddressSet])(implicit p: Parameters) extends LazyModule {
  val node = APBSlaveNode(Seq(APBSlavePortParameters(
    Seq(APBSlaveParameters(
      address       = address,
      executable    = true,
      supportsRead  = true,
      supportsWrite = true)),
    beatBytes  = 4)))

  lazy val module = new Impl
  class Impl extends LazyModuleImp(this) {
    val (in, _) = node.in(0)
    val vga_bundle = IO(new VGAIO)

    val mvga = Module(new vgaChisel)
    mvga.io.clock := clock
    mvga.io.reset := reset
    mvga.io.in <> in
    vga_bundle <> mvga.io.vga
  }
}
