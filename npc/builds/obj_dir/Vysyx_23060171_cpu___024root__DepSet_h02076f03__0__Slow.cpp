// Verilated -*- C++ -*-
// DESCRIPTION: Verilator output: Design implementation internals
// See Vysyx_23060171_cpu.h for the primary calling header

#include "verilated.h"
#include "verilated_dpi.h"

#include "Vysyx_23060171_cpu___024root.h"

VL_ATTR_COLD void Vysyx_23060171_cpu___024root___eval_static(Vysyx_23060171_cpu___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    Vysyx_23060171_cpu__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vysyx_23060171_cpu___024root___eval_static\n"); );
}

VL_ATTR_COLD void Vysyx_23060171_cpu___024root___eval_initial__TOP(Vysyx_23060171_cpu___024root* vlSelf);

VL_ATTR_COLD void Vysyx_23060171_cpu___024root___eval_initial(Vysyx_23060171_cpu___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    Vysyx_23060171_cpu__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vysyx_23060171_cpu___024root___eval_initial\n"); );
    // Body
    Vysyx_23060171_cpu___024root___eval_initial__TOP(vlSelf);
    vlSelf->__Vm_traceActivity[2U] = 1U;
    vlSelf->__Vm_traceActivity[1U] = 1U;
    vlSelf->__Vm_traceActivity[0U] = 1U;
    vlSelf->__Vtrigrprev__TOP__clk = vlSelf->clk;
}

VL_ATTR_COLD void Vysyx_23060171_cpu___024root___eval_initial__TOP(Vysyx_23060171_cpu___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    Vysyx_23060171_cpu__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vysyx_23060171_cpu___024root___eval_initial__TOP\n"); );
    // Body
    vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__key_list[0U] = 3U;
    vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__key_list[1U] = 2U;
    vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__key_list[2U] = 1U;
    vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__key_list[3U] = 0U;
}

VL_ATTR_COLD void Vysyx_23060171_cpu___024root___eval_final(Vysyx_23060171_cpu___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    Vysyx_23060171_cpu__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vysyx_23060171_cpu___024root___eval_final\n"); );
}

VL_ATTR_COLD void Vysyx_23060171_cpu___024root___eval_triggers__stl(Vysyx_23060171_cpu___024root* vlSelf);
#ifdef VL_DEBUG
VL_ATTR_COLD void Vysyx_23060171_cpu___024root___dump_triggers__stl(Vysyx_23060171_cpu___024root* vlSelf);
#endif  // VL_DEBUG
VL_ATTR_COLD void Vysyx_23060171_cpu___024root___eval_stl(Vysyx_23060171_cpu___024root* vlSelf);

VL_ATTR_COLD void Vysyx_23060171_cpu___024root___eval_settle(Vysyx_23060171_cpu___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    Vysyx_23060171_cpu__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vysyx_23060171_cpu___024root___eval_settle\n"); );
    // Init
    CData/*0:0*/ __VstlContinue;
    // Body
    vlSelf->__VstlIterCount = 0U;
    __VstlContinue = 1U;
    while (__VstlContinue) {
        __VstlContinue = 0U;
        Vysyx_23060171_cpu___024root___eval_triggers__stl(vlSelf);
        if (vlSelf->__VstlTriggered.any()) {
            __VstlContinue = 1U;
            if (VL_UNLIKELY((0x64U < vlSelf->__VstlIterCount))) {
#ifdef VL_DEBUG
                Vysyx_23060171_cpu___024root___dump_triggers__stl(vlSelf);
#endif
                VL_FATAL_MT("/home/awaken/ysyx-workbench/npc/vsrc/ysyx_23060171_cpu.v", 1, "", "Settle region did not converge.");
            }
            vlSelf->__VstlIterCount = ((IData)(1U) 
                                       + vlSelf->__VstlIterCount);
            Vysyx_23060171_cpu___024root___eval_stl(vlSelf);
        }
    }
}

#ifdef VL_DEBUG
VL_ATTR_COLD void Vysyx_23060171_cpu___024root___dump_triggers__stl(Vysyx_23060171_cpu___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    Vysyx_23060171_cpu__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vysyx_23060171_cpu___024root___dump_triggers__stl\n"); );
    // Body
    if ((1U & (~ (IData)(vlSelf->__VstlTriggered.any())))) {
        VL_DBG_MSGF("         No triggers active\n");
    }
    if (vlSelf->__VstlTriggered.at(0U)) {
        VL_DBG_MSGF("         'stl' region trigger index 0 is active: Internal 'stl' trigger - first iteration\n");
    }
}
#endif  // VL_DEBUG

