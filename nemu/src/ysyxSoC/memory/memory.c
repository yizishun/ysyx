#include "../include/memory.h"
static uint8_t *mrom = NULL;
static uint8_t *sram = NULL;
uint8_t *flash = NULL;
static uint8_t *psram = NULL;
static uint8_t *sdram = NULL;
static FILE *itrace_fp = NULL;
static FILE *mtrace_fp = NULL;
static FILE *btrace_fp = NULL;
static char * itrace_file = "build/itrace.log";
static char * mtrace_file = "build/mtrace.log";
static char * btrace_file = "build/btrace.log";
void init_icacheitrace();
void init_mcacheitrace();
void init_btrace();
void init_mrom() {
  mrom = malloc(0xfff);
  assert(mrom);
  Log("mrom area [" FMT_PADDR ", " FMT_PADDR "]", MROM_BASE, MROM_BASE + MROM_SIZE);
}

void init_flash() {
  flash = malloc(0xfffffff);
  assert(flash);
  Log("flash area [" FMT_PADDR ", " FMT_PADDR "]", FLASH_BASE, MROM_BASE + FLASH_SIZE);
}

void init_sram() {
  sram = malloc(0x1fff);
  assert(sram);
  Log("sram area [" FMT_PADDR ", " FMT_PADDR "]", SRAM_BASE, SRAM_BASE + SRAM_SIZE);
}

void init_psram() {
  psram = malloc(0x20000000);
  assert(psram);
  Log("psram area [" FMT_PADDR ", " FMT_PADDR "]", PSRAM_BASE, PSRAM_BASE + PSRAM_SIZE);
}

void init_sdram() {
  sdram = malloc(0x20000000);
  assert(sdram);
  Log("sdram area [" FMT_PADDR ", " FMT_PADDR "]", SDRAM_BASE, SDRAM_BASE + SDRAM_SIZE);
}

void init_soc() {
    init_flash();
    init_sram();
    init_sdram();
    #ifndef CONFIG_TARGET_SHARE
    init_icacheitrace();
    init_mcacheitrace();
    Log("cache trace init");  
    init_btrace();
    Log("btrace init");  
    #endif
    Log("soc init");    
}



inline bool in_flash(paddr_t addr) {
  return addr - FLASH_BASE < FLASH_SIZE;
}

static inline bool in_sram(paddr_t addr) {
  return addr - SRAM_BASE < SRAM_SIZE;
}

static inline bool in_uart(paddr_t addr) {
  return addr - UART_BASE < UART_SIZE;
}

static inline bool in_sdram(paddr_t addr) {
  return addr - SDRAM_BASE < SDRAM_SIZE;
}

static inline bool in_clint(paddr_t addr) {
    return addr - CLINT_BASE < CLINT_SIZE;
}

static inline bool in_gpio(paddr_t addr) {
  return addr - GPIO_BASE < GPIO_SIZE;
}

bool in_socMem(paddr_t addr){
    return in_flash(addr) || in_sram(addr) || in_sdram(addr);
}
//only support uart and clint
bool in_socDevW(paddr_t addr){
    return in_uart(addr) || in_gpio(addr);
}
bool in_socDevR(paddr_t addr){
    return in_uart(addr) || in_clint(addr);
}


word_t soc_read(paddr_t paddr, int len){
    uint8_t *ptr;
    if(in_sram(paddr))
        ptr = sram + paddr - SRAM_BASE;
    else if(in_flash(paddr))
        ptr = flash + paddr - FLASH_BASE;
    else if(in_sdram(paddr))
        ptr = sdram + paddr - SDRAM_BASE;
    else assert(0);
    
    //printf("read from paddr %#x, data = %#x\n", paddr, *(uint32_t *)ptr);
    switch (len) {
        case 1: return *(uint8_t  *)ptr;
        case 2: return *(uint16_t *)ptr;
        case 4: return *(uint32_t *)ptr;
        default: assert(0);
    }
    assert(0);
    return 0; // unreachable
}
void soc_write(paddr_t paddr, int len, word_t data){
    uint8_t *ptr;
    if(in_sram(paddr))
        ptr = sram + paddr - SRAM_BASE;
    else if(in_flash(paddr))
        ptr = flash + paddr - FLASH_BASE;
    else if(in_sdram(paddr))
        ptr = sdram + paddr - SDRAM_BASE;
    else assert(0);
    
    //printf("wirte to paddr %#x data = %#x\n",paddr, data);
    switch (len) {
        case 1: *(uint8_t  *)ptr = data; return;
        case 2: *(uint16_t *)ptr = data; return;
        case 4: *(uint32_t *)ptr = data; return;
        default: assert(0);
    }
}

word_t uart_io_read(paddr_t addr, int len){
    assert(len == 1);
    if(addr == UART_REG_LS)
        return 32;
    return 0;
}
void uart_io_write(paddr_t addr, int len, word_t data){
    assert(len == 1);
    if(addr == UART_REG_RB){
        putchar(data);
    }
}
word_t clint_io_read(paddr_t addr, int len){
    int offset = addr - CLINT_BASE;
    assert(offset == 0 || offset == 4);
    uint64_t us = get_time();
    if(offset == 0)
        return (uint32_t)us;
    else if(offset == 4)
        return us >> 32;
    assert(0);
    return 0;
}
void clint_io_write(paddr_t addr, int len, word_t data){
    assert(0);
}
word_t socDev_read(paddr_t addr, int len){
    word_t ret;
    if(in_uart(addr))
        ret = uart_io_read(addr, len);
    else if(in_clint(addr))
        ret = clint_io_read(addr, len);
    else assert(0);
    return ret;
}
void socDev_write(paddr_t addr, int len, word_t data){
    if(in_uart(addr))
        uart_io_write(addr, len, data);
    else if(in_clint(addr))
        clint_io_write(addr, len, data);
    else if(in_gpio(addr))
        return;
    else assert(0);
}

void init_icacheitrace(){
    itrace_fp = fopen(itrace_file, "w");
    Log("icache trace file %s", itrace_file);
    assert(itrace_fp);
}

void write_icacheitrace(paddr_t addr){
    fprintf(itrace_fp, "%u\n", addr);
}

void init_mcacheitrace(){
    mtrace_fp = fopen(mtrace_file, "w");
    Log("mcache trace file %s", mtrace_file);
    assert(mtrace_fp);
}

void write_mcacheitrace(paddr_t addr){
    fprintf(mtrace_fp, "%u\n", addr);
}

void init_btrace(){
    btrace_fp = fopen(btrace_file, "w");
    Log("btrace file %s", btrace_file);
    assert(btrace_fp);
}

void write_btrace(uint32_t inst, bool is_taken, paddr_t pc){
    fprintf(btrace_fp, "%u %u %u\n", pc, inst, is_taken);
}
