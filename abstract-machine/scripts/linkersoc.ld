ENTRY(_start)
PHDRS 
{ 
  text PT_LOAD; 
  data PT_LOAD; 
  fsbl PT_LOAD;
  ssbl PT_LOAD;
}

MEMORY 
{
  flash : ORIGIN = 0x30000000, LENGTH = 1600000M
  psram : ORIGIN = 0x80000000, LENGTH = 400000M
  sram : ORIGIN = 0x0f000000, LENGTH = 800000K
  sdram : ORIGIN = 0xa0000000, LENGTH = 1000000M
}

SECTIONS 
{
  . = ORIGIN(flash);

  .text.fsbootloader :
  {
    *(entry)
    _fsbl = .;
    *(.text.fsbl)
    _efsbl = .;
  } > flash AT >flash : fsbl

  .text.ssbootloader :
  {
    _ssbl = .;
    *(.text.ssbl)
    _essbl = .;
  } > sdram AT >flash : ssbl

  .text : 
  {
    _text = .;
    *(.text*)
    _etext = .;
  } > sdram AT >flash : text

  .rodata : 
  {
    _rodata = .;
    *(.rodata*)
    *(.srodata*)
    _erodata = .;
  } > sdram AT >flash

  .rela ALIGN(1) :
  {
    _rela = .;
    *(.rela*);
    _erela = .;
  }

  .data ALIGN(1) : 
  {
    _data = .;
    *(.data*)
    *(.sdata*)
    _edata = .;
  } > sdram AT >flash : data

  .bss ALIGN(1) : 
  {
    _bstart = .;
    *(.bss*)
    *(.sbss*)
    *(.scommon)
    _bend = .;
  } > sdram AT >flash

  end = .;
  _end = .;
  _heap_start = ALIGN(0x10);
  _heap_end = _heap_start + 0x40000;
  _stack_top = _sram_start;
  . = _stack_top + _sram_size - 0x100;
  _stack_pointer = .;
} 
