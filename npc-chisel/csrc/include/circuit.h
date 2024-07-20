#ifndef __CIRCUIT_H__
#define __CIRCUIT_H__
#include <VysyxSoCFull.h>
#include <verilated.h>
#include <verilated_vcd_c.h>
#include <svdpi.h>
#include <VysyxSoCFull__Dpi.h>
#include <VysyxSoCFull___024root.h>
#include <common.h>
extern VysyxSoCFull cpu;
extern word_t inst,pc;
extern uint64_t g_nr_guest_inst;
//circuit
void single_cycle();
void cpu_exec(uint32_t n);
void reset(int n);
//wave
void init_wave();
void dump_wave_inc();
void close_wave();
//nvboard
#include <nvboard.h>
void nvboard_bind_all_pins(VysyxSoCFull* top);
//some simulator action
#define BITMASK(bits) ((1ull << (bits)) - 1)
#define BITS(x, hi, lo) (((x) >> (lo)) & BITMASK((hi) - (lo) + 1)) // similar to x[hi:lo] in verilog
#define SEXT(x, len) ({ struct { int64_t n : len; } __x = { .n = x }; (uint64_t)__x.n; })
#endif