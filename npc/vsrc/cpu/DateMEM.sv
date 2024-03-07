module ysyx_23060171_data_memory(
    input clk,
    input wen,
    input valid,
    input [31:0] addr,
    input [31:0] wdata,
    input [3:0] wmask,
    output reg [31:0] rdata
);
    import "DPI-C" function int pmem_read(input int raddr);
    import "DPI-C" function void pmem_write(
    input int waddr, input int wdata, input byte wmask);
    reg [31:0] rdata_buf;
    always @(*) begin
        if (valid) begin // 有读写请求时
            rdata_buf = pmem_read(raddr);
            rdata = rdata_buf;
            if (wen) begin // 有写请求时
                pmem_write(waddr, wdata, wmask);
            end
  end
  else begin
    rdata_buf = 0;
  end
end
endmodule
