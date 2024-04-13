module ysyx_23060171_lsu(
    input clk,
    //from EXU
    input [31:0]aluresult,
    input [31:0]crd1S,
    input [31:0]pcS,
    input [31:0]immextS,
    input [31:0]pc_plus_4S,
    input [31:0]rd1S,
    input [31:0]rd2S,
    //control signal from EXU
    input irqS,
    input [2:0]RegwriteDS,
    input [1:0]CSRWriteDS,
    input [2:0]MemRDS,
    input MemValidS,
    input [7:0]MemWmaskS,
    input MemWriteES,
    //to WBU
    output [31:0]rd1W,
    output [31:0]aluresultW,
    output [31:0]crd1W,
    output [31:0]pcW,
    output [31:0]immextW,
    output [31:0]pc_plus_4W,
    output [31:0]MemRW,
    //control signal to WBU
    output irqW,
    output [2:0]RegwriteDW,
    output [1:0]CSRWriteDW
);
    wire [31:0]maddr;
	wire [31:0]place;
	wire [7:0]RealMemWmask;
	wire [31:0]rdplace;
	wire [31:0]rdraw;
    assign irqW = irqS; 
    assign RegwriteDW = RegwriteDS;
    assign CSRWriteDW = CSRWriteDS;
    assign rd1W = rd1S;
    assign aluresultW = aluresult;
    assign crd1W = crd1S;
    assign pcW = pcS;
    assign immextW = immextS;
    assign pc_plus_4W = pc_plus_4S;

	assign maddr = aluresult & (~32'h3);
	assign place = aluresult - maddr;
	assign RealMemWmask = MemWmaskS << place;
	ysyx_23060171_data_memory data_memory(
		.clk(clk),
		.wen(MemWriteES),
		.valid(MemValidS),
		.raddr(maddr),
		.waddr(maddr),
		.wdata(rd2S),
		.wmask(RealMemWmask),
		.rdata(rdraw)
	);
	assign rdplace = rdraw >> (place << 3);
	ysyx_23060171_MuxKey #(5,3,32) Mrdmux(MemRW,MemRDS,{
		3'b010,rdraw,
		3'b001,{{16{rdraw[15]}},rdplace[15:0]},
		3'b000,{{24{rdraw[7]}},rdplace[7:0]},
		3'b011,{24'b0,rdplace[7:0]},
		3'b100,{16'b0,rdplace[15:0]}
	});


endmodule