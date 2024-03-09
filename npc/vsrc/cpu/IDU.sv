/*-----------instruction----(RV32E)--*/
//opcode
`define ENV            7'b1110011
`define IMMARITH       7'b0010011
`define REGARITH       7'b0110011
`define STORE          7'b0100011
`define LOAD           7'b0000011
`define BRANCH         7'b1100011
`define auipc          7'b0010111
`define lui            7'b0110111
`define jal            7'b1101111
`define jalr           7'b1100111
//function
  //env
`define ebreak         12'b000000000001
  //IMMARITH
`define addi           3'b000
`define slli           3'b001
`define slti           3'b010
`define sltiu          3'b011
`define sri            3'b101
`define srli           7'b0000000
`define srai           7'b0100000
`define xori           3'b100
`define ori            3'b110
`define andi           3'b111
  //REGARITH
`define add_sub        3'b000
`define add            7'b0000000
`define sub            7'b0100000
`define sll            3'b001
`define slt            3'b010
`define sltu           3'b011
`define xor            3'b100
`define sr             3'b101
`define srl            7'b0000000
`define sra            7'b0100000
`define or             3'b110
`define and            3'b111
  //STORE
`define sw             3'b010
`define sh             3'b001
`define sb 			   3'b000
  //LOAD
`define lw             3'b010
`define lh             3'b001
`define lb             3'b000
`define lbu            3'b100
`define lhu		       3'b101 
  //BRANCH
`define beq            3'b000
`define bne            3'b001
`define blt            3'b100
`define bge            3'b101
`define bltu           3'b110
`define bgeu           3'b111
/*----------control signal----------*/
//MemRD
`define RBYTE          3'b000
`define RHALFW         3'b001
`define RWORD          3'b010
`define RBYTEU         3'b011
`define RHALFWU        3'b100
//MemWriteE
`define MWRITE         1'b1
`define MNWRITE        1'b0
//MemWmask
`define WBYTE          8'b00000001
`define WHALFW         8'b00000011
`define WWORD          8'b00001111
//MemValid
`define VALID          1'b1
`define INVALID        1'b0
//alucontrol
`define ALUADD         4'b0000
`define ALUSUB         4'b0001
`define ALUXOR         4'b0100
`define ALUCMPU        4'b0101
`define ALUCMP         4'b0011
`define ALUOR          4'b0110
`define ALUAND         4'b0010
`define ALUSRA         4'b0111
`define ALUSRL         4'b1000
`define ALUSLL         4'b1001
//RegwriteE
`define RWRITE         1'b1
`define RNWRITE        1'b0
//RegwriteD
`define Aluresult      2'b00
`define imm            2'b01
`define pc_plus_4_2	   2'b10
`define MemRD          2'b11 
//immtype
`define immI           3'b000
`define immU           3'b001
`define immJ           3'b010
`define immS		   3'b011
`define immB           3'b100
//AluSrcA
`define ALUaPC         1'b1
`define ALUaRD1        1'b0
//AluSrcB
`define ALUbimm        1'b1
`define ALUbRD2        1'b0
module ysyx_23060171_idu(
	input [6:0]opcode,
	input [2:0]f3,
	input [6:0]f7,
	input [11:0]f12,
	output reg MemWriteE,
	output reg [7:0]MemWmask,
	output reg MemValid,
	output reg [2:0]MemRD,
	output reg [3:0]alucontrol,
	output reg RegwriteE,
	output reg [2:0]immtype,
	output reg AluSrcA,
	output reg AluSrcB,
	output reg [1:0]RegwriteD
);
	import "DPI-C" function void npc_trap();
	always @(*) begin
		case(opcode)
			`ENV: begin
				case(f12)
					`ebreak: begin
						npc_trap();
					end
					default:begin
						RegwriteE = `RNWRITE;
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
					end
				endcase
			end
			`IMMARITH:begin
				case(f3)
					`addi:begin
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbimm;
						alucontrol = `ALUADD;
						RegwriteE = `RWRITE;
						immtype = `immI;
						RegwriteD = `Aluresult;
					end
					`slli:begin
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbimm;
						alucontrol = `ALUSLL;
						RegwriteE = `RWRITE;
						immtype = `immI;
						RegwriteD = `Aluresult;
					end
					`slti:begin
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbimm;
						alucontrol = `ALUCMP;
						RegwriteE = `RWRITE;
						immtype = `immI;
						RegwriteD = `Aluresult;
					end
					`sltiu:begin
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbimm;
						alucontrol = `ALUCMPU;
						RegwriteE = `RWRITE;
						immtype = `immI;
						RegwriteD = `Aluresult;
					end
					`sri:begin
						case(f7)
							`srli:begin
								MemWriteE = `MNWRITE;
								MemValid = `INVALID;
								AluSrcA = `ALUaRD1;
								AluSrcB = `ALUbimm;
								alucontrol = `ALUSRL;
								RegwriteE = `RWRITE;
								immtype = `immI;
								RegwriteD = `Aluresult;
							end
							`srai:begin
								MemWriteE = `MNWRITE;
								MemValid = `INVALID;
								AluSrcA = `ALUaRD1;
								AluSrcB = `ALUbimm;
								alucontrol = `ALUSRA;
								RegwriteE = `RWRITE;
								immtype = `immI;
								RegwriteD = `Aluresult;
							end
							default:begin 
								RegwriteE = `RNWRITE;
								MemWriteE = `MNWRITE;
								MemValid = `INVALID;
							end
						endcase
					end
					`xori:begin
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbimm;
						alucontrol = `ALUXOR;
						RegwriteE = `RWRITE;
						immtype = `immI;
						RegwriteD = `Aluresult;
					end
					`ori:begin
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbimm;
						alucontrol = `ALUOR;
						RegwriteE = `RWRITE;
						immtype = `immI;
						RegwriteD = `Aluresult;
					end
					`andi:begin
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbimm;
						alucontrol = `ALUAND;
						RegwriteE = `RWRITE;
						immtype = `immI;
						RegwriteD = `Aluresult;
					end
					default:begin 
						RegwriteE = `RNWRITE;
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
					end
				endcase
			end
			`REGARITH:begin
				case(f3)
					`add_sub:begin
						case(f7)
							`add:begin
								MemWriteE = `MNWRITE;
								MemValid = `INVALID;
								AluSrcA = `ALUaRD1;
								AluSrcB = `ALUbRD2;
								alucontrol = `ALUADD;
								RegwriteE = `RWRITE;
								RegwriteD = `Aluresult;
							end
							`sub:begin
								MemWriteE = `MNWRITE;
								MemValid = `INVALID;
								AluSrcA = `ALUaRD1;
								AluSrcB = `ALUbRD2;
								alucontrol = `ALUSUB;
								RegwriteE = `RWRITE;
								RegwriteD = `Aluresult;
							end
							default:begin 
								RegwriteE = `RNWRITE;
								MemWriteE = `MNWRITE;
								MemValid = `INVALID;
							end
						endcase
					end
					`sll:begin
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbRD2;
						alucontrol = `ALUSLL;
						RegwriteE = `RWRITE;
						RegwriteD = `Aluresult;
					end
					`slt:begin
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbRD2;
						alucontrol = `ALUCMP;
						RegwriteE = `RWRITE;
						RegwriteD = `Aluresult;
					end
					`sltu:begin
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbRD2;
						alucontrol = `ALUCMPU;
						RegwriteE = `RWRITE;
						RegwriteD = `Aluresult;
					end
					`xor:begin
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbRD2;
						alucontrol = `ALUXOR;
						RegwriteE = `RWRITE;
						RegwriteD = `Aluresult;
					end
					`sr:begin
						case(f7)
							`srl:begin
								MemWriteE = `MNWRITE;
								MemValid = `INVALID;
								AluSrcA = `ALUaRD1;
								AluSrcB = `ALUbRD2;
								alucontrol = `ALUSRL;
								RegwriteE = `RWRITE;
								RegwriteD = `Aluresult;
							end
							`sra:begin
								MemWriteE = `MNWRITE;
								MemValid = `INVALID;
								AluSrcA = `ALUaRD1;
								AluSrcB = `ALUbRD2;
								alucontrol = `ALUSRA;
								RegwriteE = `RWRITE;
								RegwriteD = `Aluresult;
							end
							default:begin 
								RegwriteE = `RNWRITE;
								MemWriteE = `MNWRITE;
								MemValid = `INVALID;
							end
						endcase
					end
					`or:begin
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbRD2;
						alucontrol = `ALUOR;
						RegwriteE = `RWRITE;
						RegwriteD = `Aluresult;
					end
					`and:begin
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbRD2;
						alucontrol = `ALUAND;
						RegwriteE = `RWRITE;
						RegwriteD = `Aluresult;
					end
					default:begin 
						RegwriteE = `RNWRITE;
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
				end
				endcase
			end
			`STORE:begin
				case(f3)
					`sw:begin
						MemWriteE = `MWRITE;
						MemValid = `VALID;
						MemWmask = `WWORD;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbimm;
						alucontrol = `ALUADD;
						immtype = `immS;
						RegwriteE = `RNWRITE;
					end
					`sh:begin
						MemWriteE = `MWRITE;
						MemValid = `VALID;
						MemWmask = `WHALFW;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbimm;
						alucontrol = `ALUADD;
						immtype = `immS;
						RegwriteE = `RNWRITE;
					end
					`sb:begin
						MemWriteE = `MWRITE;
						MemValid = `VALID;
						MemWmask = `WBYTE;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbimm;
						alucontrol = `ALUADD;
						immtype = `immS;
						RegwriteE = `RNWRITE;
					end
					default:begin 
						RegwriteE = `RNWRITE;
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
					end
				endcase 
			end
			`LOAD:begin
				case(f3)
					`lw:begin
						MemWriteE = `MNWRITE;
						MemValid = `VALID;
						MemWmask = `WWORD;
						MemRD = `RWORD;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbimm;
						alucontrol = `ALUADD;
						immtype = `immI;
						RegwriteE = `RWRITE;
						RegwriteD = `MemRD;
					end
					`lh:begin
						MemWriteE = `MNWRITE;
						MemValid = `VALID;
						MemWmask = `WWORD;
						MemRD = `RHALFW;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbimm;
						alucontrol = `ALUADD;
						immtype = `immI;
						RegwriteE = `RWRITE;
						RegwriteD = `MemRD;
					end
					`lb:begin
						MemWriteE = `MNWRITE;
						MemValid = `VALID;
						MemWmask = `WWORD;
						MemRD = `RBYTE;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbimm;
						alucontrol = `ALUADD;
						immtype = `immI;
						RegwriteE = `RWRITE;
						RegwriteD = `MemRD;
					end
					`lbu:begin
						MemWriteE = `MNWRITE;
						MemValid = `VALID;
						MemWmask = `WWORD;
						MemRD = `RBYTEU;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbimm;
						alucontrol = `ALUADD;
						immtype = `immI;
						RegwriteE = `RWRITE;
						RegwriteD = `MemRD;
					end
					`lhu:begin
						MemWriteE = `MNWRITE;
						MemValid = `VALID;
						MemWmask = `WWORD;
						MemRD = `RHALFWU;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbimm;
						alucontrol = `ALUADD;
						immtype = `immI;
						RegwriteE = `RWRITE;
						RegwriteD = `MemRD;
					end
					default:begin 
						RegwriteE = `RNWRITE;
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
					end
				endcase
			end
			`BRANCH:begin
				case(f3)
					`beq:begin
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbRD2;
						alucontrol = `ALUSUB;
						immtype = `immB;
						RegwriteE = `RNWRITE;
					end
					`bne:begin
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbRD2;
						alucontrol = `ALUSUB;
						immtype = `immB;
						RegwriteE = `RNWRITE;
					end
					`bge:begin
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbRD2;
						alucontrol = `ALUCMP;
						immtype = `immB;
						RegwriteE = `RNWRITE;
					end
					`blt:begin
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbRD2;
						alucontrol = `ALUCMP;
						immtype = `immB;
						RegwriteE = `RNWRITE;
					end
					`bltu:begin
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbRD2;
						alucontrol = `ALUCMPU;
						immtype = `immB;
						RegwriteE = `RNWRITE;
					end
					`bgeu:begin
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
						AluSrcA = `ALUaRD1;
						AluSrcB = `ALUbRD2;
						alucontrol = `ALUCMPU;
						immtype = `immB;
						RegwriteE = `RNWRITE;
					end
					default:begin 
						RegwriteE = `RNWRITE;
						MemWriteE = `MNWRITE;
						MemValid = `INVALID;
					end
				endcase
			end
			`auipc:begin
				MemWriteE = `MNWRITE;
				MemValid = `INVALID;
				AluSrcA = `ALUaPC;
				AluSrcB = `ALUbimm;
				alucontrol = `ALUADD;
				RegwriteE = `RWRITE;
				immtype = `immU;
				RegwriteD = `Aluresult;
			end
			`lui:begin
				MemWriteE = `MNWRITE;
				MemValid = `INVALID;
				RegwriteE = `RWRITE;
				immtype = `immU;
				RegwriteD = `imm;
			end
			`jal:begin
				MemWriteE = `MNWRITE;
				MemValid = `INVALID;
				AluSrcA = `ALUaPC;
				AluSrcB = `ALUbimm;
				alucontrol = `ALUADD;
				RegwriteE = `RWRITE;
				immtype = `immJ;
				RegwriteD = `pc_plus_4_2;
			end
			`jalr:begin
				MemWriteE = `MNWRITE;
				MemValid = `INVALID;
				AluSrcA = `ALUaRD1;
				AluSrcB = `ALUbimm;
				alucontrol = `ALUADD;
				RegwriteE = `RWRITE;
				immtype = `immI;
				RegwriteD = `pc_plus_4_2;
			end
			default:begin 
				RegwriteE = `RNWRITE;
				MemWriteE = `MNWRITE;
				MemValid = `INVALID;
			end
		endcase
	end
endmodule
