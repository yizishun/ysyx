`define BRANCH         7'b1100011
`define SYSTEM         7'b1110011
`define jal            7'b1101111
`define jalr           7'b1100111
//BRANCH
`define beq            3'b000
`define bne            3'b001
`define blt            3'b100
`define bge            3'b101
`define bltu           3'b110
`define bgeu           3'b111
//SYSTEM
`define mret           12'b001100000010
//PCSrc
`define snpc           3'b000
`define dnpc           3'b001
`define dnpc_r         3'b010
`define mtvec          3'b011
`define mepc           3'b100
module ysyx_23060171_idupc(
    input [6:0]opcode,
	input [2:0]f3,
	input [6:0]f7,
	input [11:0]f12,
	input zf,
	input cmp,
    output reg [2:0]PCSrc
);
always @(*) begin
		case(opcode)
			`BRANCH: begin
				case(f3)
					`beq:begin
						PCSrc = zf ? `dnpc : `snpc;
					end
					`bne: begin
						PCSrc = zf ? `snpc : `dnpc;
					end
					`bge:begin
						PCSrc = cmp ? `snpc : `dnpc;
					end
					`blt:begin
						PCSrc = cmp ? `dnpc : `snpc;
					end
					`bltu:begin
						PCSrc = cmp ? `dnpc : `snpc;
					end
					`bgeu:begin
						PCSrc = cmp ? `snpc : `dnpc;
					end
                    default:PCSrc = `snpc;
                endcase
            end
            `jal: PCSrc = `dnpc;
            `jalr:PCSrc = `dnpc_r;
			`SYSTEM:
				case(f12)
					`mret:begin
						PCSrc = `mepc;
					end
					default:PCSrc = `snpc;
				endcase
            default:PCSrc = `snpc;
        endcase
end
endmodule