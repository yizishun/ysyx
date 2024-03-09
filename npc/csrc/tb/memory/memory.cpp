#include <memory.h>
#include <common.h>
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
static uint8_t *pmem = NULL;
void init_mem(size_t size){ 
	pmem = (uint8_t *)malloc(size * sizeof(uint8_t));
	memcpy(pmem , img , sizeof(img));
	if(pmem == NULL){exit(0);}
	Log("npc physical memory area [%#x, %#lx]",RESET_VECTOR, RESET_VECTOR + size * sizeof(uint8_t));
}
uint8_t *guest_to_host(uint32_t paddr){return pmem + (paddr - RESET_VECTOR);}
#define READ 1
#define WRITE 0
char mtrace[128] = {0};
void record_mem_trace(int rw,paddr_t addr, int len){
	char *m = mtrace;
	if (rw == READ)
		m += sprintf(m , "READ  ");
	else
		m += sprintf(m,  "WRITE ");
	m += sprintf(m, "%dbyte/mask in %#x",len,addr);
}

extern "C" uint32_t pmem_read(uint32_t paddr){
	if(paddr > 0x87ffffff || paddr < RESET_VECTOR) return 0;
	uint32_t *inst_paddr = (uint32_t *)guest_to_host(paddr);
	record_mem_trace(READ, paddr , sizeof(uint32_t));	
	log_write("%s ", mtrace);
	log_write("content : %#x\n",*inst_paddr);
	return *inst_paddr;
}

extern "C" void pmem_write(int waddr, int wdata, char wmask){
	if(waddr > 0x87ffffff) return;
	uint8_t *vaddr = guest_to_host(waddr);
	uint8_t *iaddr;
	int i;
	int j;
	for(i = 0,j = 0;i < 4;i++){
		if(wmask & (1 << i)){
			iaddr = vaddr + i;
			*iaddr = (wdata >> (j * 8)) & 0xFF;
			j++;
		}
	}
	record_mem_trace(WRITE,waddr,wmask);	
	log_write("%s\n", mtrace);
}
