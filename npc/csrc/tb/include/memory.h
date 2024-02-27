#ifndef __MEMORY_H__
#define __MEMORY_H__
#include <stdint.h>
#include <stdlib.h>
#define RESET_VECTOR 0x80000000
void init_mem(size_t size);
uint32_t *guest_to_host(uint32_t addr);
uint32_t pmem_read(uint32_t vaddr);

#endif