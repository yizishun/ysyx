#ifndef __MEMORY_H__
#define __MEMORY_H__
#include <stdint.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <VysyxSoCFull.h>
#define RESET_VECTOR 0x20000000
#define REGNUM 32
extern VysyxSoCFull cpu;
extern uint32_t gpr[REGNUM];
extern uint32_t csr[4];
extern const char *regs[];
void init_mem(size_t size);
uint8_t *guest_to_host(uint32_t addr);
void isa_reg_display();
uint32_t isa_reg_str2val(const char *s, bool *success);
void get_reg();

#endif