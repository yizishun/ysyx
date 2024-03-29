#ifndef __CIRCUIT_H__
#define __CIRCUIT_H__
#include <Vysyx_23060171_cpu.h>
#include <verilated.h>
#include <verilated_vcd_c.h>
#include <svdpi.h>
#include <Vysyx_23060171_cpu__Dpi.h>
#include <Vysyx_23060171_cpu___024root.h>
#include <common.h>
extern Vysyx_23060171_cpu cpu;
extern word_t inst,pc;
//circuit
void single_cycle();
void cpu_exec(uint32_t n);
void reset(int n);
//wave
void init_wave();
void dump_wave_inc();
void close_wave();
//some simulator action
#define BITMASK(bits) ((1ull << (bits)) - 1)
#define BITS(x, hi, lo) (((x) >> (lo)) & BITMASK((hi) - (lo) + 1)) // similar to x[hi:lo] in verilog
#define SEXT(x, len) ({ struct { int64_t n : len; } __x = { .n = x }; (uint64_t)__x.n; })
#endif