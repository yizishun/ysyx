#include "trap.h"
static inline uint8_t  inb(uintptr_t addr) { return *(volatile uint8_t  *)addr; }
static inline uint16_t inw(uintptr_t addr) { return *(volatile uint16_t *)addr; }
static inline uint32_t inl(uintptr_t addr) { return *(volatile uint32_t *)addr; }

static inline void outb(uintptr_t addr, uint8_t  data) { *(volatile uint8_t  *)addr = data; }
static inline void outw(uintptr_t addr, uint16_t data) { *(volatile uint16_t *)addr = data; }
static inline void outl(uintptr_t addr, uint32_t data) { *(volatile uint32_t *)addr = data; }

int main(){
    //config
    outl(0x10001000, 0b10101010); //data
    outl(0x10001014, 10); //divid
    outl(0x10001010, 0b10101000010000); //control
    outl(0x10001018, 0b10000000); //ss
    outl(0x10001010, 0b10101100010000); //start trans
    //poll
    uint32_t ctrl;
    uint32_t end;
    do{
        ctrl = inl(0x10001010);
        end = (ctrl >> 8) & 1;
    }  
    while (end != 0);
    uint8_t data = (uint8_t)(inl(0x10001000) >> 8);
    check(data == 0x55);
    putch(data);
    return 0;
}