module ysyx_23060171_inst_memory(
    input [31:0] pc,
    output reg [31:0] inst
);
    import "DPI-C" function int pmem_read(input int raddr);
    always @(*) begin
        inst =  pmem_read(pc);
    end
endmodule