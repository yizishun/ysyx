module ysyx_23060171_pc(
	input clk,
	input rst,
	input [31:0]next_pc,
	output [31:0]pc
);
	ysyx_23060171_Reg #(32,32'h80000000) pc1(
		.clk(clk),
		.rst(rst),
		.din(next_pc),
		.dout(pc),
		.wen(1'b1)
	);	
endmodule
