`define MSTATUS 12'h300
`define MTVEC   12'h305
`define MCAUSE  12'h342
`define MEPC    12'h341
module ysyx_23060171_csr #(ADDR_WIDTH = 12, DATA_WIDTH = 32) (
  input clk,
  input irq,
  input [7:0]irq_no,
  input [DATA_WIDTH-1:0] wdata,
  input [ADDR_WIDTH-1:0] waddr,
  input wen,
  input [ADDR_WIDTH-1:0] raddr1,
  output [DATA_WIDTH-1:0] rdata1,
  output [DATA_WIDTH-1:0] mtvec,
  output [DATA_WIDTH-1:0] mepc
);
  reg [DATA_WIDTH-1:0] rf [3:0];
  wire [1:0]waddr_in;
  wire [1:0]raddr_in;
  ysyx_23060171_MuxKey #(4, 12, 2)waddr_mux(waddr_in,waddr,{
    `MSTATUS,2'b00,
    `MTVEC  ,2'b01,
    `MCAUSE ,2'b10,
    `MEPC   ,2'b11
  });
  ysyx_23060171_MuxKey #(4, 12, 2)raddr_mux(raddr_in,raddr1,{
    `MSTATUS,2'b00,
    `MTVEC  ,2'b01,
    `MCAUSE ,2'b10,
    `MEPC   ,2'b11
  });
  always @(posedge clk) begin
    if (wen) begin 
      rf[waddr_in] <= wdata;
    end
    if (irq) begin
      rf[2] <= {{24'b0},irq_no};
      rf[3] <= wdata;
    end
  end
  assign rf[0] = 32'h1800;
	assign rdata1 = rf[raddr_in];
  assign mtvec = rf[1];
  assign mepc = rf[3];
endmodule
