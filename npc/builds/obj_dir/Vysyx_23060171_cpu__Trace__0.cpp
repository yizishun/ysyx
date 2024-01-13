// Verilated -*- C++ -*-
// DESCRIPTION: Verilator output: Tracing implementation internals
#include "verilated_vcd_c.h"
#include "Vysyx_23060171_cpu__Syms.h"


void Vysyx_23060171_cpu___024root__trace_chg_sub_0(Vysyx_23060171_cpu___024root* vlSelf, VerilatedVcd::Buffer* bufp);

void Vysyx_23060171_cpu___024root__trace_chg_top_0(void* voidSelf, VerilatedVcd::Buffer* bufp) {
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vysyx_23060171_cpu___024root__trace_chg_top_0\n"); );
    // Init
    Vysyx_23060171_cpu___024root* const __restrict vlSelf VL_ATTR_UNUSED = static_cast<Vysyx_23060171_cpu___024root*>(voidSelf);
    Vysyx_23060171_cpu__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    if (VL_UNLIKELY(!vlSymsp->__Vm_activity)) return;
    // Body
    Vysyx_23060171_cpu___024root__trace_chg_sub_0((&vlSymsp->TOP), bufp);
}

void Vysyx_23060171_cpu___024root__trace_chg_sub_0(Vysyx_23060171_cpu___024root* vlSelf, VerilatedVcd::Buffer* bufp) {
    if (false && vlSelf) {}  // Prevent unused
    Vysyx_23060171_cpu__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vysyx_23060171_cpu___024root__trace_chg_sub_0\n"); );
    // Init
    uint32_t* const oldp VL_ATTR_UNUSED = bufp->oldp(vlSymsp->__Vm_baseCode + 1);
    VlWide<5>/*159:0*/ __Vtemp_h85ff9197__0;
    // Body
    if (VL_UNLIKELY(vlSelf->__Vm_traceActivity[0U])) {
        bufp->chgCData(oldp+0,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__key_list[0]),3);
        bufp->chgCData(oldp+1,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__key_list[1]),3);
        bufp->chgCData(oldp+2,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__key_list[2]),3);
        bufp->chgCData(oldp+3,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__key_list[3]),3);
        bufp->chgBit(oldp+4,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__hit));
    }
    if (VL_UNLIKELY((vlSelf->__Vm_traceActivity[1U] 
                     | vlSelf->__Vm_traceActivity[2U]))) {
        bufp->chgIData(oldp+5,(vlSelf->ysyx_23060171_cpu__DOT__aluresult),32);
        bufp->chgIData(oldp+6,(vlSelf->ysyx_23060171_cpu__DOT__rd1),32);
        bufp->chgCData(oldp+7,(((8U & ((IData)((1ULL 
                                                & (((QData)((IData)(vlSelf->ysyx_23060171_cpu__DOT__rd1)) 
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
        bufp->chgBit(oldp+8,((1U & ((~ ((vlSelf->ysyx_23060171_cpu__DOT__rd1 
                                         ^ vlSelf->ysyx_23060171_cpu__DOT__immext) 
                                        >> 0x1fU)) 
                                    & ((vlSelf->ysyx_23060171_cpu__DOT__rd1 
                                        ^ vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__raddsub) 
                                       >> 0x1fU)))));
        bufp->chgBit(oldp+9,((1U & (~ (IData)((0U != vlSelf->ysyx_23060171_cpu__DOT__aluresult))))));
        bufp->chgBit(oldp+10,((vlSelf->ysyx_23060171_cpu__DOT__aluresult 
                               >> 0x1fU)));
        bufp->chgBit(oldp+11,((1U & (IData)((1ULL & 
                                             (((QData)((IData)(vlSelf->ysyx_23060171_cpu__DOT__rd1)) 
                                               + (QData)((IData)(vlSelf->ysyx_23060171_cpu__DOT__immext))) 
                                              >> 0x20U))))));
        bufp->chgIData(oldp+12,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__raddsub),32);
        bufp->chgIData(oldp+13,((vlSelf->ysyx_23060171_cpu__DOT__rd1 
                                 ^ vlSelf->ysyx_23060171_cpu__DOT__immext)),32);
        bufp->chgIData(oldp+14,((vlSelf->ysyx_23060171_cpu__DOT__rd1 
                                 | vlSelf->ysyx_23060171_cpu__DOT__immext)),32);
        __Vtemp_h85ff9197__0[0U] = (IData)((0x300000000ULL 
                                            | (QData)((IData)(
                                                              (vlSelf->ysyx_23060171_cpu__DOT__rd1 
                                                               | vlSelf->ysyx_23060171_cpu__DOT__immext)))));
        __Vtemp_h85ff9197__0[1U] = (((vlSelf->ysyx_23060171_cpu__DOT__rd1 
                                      ^ vlSelf->ysyx_23060171_cpu__DOT__immext) 
                                     << 3U) | (IData)(
                                                      ((0x300000000ULL 
                                                        | (QData)((IData)(
                                                                          (vlSelf->ysyx_23060171_cpu__DOT__rd1 
                                                                           | vlSelf->ysyx_23060171_cpu__DOT__immext)))) 
                                                       >> 0x20U)));
        __Vtemp_h85ff9197__0[2U] = (0x10U | ((vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__raddsub 
                                              << 6U) 
                                             | ((vlSelf->ysyx_23060171_cpu__DOT__rd1 
                                                 ^ vlSelf->ysyx_23060171_cpu__DOT__immext) 
                                                >> 0x1dU)));
        __Vtemp_h85ff9197__0[3U] = (0x40U | ((vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__raddsub 
                                              << 9U) 
                                             | (vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__raddsub 
                                                >> 0x1aU)));
        __Vtemp_h85ff9197__0[4U] = (vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__raddsub 
                                    >> 0x17U);
        bufp->chgWData(oldp+15,(__Vtemp_h85ff9197__0),140);
        bufp->chgQData(oldp+20,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__pair_list[0]),35);
        bufp->chgQData(oldp+22,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__pair_list[1]),35);
        bufp->chgQData(oldp+24,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__pair_list[2]),35);
        bufp->chgQData(oldp+26,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__pair_list[3]),35);
        bufp->chgIData(oldp+28,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__data_list[0]),32);
        bufp->chgIData(oldp+29,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__data_list[1]),32);
        bufp->chgIData(oldp+30,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__data_list[2]),32);
        bufp->chgIData(oldp+31,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__data_list[3]),32);
        bufp->chgIData(oldp+32,(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__lut_out),32);
    }
    if (VL_UNLIKELY(vlSelf->__Vm_traceActivity[2U])) {
        bufp->chgIData(oldp+33,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[0]),32);
        bufp->chgIData(oldp+34,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[1]),32);
        bufp->chgIData(oldp+35,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[2]),32);
        bufp->chgIData(oldp+36,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[3]),32);
        bufp->chgIData(oldp+37,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[4]),32);
        bufp->chgIData(oldp+38,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[5]),32);
        bufp->chgIData(oldp+39,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[6]),32);
        bufp->chgIData(oldp+40,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[7]),32);
        bufp->chgIData(oldp+41,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[8]),32);
        bufp->chgIData(oldp+42,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[9]),32);
        bufp->chgIData(oldp+43,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[10]),32);
        bufp->chgIData(oldp+44,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[11]),32);
        bufp->chgIData(oldp+45,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[12]),32);
        bufp->chgIData(oldp+46,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[13]),32);
        bufp->chgIData(oldp+47,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[14]),32);
        bufp->chgIData(oldp+48,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[15]),32);
        bufp->chgIData(oldp+49,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[16]),32);
        bufp->chgIData(oldp+50,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[17]),32);
        bufp->chgIData(oldp+51,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[18]),32);
        bufp->chgIData(oldp+52,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[19]),32);
        bufp->chgIData(oldp+53,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[20]),32);
        bufp->chgIData(oldp+54,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[21]),32);
        bufp->chgIData(oldp+55,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[22]),32);
        bufp->chgIData(oldp+56,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[23]),32);
        bufp->chgIData(oldp+57,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[24]),32);
        bufp->chgIData(oldp+58,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[25]),32);
        bufp->chgIData(oldp+59,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[26]),32);
        bufp->chgIData(oldp+60,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[27]),32);
        bufp->chgIData(oldp+61,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[28]),32);
        bufp->chgIData(oldp+62,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[29]),32);
        bufp->chgIData(oldp+63,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[30]),32);
        bufp->chgIData(oldp+64,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[31]),32);
    }
    bufp->chgIData(oldp+65,(vlSelf->inst),32);
    bufp->chgBit(oldp+66,(vlSelf->clk));
    bufp->chgBit(oldp+67,(vlSelf->rst));
    bufp->chgIData(oldp+68,(vlSelf->pc),32);
    bufp->chgIData(oldp+69,(((IData)(4U) + vlSelf->pc)),32);
    bufp->chgIData(oldp+70,(vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf
                            [(0x1fU & (vlSelf->inst 
                                       >> 0x14U))]),32);
    bufp->chgIData(oldp+71,(vlSelf->ysyx_23060171_cpu__DOT__immext),32);
    bufp->chgCData(oldp+72,((0x1fU & (vlSelf->inst 
                                      >> 7U))),5);
    bufp->chgCData(oldp+73,((0x1fU & (vlSelf->inst 
                                      >> 0xfU))),5);
    bufp->chgCData(oldp+74,((0x1fU & (vlSelf->inst 
                                      >> 0x14U))),5);
    bufp->chgCData(oldp+75,((0x7fU & vlSelf->inst)),7);
    bufp->chgCData(oldp+76,((7U & (vlSelf->inst >> 0xcU))),3);
    bufp->chgCData(oldp+77,((vlSelf->inst >> 0x19U)),7);
    bufp->chgSData(oldp+78,((vlSelf->inst >> 0x14U)),12);
}

void Vysyx_23060171_cpu___024root__trace_cleanup(void* voidSelf, VerilatedVcd* /*unused*/) {
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vysyx_23060171_cpu___024root__trace_cleanup\n"); );
    // Init
    Vysyx_23060171_cpu___024root* const __restrict vlSelf VL_ATTR_UNUSED = static_cast<Vysyx_23060171_cpu___024root*>(voidSelf);
    Vysyx_23060171_cpu__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    // Body
    vlSymsp->__Vm_activity = false;
    vlSymsp->TOP.__Vm_traceActivity[0U] = 0U;
    vlSymsp->TOP.__Vm_traceActivity[1U] = 0U;
    vlSymsp->TOP.__Vm_traceActivity[2U] = 0U;
}
