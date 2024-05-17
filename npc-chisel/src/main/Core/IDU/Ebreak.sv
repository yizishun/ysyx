module Ebreak(
  input [31:0]inst
);
  import "DPI-C" function void npc_trap();
  always @(*) begin
    if(inst == 32'b00000000000100000000000001110011)
      npc_trap();
  end
endmodule