/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include <memory/host.h>
#include <memory/paddr.h>
#include <device/mmio.h>
#include <isa.h>

//difftese
bool skip = false;

#if   defined(CONFIG_PMEM_MALLOC)
static uint8_t *pmem = NULL;
#else // CONFIG_PMEM_GARRAY
static uint8_t pmem[CONFIG_MSIZE] PG_ALIGN = {};
#endif
static uint8_t *mrom = NULL;
static uint8_t *sram = NULL;
static uint8_t *flash = NULL;
static void out_of_bound(paddr_t addr);

uint8_t* guest_to_host(paddr_t paddr) {
  uint8_t* ptr = NULL;
  if(in_pmem(paddr)) 
    ptr = pmem + paddr - CONFIG_MBASE;
  else if(in_mrom(paddr))
    ptr = mrom + paddr - MROM_BASE;
  else if(in_sram(paddr))
    ptr = sram + paddr - SRAM_BASE;
  else if(in_flash(paddr))
    ptr = flash + paddr - FLASH_BASE;
  else 
    out_of_bound(paddr);
  return ptr;
}
paddr_t host_to_guest(uint8_t *haddr) { return haddr - pmem + CONFIG_MBASE; }

static word_t pmem_read(paddr_t addr, int len) {
  word_t ret = host_read(guest_to_host(addr), len);
  return ret;
}

static void pmem_write(paddr_t addr, int len, word_t data) {
  host_write(guest_to_host(addr), len, data);
}

static void out_of_bound(paddr_t addr) {
  panic("address = " FMT_PADDR " is out of bound of pmem [" FMT_PADDR ", " FMT_PADDR "] at pc = " FMT_WORD,
      addr, PMEM_LEFT, PMEM_RIGHT, cpu.pc);
}

void init_mem() {
#if   defined(CONFIG_PMEM_MALLOC)
  pmem = malloc(CONFIG_MSIZE);
  assert(pmem); 
#endif
  IFDEF(CONFIG_MEM_RANDOM, memset(pmem, rand(), CONFIG_MSIZE));
  Log("physical memory area [" FMT_PADDR ", " FMT_PADDR "]", PMEM_LEFT, PMEM_RIGHT);
}

void init_mrom() {
  mrom = malloc(0xfff);
  assert(mrom);
  Log("mrom area [" FMT_PADDR ", " FMT_PADDR "]", MROM_BASE, MROM_BASE + MROM_SIZE);
}

void init_flash() {
  flash = malloc(0xfff);
  assert(flash);
  Log("flash area [" FMT_PADDR ", " FMT_PADDR "]", FLASH_BASE, MROM_BASE + FLASH_SIZE);
}

void init_sram() {
  sram = malloc(0x1fff);
  assert(sram);
  Log("sram area [" FMT_PADDR ", " FMT_PADDR "]", SRAM_BASE, SRAM_BASE + SRAM_SIZE);
}

#ifdef CONFIG_MTRACE
#define READ 0
#define WRITE 1
char mtrace[128] = {0};
void record_mem_trace(int rw,paddr_t addr, int len){
	char *m = mtrace;
	if (rw == READ)
		m += sprintf(m , "READ  ");
	else
		m += sprintf(m,  "WRITE ");
	m += sprintf(m, "%d in " FMT_PADDR,len,addr);
}

#endif

word_t paddr_read(paddr_t addr, int len) {
	#ifdef CONFIG_MTRACE
	record_mem_trace(READ, addr , len);	
	#endif
  #ifdef CONFIG_MTRACE_COND
    if (MTRACE_COND) { log_write("%s\n", mtrace); }
  #endif
  if (likely(in_pmem(addr)) || in_mrom(addr) || in_sram(addr) || in_flash(addr)) return pmem_read(addr, len);
  if (in_uart(addr)) {skip = true; return 0;}
  IFDEF(CONFIG_DEVICE, return mmio_read(addr, len));
  out_of_bound(addr);
  return 0;
}

void paddr_write(paddr_t addr, int len, word_t data) {
	#ifdef CONFIG_MTRACE
	record_mem_trace(WRITE,addr , len);	
	#endif
  #ifdef CONFIG_MTRACE_COND
    if (MTRACE_COND) { log_write("%s content = %d\n", mtrace,data); }
  #endif
  if (likely(in_pmem(addr)) || in_sram(addr)) { pmem_write(addr, len, data); return; }
  if (in_uart(addr)) return;
  IFDEF(CONFIG_DEVICE, mmio_write(addr, len, data); return);
  out_of_bound(addr);
}