void Vysyx_23060171_cpu___024root____Vdpiimwrap_ysyx_23060171_cpu__DOT__idu__DOT__npc_trap_TOP();

VL_ATTR_COLD void Vysyx_23060171_cpu___024root___stl_sequent__TOP__0(Vysyx_23060171_cpu___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    Vysyx_23060171_cpu__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vysyx_23060171_cpu___024root___stl_sequent__TOP__0\n"); );
    // Body
    if ((IData)((0x100073U == (0xfff0007fU & vlSelf->inst)))) {
        Vysyx_23060171_cpu___024root____Vdpiimwrap_ysyx_23060171_cpu__DOT__idu__DOT__npc_trap_TOP();
    }
    vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__hit 
        = (0U == vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__key_list
           [0U]);
    vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__hit 
        = ((IData)(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__hit) 
           | (0U == vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__key_list
              [1U]));
    vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__hit 
        = ((IData)(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__hit) 
           | (0U == vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__key_list
              [2U]));
    vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__hit 
        = ((IData)(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__hit) 
           | (0U == vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__key_list
              [3U]));
    vlSelf->ysyx_23060171_cpu__DOT__next_pc = ((IData)(4U) 
                                               + vlSelf->pc);
    vlSelf->ysyx_23060171_cpu__DOT__immext = (((- (IData)(
                                                          (vlSelf->inst 
                                                           >> 0x1fU))) 
                                               << 0xcU) 
                                              | (vlSelf->inst 
                                                 >> 0x14U));
    vlSelf->ysyx_23060171_cpu__DOT__rd1 = vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf
        [(0x1fU & (vlSelf->inst >> 0xfU))];
    vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__pair_list[0U] 
        = (0x300000000ULL | (QData)((IData)((vlSelf->ysyx_23060171_cpu__DOT__rd1 
                                             | vlSelf->ysyx_23060171_cpu__DOT__immext))));
    vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__pair_list[1U] 
        = (0x200000000ULL | (QData)((IData)((vlSelf->ysyx_23060171_cpu__DOT__rd1 
                                             ^ vlSelf->ysyx_23060171_cpu__DOT__immext))));
    vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__data_list[0U] 
        = (vlSelf->ysyx_23060171_cpu__DOT__rd1 | vlSelf->ysyx_23060171_cpu__DOT__immext);
    vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__data_list[1U] 
        = (vlSelf->ysyx_23060171_cpu__DOT__rd1 ^ vlSelf->ysyx_23060171_cpu__DOT__immext);
    vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__data_list[2U] 
        = (vlSelf->ysyx_23060171_cpu__DOT__rd1 + vlSelf->ysyx_23060171_cpu__DOT__immext);
    vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__data_list[3U] 
        = (vlSelf->ysyx_23060171_cpu__DOT__rd1 + vlSelf->ysyx_23060171_cpu__DOT__immext);
    vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__raddsub 
        = (vlSelf->ysyx_23060171_cpu__DOT__rd1 + vlSelf->ysyx_23060171_cpu__DOT__immext);
    vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__lut_out 
        = ((- (IData)((0U == vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__key_list
                       [0U]))) & vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__data_list
           [0U]);
    vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__lut_out 
        = (vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__lut_out 
           | ((- (IData)((0U == vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__key_list
                          [1U]))) & vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__data_list
              [1U]));
    vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__lut_out 
        = (vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__lut_out 
           | ((- (IData)((0U == vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__key_list
                          [2U]))) & vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__data_list
              [2U]));
    vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__lut_out 
        = (vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__lut_out 
           | ((- (IData)((0U == vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__key_list
                          [3U]))) & vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__data_list
              [3U]));
    vlSelf->ysyx_23060171_cpu__DOT__aluresult = vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__lut_out;
    vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__pair_list[2U] 
        = (0x100000000ULL | (QData)((IData)(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__raddsub)));
    vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__pair_list[3U] 
        = (QData)((IData)(vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__raddsub));
}

