// Verilated -*- C++ -*-
// DESCRIPTION: Verilator output: Design implementation internals
// See Vysyx_23060171_cpu.h for the primary calling header

#include "verilated.h"
#include "verilated_dpi.h"

#include "Vysyx_23060171_cpu__Syms.h"
#include "Vysyx_23060171_cpu___024root.h"

extern "C" void npc_trap();

VL_INLINE_OPT void Vysyx_23060171_cpu___024root____Vdpiimwrap_ysyx_23060171_cpu__DOT__idu__DOT__npc_trap_TOP() {
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vysyx_23060171_cpu___024root____Vdpiimwrap_ysyx_23060171_cpu__DOT__idu__DOT__npc_trap_TOP\n"); );
    // Body
    npc_trap();
}

#ifdef VL_DEBUG
VL_ATTR_COLD void Vysyx_23060171_cpu___024root___dump_triggers__ico(Vysyx_23060171_cpu___024root* vlSelf);
#endif  // VL_DEBUG

void Vysyx_23060171_cpu___024root___eval_triggers__ico(Vysyx_23060171_cpu___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    Vysyx_23060171_cpu__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vysyx_23060171_cpu___024root___eval_triggers__ico\n"); );
    // Body
    vlSelf->__VicoTriggered.at(0U) = (0U == vlSelf->__VicoIterCount);
#ifdef VL_DEBUG
    if (VL_UNLIKELY(vlSymsp->_vm_contextp__->debug())) {
        Vysyx_23060171_cpu___024root___dump_triggers__ico(vlSelf);
    }
#endif
}

#ifdef VL_DEBUG
VL_ATTR_COLD void Vysyx_23060171_cpu___024root___dump_triggers__act(Vysyx_23060171_cpu___024root* vlSelf);
#endif  // VL_DEBUG

void Vysyx_23060171_cpu___024root___eval_triggers__act(Vysyx_23060171_cpu___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    Vysyx_23060171_cpu__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    Vysyx_23060171_cpu___024root___eval_triggers__act\n"); );
    // Body
    vlSelf->__VactTriggered.at(0U) = ((IData)(vlSelf->clk) 
                                      & (~ (IData)(vlSelf->__Vtrigrprev__TOP__clk)));
    vlSelf->__Vtrigrprev__TOP__clk = vlSelf->clk;
#ifdef VL_DEBUG
    if (VL_UNLIKELY(vlSymsp->_vm_contextp__->debug())) {
        Vysyx_23060171_cpu___024root___dump_triggers__act(vlSelf);
    }
#endif
}