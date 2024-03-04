#include <memory.h>
#include <Vysyx_23060171_cpu.h>
#include <svdpi.h>
uint32_t gpr[32];
static int size = 32;
void isa_reg_display();
extern Vysyx_23060171_cpu cpu;
const char *regs[] = {
  "$0", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
  "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
  "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
  "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
};

extern "C" void Get_reg(uint32_t rf[]){  //dpi-c
    for (int i = 0; i < 32; ++i) {
        gpr[i] = rf[i];
    }
}

void isa_reg_display() {
  int i;
  printf("\ndut-pc=%x\n",cpu.pc);
  for(i = 0;i < size;i++){
    if(gpr[i] >= 0x80000000){
      printf("dut-%3s = %-#11x",regs[i],gpr[i]);
      if(i % 3 == 0) printf("\n");
      }
    else{
      printf("dut-%3s = %-11d",regs[i],gpr[i]);
      if(i % 3 == 0) printf("\n");
      } 
    }
}

uint32_t isa_reg_str2val(const char *s, bool *success) {
  int i;
  for(i = 0;i < size;i++){
    if(strcmp(regs[i],s+1) == 0)
      break;
  }
  
  if(i < size){
    *success = true;
    //printf("%3s		%d\n",regs[i],gpr[i]);
  }
  else {
    if(strcmp("pc",s+1) == 0){
	*success = true;
	return cpu.pc;
    }
    *success = false;
}
  //printf("i = %d gpr = %d",i,gpr[10]);
  return gpr[i];
}