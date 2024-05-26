module Mem(
    input clk,
    input rst,
    //AR
    input [31:0]araddr,
    input arvalid,
    output arready,
    //R
    output reg [31:0] rdata,
    output [1:0] rresp,
    output rvalid,
    input rready,
    //AW
    input [31:0]awaddr,
    input awvalid,
    output awready,
    //W
    input [31:0] wdata,
    input [3:0] wstrb,
    input wvalid,
    output wready,
    //B
    output [1:0] bresp,
    output bvalid,
    input bready
);
    import "DPI-C" function int pmem_read(input int raddr);
    import "DPI-C" function void pmem_write(
    input int waddr, input int wdata, input byte wmask);
//Read
    reg arready_r;
    reg [1:0]rresp_r;
    reg rvalid_r;
    assign arready = arready_r;
    assign rresp = rresp_r;
    assign rvalid = rvalid_r;
    parameter sr_beforeFire1 = 1'b0;
    parameter sr_betweenFire12 = 1'b1;

    reg stateR;
    reg nextStateR;
    reg [3:0]delayR;
    reg [3:0]lfsr;

    always @(posedge clk or posedge rst) begin
        if (rst) begin
            lfsr <= 4'hF; // Initial value
        end else begin
            lfsr <= {lfsr[2:0], lfsr[3] ^ lfsr[2]};
        end
    end

    always @(posedge clk) begin
        if(rst) begin
            stateR <= sr_beforeFire1;
            delayR <= lfsr;
        end
        else begin
            stateR <= nextStateR;
        end
    end

    always @(*) begin
        case(stateR)
            sr_beforeFire1:begin
                nextStateR = arvalid & arready_r ? sr_betweenFire12:sr_beforeFire1;
            end
            sr_betweenFire12:begin
                nextStateR = rvalid_r & rready ? sr_beforeFire1:sr_betweenFire12;
            end
            default:begin
                nextStateR = sr_beforeFire1;
            end
        endcase
    end

    always @(posedge clk) begin
        case(nextStateR)
            sr_beforeFire1:begin
                arready_r <= 1'b1;
                rresp_r <= 2'b00;
                rvalid_r <= 1'b0;
                delayR <= lfsr;
            end
            sr_betweenFire12:begin
                arready_r <= 1'b0;
                rresp_r <= 2'b00;
                delayR <= delayR - 1;
                if(delayR == 0)begin
                    rvalid_r <= 1'b1;
                    rdata = pmem_read(araddr);
                end
                else begin
                    rvalid_r <= 1'b0;
                end
            end
            default:begin
                arready_r <= 1'b1;
                rresp_r <= 2'b00;
                rvalid_r <= 1'b0;
            end
        endcase
    end
//Wirte   
    reg awready_r;
    reg wready_r;
    reg [1:0]bresp_r;
    reg bvalid_r;
    assign awready = awready_r;
    assign wready = wready_r;
    assign bresp = bresp_r;
    assign bvalid = bvalid_r;
    parameter sw_beforeFire1 = 1'b0;
    parameter sw_betweenFire12 = 1'b1;

    reg stateW;
    reg nextStateW;
    reg [3:0]delayW;

    always @(posedge clk)begin
        if(rst)begin
            stateW <= sw_beforeFire1;
            delayW <= lfsr;
        end
        else begin
            stateW <= nextStateW;
        end
    end

    always @(*)begin
        case(stateW)
            sw_beforeFire1:begin
                nextStateW = (awvalid & awready_r) & (wvalid & wready_r) ? sw_betweenFire12:sw_beforeFire1;
            end
            sw_betweenFire12:begin
                nextStateW = (bvalid_r & bready) ? sw_beforeFire1:sw_betweenFire12;
            end
        endcase
    end

    always @(posedge clk)begin
        case(nextStateW)
            sw_beforeFire1:begin
                awready_r <= 1'b1;
                wready_r <= 1'b1;
                bresp_r <= 2'b00;
                bvalid_r <= 1'b0;
                delayW <= lfsr;
            end
            sw_betweenFire12:begin
                awready_r <= 1'b0;
                wready_r <= 1'b0;
                bresp_r <= 2'b00;
                delayW <= delayW - 1;
                if(delayW == 0)begin
                    bvalid_r <= 1'b1;
                    pmem_write(awaddr, wdata, {{4'b0000}, wstrb});
                end
                else begin
                    bvalid_r <= 1'b0;
                end
            end
        endcase
    end
endmodule
