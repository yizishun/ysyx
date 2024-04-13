module ysyx_23060171_idu(
    //from IFU
    input clk,
    input [31:0]inst,
    input [31:0]pcD,
    input [31:0]pc_plus_4D,
    //from WBU
    input [31:0]wd,
    input [31:0]csr_wd,
    //to EXU
    output [31:0]rd1,
    output [31:0]rd2,
    output [31:0]crd1,
    output [31:0]pcE,
    output [31:0]pc_plus_4E,
    output [31:0]immextD,
    //control signals------------
    output irqE,
    output irqF,
    output [2:0]RegwriteD,
    output [1:0]CSRWriteD,
    output [2:0]MemRD,
    output MemValid,
    output [7:0]MemWmask,
    output MemWriteE,
    output [3:0]alucontrol,
    output [3:0]Jump,
    output AluSrcA,
    output AluSrcB,
    //to IFU
    output [31:0]mtvec,
    output [31:0]mepc
);
    wire [7:0]irq_no;
    wire [2:0]immtype;
    wire CSRWriteE;
    wire RegwriteE;
    wire irq;
    assign pcE = pcD;
    assign pc_plus_4E = pc_plus_4D;    
	ysyx_23060171_controller controller(
		.opcode(inst[6:0]),
		.f3(inst[14:12]),
		.f7(inst[31:25]),
		.f12(inst[31:20]),
		.irq(irq),
		.irq_no(irq_no),
		.MemWriteE(MemWriteE),
		.MemWmask(MemWmask),
		.MemValid(MemValid),
		.MemRD(MemRD),
		.alucontrol(alucontrol),
		.CSRWriteE(CSRWriteE),
		.RegwriteE(RegwriteE),
		.immtype(immtype),
		.AluSrcA(AluSrcA),
		.AluSrcB(AluSrcB),
		.CSRWriteD(CSRWriteD),
		.RegwriteD(RegwriteD),
        .Jump(Jump)
	);
    assign irqE = irq;
    assign irqF = irq;
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
	ysyx_23060171_csr   csr(
		.clk(clk),
		.irq(irq),
		.irq_no(irq_no),
		.wen(CSRWriteE),
		.waddr(inst[31:20]),
		.wdata(csr_wd),
		.raddr1(inst[31:20]),
		.rdata1(crd1),
		.mtvec(mtvec),
		.mepc(mepc)
	);
	ysyx_23060171_imm imm(
		.inst(inst),
		.immtype(immtype),
		.immext(immextD)
	);

endmodule