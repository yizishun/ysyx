module ysyx_23060171_addpc(
	input [31:0]pc,
	output [31:0]nextpc
);
	assign nextpc = pc + 4;
endmodule
