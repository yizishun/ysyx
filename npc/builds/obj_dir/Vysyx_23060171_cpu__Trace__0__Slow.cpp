// Verilated -*- C++ -*-
// DESCRIPTION: Verilator output: Tracing implementation internals
#include "verilated_vcd_c.h"
#include "Vysyx_23060171_cpu__Syms.h"


VL_ATTR_COLD void Vysyx_23060171_cpu___024root__trace_init_sub__TOP__0(Vysyx_23060171_cpu___024root* vlSelf, VerilatedVcd* tracep) {
    if (false && vlSelf) {}  // Prevent unused
    Vysyx_23060171_cpu__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vysyx_23060171_cpu___024root__trace_init_sub__TOP__0\n"); );
    // Init
    const int c = vlSymsp->__Vm_baseCode;
    // Body
    tracep->declBus(c+66,"inst", false,-1, 31,0);
    tracep->declBit(c+67,"clk", false,-1);
    tracep->declBit(c+68,"rst", false,-1);
    tracep->declBus(c+69,"pc", false,-1, 31,0);
    tracep->pushNamePrefix("ysyx_23060171_cpu ");
    tracep->declBus(c+66,"inst", false,-1, 31,0);
    tracep->declBit(c+67,"clk", false,-1);
    tracep->declBit(c+68,"rst", false,-1);
    tracep->declBus(c+69,"pc", false,-1, 31,0);
    tracep->declBus(c+70,"next_pc", false,-1, 31,0);
    tracep->declBus(c+80,"alucontrol", false,-1, 2,0);
    tracep->declBit(c+81,"Regwrite", false,-1);
    tracep->declBus(c+80,"immtype", false,-1, 2,0);
    tracep->declBus(c+6,"aluresult", false,-1, 31,0);
    tracep->declBus(c+7,"rd1", false,-1, 31,0);
    tracep->declBus(c+71,"rd2", false,-1, 31,0);
    tracep->declBus(c+72,"immext", false,-1, 31,0);
    tracep->declBus(c+8,"flag", false,-1, 3,0);
    tracep->pushNamePrefix("addpc ");
    tracep->declBus(c+69,"pc", false,-1, 31,0);
    tracep->declBus(c+70,"nextpc", false,-1, 31,0);
    tracep->popNamePrefix(1);
    tracep->pushNamePrefix("alu ");
    tracep->declBus(c+7,"a", false,-1, 31,0);
    tracep->declBus(c+72,"b", false,-1, 31,0);
    tracep->declBus(c+80,"alucontrol", false,-1, 2,0);
    tracep->declBus(c+6,"result", false,-1, 31,0);
    tracep->declBit(c+9,"of", false,-1);
    tracep->declBit(c+10,"zf", false,-1);
    tracep->declBit(c+11,"nf", false,-1);
    tracep->declBit(c+12,"cf", false,-1);
    tracep->declBus(c+13,"raddsub", false,-1, 31,0);
    tracep->declBus(c+14,"reand", false,-1, 31,0);
    tracep->declBus(c+15,"reor", false,-1, 31,0);
    tracep->declBus(c+72,"b2", false,-1, 31,0);
    tracep->pushNamePrefix("alumux ");
    tracep->declBus(c+82,"NR_KEY", false,-1, 31,0);
    tracep->declBus(c+83,"KEY_LEN", false,-1, 31,0);
    tracep->declBus(c+84,"DATA_LEN", false,-1, 31,0);
    tracep->declBus(c+6,"out", false,-1, 31,0);
    tracep->declBus(c+80,"key", false,-1, 2,0);
    tracep->declArray(c+16,"lut", false,-1, 139,0);
    tracep->pushNamePrefix("i0 ");
    tracep->declBus(c+82,"NR_KEY", false,-1, 31,0);
    tracep->declBus(c+83,"KEY_LEN", false,-1, 31,0);
    tracep->declBus(c+84,"DATA_LEN", false,-1, 31,0);
    tracep->declBus(c+85,"HAS_DEFAULT", false,-1, 31,0);
    tracep->declBus(c+6,"out", false,-1, 31,0);
    tracep->declBus(c+80,"key", false,-1, 2,0);
    tracep->declBus(c+86,"default_out", false,-1, 31,0);
    tracep->declArray(c+16,"lut", false,-1, 139,0);
    tracep->declBus(c+87,"PAIR_LEN", false,-1, 31,0);
    for (int i = 0; i < 4; ++i) {
        tracep->declQuad(c+21+i*2,"pair_list", true,(i+0), 34,0);
    }
    for (int i = 0; i < 4; ++i) {
        tracep->declBus(c+1+i*1,"key_list", true,(i+0), 2,0);
    }
    for (int i = 0; i < 4; ++i) {
        tracep->declBus(c+29+i*1,"data_list", true,(i+0), 31,0);
    }
    tracep->declBus(c+33,"lut_out", false,-1, 31,0);
    tracep->declBit(c+5,"hit", false,-1);
    tracep->declBus(c+88,"i", false,-1, 31,0);
    tracep->pushNamePrefix("genblk1 ");
    tracep->popNamePrefix(4);
    tracep->pushNamePrefix("gpr ");
    tracep->declBus(c+89,"ADDR_WIDTH", false,-1, 31,0);
    tracep->declBus(c+84,"DATA_WIDTH", false,-1, 31,0);
    tracep->declBit(c+67,"clk", false,-1);
    tracep->declBus(c+6,"wdata", false,-1, 31,0);
    tracep->declBus(c+73,"waddr", false,-1, 4,0);
    tracep->declBit(c+81,"wen", false,-1);
    tracep->declBus(c+74,"raddr1", false,-1, 4,0);
    tracep->declBus(c+75,"raddr2", false,-1, 4,0);
    tracep->declBus(c+7,"rdata1", false,-1, 31,0);
    tracep->declBus(c+71,"rdata2", false,-1, 31,0);
    for (int i = 0; i < 32; ++i) {
        tracep->declBus(c+34+i*1,"rf", true,(i+0), 31,0);
    }
    tracep->popNamePrefix(1);
    tracep->pushNamePrefix("idu ");
    tracep->declBus(c+76,"opcode", false,-1, 6,0);
    tracep->declBus(c+77,"f3", false,-1, 2,0);
    tracep->declBus(c+78,"f7", false,-1, 6,0);
    tracep->declBus(c+79,"f12", false,-1, 11,0);
    tracep->declBus(c+80,"alucontrol", false,-1, 2,0);
    tracep->declBit(c+81,"Regwrite", false,-1);
    tracep->declBus(c+80,"immtype", false,-1, 2,0);
    tracep->popNamePrefix(1);
    tracep->pushNamePrefix("imm ");
    tracep->declBus(c+66,"inst", false,-1, 31,0);
    tracep->declBus(c+80,"immtype", false,-1, 2,0);
    tracep->declBus(c+72,"immext", false,-1, 31,0);
    tracep->declBus(c+72,"immI", false,-1, 31,0);
    tracep->popNamePrefix(1);
    tracep->pushNamePrefix("pc2 ");
    tracep->declBit(c+67,"clk", false,-1);
    tracep->declBit(c+68,"rst", false,-1);
    tracep->declBus(c+70,"next_pc", false,-1, 31,0);
    tracep->declBus(c+69,"pc", false,-1, 31,0);
    tracep->pushNamePrefix("pc1 ");
    tracep->declBus(c+84,"WIDTH", false,-1, 31,0);
    tracep->declBus(c+90,"RESET_VAL", false,-1, 31,0);
    tracep->declBit(c+67,"clk", false,-1);
    tracep->declBit(c+68,"rst", false,-1);
    tracep->declBus(c+70,"din", false,-1, 31,0);
    tracep->declBus(c+69,"dout", false,-1, 31,0);
    tracep->declBit(c+81,"wen", false,-1);
    tracep->popNamePrefix(3);
}

