module ysyx_23060171_cpu(
	input [31:0]inst,
	input clk,
	input rst,
	output [31:0]pc
);
	wire [31:0]next_pc;
	wire [2:0]alucontrol;
	wire Regwrite;
	wire [2:0]immtype;
	wire [31:0]aluresult;
	wire [31:0]rd1;
	wire [31:0]rd2;
	wire [31:0]immext;
	wire [3:0]flag;
	ysyx_23060171_pc pc2(
		.clk(clk),
		.rst(rst),
		.next_pc(next_pc),
		.pc(pc)
	);
	ysyx_23060171_addpc addpc(
		.pc(pc),
		.nextpc(next_pc)
	);
	ysyx_23060171_gpr	gpr(
		.clk(clk),
		.wen(Regwrite),
		.waddr(inst[11:7]),
		.raddr1(inst[19:15]),
		.raddr2(inst[24:20]),
		.wdata(aluresult),
		.rdata1(rd1),
		.rdata2(rd2)
	);
	ysyx_23060171_idu idu(
		.opcode(inst[6:0]),
		.f3(inst[14:12]),
		.f7(inst[31:25]),
		.f12(inst[31:20]),
		.alucontrol(alucontrol),
		.Regwrite(Regwrite),
		.immtype(immtype)
	);
	ysyx_23060171_imm imm(
		.inst(inst),
		.immtype(immtype),
		.immext(immext)
	);
	ysyx_23060171_alu alu(
		.a(rd1),
		.b(immext),
		.alucontrol(alucontrol),
		.result(aluresult),
		.of(flag[0]),
		.zf(flag[1]),
		.nf(flag[2]),
		.cf(flag[3])
	);
endmodule
