module Clint(
    input clk,
    input rst,
    //AR
    input [31:0]axi_araddr,
    input axi_arvalid,
    input [3:0]axi_arid,
    input [7:0]axi_arlen,
    input [2:0]axi_arsize,
    input [1:0]axi_arburst,
    output axi_arready,
    //R
    output reg [63:0] axi_rdata,
    output [1:0] axi_rresp,
    output axi_rvalid,
    output axi_rlast,
    output [3:0]axi_rid,
    input axi_rready,
    //AW
    input [31:0]axi_awaddr,
    input axi_awvalid,
    input [3:0]axi_awid,
    input [7:0]axi_awlen,
    input [2:0]axi_awsize,
    input [1:0]axi_awburst,
    output axi_awready,
    //W
    input [63:0] axi_wdata,
    input [7:0] axi_wstrb,
    input axi_wvalid,
    input axi_wlast,
    output axi_wready,
    //B
    output [1:0] axi_bresp,
    output axi_bvalid,
    output [3:0]axi_bid,
    input axi_bready
);
    import "DPI-C" function void skip();
    assign axi_rlast = 1'b1;
    assign axi_rid = 4'b0000;
    assign axi_bid = 4'b0000;
    //mtime reg: Provides the current timer value.
    reg [63:0]mtime;
    always @(posedge clk)begin
        if(rst)begin
            mtime <= 64'h0;
        end
        else begin
            mtime <= mtime + 1;
        end
    end
//Read
    reg axi_arready_r;
    reg [1:0]axi_rresp_r;
    reg axi_rvalid_r;
    assign axi_arready = axi_arready_r;
    assign axi_rresp = axi_rresp_r;
    assign axi_rvalid = axi_rvalid_r;
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
                nextStateR = axi_arvalid & axi_arready_r ? sr_betweenFire12:sr_beforeFire1;
            end
            sr_betweenFire12:begin
                nextStateR = axi_rvalid_r & axi_rready ? sr_beforeFire1:sr_betweenFire12;
            end
            default:begin
                nextStateR = sr_beforeFire1;
            end
        endcase
    end

    always @(posedge clk) begin
        case(nextStateR)
            sr_beforeFire1:begin
                axi_arready_r <= 1'b1;
                axi_rresp_r <= 2'b00;
                axi_rvalid_r <= 1'b0;
                delayR <= lfsr;
            end
            sr_betweenFire12:begin
                axi_arready_r <= 1'b0;
                delayR <= delayR - 1;
                if(delayR == 0)begin
                    skip();
                    if(axi_araddr == 32'ha0000048)begin
                        axi_rdata <= {{32'b0} , mtime[31:0]};
                        axi_rresp_r <= 2'b00;
                        axi_rvalid_r <= 1'b1;
                    end
                    else if(axi_araddr == 32'ha000004c)begin
                        axi_rdata <= {{32'b0}, mtime[63:32]};
                        axi_rresp_r <= 2'b00;
                        axi_rvalid_r <= 1'b1;
                    end
                    else begin
                        axi_rvalid_r <= 1'b1;
                        axi_rresp_r <= 2'b01;
                        $error("invalid addr:%h",axi_araddr);
                    end
                end
                else begin
                    axi_rvalid_r <= 1'b0;
                end
            end
            default:begin
                axi_arready_r <= 1'b1;
                axi_rresp_r <= 2'b00;
                axi_rvalid_r <= 1'b0;
            end
        endcase
    end
//Wirte   
    reg axi_awready_r;
    reg axi_wready_r;
    reg [1:0]axi_bresp_r;
    reg axi_bvalid_r;
    assign axi_awready = axi_awready_r;
    assign axi_wready = axi_wready_r;
    assign axi_bresp = axi_bresp_r;
    assign axi_bvalid = axi_bvalid_r;
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
                nextStateW = (axi_awvalid & axi_awready_r) & (axi_wvalid & axi_wready_r) ? sw_betweenFire12:sw_beforeFire1;
            end
            sw_betweenFire12:begin
                nextStateW = (axi_bvalid_r & axi_bready) ? sw_beforeFire1:sw_betweenFire12;
            end
        endcase
    end

    always @(posedge clk)begin
        case(nextStateW)
            sw_beforeFire1:begin
                axi_awready_r <= 1'b1;
                axi_wready_r <= 1'b1;
                axi_bresp_r <= 2'b00;
                axi_bvalid_r <= 1'b0;
                delayW <= lfsr;
            end
            sw_betweenFire12:begin
                axi_awready_r <= 1'b0;
                axi_wready_r <= 1'b0;
                axi_bresp_r <= 2'b00;
                delayW <= delayW - 1;
                if(delayW == 0)begin
                    axi_bvalid_r <= 1'b1;
                    skip();
                    $error("You cannot write to mtime");
                end
                else begin
                    axi_bvalid_r <= 1'b0;
                end
            end
        endcase
    end
endmodule