VL_ATTR_COLD void Vysyx_23060171_cpu___024root__trace_init_top(Vysyx_23060171_cpu___024root* vlSelf, VerilatedVcd* tracep) {
    if (false && vlSelf) {}  // Prevent unused
    Vysyx_23060171_cpu__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vysyx_23060171_cpu___024root__trace_init_top\n"); );
    // Body
    Vysyx_23060171_cpu___024root__trace_init_sub__TOP__0(vlSelf, tracep);
}

VL_ATTR_COLD void Vysyx_23060171_cpu___024root__trace_full_top_0(void* voidSelf, VerilatedVcd::Buffer* bufp);
void Vysyx_23060171_cpu___024root__trace_chg_top_0(void* voidSelf, VerilatedVcd::Buffer* bufp);
void Vysyx_23060171_cpu___024root__trace_cleanup(void* voidSelf, VerilatedVcd* /*unused*/);

VL_ATTR_COLD void Vysyx_23060171_cpu___024root__trace_register(Vysyx_23060171_cpu___024root* vlSelf, VerilatedVcd* tracep) {
    if (false && vlSelf) {}  // Prevent unused
    Vysyx_23060171_cpu__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vysyx_23060171_cpu___024root__trace_register\n"); );
    // Body
    tracep->addFullCb(&Vysyx_23060171_cpu___024root__trace_full_top_0, vlSelf);
    tracep->addChgCb(&Vysyx_23060171_cpu___024root__trace_chg_top_0, vlSelf);
    tracep->addCleanupCb(&Vysyx_23060171_cpu___024root__trace_cleanup, vlSelf);
}

