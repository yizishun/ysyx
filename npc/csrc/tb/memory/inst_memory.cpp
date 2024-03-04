#include <memory.h>
static const uint32_t img[] = {
	0b00000000110000000000001011101111, //jal   x5 12   0x80000000
	0b00000000000000000001001000110111, //lui   x4 1    0x80000004
	0b00000000000000000001000110010111, //auipc x3 1    0x80000008
	0b00000000010100000000000010010011, //addi  x1 x0 5 0x8000000c
	0b00000000010100000000000010010011, //addi  x1 x0 5 0x80000010
	0b00000000000100000000000100010011, //addi  x2 x0 1 0x80000014
	0b00000000001000000000000100010011, //addi  x2 x0 2 0x80000018
	0b00000000000001010000010100010011, //addi x10 x10 0 0x8000001c mv a0,a0;
	0b00000000000100000000000001110011  //ebreak        0x80000020  
};
static uint32_t *pmem = NULL;
void init_mem(size_t size){
	pmem = (uint32_t *)malloc(size * sizeof(uint32_t));
	memcpy(pmem , img , sizeof(img));
	if(pmem == NULL){exit(0);}
}
uint32_t *guest_to_host(uint32_t vaddr){return pmem + (vaddr - RESET_VECTOR)/4;}
uint32_t pmem_read(uint32_t vaddr){
	uint32_t *inst_paddr = guest_to_host(vaddr);
	return *inst_paddr;
}
