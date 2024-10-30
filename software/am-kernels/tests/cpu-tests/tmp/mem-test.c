#include "trap.h"
#include <stdint.h>

#define SRAM_BASE 0x0f000000
extern char _heap_start;
extern char _heap_end;

int main() {
    volatile uint16_t *start = (uint16_t *)(uintptr_t)0x80100000;
    volatile uint16_t *end = (uint16_t *)(uintptr_t)(0x80100000+ 0x0002000);
    int len_mask = 0xFFFF;

    //volatile uint32_t *test = (uint32_t *)(uintptr_t)0xa2000000;
    //*test = 0x12345678;
    //check(*(uint32_t *)test == 0x12345678);
    //check(*(uint8_t *)test == 0x78);
    //check(*((uint16_t *)test + 1) == 0x1234);

    for (; start < end; start++) {
        volatile uintptr_t addr = (uintptr_t)start;
        *start = (uint16_t)(addr & len_mask);

        check(*start == (uint16_t)(addr & len_mask));
    }

    return 0;
}
