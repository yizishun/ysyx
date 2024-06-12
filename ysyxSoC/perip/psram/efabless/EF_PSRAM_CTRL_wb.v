/*
	Copyright 2020 Efabless Corp.

	Author: Mohamed Shalan (mshalan@efabless.com)

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at:
	http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

`timescale              1ns/1ps
`default_nettype        none

// Using EBH Command
module EF_PSRAM_CTRL_wb (
    // WB bus Interface
    input   wire        clk_i,
    input   wire        rst_i,
    input   wire [31:0] adr_i,
    input   wire [31:0] dat_i,
    output  wire [31:0] dat_o,
    input   wire [3:0]  sel_i,
    input   wire        cyc_i,
    input   wire        stb_i,
    output  wire        ack_o,
    input   wire        we_i,

    // External Interface to Quad I/O
    output  reg            sck,
    output  reg            ce_n,
    input   wire [3:0]      din,
    output  reg [3:0]      dout,
    output  reg [3:0]      douten
);

    localparam  ST_IDLE = 2'b00,
                ST_WAIT = 2'b01,
                ST_QPI  = 2'b10;

    wire        mr_sck;
    wire        mr_ce_n;
    wire [3:0]  mr_din;
    wire [3:0]  mr_dout;
    wire        mr_doe;

    wire        mw_sck;
    wire        mw_ce_n;
    wire [3:0]  mw_din;
    wire [3:0]  mw_dout;
    wire        mw_doe;

    // PSRAM Reader and Writer wires
    wire        mr_rd;
    wire        mr_done;
    wire        mw_wr;
    wire        mw_done;
    //QPI mode
    reg        sck_temp;
    reg        ce_n_temp;
    wire        [4:0]FINAL_COUNT = 5'b00111;
    wire        qpi = 1'b1;
    wire        qpiDone = (qpiCounter == FINAL_COUNT + 1);
    reg         [4:0]qpiCounter;
    wire[7:0]   CMD_35H = 8'h35;

    //wire        doe;

    // WB Control Signals
    wire        wb_valid        =   cyc_i & stb_i;
    wire        wb_we           =   we_i & wb_valid;
    wire        wb_re           =   ~we_i & wb_valid;
    //wire[3:0]   wb_byte_sel     =   sel_i & {4{wb_we}};

    // The FSM
    reg         [1:0]state, nstate;
    always @ (posedge clk_i or posedge rst_i)
        if(rst_i)
            state <= ST_QPI;
        else
            state <= nstate;

    always @* begin
        case(state)
            ST_QPI  :
                if(qpiDone)
                    nstate = ST_IDLE;
                else
                    nstate = ST_QPI;

            ST_IDLE :
                if(wb_valid)
                    nstate = ST_WAIT;
                else
                    nstate = ST_IDLE;

            ST_WAIT :
                if((mw_done & wb_we) | (mr_done & wb_re))
                    nstate = ST_IDLE;
                else
                    nstate = ST_WAIT;
            default : nstate = ST_IDLE;
        endcase
    end
    //QPI mode
    always @(posedge clk_i or posedge rst_i)begin
        if(rst_i)
            qpiCounter <= 5'b0;
        else if(state == ST_QPI & sck & !qpiDone)
            qpiCounter <= qpiCounter + 1;
    end

    wire [2:0]  size =  (sel_i == 4'b0001) ? 1 :
                        (sel_i == 4'b0010) ? 1 :
                        (sel_i == 4'b0100) ? 1 :
                        (sel_i == 4'b1000) ? 1 :
                        (sel_i == 4'b0011) ? 2 :
                        (sel_i == 4'b1100) ? 2 :
                        (sel_i == 4'b1111) ? 4 : 4;



    wire [7:0]  byte0 = (sel_i[0])          ? dat_i[7:0]   :
                        (sel_i[1] & size==1)? dat_i[15:8]  :
                        (sel_i[2] & size==1)? dat_i[23:16] :
                        (sel_i[3] & size==1)? dat_i[31:24] :
                        (sel_i[2] & size==2)? dat_i[23:16] :
                        dat_i[7:0];

    wire [7:0]  byte1 = (sel_i[1])          ? dat_i[15:8]  :
                        dat_i[31:24];

    wire [7:0]  byte2 = dat_i[23:16];

    wire [7:0]  byte3 = dat_i[31:24];

    wire [31:0] wdata = {byte3, byte2, byte1, byte0};

    /*
    wire [1:0]  waddr = (size==1 && sel_i[0]==1) ? 2'b00 :
                        (size==1 && sel_i[1]==1) ? 2'b01 :
                        (size==1 && sel_i[2]==1) ? 2'b10 :
                        (size==1 && sel_i[3]==1) ? 2'b11 :
                        (size==2 && sel_i[2]==1) ? 2'b10 :
                        2'b00;
                      */

    assign mr_rd    = ( (state==ST_IDLE ) & wb_re );
    assign mw_wr    = ( (state==ST_IDLE ) & wb_we );

    PSRAM_READER MR (
        .clk(clk_i),
        .rst_n(~rst_i),
        .qpi(qpi),
        .addr({adr_i[23:2],2'b0}),
        .rd(mr_rd),
        //.size(size), Always read a word
        .size(3'd4),
        .done(mr_done),
        .line(dat_o),
        .sck(mr_sck),
        .ce_n(mr_ce_n),
        .din(mr_din),
        .dout(mr_dout),
        .douten(mr_doe)
    );

    PSRAM_WRITER MW (
        .clk(clk_i),
        .rst_n(~rst_i),
        .qpi(qpi),
        .addr({adr_i[23:0]}),
        .wr(mw_wr),
        .size(size),
        .done(mw_done),
        .line(wdata),
        .sck(mw_sck),
        .ce_n(mw_ce_n),
        .din(mw_din),
        .dout(mw_dout),
        .douten(mw_doe)
    );

    always @(*)begin
       if(state == ST_QPI)begin
            dout =  (qpiCounter == 0) ? {3'b0, CMD_35H[7]} :
                    (qpiCounter == 1) ? {3'b0, CMD_35H[6]} :
                    (qpiCounter == 2) ? {3'b0, CMD_35H[5]} :
                    (qpiCounter == 3) ? {3'b0, CMD_35H[4]} :
                    (qpiCounter == 4) ? {3'b0, CMD_35H[3]} :
                    (qpiCounter == 5) ? {3'b0, CMD_35H[2]} :
                    (qpiCounter == 6) ? {3'b0, CMD_35H[1]} :
                    (qpiCounter == 7) ? {3'b0, CMD_35H[0]} :4'h0;
            douten = {4{qpiCounter < 8}};
       end 
       else begin
            dout = wb_we ? mw_dout : mr_dout;
            douten  = wb_we ? {4{mw_doe}}  : {4{mr_doe}};
       end
    end
    always @(posedge clk_i or posedge rst_i)begin
        if(rst_i)
            ce_n_temp <= 1'b1;
        else if(state == ST_QPI)
            ce_n_temp <= 1'b0;
        else
            ce_n_temp <= wb_we ? mw_ce_n : mr_ce_n;
    end
    assign ce_n = (nstate == ST_QPI) ? ce_n_temp :
                              wb_we ? mw_ce_n : mr_ce_n;
    always @(posedge clk_i or posedge rst_i)begin
        if(rst_i)
            sck_temp <= 1'b0;
        else if(state == ST_QPI & !ce_n)begin
            sck_temp <= ~ sck_temp;
        end
        else begin
            sck_temp  <= wb_we ? mw_sck  : mr_sck;
        end
    end
    assign sck  = (nstate == ST_QPI) ? sck_temp : 
                              wb_we ? mw_sck  : mr_sck;

    assign mw_din = din;
    assign mr_din = din;
    assign ack_o = wb_we ? mw_done :mr_done ;
endmodule
