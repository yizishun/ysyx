module ysyx_23060171_alu(
	input [31:0]a,
	input [31:0]b,
	input [3:0]alucontrol,
	output [31:0]result,
	output of,
	output zf,
	output nf,
	output cf
);
	wire [31:0]raddsub;
	wire [31:0]reand;
	wire [31:0]reor;
	wire [31:0]rexor;
	wire [31:0]recmp;
	wire [31:0]recmpu;
	wire [31:0]resra;
	wire [31:0]resrl;
	wire [31:0]resll;
	wire [31:0]b2;
	assign b2 = b^{32{alucontrol[0]}};
	/* verilator lint_off WIDTHEXPAND */
	assign {cf,raddsub} = a + b2 + alucontrol[0];
	/* verilator lint_off WIDTHEXPAND */
	assign of = (~(a[31]^b2[31]))&(a[31]^raddsub[31]);
	assign reand = a&b;
	assign reor = a|b;
	assign rexor = a^b;
	assign recmpu = |b ? {31'b0,{~cf}} : 32'b0; 
	assign recmp = {31'b0,{raddsub[31]^of}};
	assign resra = ($signed(a)) >>> b[4:0];
	assign resrl = a >> b[4:0];
	assign resll = a << b[4:0];
	ysyx_23060171_MuxKey #(10,4,32) alumux(result,alucontrol,{
		4'b0000,raddsub,
		4'b0001,raddsub,
		4'b0010,reand,
		4'b0011,recmp,
		4'b0100,rexor,
		4'b0101,recmpu,
		4'b0110,reor,
		4'b0111,resra,
		4'b1000,resrl,
		4'b1001,resll
	});
	assign zf = ~(|result);
	assign nf = result[31];
endmodule

