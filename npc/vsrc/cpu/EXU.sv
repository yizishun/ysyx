module ysyx_23060171_exu(
    //from IDU
    input [31:0]rd1E,
    input [31:0]rd2E,
    input [31:0]crd1E,
    input [31:0]pcE,
    input [31:0]pc_plus_4E,
    input [31:0]immextE,
    input [4:0]rwE,
	input [11:0]crwE,
    //control signal from IDU
    input irq,
    input [2:0]RegwriteD,
    input [1:0]CSRWriteD,
	input RegwriteE,
	input CSRWriteE,
    input [2:0]MemRD,
    input MemValid,
    input [7:0]MemWmask,
    input MemWriteE,
    input [3:0]alucontrol,
    input [3:0]Jump,
    input AluSrcA,
    input AluSrcB,
    //to IFU
    output [31:0]pc_plus_imm,
    output [31:0]pc_plus_rs2,
    //to ISU
    output [31:0]aluresult,
    output [31:0]crd1S,
    output [31:0]pcS,
    output [31:0]immextS,
    output [31:0]pc_plus_4S,
    output [31:0]rd1S,
    output [31:0]rd2S,
    output [4:0]rwS,
	output [11:0]crwS,
    //control signal to ISU
    output irqS,
    output [2:0]RegwriteDS,
    output [1:0]CSRWriteDS,
    output RegwriteES,
	output CSRWriteES,
    output [2:0]MemRDS,
    output MemValidS,
    output [7:0]MemWmaskS,
    output MemWriteES,
    //control signal to IFU
    output [2:0]PCSrc
);
    wire [3:0]flag;
    wire [31:0]aluA;
    wire [31:0]aluB;
    assign irqS = irq;
    assign RegwriteDS = RegwriteD;
    assign CSRWriteDS = CSRWriteD;
    assign RegwriteES = RegwriteE;
    assign CSRWriteES = CSRWriteE;
    assign MemRDS = MemRD;
    assign MemValidS = MemValid;
    assign MemWmaskS = MemWmask;
    assign MemWriteES = MemWriteE;
    assign crd1S = crd1E;
    assign pcS = pcE;
    assign pc_plus_4S = pc_plus_4E;
    assign rd1S = rd1E;
    assign rd2S = rd2E;
    assign immextS = immextE;
    assign rwS = rwE;
    assign crwS = crwE;

	ysyx_23060171_idupc idupc(
        .Jump(Jump),
		.zf(flag[1]),
		.cmp(aluresult[0]),
		.PCSrc(PCSrc)
	);
	ysyx_23060171_MuxKey #(2,1,32) alusrcamux(aluA,AluSrcA,{
		1'b0,rd1E,
		1'b1,pcE
	});
	ysyx_23060171_MuxKey #(2,1,32) alusrcbmux(aluB,AluSrcB,{
		1'b0,rd2E,
		1'b1,immextE
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
	ysyx_23060171_jumppc jumppc(
		.pc(pcE),
		.imm(immextE),
		.nextpc(pc_plus_imm)
	);
	assign pc_plus_rs2 = aluresult & (~32'b1);

endmodule