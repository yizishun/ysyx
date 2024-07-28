#include <memory.h>
#include <svdpi.h>
#include <circuit.h>
uint32_t gpr[REGNUM];
uint32_t csr[4];
void isa_reg_display();
const char *regs[] = {
  "$0", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
  "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
  "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
  "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
};

void get_reg(){
  int i;
  for(i = 0;i < REGNUM; i++)
    gpr[i] = cpu->rootp -> ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__gpr__DOT__rf_ext__DOT__Memory[i];
  for(i = 0;i < 4;i++)
    csr[i] = cpu->rootp -> ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__csr__DOT__rf_ext__DOT__Memory[i];
    //0:mstatus 1:mtvec 2:mepc 3:mcause
}

void isa_reg_display() {
  int i;
  printf("\ndut-pc=%x\n",pc);
  for(i = 0;i < REGNUM;i++){
    if(gpr[i] >= 0x02000000){
      printf("dut-%3s = %-#11x",regs[i],gpr[i]);
      if(i % 3 == 0) printf("\n");
      }
    else{
      printf("dut-%3s = %-11d",regs[i],gpr[i]);
      if(i % 3 == 0) printf("\n");
      } 
    }
  printf("\n");
  printf("dut-mstatus = %#x\n",csr[0]);
  printf("dut-mtvec = %#x\n",csr[1]);
  printf("dut-mepc = %#x\n",csr[2]);
  printf("dut-mcause = %#x\n",csr[3]);
}

uint32_t isa_reg_str2val(const char *s, bool *success) {
  int i;
  for(i = 0;i < REGNUM;i++){
    if(strcmp(regs[i],s+1) == 0)
      break;
  }
  
  if(i < REGNUM){
    *success = true;
    //printf("%3s		%d\n",regs[i],gpr[i]);
  }
  else {
    if(strcmp("pc",s+1) == 0){
	*success = true;
	return pc;
    }
    *success = false;
}
  //printf("i = %d gpr = %d",i,gpr[10]);
  return gpr[i];
}