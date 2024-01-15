module ysyx_23060171_idu(
	input [6:0]opcode,
	input [2:0]f3,
	input [6:0]f7,
	input [11:0]f12,
	output [2:0]alucontrol,
	output Regwrite,
	output [2:0]immtype
);
	import "DPI-C" function void npc_trap();
	assign alucontrol = 3'b000;
	assign Regwrite = 1;
	assign immtype = 3'b000;
	always @(*) begin
		if(opcode == 7'b1110011 && f12 == 12'b000000000001)begin
			npc_trap();
			end
	end
endmodule
