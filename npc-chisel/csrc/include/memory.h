#ifndef __MEMORY_H__
#define __MEMORY_H__
#include <stdint.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <circuit.h>
#if defined(ysyxSoCFull)
#define RESET_VECTOR 0x30000000
#elif defined(NPC)
#define RESET_VECTOR 0x80000000
#endif
#define PMEM_BASE    0x80000000
#define FLASH_BASE   0x30000000
#define FLASH_SIZE   0x0fffffff
#define PSRAM_BASE   0x80000000
#define PSRAM_SIZE   0x20000000
#define SDRAM_BASE   0xa0000000
#define SDRAM_SIZE   0x20000000
#define REGNUM 32
extern uint32_t gpr[REGNUM];
extern uint32_t csr[4];
extern const char *regs[];
void init_mem(size_t size);
void init_flash();
void init_psram();
void init_sdram();
uint8_t *guest_to_host(uint32_t addr);
void isa_reg_display();
uint32_t isa_reg_str2val(const char *s, bool *success);
void get_reg();


static inline bool in_flash(uint32_t addr) {
  return addr - FLASH_BASE < FLASH_SIZE;
}
static inline bool in_pmem(uint32_t addr) {
    return addr - PMEM_BASE < 0xfffffff;
}
static inline bool in_psram(uint32_t addr) {
  return addr - PSRAM_BASE < PSRAM_SIZE;
}
static inline bool in_sdram(uint32_t addr) {
  return addr - SDRAM_BASE < SDRAM_SIZE;
}
#endif