VL_ATTR_COLD void Vysyx_23060171_cpu___024root___eval_stl(Vysyx_23060171_cpu___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    Vysyx_23060171_cpu__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vysyx_23060171_cpu___024root___eval_stl\n"); );
    // Body
    if (vlSelf->__VstlTriggered.at(0U)) {
        Vysyx_23060171_cpu___024root___stl_sequent__TOP__0(vlSelf);
        vlSelf->__Vm_traceActivity[2U] = 1U;
        vlSelf->__Vm_traceActivity[1U] = 1U;
        vlSelf->__Vm_traceActivity[0U] = 1U;
    }
}

#ifdef VL_DEBUG
VL_ATTR_COLD void Vysyx_23060171_cpu___024root___dump_triggers__ico(Vysyx_23060171_cpu___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    Vysyx_23060171_cpu__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vysyx_23060171_cpu___024root___dump_triggers__ico\n"); );
    // Body
    if ((1U & (~ (IData)(vlSelf->__VicoTriggered.any())))) {
        VL_DBG_MSGF("         No triggers active\n");
    }
    if (vlSelf->__VicoTriggered.at(0U)) {
        VL_DBG_MSGF("         'ico' region trigger index 0 is active: Internal 'ico' trigger - first iteration\n");
    }
}
#endif  // VL_DEBUG

#ifdef VL_DEBUG
VL_ATTR_COLD void Vysyx_23060171_cpu___024root___dump_triggers__act(Vysyx_23060171_cpu___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    Vysyx_23060171_cpu__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vysyx_23060171_cpu___024root___dump_triggers__act\n"); );
    // Body
    if ((1U & (~ (IData)(vlSelf->__VactTriggered.any())))) {
        VL_DBG_MSGF("         No triggers active\n");
    }
    if (vlSelf->__VactTriggered.at(0U)) {
        VL_DBG_MSGF("         'act' region trigger index 0 is active: @(posedge clk)\n");
    }
}
#endif  // VL_DEBUG

#ifdef VL_DEBUG
VL_ATTR_COLD void Vysyx_23060171_cpu___024root___dump_triggers__nba(Vysyx_23060171_cpu___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    Vysyx_23060171_cpu__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vysyx_23060171_cpu___024root___dump_triggers__nba\n"); );
    // Body
    if ((1U & (~ (IData)(vlSelf->__VnbaTriggered.any())))) {
        VL_DBG_MSGF("         No triggers active\n");
    }
    if (vlSelf->__VnbaTriggered.at(0U)) {
        VL_DBG_MSGF("         'nba' region trigger index 0 is active: @(posedge clk)\n");
    }
}
#endif  // VL_DEBUG

VL_ATTR_COLD void Vysyx_23060171_cpu___024root___ctor_var_reset(Vysyx_23060171_cpu___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    Vysyx_23060171_cpu__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vysyx_23060171_cpu___024root___ctor_var_reset\n"); );
    // Body
    vlSelf->inst = 0;
    vlSelf->clk = 0;
    vlSelf->rst = 0;
    vlSelf->pc = 0;
    vlSelf->ysyx_23060171_cpu__DOT__next_pc = 0;
    vlSelf->ysyx_23060171_cpu__DOT__aluresult = 0;
    vlSelf->ysyx_23060171_cpu__DOT__rd1 = 0;
    vlSelf->ysyx_23060171_cpu__DOT__immext = 0;
    for (int __Vi0 = 0; __Vi0 < 32; ++__Vi0) {
        vlSelf->ysyx_23060171_cpu__DOT__gpr__DOT__rf[__Vi0] = 0;
    }
    vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__raddsub = 0;
    for (int __Vi0 = 0; __Vi0 < 4; ++__Vi0) {
        vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__pair_list[__Vi0] = 0;
    }
    for (int __Vi0 = 0; __Vi0 < 4; ++__Vi0) {
        vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__key_list[__Vi0] = 0;
    }
    for (int __Vi0 = 0; __Vi0 < 4; ++__Vi0) {
        vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__data_list[__Vi0] = 0;
    }
    vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__lut_out = 0;
    vlSelf->ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__hit = 0;
    vlSelf->__Vtrigrprev__TOP__clk = 0;
    for (int __Vi0 = 0; __Vi0 < 3; ++__Vi0) {
        vlSelf->__Vm_traceActivity[__Vi0] = 0;
    }
}