VL_ATTR_COLD void Vysyx_23060171_cpu___024root__trace_full_sub_0(Vysyx_23060171_cpu___024root* vlSelf, VerilatedVcd::Buffer* bufp);

VL_ATTR_COLD void Vysyx_23060171_cpu___024root__trace_full_top_0(void* voidSelf, VerilatedVcd::Buffer* bufp) {
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vysyx_23060171_cpu___024root__trace_full_top_0\n"); );
    // Init
    Vysyx_23060171_cpu___024root* const __restrict vlSelf VL_ATTR_UNUSED = static_cast<Vysyx_23060171_cpu___024root*>(voidSelf);
    Vysyx_23060171_cpu__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    // Body
    Vysyx_23060171_cpu___024root__trace_full_sub_0((&vlSymsp->TOP), bufp);
}

VL_ATTR_COLD void Vysyx_23060171_cpu___024root__trace_full_sub_0(Vysyx_23060171_cpu___024root* vlSelf, VerilatedVcd::Buffer* bufp) {
    if (false && vlSelf) {}  // Prevent unused
    Vysyx_23060171_cpu__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vysyx_23060171_cpu___024root__trace_full_sub_0\n"); );
    // Init
    uint32_t* const oldp VL_ATTR_UNUSED = bufp->oldp(vlSymsp->__Vm_baseCode);
    VlWide<5>/*159:0*/ __Vtemp_h85ff9197__0;
    // Body
    bufp->fullCData(oldp+1,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__key_list[0]),3);
    bufp->fullCData(oldp+2,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__key_list[1]),3);
    bufp->fullCData(oldp+3,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__key_list[2]),3);
    bufp->fullCData(oldp+4,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__key_list[3]),3);
    bufp->fullBit(oldp+5,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__hit));
    bufp->fullIData(oldp+6,(vlSelf->ysyx_23060171_cpu__DOT__aluresult),32);
    bufp->fullIData(oldp+7,(vlSelf->ysyx_23060171_cpu__DOT__rd1),32);
    bufp->fullCData(oldp+8,(((8U & ((IData)((1ULL & 
                                             (((QData)((IData)(vlSelf->ysyx_23060171_cpu__DOT__rd1)) 
                                               + (QData)((IData)(vlSelf->ysyx_23060171_cpu__DOT__immext))) 
                                              >> 0x20U))) 
                                    << 3U)) | ((4U 
                                                & (vlSelf->ysyx_23060171_cpu__DOT__aluresult 
                                                   >> 0x1dU)) 
                                               | ((2U 
                                                   & ((~ (IData)(
                                                                 (0U 
                                                                  != vlSelf->ysyx_23060171_cpu__DOT__aluresult))) 
                                                      << 1U)) 
                                                  | (1U 
                                                     & ((~ 
                                                         ((vlSelf->ysyx_23060171_cpu__DOT__rd1 
                                                           ^ vlSelf->ysyx_23060171_cpu__DOT__immext) 
                                                          >> 0x1fU)) 
                                                        & ((vlSelf->ysyx_23060171_cpu__DOT__rd1 
                                                            ^ vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__raddsub) 
                                                           >> 0x1fU))))))),4);
    bufp->fullBit(oldp+9,((1U & ((~ ((vlSelf->ysyx_23060171_cpu__DOT__rd1 
                                      ^ vlSelf->ysyx_23060171_cpu__DOT__immext) 
                                     >> 0x1fU)) & (
                                                   (vlSelf->ysyx_23060171_cpu__DOT__rd1 
                                                    ^ vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__raddsub) 
                                                   >> 0x1fU)))));
    bufp->fullBit(oldp+10,((1U & (~ (IData)((0U != vlSelf->ysyx_23060171_cpu__DOT__aluresult))))));
    bufp->fullBit(oldp+11,((vlSelf->ysyx_23060171_cpu__DOT__aluresult 
                            >> 0x1fU)));
    bufp->fullBit(oldp+12,((1U & (IData)((1ULL & (((QData)((IData)(vlSelf->ysyx_23060171_cpu__DOT__rd1)) 
                                                   + (QData)((IData)(vlSelf->ysyx_23060171_cpu__DOT__immext))) 
                                                  >> 0x20U))))));
    bufp->fullIData(oldp+13,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__raddsub),32);
    bufp->fullIData(oldp+14,((vlSelf->ysyx_23060171_cpu__DOT__rd1 
                              ^ vlSelf->ysyx_23060171_cpu__DOT__immext)),32);
    bufp->fullIData(oldp+15,((vlSelf->ysyx_23060171_cpu__DOT__rd1 
                              | vlSelf->ysyx_23060171_cpu__DOT__immext)),32);
    __Vtemp_h85ff9197__0[0U] = (IData)((0x300000000ULL 
                                        | (QData)((IData)(
                                                          (vlSelf->ysyx_23060171_cpu__DOT__rd1 
                                                           | vlSelf->ysyx_23060171_cpu__DOT__immext)))));
    __Vtemp_h85ff9197__0[1U] = (((vlSelf->ysyx_23060171_cpu__DOT__rd1 
                                  ^ vlSelf->ysyx_23060171_cpu__DOT__immext) 
                                 << 3U) | (IData)((
                                                   (0x300000000ULL 
                                                    | (QData)((IData)(
                                                                      (vlSelf->ysyx_23060171_cpu__DOT__rd1 
                                                                       | vlSelf->ysyx_23060171_cpu__DOT__immext)))) 
                                                   >> 0x20U)));
    __Vtemp_h85ff9197__0[2U] = (0x10U | ((vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__raddsub 
                                          << 6U) | 
                                         ((vlSelf->ysyx_23060171_cpu__DOT__rd1 
                                           ^ vlSelf->ysyx_23060171_cpu__DOT__immext) 
                                          >> 0x1dU)));
    __Vtemp_h85ff9197__0[3U] = (0x40U | ((vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__raddsub 
                                          << 9U) | 
                                         (vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__raddsub 
                                          >> 0x1aU)));
    __Vtemp_h85ff9197__0[4U] = (vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__raddsub 
                                >> 0x17U);
    bufp->fullWData(oldp+16,(__Vtemp_h85ff9197__0),140);
    bufp->fullQData(oldp+21,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__pair_list[0]),35);
    bufp->fullQData(oldp+23,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__pair_list[1]),35);
    bufp->fullQData(oldp+25,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__pair_list[2]),35);
    bufp->fullQData(oldp+27,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__pair_list[3]),35);
    bufp->fullIData(oldp+29,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__data_list[0]),32);
    bufp->fullIData(oldp+30,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__data_list[1]),32);
    bufp->fullIData(oldp+31,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__data_list[2]),32);
    bufp->fullIData(oldp+32,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__data_list[3]),32);
    bufp->fullIData(oldp+33,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__lut_out),32);
    bufp->fullIData(oldp+34,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[0]),32);
    bufp->fullIData(oldp+35,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[1]),32);
    bufp->fullIData(oldp+36,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[2]),32);
    bufp->fullIData(oldp+37,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[3]),32);
    bufp->fullIData(oldp+38,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[4]),32);
    bufp->fullIData(oldp+39,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[5]),32);
    bufp->fullIData(oldp+40,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[6]),32);
    bufp->fullIData(oldp+41,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[7]),32);
    bufp->fullIData(oldp+42,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[8]),32);
    bufp->fullIData(oldp+43,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[9]),32);
    bufp->fullIData(oldp+44,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[10]),32);
    bufp->fullIData(oldp+45,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[11]),32);
    bufp->fullIData(oldp+46,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[12]),32);
    bufp->fullIData(oldp+47,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[13]),32);
    bufp->fullIData(oldp+48,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[14]),32);
    bufp->fullIData(oldp+49,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[15]),32);
    bufp->fullIData(oldp+50,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[16]),32);
    bufp->fullIData(oldp+51,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[17]),32);
    bufp->fullIData(oldp+52,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[18]),32);
    bufp->fullIData(oldp+53,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[19]),32);
    bufp->fullIData(oldp+54,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[20]),32);
    bufp->fullIData(oldp+55,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[21]),32);
    bufp->fullIData(oldp+56,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[22]),32);
    bufp->fullIData(oldp+57,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[23]),32);
    bufp->fullIData(oldp+58,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[24]),32);
    bufp->fullIData(oldp+59,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[25]),32);
    bufp->fullIData(oldp+60,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[26]),32);
    bufp->fullIData(oldp+61,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[27]),32);
    bufp->fullIData(oldp+62,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[28]),32);
    bufp->fullIData(oldp+63,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[29]),32);
    bufp->fullIData(oldp+64,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[30]),32);
    bufp->fullIData(oldp+65,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[31]),32);
    bufp->fullIData(oldp+66,(vlSelf->inst),32);
    bufp->fullBit(oldp+67,(vlSelf->clk));
    bufp->fullBit(oldp+68,(vlSelf->rst));
    bufp->fullIData(oldp+69,(vlSelf->pc),32);
    bufp->fullIData(oldp+70,(((IData)(4U) + vlSelf->pc)),32);
    bufp->fullIData(oldp+71,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf
                             [(0x1fU & (vlSelf->inst 
                                        >> 0x14U))]),32);
    bufp->fullIData(oldp+72,(vlSelf->ysyx_23060171_cpu__DOT__immext),32);
    bufp->fullCData(oldp+73,((0x1fU & (vlSelf->inst 
                                       >> 7U))),5);
    bufp->fullCData(oldp+74,((0x1fU & (vlSelf->inst 
                                       >> 0xfU))),5);
    bufp->fullCData(oldp+75,((0x1fU & (vlSelf->inst 
                                       >> 0x14U))),5);
    bufp->fullCData(oldp+76,((0x7fU & vlSelf->inst)),7);
    bufp->fullCData(oldp+77,((7U & (vlSelf->inst >> 0xcU))),3);
    bufp->fullCData(oldp+78,((vlSelf->inst >> 0x19U)),7);
    bufp->fullSData(oldp+79,((vlSelf->inst >> 0x14U)),12);
    bufp->fullCData(oldp+80,(0U),3);
    bufp->fullBit(oldp+81,(1U));
    bufp->fullIData(oldp+82,(4U),32);
    bufp->fullIData(oldp+83,(3U),32);
    bufp->fullIData(oldp+84,(0x20U),32);
    bufp->fullIData(oldp+85,(0U),32);
    bufp->fullIData(oldp+86,(0U),32);
    bufp->fullIData(oldp+87,(0x23U),32);
    bufp->fullIData(oldp+88,(4U),32);
    bufp->fullIData(oldp+89,(5U),32);
    bufp->fullIData(oldp+90,(0x80000000U),32);
}
