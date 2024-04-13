`define BEQ            4'b0000
`define BNE            4'b0001
`define BLT            4'b0100
`define BGE            4'b0101
`define BLTU           4'b0110
`define BGEU           4'b0111
`define JAL            4'b1000
`define JALR           4'b1001
`define MRET           4'b1010
`define NJUMP          4'b1111
//PCSrc
`define pc_plus_4           3'b000
`define pc_plus_imm         3'b001
`define pc_plus_rs2         3'b010
`define mtvec               3'b011
`define mepc                3'b100
module ysyx_23060171_idupc(
	input [3:0]Jump,
	input zf,
	input cmp,
    output reg [2:0]PCSrc
);
always @(*) begin
		case(Jump)
			`BEQ:begin
				PCSrc = zf ? `pc_plus_imm : `pc_plus_4;
			end
			`BNE: begin
				PCSrc = zf ? `pc_plus_4 : `pc_plus_imm;
			end
			`BGE:begin
				PCSrc = cmp ? `pc_plus_4 : `pc_plus_imm;
			end
			`BLT:begin
				PCSrc = cmp ? `pc_plus_imm : `pc_plus_4;
			end
			`BLTU:begin
				PCSrc = cmp ? `pc_plus_imm : `pc_plus_4;
			end
			`BGEU:begin
				PCSrc = cmp ? `pc_plus_4 : `pc_plus_imm;
			end
            `JAL: PCSrc = `pc_plus_imm;
            `JALR:PCSrc = `pc_plus_rs2;
			`MRET:begin
				PCSrc = `mepc;
			end
			`NJUMP: PCSrc = `pc_plus_4;
            default:PCSrc = `pc_plus_4;
        endcase
end
endmodule