module Ebreak(
  input isEbreak
);
  import "DPI-C" function void npc_trap();
  always @(*) begin
    if(isEbreak)
      npc_trap();
  end
endmodule
