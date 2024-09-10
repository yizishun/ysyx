#ifndef _YSYXSOC_H_
#define _YSYXSOC_H_
#include <common.h>

extern uint8_t *flash;
void init_soc();
bool in_socMem(paddr_t addr);
bool in_socDevW(paddr_t addr); //write only device
bool in_socDevR(paddr_t addr); //can read or write device, its result is unpredictable.
bool in_flash(paddr_t addr);

word_t soc_read(paddr_t addr, int len);
void soc_write(paddr_t addr, int len, word_t data);

word_t socDev_read(paddr_t addr, int len);
void socDev_write(paddr_t addr, int len, word_t data);
void write_icacheitrace(paddr_t addr);
#endif