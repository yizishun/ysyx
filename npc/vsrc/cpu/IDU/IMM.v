module ysyx_23060171_imm(
	input [31:0]inst,
	input [2:0]immtype,
	output[31:0]immext
);
	wire [31:0]immI;
	wire [31:0]immU;
	wire [31:0]immJ;
	wire [31:0]immS;
	wire [31:0]immB;
	assign immI = {{21{inst[31]}},inst[30:20]};
	assign immU = {inst[31:12],12'b0};
	assign immJ = {{12{inst[31]}},inst[19:12],inst[20],inst[30:21],1'b0};
	assign immS = {{21{inst[31]}},inst[30:25],inst[11:7]};
	assign immB = {{20{inst[31]}},inst[7],inst[30:25],inst[11:8],1'b0};
	ysyx_23060171_MuxKey #(5,3,32) immmux(immext,immtype,{
		3'b000,immI,
		3'b001,immU,
		3'b010,immJ,
		3'b011,immS,
		3'b100,immB
	});
endmodule
