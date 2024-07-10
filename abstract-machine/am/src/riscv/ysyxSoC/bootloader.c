void ssbl(volatile char *src) __attribute__((section(".text.ssbl"), noinline));
void fsbl() __attribute__((section(".text.fsbl")));
void _trm_init();
void fsbl(){
    extern char _ssbl, _efsbl, _essbl;
    volatile char *src = &_efsbl;
    volatile char *dst = &_ssbl;
    
    /* ROM has data at end of text; copy it.  */
    while (dst < &_essbl)
      *dst++ = *src++;
    ssbl(src);
}
void ssbl(volatile char *src){
    extern char _efsbl, _essbl, _edata, _bstart, _bend;
    volatile char *dst = &_essbl;
    
    /* ROM has data at end of text; copy it.  */
    while (dst < &_edata)
      *dst++ = *src++;
    
    /* Zero bss.  */
    for (dst = &_bstart; dst< &_bend; dst++)
      *dst = 0;
    _trm_init();
    
}
