#include <memory.h>
#include <string.h>
#include <assert.h>
static const uint32_t img[] = {
	0b00000000010100000000000010010011, //addi x1 x0 5 0x80000000
	0b00000000000100000000000100010011, //addi x2 x0 1 0x80000004
	0b00000000001000000000000100010011, //addi x2 x0 2 0x80000008
	0b00000000010100001000000100010011, //addi x2 x1 5 0x8000000c
	0b00000000000100000000000001110011  //ebreak       0x80000010  
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
