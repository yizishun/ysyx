#include <am.h>
#include <klib-macros.h>
#include "ysyxSoC.h"
extern char _heap_start;
int main(const char *args);
void bootloader();

extern char _pmem_start;
#define PMEM_SIZE (0xfff)
#define PMEM_END  ((uintptr_t)&_pmem_start + PMEM_SIZE)
#define npc_trap(code) asm volatile("mv a0, %0; ebreak" : :"r"(code))

Area heap = RANGE(&_heap_start, PMEM_END);
#ifndef MAINARGS
#define MAINARGS ""
#endif
static const char mainargs[] = MAINARGS;

void init_uart(uint16_t div) {
  outb(UART_REG_LC, 0b10000011);
  outb(UART_REG_DL2, (uint8_t)(div >> 8));
  outb(UART_REG_DL1, (uint8_t)div);
  outb(UART_REG_LC, 0b00000011);
}

void brandShow(){
  int i;
  int index;
  char buf[10];
  uint32_t number;
  uint32_t mvendorid;
  uint32_t marchid;
  asm volatile("csrr %0, mvendorid" : "=r"(mvendorid));
  asm volatile("csrr %0, marchid" : "=r"(marchid));
  for(i = 3;i >= 0;i--){
      putch((char)((mvendorid >> i*8) & 0xFF));
  }
  putch('\n');
  number = marchid;
  index = 0;
  while (number > 0)
  {
    buf[index++] = (number % 10) + '0';
    number /= 10;
  }
  for(i = index - 1;i >= 0;i--){
    putch(buf[i]);
  }
  putch('\n');
  
}

void putch(char ch) {
  uint8_t lsr;
  uint8_t tfe;
  do
  {
    lsr = inb(UART_REG_LS);
    tfe = (lsr >> UART_LS_TFE) & 1;
  } while (tfe == 0);
  outb(UART_REG_RB, ch);
}

void halt(int code) {
  npc_trap(code);
  while (1);
}

void _trm_init() {
  bootloader();
  init_uart(300);
  brandShow();
  int ret = main(mainargs);
  halt(ret);
}
