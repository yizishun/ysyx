/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include <isa.h>
#include "local-include/reg.h"
#define REGNUMBER ARRLEN(regs)
const char *regs[] = {
  "$0", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
  "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
  "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
  "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
};

void isa_reg_display() {
  int i;
  printf("\ndut-pc=%x\n",cpu.pc);
  for(i = 0;i < REGNUMBER;i++){
    if(cpu.gpr[i] >= 0x80000000){
      printf("dut-%3s = %-#11x",regs[i],cpu.gpr[i]);
      if(i % 3 == 0) printf("\n");
      }
    else{
      printf("dut-%3s = %-11d",regs[i],cpu.gpr[i]);
      if(i % 3 == 0) printf("\n");
      } 
    }
  printf("\n");
  printf("dut-mtvec  = %-#11x\n",cpu.csr[MTVEC]);
  printf("dut-mepc   = %-#11x\n",cpu.csr[MEPC]);
  printf("dut-mstatus= %-#11x\n",cpu.csr[MSTATUS]);
  printf("dut-mcause = %-#11x\n",cpu.csr[MCAUSE]);
}

word_t isa_reg_str2val(const char *s, bool *success) {
  int i;
  for(i = 0;i < REGNUMBER;i++){
    if(strcmp(regs[i],s+1) == 0)
      break;
  }
  
  if(i < REGNUMBER){
    *success = true;
    //printf("%3s		%d\n",regs[i],cpu.gpr[i]);
  }
  else {
    if(strcmp("pc",s+1) == 0){
	*success = true;
	return cpu.pc;
}
    *success = false;
}
  return cpu.gpr[i];
}
