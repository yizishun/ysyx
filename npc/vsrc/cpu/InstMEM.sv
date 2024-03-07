module ysyx_23060171_inst_memory(
    input valid,
    input [31:0] pc,
    output reg [31:0] inst
);
    import "DPI-C" function int pmem_read(input int raddr);
    always @(*) begin
        if(~valid) begin
        inst =  pmem_read(pc);
        end
        else begin
            inst = 0;
        end
    end
endmodule