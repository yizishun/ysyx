#include <circuit.h>
#include <memory.h>
#include <stdio.h>
#include <string.h>
Vysyx_23060171_cpu cpu;
extern "C" void disassemble(char *str, int size, uint64_t pc, uint8_t *code, int nbyte);
#define MAX_INST_TO_PRINT 10
static bool g_print_step = false;

void single_cycle(){  //  0 --> 0 > 1 --> 1 > 0 this is a cycle in cpu
	cpu.clk=0;   //negedge 1->0 no
    cpu.eval();  //process 0->0 refresh combination logic and make them stable
	cpu.clk=1;   //posedge 0->1 refresh sequential logic
    cpu.eval();  //process 1->1 refresh sequential logic(sim)
}

void reset(int n) {
	cpu.rst = 1;
 	while (n -- > 0) single_cycle();
	cpu.rst = 0;
	dump_wave_inc();
}

void assert_fail_msg() {
  isa_reg_display();
}

void record_inst_trace(char *p, uint8_t *inst ,uint32_t pc){
  char *ps = p;
  p += snprintf(p,128, "%#x:",pc);
  int ilen = 4;
  int i;
  for (i = ilen - 1; i >= 0; i --) {
    p += snprintf(p, 4, " %02x", inst[i]);
  }
  int ilen_max = 4;
  int space_len = ilen_max - ilen;
  if (space_len < 0) space_len = 0;
  space_len = space_len * 3 + 1;
  memset(p, ' ', space_len);
  p += space_len;

  disassemble(p, ps+128-p, (uint64_t)pc, inst, ilen);
}
void cpu_exec(uint32_t n){
	g_print_step = (n < MAX_INST_TO_PRINT);
	while(n > 0){
		cpu.inst = pmem_read(cpu.pc);
		char p2[128] = {0};
		record_inst_trace(p2,(uint8_t *)&cpu.inst,cpu.pc);
		if(g_print_step)
			puts(p2);
		extern int check_w();
  		int no = check_w();
  		if(no != 0){
    		printf("NO.%d watchpoint has been trigger\n",no);
			return;
  		}
		single_cycle();
		dump_wave_inc();
		n--;
	}
}
extern "C" void npc_trap(){
	dump_wave_inc();
	close_wave();
	bool success;
	int code = isa_reg_str2val("$a0",&success);
	if(code == 0)
		printf("\033[1;32mHIT GOOD TRAP\033[0m");
	else
		printf("\033[1;31mHIT BAD TRAP\033[0m exit code = %d",code);
	printf(" trap in %#x\n",cpu.pc);
	exit(0);
}
