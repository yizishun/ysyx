#include "trap.h"
#include <stdint.h>
int main(){
    volatile int i = 0;
    volatile int j = 1;
    volatile uint16_t *led = (uint16_t *)(uintptr_t)0x10002000;
    volatile uint16_t *sw = (uint16_t *)(uintptr_t)0x10002004;
    volatile uint32_t *seg = (uint32_t *)(uintptr_t)0x10002008;
    while (*sw != 1);
    
    *seg = 0x12345678;
    
    while (1)
    {
        while(i++ < 1000);
        *led = j;
        j <<= 1;
        if(j == 0x10000) j = 1;
        i = 0;
    }

    return 0;
}