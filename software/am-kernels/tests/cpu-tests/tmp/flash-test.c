#include "trap.h"
static inline uint8_t  inb(uintptr_t addr) { return *(volatile uint8_t  *)addr; }
static inline uint16_t inw(uintptr_t addr) { return *(volatile uint16_t *)addr; }
static inline uint32_t inl(uintptr_t addr) { return *(volatile uint32_t *)addr; }

static inline void outb(uintptr_t addr, uint8_t  data) { *(volatile uint8_t  *)addr = data; }
static inline void outw(uintptr_t addr, uint16_t data) { *(volatile uint16_t *)addr = data; }
static inline void outl(uintptr_t addr, uint32_t data) { *(volatile uint32_t *)addr = data; }
extern char _heap_start;

uint32_t flash_read(uint32_t addr){
    //config
    outl(0x10001000, 0);
    outl(0x10001004, (0x3 << 24) + (addr & 0xffffff)); //data
    outl(0x10001014, 10); //divid
    outl(0x10001010, 0b10010001000000); //control
    outl(0x10001018, 0b00000001); //ss
    outl(0x10001010, 0b10010101000000); //start trans
    //poll
    uint32_t ctrl;
    uint32_t end;
    do{
        ctrl = inl(0x10001010);
        end = (ctrl >> 8) & 1;
    }  
    while (end != 0);
    uint32_t bigEndianData = (uint32_t)inl(0x10001000); //big endian
    uint8_t byte1 = (bigEndianData >> 24) & 0xFF;
    uint8_t byte2 = (bigEndianData >> 16) & 0xFF;
    uint8_t byte3 = (bigEndianData >> 8) & 0xFF;
    uint8_t byte4 = bigEndianData & 0xFF;
    uint32_t littleEndianData =  (byte4 << 24) | (byte3 << 16) | (byte2 << 8) | byte1;
    return littleEndianData;
}

int main(){
    void (*char_test)() = (void *)&_heap_start;
    *(uint32_t *)char_test = inl(0x30000000);
    *((uint32_t *)char_test + 4) = inl(0x30000004);
    *((uint32_t *)char_test + 8) = inl(0x30000008);
    *((uint32_t *)char_test + 12) = inl(0x3000000c);
    *((uint32_t *)char_test + 16) = inl(0x30000010);
    char_test();
//    uint32_t data; 
//    data = inl(0x30000000);
//    check(data == 0x100007b7);
//    data = inl(0x30000004);
//    check(data == 0x04100713);
    return 0;
}