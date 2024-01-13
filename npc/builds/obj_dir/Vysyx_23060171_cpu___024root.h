// Verilated -*- C++ -*-
// DESCRIPTION: Verilator output: Design internal header
// See Vysyx_23060171_cpu.h for the primary calling header

#ifndef VERILATED_VYSYX_23060171_CPU___024ROOT_H_
#define VERILATED_VYSYX_23060171_CPU___024ROOT_H_  // guard

#include "verilated.h"

class Vysyx_23060171_cpu__Syms;

class Vysyx_23060171_cpu___024root final : public VerilatedModule {
  public:

    // DESIGN SPECIFIC STATE
    VL_IN8(clk,0,0);
    VL_IN8(rst,0,0);
    CData/*0:0*/ ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__hit;
    CData/*0:0*/ __Vtrigrprev__TOP__clk;
    CData/*0:0*/ __VactContinue;
    VL_IN(inst,31,0);
    VL_OUT(pc,31,0);
    IData/*31:0*/ ysyx_23060171_cpu__DOT__next_pc;
    IData/*31:0*/ ysyx_23060171_cpu__DOT__aluresult;
    IData/*31:0*/ ysyx_23060171_cpu__DOT__rd1;
    IData/*31:0*/ ysyx_23060171_cpu__DOT__immext;
    IData/*31:0*/ ysyx_23060171_cpu__DOT__alu__DOT__raddsub;
    IData/*31:0*/ ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__lut_out;
    IData/*31:0*/ __VstlIterCount;
    IData/*31:0*/ __VicoIterCount;
    IData/*31:0*/ __VactIterCount;
    VlUnpacked<IData/*31:0*/, 32> ysyx_23060171_cpu__DOT__gpr__DOT__rf;
    VlUnpacked<QData/*34:0*/, 4> ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__pair_list;
    VlUnpacked<CData/*2:0*/, 4> ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__key_list;
    VlUnpacked<IData/*31:0*/, 4> ysyx_23060171_cpu__DOT__alu__DOT__alumux__DOT__i0__DOT__data_list;
    VlUnpacked<CData/*0:0*/, 3> __Vm_traceActivity;
    VlTriggerVec<1> __VstlTriggered;
    VlTriggerVec<1> __VicoTriggered;
    VlTriggerVec<1> __VactTriggered;
    VlTriggerVec<1> __VnbaTriggered;

    // INTERNAL VARIABLES
    Vysyx_23060171_cpu__Syms* const vlSymsp;

    // CONSTRUCTORS
    Vysyx_23060171_cpu___024root(Vysyx_23060171_cpu__Syms* symsp, const char* v__name);
    ~Vysyx_23060171_cpu___024root();
    VL_UNCOPYABLE(Vysyx_23060171_cpu___024root);

    // INTERNAL METHODS
    void __Vconfigure(bool first);
} VL_ATTR_ALIGNED(VL_CACHE_LINE_BYTES);


#endif  // guard
