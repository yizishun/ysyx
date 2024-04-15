module ysyx_23060171_cpu(
        input clk,
        input rst
    );
    //EXU -> IFU
    wire [31:0]pc_plus_rs2EF;
    wire [31:0]pc_plus_immEF;
    wire [2:0]PCSrcEF;
    //IDU -> IFU
    wire [31:0]mtvecDF;
    wire [31:0]mepcDF;
    wire irqDF;
    //IFU -> IDU
    wire [31:0]instFD;
    wire [31:0]pc_plus_4FD;
    wire [31:0]pcFD;
    //WBU -> IDU
    wire [31:0]wdWD;
    wire [31:0]csr_wdWD;
    wire [4:0]rwWD;
    wire [11:0]crwWD;
    wire RegwriteEWD;
    wire CSRWriteEWD;
    //IDU -> EXU
    wire [31:0]rd1DE;
    wire [31:0]rd2DE;
    wire [31:0]crd1DE;
    wire [31:0]pcDE;
    wire [31:0]pc_plus_4DE;
    wire [31:0]immextDE;
    wire irqDE;
    wire [4:0]rwDE;
    wire [11:0]crwDE;
    wire [2:0]RegwriteDDE;
    wire [1:0]CSRWriteDDE;
    wire RegwriteEDE;
    wire CSRWriteEDE;
    wire [2:0]MemRDDE;
    wire MemValidDE;
    wire [7:0]MemWmaskDE;
    wire MemWriteEDE;
    wire [3:0]alucontrolDE;
    wire [3:0]JumpDE;
    wire AluSrcADE;
    wire AluSrcBDE;
    //EXU -> LSU
    wire [31:0]aluresultES;
    wire [31:0]crd1ES;
    wire [31:0]pcES;
    wire [31:0]immextES;
    wire [31:0]pc_plus_4ES;
    wire [31:0]rd1ES;
    wire [31:0]rd2ES;
    wire irqES;
    wire [4:0]rwES;
    wire [11:0]crwES;
    wire [2:0]RegwriteDES;
    wire [1:0]CSRWriteDES;
    wire RegwriteEES;
    wire CSRWriteEES;
    wire [2:0]MemRDES;
    wire MemValidES;
    wire [7:0]MemWmaskES;
    wire MemWriteEES;
    //LSU to WBU
    wire [31:0]rd1LW;
    wire [31:0]aluresultLW;
    wire [31:0]crd1LW;
    wire [31:0]pcLW;
    wire [31:0]immextLW;
    wire [31:0]pc_plus_4LW;
    wire [31:0]MemRLW;
    wire irqLW;
    wire [4:0]rwLW;
    wire [11:0]crwLW;
    wire [2:0]RegwriteDLW;
    wire [1:0]CSRWriteDLW;
    wire RegwriteELW;
    wire CSRWriteELW;
    ysyx_23060171_ifu ifu(
                          .clk(clk),
                          .rst(rst),
                          //from EXU
                          .pc_plus_rs2(pc_plus_rs2EF),
                          .pc_plus_imm(pc_plus_immEF),
                          .PCSrc(PCSrcEF), //control signal
                          //from IDU
                          .mtvec(mtvecDF),
                          .mepc(mepcDF),
                          .irq(irqDF),  //control signal
                          //to IDU
                          .inst(instFD),
                          .pc_plus_4F(pc_plus_4FD),
                          .pcF(pcFD)
                      );
    ysyx_23060171_idu idu(
                          .clk(clk),
                          //from IFU
                          .inst(instFD),
                          .pcD(pcFD),
                          .pc_plus_4D(pc_plus_4FD),
                          //from WBU
                          .wd(wdWD),
                          .csr_wd(csr_wdWD),
                          .rwW(rwWD),
                          .crwW(crwWD),
                          .CSRWriteEW(CSRWriteEWD),
                          .RegwriteEW(RegwriteEWD),
                          //to EXU
                          .rd1(rd1DE),
                          .rd2(rd2DE),
                          .crd1(crd1DE),
                          .pcE(pcDE),
                          .pc_plus_4E(pc_plus_4DE),
                          .immextD(immextDE),
                          .rwE(rwDE),
                          .crwE(crwDE),
                          //control signals to EXU
                          .irqE(irqDE),
                          .irqF(irqDF),
                          .RegwriteD(RegwriteDDE),
                          .CSRWriteD(CSRWriteDDE),
                          .RegwriteE(RegwriteEDE),
                          .CSRWriteE(CSRWriteEDE),
                          .MemRD(MemRDDE),
                          .MemValid(MemValidDE),
                          .MemWmask(MemWmaskDE),
                          .MemWriteE(MemWriteEDE),
                          .alucontrol(alucontrolDE),
                          .Jump(JumpDE),
                          .AluSrcA(AluSrcADE),
                          .AluSrcB(AluSrcBDE),
                          //to IFU
                          .mtvec(mtvecDF),
                          .mepc(mepcDF)
                      );
    ysyx_23060171_exu exu(
                          //from IDU
                          .rd1E(rd1DE),
                          .rd2E(rd2DE),
                          .crd1E(crd1DE),
                          .pcE(pcDE),
                          .pc_plus_4E(pc_plus_4DE),
                          .immextE(immextDE),
                          .rwE(rwDE),
                          .crwE(crwDE),
                          //control signal from IDU
                          .irq(irqDE),
                          .RegwriteD(RegwriteDDE),
                          .CSRWriteD(CSRWriteDDE),
                          .RegwriteE(RegwriteEDE),
                          .CSRWriteE(CSRWriteEDE),
                          .MemRD(MemRDDE),
                          .MemValid(MemValidDE),
                          .MemWmask(MemWmaskDE),
                          .MemWriteE(MemWriteEDE),
                          .alucontrol(alucontrolDE),
                          .Jump(JumpDE),
                          .AluSrcA(AluSrcADE),
                          .AluSrcB(AluSrcBDE),
                          //to IFU
                          .pc_plus_imm(pc_plus_immEF),
                          .pc_plus_rs2(pc_plus_rs2EF),
                          //to LSU
                          .aluresult(aluresultES),
                          .crd1S(crd1ES),
                          .pcS(pcES),
                          .immextS(immextES),
                          .pc_plus_4S(pc_plus_4ES),
                          .rwS(rwES),
                          .crwS(crwES),
                          .rd1S(rd1ES),
                          .rd2S(rd2ES),
                          //control signal to LSU
                          .irqS(irqES),
                          .RegwriteDS(RegwriteDES),
                          .CSRWriteDS(CSRWriteDES),
                          .RegwriteES(RegwriteEES),
                          .CSRWriteES(CSRWriteEES),
                          .MemRDS(MemRDES),
                          .MemValidS(MemValidES),
                          .MemWmaskS(MemWmaskES),
                          .MemWriteES(MemWriteEES),
                          //control signal to IFU
                          .PCSrc(PCSrcEF)
                      );
    ysyx_23060171_lsu lsu(
                          .clk(clk),
                          //from EXU
                          .aluresult(aluresultES),
                          .crd1S(crd1ES),
                          .pcS(pcES),
                          .immextS(immextES),
                          .pc_plus_4S(pc_plus_4ES),
                          .rd1S(rd1ES),
                          .rd2S(rd2ES),
                          .rwS(rwES),
                          .crwS(crwES),
                          //control signal from EXU
                          .irqS(irqES),
                          .RegwriteDS(RegwriteDES),
                          .CSRWriteDS(CSRWriteDES),
                          .RegwriteES(RegwriteEES),
                          .CSRWriteES(CSRWriteEES),
                          .MemRDS(MemRDES),
                          .MemValidS(MemValidES),
                          .MemWmaskS(MemWmaskES),
                          .MemWriteES(MemWriteEES),
                          //to WBU
                          .rd1W(rd1LW),
                          .aluresultW(aluresultLW),
                          .crd1W(crd1LW),
                          .pcW(pcLW),
                          .immextW(immextLW),
                          .pc_plus_4W(pc_plus_4LW),
                          .MemRW(MemRLW),
                          .rwW(rwLW),
                          .crwW(crwLW),
                          //control signal to WBU
                          .irqW(irqLW),
                          .RegwriteDW(RegwriteDLW),
                          .CSRWriteDW(CSRWriteDLW),
                          .RegwriteEW(RegwriteELW),
                          .CSRWriteEW(CSRWriteELW)
                      );
    ysyx_23060171_wbu wbu(
                          //from LSU
                          .rd1W(rd1LW),
                          .aluresultW(aluresultLW),
                          .crd1W(crd1LW),
                          .pcW(pcLW),
                          .immextW(immextLW),
                          .pc_plus_4W(pc_plus_4LW),
                          .MemRW(MemRLW),
                          .rwW(rwLW),
                          .crwW(crwLW),
                          //control signal from LSU
                          .irqW(irqLW),
                          .RegwriteDW(RegwriteDLW),
                          .CSRWriteDW(CSRWriteDLW),
                          .RegwriteEW(RegwriteELW),
                          .CSRWriteEW(CSRWriteELW),
                          //to IDU
                          .WDW(wdWD),
                          .CWDW(csr_wdWD),
                          .rwD(rwWD),
                          .crwD(crwWD),
                          .RegwriteED(RegwriteEWD),
                          .CSRWriteED(CSRWriteEWD)
                      );
endmodule
