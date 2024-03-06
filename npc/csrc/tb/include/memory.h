#ifndef __MEMORY_H__
#define __MEMORY_H__
#include <stdint.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <Vysyx_23060171_cpu.h>
#define RESET_VECTOR 0x80000000
#define REGNUM 32
extern Vysyx_23060171_cpu cpu;
extern uint32_t gpr[REGNUM];
extern const char *regs[];
void init_mem(size_t size);
uint32_t *guest_to_host(uint32_t addr);
uint32_t pmem_read(uint32_t vaddr);
void isa_reg_display();
uint32_t isa_reg_str2val(const char *s, bool *success);
void get_reg();

#endif