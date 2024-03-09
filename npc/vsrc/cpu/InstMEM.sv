module ysyx_23060171_inst_memory(
    input valid,
    input [31:0] pc,
    output reg [31:0] inst
);
    import "DPI-C" function int pmem_read(input int raddr);
    always_latch @(*) begin  // a latch to make sim env to record the inst
        if(valid) begin
            inst =  pmem_read(pc);
        end
    end
endmodule