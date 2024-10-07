#ifndef __CIRCUIT_H__
#define __CIRCUIT_H__

#if defined(ysyxSoCFull)
#include <VysyxSoCFull.h>
#include <VysyxSoCFull__Dpi.h>
#include <VysyxSoCFull___024root.h>
#define CPU VysyxSoCFull
#define V_PC ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__wbu_io_in_bits_r_pc
#define V_INST ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__idu_io_in_bits_r_inst
#define V_E_GETINST ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__idu_io_in_valid_r
#define WBU ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__wbu_io_in_valid_r
#define V_GPR ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__gpr__DOT__rf_ext__DOT__Memory
#define V_CSR ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__csr__DOT__rf_ext__DOT__Memory

#elif defined(NPC)
#include <VNPC.h>
#include <VNPC__Dpi.h>
#include <VNPC___024root.h>
#define CPU VNPC
#define V_PC NPC__DOT__core__DOT__ifu__DOT__pc__DOT__pcReg
#define V_INST NPC__DOT__core__DOT__ifu__DOT__inst
#define V_E_GETINST NPC__DOT__core__DOT__idu__DOT__nextState 
#define V_GPR NPC__DOT__core__DOT__gpr__DOT__rf_ext__DOT__Memory
#define V_CSR NPC__DOT__core__DOT__csr__DOT__rf_ext__DOT__Memory

#endif
#include <verilated.h>
#include <verilated_vcd_c.h>
#include <svdpi.h>
#include <common.h>
extern CPU *cpu;
extern word_t inst,pc;
extern uint64_t cycle;
//circuit
void single_cycle();
void cpu_exec(uint64_t n);
void reset(int n);
//wave
void init_wave();
void dump_wave_inc();
void close_wave();
//nvboard
#include <nvboard.h>
void nvboard_bind_all_pins(CPU* top);
//some simulator action
#define BITMASK(bits) ((1ull << (bits)) - 1)
#define BITS(x, hi, lo) (((x) >> (lo)) & BITMASK((hi) - (lo) + 1)) // similar to x[hi:lo] in verilog
#define SEXT(x, len) ({ struct { int64_t n : len; } __x = { .n = x }; (uint64_t)__x.n; })
#endif