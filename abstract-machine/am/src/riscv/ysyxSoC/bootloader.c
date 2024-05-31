void bootloader(){
    extern char _erodata, _data, _edata, _bstart, _bend;
    char *src = &_erodata;
    char *dst = &_data;
    
    /* ROM has data at end of text; copy it.  */
    while (dst < &_edata)
      *dst++ = *src++;
    
    /* Zero bss.  */
    for (dst = &_bstart; dst< &_bend; dst++)
      *dst = 0;
}