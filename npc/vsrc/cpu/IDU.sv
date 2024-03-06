/*-----------instruction---------*/
//opcode
`define ENV            7'b1110011
`define IMMARITH       7'b0010011
`define auipc          7'b0010111
`define lui            7'b0110111
`define jal            7'b1101111
`define jalr           7'b1100111
//function
`define ebreak         12'b000000000001
`define addi           3'b000
/*----------control signal----------*/
//alucontrol
`define ALUADD         3'b000
//RegwriteE
`define WRITE          1'b1
`define NWRITE         1'b0
//RegwriteD
`define Aluresult_1    2'b00
`define imm            2'b01
`define pc_plus_4_2	   2'b10
//immtype
`define immI           3'b000
`define immU           3'b001
`define immJ           3'b010
//AluSrcA
`define ALUaPC         1'b1
`define ALUaRD1        1'b0
//PCSrc
`define pc_plus_4_1    2'b00
`define Aluresult_2    2'b01
`define addr           2'b10
module ysyx_23060171_idu(
	input [6:0]opcode,
	input [2:0]f3,
	input [6:0]f7,
	input [11:0]f12,
	output reg [2:0]alucontrol,
	output reg RegwriteE,
	output reg [2:0]immtype,
	output reg AluSrcA,
	output reg [1:0]RegwriteD,
	output reg [1:0]PCSrc
);

	import "DPI-C" function void npc_trap();
	always @(*) begin
		case(opcode)
			`ENV: begin
				case(f12)
					`ebreak: npc_trap();
					default:begin
						PCSrc = `pc_plus_4_1;
						RegwriteE = `NWRITE;
					end
				endcase
			end
			`IMMARITH:begin
				case(f3)
					`addi:begin
						AluSrcA = `ALUaRD1;
						alucontrol = `ALUADD;
						RegwriteE = `WRITE;
						immtype = `immI;
						RegwriteD = `Aluresult_1;
						PCSrc = `pc_plus_4_1;
					end
				default:begin 
					PCSrc = `pc_plus_4_1;
					RegwriteE = `NWRITE;
				end
				endcase
			end
			`auipc:begin
				AluSrcA = `ALUaPC;
				alucontrol = `ALUADD;
				RegwriteE = `WRITE;
				immtype = `immU;
				RegwriteD = `Aluresult_1;
				PCSrc = `pc_plus_4_1;
			end
			`lui:begin
				RegwriteE = `WRITE;
				immtype = `immU;
				RegwriteD = `imm;
				PCSrc = `pc_plus_4_1;
			end
			`jal:begin
				AluSrcA = `ALUaPC;
				alucontrol = `ALUADD;
				RegwriteE = `WRITE;
				immtype = `immJ;
				RegwriteD = `pc_plus_4_2;
				PCSrc = `Aluresult_2;
			end
			`jalr:begin
				AluSrcA = `ALUaRD1;
				alucontrol = `ALUADD;
				RegwriteE = `WRITE;
				immtype = `immI;
				RegwriteD = `pc_plus_4_2;
				PCSrc = `addr;
			end
			default:begin 
				PCSrc = `pc_plus_4_1;
				RegwriteE = `NWRITE;
			end
		endcase
	end
endmodule
