#include <memory.h>
#include <common.h>
#include <device.h>
static uint8_t *pmem = NULL;
static uint8_t *flash = NULL;
static uint8_t *psram = NULL;
static uint8_t *sdramChip0 = NULL;
static uint8_t *sdramChip1 = NULL;
static uint8_t *sdramChip2 = NULL;
static uint8_t *sdramChip3 = NULL;
char mtrace[128] = {0};
char *char_test = "/Users/yizishun/ysyx-workbench/char-test.bin";
#define READ 1
#define WRITE 0

//static const uint32_t img[] = {
//	0b00000000010000000000000001101111, // jal x0, 0x4 (jump to next instruction)   0x80000000
//    0b00000000000100000000000010010011, // addi x1, x0, 1 (x1 = 1)                  0x80000004
//    0b00000000001000000000000100010011, // addi x2, x0, 2 (x2 = 2)                  0x80000008
//    0b00000000001100000000000110010011, // addi x3, x0, 3 (x3 = 3)                  0x8000000C
//    0b00000000010000000000001000010011, // addi x4, x0, 4 (x4 = 4)                  0x80000010
//    0b00000000010100000000001010010011, // addi x5, x0, 5 (x5 = 5)                  0x80000014
//    0b00000000011000000000001100010011, // addi x6, x0, 6 (x6 = 6)                  0x80000018
//    0b00000000011100000000001110010011, // addi x7, x0, 7 (x7 = 7)                  0x8000001C
//    0b00000000000000010000010000110011, // add  x8, x2, x0 (x8 = x2 + x0 = 2)       0x80000020
//    0b00000000000100100000010010110011, // add  x9, x5, x1 (x9 = x5 + x1 = 6)       0x80000024
//    0b00000000001000110000010100110011, // add x10, x6, x2 (x10 = x6 + x2 = 8)      0x80000028
//    0b00000000001101000000010110110011, // add x11, x8, x3 (x11 = x8 + x3 = 5)      0x8000002C
//    0b00000000000100000000000001110011  // ebreak                                  0x80000030
//};
static const uint32_t img[] = {
	0b00000000010000000000000001101111, // jal x0, 0x4 (jump to next instruction)   0x30000000
    0b00000000000100000000000010010011, // addi x1, x0, 1 (x1 = 1)                  0x30000004
    0b00000000001000000000000100010011, // addi x2, x0, 2 (x2 = 2)                  0x30000008
    0b00000000001100000000000110010011, // addi x3, x0, 3 (x3 = 3)                  0x3000000C
    0b00000000010000000000001000010011, // addi x4, x0, 4 (x4 = 4)                  0x30000010
    0b00000000010100000000001010010011, // addi x5, x0, 5 (x5 = 5)                  0x30000014
    0b00000001000000000000000001101111, // jal x0, 0x30000028 (jump to 0x30000028)   0x30000018
    0b00000000011100000000001110010011, // addi x7, x0, 7 (x7 = 7)                  0x3000001C
    0b00000000000000010000010000110011, // add  x8, x2, x0 (x8 = x2 + x0 = 2)       0x30000020
    0b00000000000100100000010010110011, // add  x9, x5, x1 (x9 = x5 + x1 = 6)       0x30000024
    0b00000000001000110000010100110011, // add x10, x6, x2 (x10 = x6 + x2 = 8)      0x30000028
    0b00000000001101000000010110110011, // add x11, x8, x3 (x11 = x8 + x3 = 5)      0x3000002C
    0b00000000000100000000000001110011  // ebreak                                  0x30000030
};

//static const uint32_t img[] = {
//    0b00000000010000000000000001101111, // jal x0, 0x4 (jump to next instruction)       0
//    0b00000000000100000000000010010011, // addi x1, x0, 1 (x1 = 1)                   	4
//    0b10100000000000000000011000110111, // lui  x12, 0xA0000 (x12 = 0xA0000000)      	8
//    0b00000000000101100000000000100011, // sb   x1, 0(x12) (store x1 to 0xA0000000)  	c
//    0b00000000000001100000011010000011, // lb   x13, 0(x12) (load from 0xA0000000 to x13) 10
//    0b00000000001000000000000100010011, // addi x2, x0, 2 (x2 = 2)                   	14
//    0b00000000001100000000000110010011, // addi x3, x0, 3 (x3 = 3)                   	18
//    0b00000000010000000000001000010011, // addi x4, x0, 4 (x4 = 4)                   	1c
//    0b00000000010100000000001010010011, // addi x5, x0, 5 (x5 = 5)                   	20
//    0b00000000011000000000001100010011, // addi x6, x0, 6 (x6 = 6)                   	24
//    0b00000000011100000000001110010011, // addi x7, x0, 7 (x7 = 7)                   	28
//    0b00000000000000010000010000110011, // add  x8, x2, x0 (x8 = x2 + x0 = 2)        	3c
//	0b00000001000001001000010100110011, //add x10, x5, x8 (x10 = 7)						40
//	0b01000000100101010000010110110011, //sub x11, x10, x5 (x11 = 2)					44
//	0b00000001000001010111011000110011, //and x12, x10, x8 (x12 = 2)					48
//	0b00000000101001010100011010110011, //xor x13, x10, x6 (x13 = 1)					4c
//	0b00000000000101010001011100110011, //sll x14, x10, 1 (x14 = 14)					50
//    0b00000000000100100000010010110011, // add  x9, x5, x1 (x9 = x5 + x1 = 6) 			54       
//    0b00000000001000110000010100110011, // add x10, x6, x2 (x10 = x6 + x2 = 8)     		58  
//    0b00000000001101000000010110110011, // add x11, x8, x3 (x11 = x8 + x3 = 5)       	5c
//    0b00000000000100000000000001110011  // ebreak                                    	60
//};



