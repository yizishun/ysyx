module ysyx_23060171_jumppc(
	input [31:0]pc,
    input [31:0]imm,
	output [31:0]nextpc
);
	assign nextpc = (pc + imm)&(~32'b1);
endmodule