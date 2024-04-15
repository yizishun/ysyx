module ysyx_23060171_wbu(
    //from LSU
    input [31:0]rd1W,
    input [31:0]aluresultW,
    input [31:0]crd1W,
    input [31:0]pcW,
    input [31:0]immextW,
    input [31:0]pc_plus_4W,
    input [31:0]MemRW,
    input [4:0]rwW,
	input [11:0]crwW,
    //control signal from LSU
    input irqW,
    input [2:0]RegwriteDW,
    input [1:0]CSRWriteDW,
    input RegwriteEW,
	input CSRWriteEW,
    //to IDU
    output [31:0]WDW,
    output [31:0]CWDW,
    output [4:0]rwD,
	output [11:0]crwD,
    output RegwriteED,
	output CSRWriteED
);
    assign rwD = rwW;
    assign crwD = crwW;
    assign RegwriteED = RegwriteEW;
    assign CSRWriteED = CSRWriteEW;
    wire [1:0]CSRWriteD_irq;
	ysyx_23060171_MuxKey #(5,3,32) wdmux(WDW,RegwriteDW,{
		3'b000,aluresultW,
		3'b001,immextW,
		3'b010,pc_plus_4W,
		3'b011,MemRW,
		3'b100,crd1W
	});
	ysyx_23060171_MuxKey #(2,1,2) csr_wdirqmux(CSRWriteD_irq,irqW,{
		1'b0,CSRWriteDW,
		1'b1,2'b10
	});	
	ysyx_23060171_MuxKey #(3,2,32) csr_wdmux(CWDW,CSRWriteD_irq,{
		2'b00,rd1W,
		2'b01,rd1W|crd1W,
		2'b10,pcW
	});
endmodule