static long load(char *img_file, uint32_t addr) {
  if (img_file == NULL) {
	assert(0);    
    return 72; // built-in image size
  }

  FILE *fp = fopen(img_file, "rb");
  assert(fp);

  fseek(fp, 0, SEEK_END);
  long size = ftell(fp);

  Log("The loading file is %s, size = %ld", img_file, size);
  fflush(stdout);

  fseek(fp, 0, SEEK_SET);
  int ret = fread(guest_to_host(addr), size, 1, fp);
  assert(ret == 1);

  fclose(fp);
  return size;
}


uint8_t *guest_to_host(uint32_t paddr){
	if(in_flash(paddr))
		return flash + (paddr - FLASH_BASE);
	#if defined(NPC)
	else if(in_pmem(paddr))
		return pmem + (paddr - PMEM_BASE);
	#elif defined(ysyxSoCFull)
	else if(in_psram(paddr))
		return psram + (paddr - PSRAM_BASE);
	#endif
	else{
		panic("%#x is out of bound in npc", paddr);
	}
}

void init_mem(size_t size){ 
	pmem = (uint8_t *)malloc(size * sizeof(uint8_t));
	memcpy(pmem , img , sizeof(img));
	if(pmem == NULL){exit(0);}
	Log("npc physical mrom area [%#x, %#lx]",RESET_VECTOR, RESET_VECTOR + size * sizeof(uint8_t));
}

void init_flash() {
	flash = (uint8_t *)malloc(256 * 16 * 16 * 256 * sizeof(uint8_t));
	if(flash == NULL) assert(0);
	memcpy(flash , img , sizeof(img));
	Log("flash area [%#x, %#x]",FLASH_BASE, FLASH_BASE + FLASH_SIZE);
}

void init_psram() {
	psram = (uint8_t *)malloc(0x20000000 * sizeof(uint8_t));
	if(psram == NULL) assert(0);
	Log("psram area [%#x, %#x]",PSRAM_BASE, PSRAM_BASE + PSRAM_SIZE);
}

void init_sdram() {
	sdramChip0 = (uint8_t *)malloc(0x20000000 * sizeof(uint8_t));
	sdramChip1 = (uint8_t *)malloc(0x20000000 * sizeof(uint8_t));
	sdramChip2 = (uint8_t *)malloc(0x20000000 * sizeof(uint8_t));
	sdramChip3 = (uint8_t *)malloc(0x20000000 * sizeof(uint8_t));
	if(sdramChip0 == NULL) assert(0);
	if(sdramChip1 == NULL) assert(0);
	if(sdramChip2 == NULL) assert(0);
	if(sdramChip3 == NULL) assert(0);
	Log("sdram area [%#x, %#x]",SDRAM_BASE, SDRAM_BASE + SDRAM_SIZE);
}

void record_mem_trace(int rw,paddr_t addr, int len){
	char *m = mtrace;
	if (rw == READ)
		m += sprintf(m , "READ  ");
	else
		m += sprintf(m,  "WRITE ");
	m += sprintf(m, "%dbyte/mask in %#x",len,addr);
}


extern "C" void mrom_read(int addr, int *data) {
	assert(0);
	int align_addr = addr & (~3);
	*data = *(int *)guest_to_host(align_addr);
	record_mem_trace(READ, addr , sizeof(uint32_t));	
	return;
}

extern "C" void flash_read(int addr, int *data) {
	int align_addr = addr + FLASH_BASE;
	*data = *(int *)guest_to_host(align_addr);
	//printf("addr = %#x , data = %#x \n",align_addr, *data);
	record_mem_trace(READ, addr , sizeof(uint32_t));	
	return;
}

extern "C" void psram_read(int addr, int *data) {
	int align_addr = addr + PSRAM_BASE;
	*data = *(int *)guest_to_host(align_addr);
	//printf("READ  addr = %#x , data = %#x \n",align_addr, *data);
	record_mem_trace(READ, addr , sizeof(uint32_t));	
	return;
}

