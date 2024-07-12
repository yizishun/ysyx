#include <memory.h>
#include <common.h>
#include <device.h>
static uint8_t *pmem = NULL;
static uint8_t *flash = NULL;
static uint8_t *psram = NULL;
static uint8_t *sdram = NULL;
char mtrace[128] = {0};
char *char_test = "/Users/yizishun/ysyx-workbench/char-test.bin";
#define READ 1
#define WRITE 0
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
	else if(in_pmem(paddr))
		return pmem + (paddr - RESET_VECTOR);
	else if(in_psram(paddr))
		return psram + (paddr - PSRAM_BASE);
	else if(in_sdram(paddr))
		return sdram + (paddr - SDRAM_BASE);
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
	Log("flash area [%#x, %#x]",FLASH_BASE, FLASH_BASE + FLASH_SIZE);
}

void init_psram() {
	psram = (uint8_t *)malloc(0x20000000 * sizeof(uint8_t));
	if(psram == NULL) assert(0);
	Log("psram area [%#x, %#x]",PSRAM_BASE, PSRAM_BASE + PSRAM_SIZE);
}

void init_sdram() {
	sdram = (uint8_t *)malloc(0x20000000 * sizeof(uint8_t));
	if(sdram == NULL) assert(0);
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

extern "C" void sdram_read(int ba, int ra, int ca, int *data) {
	int align_addr = ba * ra * ca + SDRAM_BASE;
	*data = *(int *)guest_to_host(align_addr);
	//printf("READ  addr = %#x , data = %#x \n",align_addr, *data);
	record_mem_trace(READ, align_addr , sizeof(uint32_t));	
	return;
}


extern "C" void sdram_write(int ba, int ra, int ca, int wdata, int wstrb) {
	int align_addr = ba * ra * ca + SDRAM_BASE;
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
		assert(0);
		*(uint32_t *)guest_to_host(align_addr) = wdata;
		//printf("WRITE addr = %#x , data = %#x ,wstrb = %d\n",align_addr, wdata, wstrb);
		break;
	default:
		break;
	}
	return;
}

//static int32_t device_read = 0;
//static uint64_t timer = 0;
//extern bool is_skip_diff;
//void init_flag(){ //bug fix
//	if(device_read < 0)
//		device_read = 0;
//}
//
//extern "C" uint32_t pmem_read(uint32_t paddr){
//	if(!((paddr >= 0x80000000 && paddr <= 0x87ffffff) || (paddr == RTC_ADDR) || (paddr == RTC_ADDR + 4))) 
//		return 0;
//	if(paddr == RTC_ADDR || paddr == RTC_ADDR + 4)
//		assert(0);
//	init_flag();
//	//printf("\ndev_r = %d\n",device_read);
//	#ifdef CONFIG_TRACE
//	#ifdef CONFIG_MTRACE
//	record_mem_trace(READ, paddr , sizeof(uint32_t));	
//	log_write("%s\n", mtrace);
//	#endif
//	#endif
//	if(device_read == 3) device_read = 0;
//	if(paddr == RTC_ADDR+4 && device_read == 0) {
//		device_read++; 
//		timer = get_time(); 
//		return (uint32_t)(timer >> 32);
//	}
//	else if(paddr == RTC_ADDR) {
//		device_read++;
//		return (uint32_t)timer;
//	}
//	else if(paddr == RTC_ADDR + 4 && device_read != 0){
//		device_read++;
//		return (uint32_t)(timer >> 32);
//	}
//	else if(paddr == SERIAL_PORT) return 0;
//	uint32_t *inst_paddr = (uint32_t *)guest_to_host(paddr);
//	record_mem_trace(READ, paddr , sizeof(uint32_t));	
//	//log_write("content : %#x\n",*inst_paddr);
//	return *inst_paddr;
//}
//
//extern "C" void pmem_write(int waddr, int wdata, char wmask){
//	if(!((waddr >= 0x80000000 && waddr <= 0x87ffffff) || (waddr == SERIAL_PORT))) 
//		return;
//	if(waddr == SERIAL_PORT){
//		assert(0);
//	}
//
//#ifdef CONFIG_TRACE
//#ifdef CONFIG_MTRACE
//	record_mem_trace(WRITE,waddr,wmask);	
//	log_write("%s\n", mtrace);
//#endif
//#endif
//	uint8_t *vaddr = guest_to_host(waddr);
//	uint8_t *iaddr;
//	int i;
//	int j;
//	for(i = 0,j = 0;i < 4;i++){
//		if(wmask & (1 << i)){
//			iaddr = vaddr + i;
//			*iaddr = (wdata >> (j * 8)) & 0xFF;
//			j++;
//		}
//	}
//}
//
//extern "C" void skip(){
//	is_skip_diff = true;
//	return;
//}
