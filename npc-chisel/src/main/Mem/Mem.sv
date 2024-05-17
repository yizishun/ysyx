module Imem(
    input clk,
    input rst,
    input validPC,
    input [31:0] pc,
    output reg validInst,
    output reg [31:0] inst
);
    import "DPI-C" function int pmem_read(input int raddr);
    reg [31:0]temp_inst;
    always @(posedge clk) begin
        if(rst) begin
            validInst <= 0;
        end
        if(validPC) begin
            temp_inst =  pmem_read(pc);
        end
        inst <= temp_inst;
        validInst <= (~rst) &(validPC);
    end
endmodule

module Dmem(
    input clk,
    input rst,
    input wen,
    input valid,
    input [31:0] raddr,
    input [31:0] waddr,
    input [31:0] wdata, 
    input [7:0] wmask,
    output reg validData,
    output reg[31:0]rdata
);
    import "DPI-C" function int pmem_read(input int raddr);
    import "DPI-C" function void pmem_write(
    input int waddr, input int wdata, input byte wmask);
    reg [31:0]temp_rdata;
    always @(posedge clk) begin
        if (valid) begin // 有读写请求时
            temp_rdata = pmem_read(raddr);
            if (wen) begin // 有写请求时
                pmem_write(waddr, wdata, wmask);
            end
        end
        rdata <= temp_rdata;
        validData <= (~rst) & (valid);
end
endmodule
