module ysyx_23060171_alu(
	input [31:0]a,
	input [31:0]b,
	input [2:0]alucontrol,
	output [31:0]result,
	output of,
	output zf,
	output nf,
	output cf
);
	wire [31:0]raddsub;
	wire [31:0]reand;
	wire [31:0]reor;
	wire [31:0]b2;
	assign b2 = b^{32{alucontrol[0]}};
	/* verilator lint_off WIDTHEXPAND */
	assign {cf,raddsub} = a + b2 + alucontrol[0];
	/* verilator lint_off WIDTHEXPAND */
	assign of = (~(a[31]^b2[31]))&(a[31]^raddsub[31]);
	assign reand = a^b;
	assign reor = a|b;
	ysyx_23060171_MuxKey #(4,3,32) alumux(result,alucontrol,{
		3'b000,raddsub,
		3'b001,raddsub,
		3'b010,reand,
		3'b011,reor
	});
	assign zf = ~(|result);
	assign nf = result[31];
endmodule

