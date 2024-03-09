module ysyx_23060171_cpu(
	input clk,
	input rst
);
	//control signal
	wire MemWriteE;
	wire [7:0]MemWmask;
	wire MemValid;
	wire [2:0]MemRD;
	wire [3:0]alucontrol;
	wire RegwriteE;
	wire [2:0]immtype;
	wire AluSrcA;
	wire AluSrcB;
	wire [1:0]RegwriteD;
	wire [1:0]PCSrc;
	//pc
	wire [31:0]pc;
	wire [31:0]inst;
	wire [31:0]nextpc;
	wire [31:0]snpc;
	wire [31:0]dnpc;
	wire [31:0]dnpc_r;
	//gpr
	wire [31:0]wd;
	wire [31:0]rd1;
	wire [31:0]rd2;
	//imm
	wire [31:0]immext;
	//alu
	wire [31:0]aluresult;
	wire [31:0]aluA;
	wire [31:0]aluB;
	wire [3:0]flag;
	//mem
	wire [31:0]maddr;
	wire [31:0]place;
	wire [7:0]RealMemWmask;
	wire [31:0]rdplace;
	wire [31:0]rdraw;
	wire [31:0]rd;
	ysyx_23060171_pc pc2(
		.clk(clk),
		.rst(rst),
		.next_pc(nextpc),
		.pc(pc)
	);
	ysyx_23060171_inst_memory inst_memory(
		.valid(~(rst | clk)),
		.pc(pc),
		.inst(inst)
	);
	assign dnpc_r = aluresult & (~32'b1);
	ysyx_23060171_MuxKey #(3,2,32) pcmux(nextpc,PCSrc,{
		2'b00,snpc,
		2'b01,dnpc,
		2'b10,dnpc_r
	});
	ysyx_23060171_addpc addpc(
		.pc(pc),
		.nextpc(snpc)
	);
	ysyx_23060171_jumppc jumppc(
		.pc(pc),
		.imm(immext),
		.nextpc(dnpc)
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
	ysyx_23060171_MuxKey #(4,2,32) wdmux(wd,RegwriteD,{
		2'b00,aluresult,
		2'b01,immext,
		2'b10,snpc,
		2'b11,rd
	});
	ysyx_23060171_idu idu(
		.opcode(inst[6:0]),
		.f3(inst[14:12]),
		.f7(inst[31:25]),
		.f12(inst[31:20]),
		.MemWriteE(MemWriteE),
		.MemWmask(MemWmask),
		.MemValid(MemValid),
		.MemRD(MemRD),
		.alucontrol(alucontrol),
		.RegwriteE(RegwriteE),
		.immtype(immtype),
		.AluSrcA(AluSrcA),
		.AluSrcB(AluSrcB),
		.RegwriteD(RegwriteD)
	);
	ysyx_23060171_idupc idupc(
		.opcode(inst[6:0]),
		.f3(inst[14:12]),
		.f7(inst[31:25]),
		.f12(inst[31:20]),
		.zf(flag[1]),
		.cmp(aluresult[0]),
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
	ysyx_23060171_MuxKey #(2,1,32) alusrcbmux(aluB,AluSrcB,{
		1'b0,rd2,
		1'b1,immext
	});
	ysyx_23060171_alu alu(
		.a(aluA),
		.b(aluB),
		.alucontrol(alucontrol),
		.result(aluresult),
		.of(flag[0]),
		.zf(flag[1]),
		.nf(flag[2]),
		.cf(flag[3])
	);
	assign maddr = aluresult & (~32'h3);
	assign place = aluresult - maddr;
	assign RealMemWmask = MemWmask << place;
	ysyx_23060171_data_memory data_memory(
		.clk(clk),
		.wen(MemWriteE),
		.valid(MemValid),
		.raddr(maddr),
		.waddr(maddr),
		.wdata(rd2),
		.wmask(RealMemWmask),
		.rdata(rdraw)
	);
	assign rdplace = rdraw >> (place << 3);
	ysyx_23060171_MuxKey #(5,3,32) Mrdmux(rd,MemRD,{
		3'b010,rdraw,
		3'b001,{{16{rdraw[15]}},rdplace[15:0]},
		3'b000,{{24{rdraw[7]}},rdplace[7:0]},
		3'b011,{24'b0,rdplace[7:0]},
		3'b100,{16'b0,rdplace[15:0]}
	});
endmodule