extern "C" void psram_write(int addr, int wdata, int wstrb) {
	int align_addr = addr + PSRAM_BASE;
	fflush(stdout);
	switch (wstrb)
	{
	case 0b0001:
		*(uint8_t *)guest_to_host(align_addr) = wdata;
		//printf("WRITE addr = %#x , data = %#x ,wstrb = %d\n",align_addr, wdata, wstrb);
		break;
	case 0b0011:
		*(uint16_t *)guest_to_host(align_addr) = wdata;
		//printf("WRITE addr = %#x , data = %#x ,wstrb = %d\n",align_addr, wdata, wstrb);
		break;
	case 0b1111:
		*(uint32_t *)guest_to_host(align_addr) = wdata;
		//printf("WRITE addr = %#x , data = %#x ,wstrb = %d\n",align_addr, wdata, wstrb);
		break;
	default:
		break;
	}
	return;
}

uint8_t *guest_to_host_sdram(uint32_t paddr, int chipid){
	if(chipid == 0)
		if(in_sdram(paddr))
			return sdramChip0 + (paddr - SDRAM_BASE);
	if(chipid == 1)
		if(in_sdram(paddr))
			return sdramChip1 + (paddr - SDRAM_BASE);
	if(chipid == 2)
		if(in_sdram(paddr))
			return sdramChip2 + (paddr - SDRAM_BASE);
	if(chipid == 3)
		if(in_sdram(paddr))
			return sdramChip3 + (paddr - SDRAM_BASE);
}

extern "C" void sdram_read(int chipid, int ba, int ra, int ca, int *data) {
	int align_addr = (ba * 512 * 2) + (ra * 512 * 2 * 4) + (ca * 2) + SDRAM_BASE;
	*data = *(uint16_t *)guest_to_host_sdram(align_addr, chipid);
	align_addr = (chipid == 2 || chipid == 3)? align_addr + 0x2000000 : align_addr;
	//printf("READ  addr = %#x , data = %#x ",align_addr, *data);
	//printf(" id = %d ba = %d, ra = %d, ca = %d\n", chipid, ba, ra, ca);
	record_mem_trace(READ, align_addr , sizeof(uint32_t));	
	return;
}


extern "C" void sdram_write(int chipid, int ba, int ra, int ca, int wdata, int wstrb) {
	int align_addr = (ba * 512 * 2) + (ra * 512 * 2 * 4) + (ca * 2) + SDRAM_BASE;
	fflush(stdout);
	switch (wstrb)
	{
	case 0b0001:
		*(uint8_t *)guest_to_host_sdram(align_addr, chipid) = wdata;
		align_addr = (chipid == 2 || chipid == 3)? align_addr + 0x2000000 : align_addr;
		//printf("WRITE addr = %#x , data = %#x ,wstrb = %d",align_addr, wdata, wstrb);
		//printf(" id = %d ba = %d, ra = %d, ca = %d\n", chipid, ba, ra, ca);
		break;
	case 0b0010:
		*(uint8_t *)(guest_to_host_sdram(align_addr, chipid) + 1) = (wdata >> 8);
		align_addr = (chipid == 2 || chipid == 3)? align_addr + 0x2000000 : align_addr;
		//printf("WRITE addr = %#x , data = %#x ,wstrb = %d",align_addr, wdata >> 8, wstrb);
		//printf(" id = %d ba = %d, ra = %d, ca = %d\n", chipid, ba, ra, ca);
		break;
	case 0b0011:
		*(uint16_t *)guest_to_host_sdram(align_addr, chipid) = wdata;
		align_addr = (chipid == 2 || chipid == 3)? align_addr + 0x2000000 : align_addr;
		//printf("WRITE addr = %#x , data = %#x ,wstrb = %d",align_addr, wdata, wstrb);
		//printf(" id = %d ba = %d, ra = %d, ca = %d\n", chipid, ba, ra, ca);
		break;
	case 0b1111:
		assert(0);
		*(uint32_t *)guest_to_host_sdram(align_addr, chipid) = wdata;
		align_addr = (chipid == 2 || chipid == 3)? align_addr + 0x2000000 : align_addr;
		//printf("WRITE addr = %#x , data = %#x ,wstrb = %d\n",align_addr, wdata, wstrb);
		//printf(" id = %d ba = %d, ra = %d, ca = %d\n", chipid, ba, ra, ca);
		break;
	default:
		//printf("wstrb is %d\n", wstrb);
		break;
	}
	return;
}

static int32_t device_read = 0;
static uint64_t timer = 0;
extern bool is_skip_diff;
void init_flag(){ //bug fix
	if(device_read < 0)
		device_read = 0;
}

extern "C" int pmem_read(int paddr){
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
	//printf("READ %#x in %#x\n",*inst_paddr,paddr);
	return *inst_paddr;
}

extern "C" void pmem_write(int waddr, int wdata, char wmask){
	//printf("WRITE %#x to %#x,wmask = %#x\n",wdata,waddr,wmask);
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
