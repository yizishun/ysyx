#include <memory.h>
#include <common.h>
#include <device.h>
extern bool is_skip_diff;
static const uint32_t img[] = {
	0b00000000110000000000001011101111, //jal   x5 12         0x80000000
	0b00000000000000000001001000110111, //lui   x4 1          0x80000004
	0b00000000000000000001000110010111, //auipc x3 1          0x80000008
	0b00000000010100000000000010010011, //addi  x1 x0 5       0x8000000c
	0b00000000010100000000000010010011, //addi  x1 x0 5       0x80000010
	0b00000000000100000000000100010011, //addi  x2 x0 1       0x80000014
	0b00000000001000000000000100010011, //addi  x2 x0 2       0x80000018
	0b00000000000001010000010100010011, //addi x10 x10 0      0x8000001c mv a0,a0;
	0b00110000010100010001001001110011, //csrrw x4 mstatus x2 0x80000020 mstatus=0b010 x2=0b010 x4=0b000
	0b00110000010100001010001011110011, //csrrs x5 mstatus x1 0x80000024 mstatus=0b111 x1=0b101 x5=0b010
	0b00000000000100000000000001110011  //ebreak              0x80000028  
};
static uint8_t *pmem = NULL;
static int32_t device_read = 0;
static uint64_t timer = 0;
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

void init_flag(){ //bug fix
	if(device_read < 0)
		device_read = 0;
}

extern "C" uint32_t pmem_read(uint32_t paddr){
	if(!((paddr >= 0x80000000 && paddr <= 0x87ffffff) || (paddr == RTC_ADDR) || (paddr == RTC_ADDR + 4))) 
		return 0;
	if(paddr == RTC_ADDR || paddr == RTC_ADDR + 4)
		assert(0);
	init_flag();
	//printf("\ndev_r = %d\n",device_read);
	#ifdef CONFIG_TRACE
	#ifdef CONFIG_MTRACE
	record_mem_trace(READ, paddr , sizeof(uint32_t));	
	log_write("%s\n", mtrace);
	#endif
	#endif
	if(device_read == 3) device_read = 0;
	if(paddr == RTC_ADDR+4 && device_read == 0) {
		device_read++; 
		timer = get_time(); 
		return (uint32_t)(timer >> 32);
	}
	else if(paddr == RTC_ADDR) {
		device_read++;
		return (uint32_t)timer;
	}
	else if(paddr == RTC_ADDR + 4 && device_read != 0){
		device_read++;
		return (uint32_t)(timer >> 32);
	}
	else if(paddr == SERIAL_PORT) return 0;
	uint32_t *inst_paddr = (uint32_t *)guest_to_host(paddr);
	record_mem_trace(READ, paddr , sizeof(uint32_t));	
	//log_write("content : %#x\n",*inst_paddr);
	return *inst_paddr;
}

extern "C" void pmem_write(int waddr, int wdata, char wmask){
	if(!((waddr >= 0x80000000 && waddr <= 0x87ffffff) || (waddr == SERIAL_PORT))) 
		return;
	if(waddr == SERIAL_PORT){
		assert(0);
	}

#ifdef CONFIG_TRACE
#ifdef CONFIG_MTRACE
	record_mem_trace(WRITE,waddr,wmask);	
	log_write("%s\n", mtrace);
#endif
#endif
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
}

extern "C" void skip(){
	is_skip_diff = true;
	return;
}


extern "C" void mrom_read(int addr, int *data) {
	*data = *(int *)guest_to_host(addr);
	record_mem_trace(READ, addr , sizeof(uint32_t));	
	return;
}