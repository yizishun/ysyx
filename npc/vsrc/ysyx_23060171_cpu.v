module ysyx_23060171_cpu(
	input [31:0]inst,
	input clk,
	input rst,
	output [31:0]pc
);
	//control signal
	wire [2:0]alucontrol;
	wire RegwriteE;
	wire [2:0]immtype;
	wire AluSrcA;
	wire [1:0]RegwriteD;
	wire [1:0]PCSrc;
	//pc
	wire [31:0]nextpc;
	wire [31:0]pc_plus_4;
	wire [31:0]addr;
	//gpr
	wire [31:0]wd;
	wire [31:0]rd1;
	wire [31:0]rd2;
	//imm
	wire [31:0]immext;
	//alu
	wire [31:0]aluresult;
	wire [31:0]aluA;
	wire [3:0]flag;
	ysyx_23060171_pc pc2(
		.clk(clk),
		.rst(rst),
		.next_pc(nextpc),
		.pc(pc)
	);
	ysyx_23060171_MuxKey #(3,2,32) pcmux(nextpc,PCSrc,{
		2'b00,pc_plus_4,
		2'b01,aluresult,
		2'b10,addr
	});
	ysyx_23060171_addpc addpc(
		.pc(pc),
		.nextpc(pc_plus_4)
	);
	ysyx_23060171_gpr	gpr(
		.clk(clk),
		.wen(RegwriteE),
		.waddr(inst[11:7]),
		.raddr1(inst[19:15]),
		.raddr2(inst[24:20]),
		.wdata(wd),
		.rdata1(rd1),
		.rdata2(rd2)
	);
	ysyx_23060171_MuxKey #(3,2,32) wdmux(wd,RegwriteD,{
		2'b00,aluresult,
		2'b01,immext,
		2'b10,pc_plus_4
	});
	ysyx_23060171_idu idu(
		.opcode(inst[6:0]),
		.f3(inst[14:12]),
		.f7(inst[31:25]),
		.f12(inst[31:20]),
		.alucontrol(alucontrol),
		.RegwriteE(RegwriteE),
		.immtype(immtype),
		.AluSrcA(AluSrcA),
		.RegwriteD(RegwriteD),
		.PCSrc(PCSrc)
	);
	ysyx_23060171_imm imm(
		.inst(inst),
		.immtype(immtype),
		.immext(immext)
	);
	ysyx_23060171_MuxKey #(2,1,32) alusrcamux(aluA,AluSrcA,{
		1'b0,rd1,
		1'b1,pc
	});
	ysyx_23060171_alu alu(
		.a(aluA),
		.b(immext),
		.alucontrol(alucontrol),
		.result(aluresult),
		.of(flag[0]),
		.zf(flag[1]),
		.nf(flag[2]),
		.cf(flag[3])
	);
	assign addr = aluresult & ~32'b1;
endmodule
