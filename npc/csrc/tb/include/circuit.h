#ifndef __CIRCUIT_H__
#define __CIRCUIT_H__
#include <Vysyx_23060171_cpu.h>
#include <verilated.h>
#include <verilated_vcd_c.h>
#include <svdpi.h>
#include <Vysyx_23060171_cpu__Dpi.h>
extern Vysyx_23060171_cpu cpu;
//circuit
void single_cycle();
void cpu_exec(uint32_t n);
void reset(int n);
//wave
void init_wave();
void dump_wave_inc();
void close_wave();
#endif