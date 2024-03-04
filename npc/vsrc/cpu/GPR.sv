module ysyx_23060171_gpr #(ADDR_WIDTH = 5, DATA_WIDTH = 32) (
  input clk,
  input [DATA_WIDTH-1:0] wdata,
  input [ADDR_WIDTH-1:0] waddr,
  input wen,
	input [ADDR_WIDTH-1:0] raddr1,
	input [ADDR_WIDTH-1:0] raddr2,
	output [DATA_WIDTH-1:0] rdata1,
	output [DATA_WIDTH-1:0] rdata2
);
  import "DPI-C" function void Get_reg(input logic [31:0] rf[31:0]);
  reg [DATA_WIDTH-1:0] rf [2**ADDR_WIDTH-1:0];
  always @(posedge clk) begin
    if (wen) begin 
      rf[waddr] <= wdata;
      Get_reg(rf);
    end
		rf[0] <= 32'b0;
  end
	assign rdata1 = rf[raddr1];
	assign rdata2 = rf[raddr2];
endmodule
