module ysyx_23060171_ifu(
    input clk,
    input rst,
    input [31:0]pc_plus_rs2,
    input [31:0]pc_plus_imm,
    input [31:0]mtvec,
    input [31:0]mepc,
    input [2:0]PCSrc, //control signal
    input irq,  //control signal
    output [31:0]inst,
    output [31:0]pc_plus_4F,
    output [31:0]pcF
);
    wire [31:0]next_pc;
    wire [2:0]PCSrc_irq;
	ysyx_23060171_addpc addpc(
		.pc(pcF),
		.nextpc(pc_plus_4F)
	);
	ysyx_23060171_MuxKey #(2,1,3) pcirqmux(PCSrc_irq,irq,{
		1'b0,PCSrc,
		1'b1,3'b011
	});
	ysyx_23060171_MuxKey #(5,3,32) pcmux(next_pc,PCSrc_irq,{
		3'b000,pc_plus_4F,
		3'b001,pc_plus_imm,
		3'b010,pc_plus_rs2,
		3'b011,mtvec,
		3'b100,mepc
	});
	ysyx_23060171_pc pc(
		.clk(clk),
		.rst(rst),
		.next_pc(next_pc),
		.pc(pcF)
	);
	ysyx_23060171_inst_memory inst_memory(
		.valid(~(rst | clk)),
		.pc(pcF),
		.inst(inst)
	);

endmodule