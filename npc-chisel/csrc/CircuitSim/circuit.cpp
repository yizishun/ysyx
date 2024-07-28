#include <circuit.h>
#include <memory.h>
#include <common.h>
#include <ftrace.h>
VysyxSoCFull *cpu;
extern "C" void disassemble(char *str, int size, uint64_t pc, uint8_t *code, int nbyte);
static void statistic();
void difftest_step();
#define MAX_INST_TO_PRINT 10
uint64_t g_nr_guest_inst = 0;
uint32_t waveCounter = 0;
static uint64_t g_timer = 0;
static bool g_print_step = false;
word_t pc, snpc, dnpc,inst , prev_pc;
static uint8_t opcode;

void single_cycle(){  //  0 --> 0 > 1 --> 1 > 0 this is a cycle in cpu  _|-|_|-
	cpu->clock=1;   //posedge 0->1 refresh sequential logic
    cpu->eval();  //process 1->1 refresh sequential logic(sim)
	#ifdef CONFIG_WAVE
	dump_wave_inc();
	#endif
	cpu->clock=0;   //negedge 1->0 no
    cpu->eval();  //process 0->0 refresh combination logic and make them stable
	#ifdef CONFIG_WAVE
	dump_wave_inc();
	#endif
}

void reset(int n) {
	cpu->reset = 1;
 	while (n -- > 0) single_cycle();
	cpu->reset = 0;
	dump_wave_inc();
}

void assert_fail_msg() {
	nvboard_quit();
	isa_reg_display();
	statistic();
}

void record_inst_trace(char *p, uint8_t *inst){
  char *ps = p;
  p += snprintf(p,128, "%#x:",prev_pc);
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

  disassemble(p, ps+128-p, (uint64_t)prev_pc, inst, ilen);
}

static void trace_and_difftest(){
	/* DiffTest */
	#ifdef CONFIG_DIFFTEST
	if(prev_pc != pc){
		difftest_step();
	}
	#endif

	/* watchpoint check */
	extern int check_w();
  	int no = check_w();
  	if(no != 0){
    	printf("NO.%d watchpoint has been trigger\n",no);
		return;
  	}

	/* trace(1):instruction trace */
	//char disasm_buf[128] = {0};
	//if(cpu.rootp -> ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__ifu__DOT__nextStateC == 3){
		//record_inst_trace(disasm_buf,(uint8_t *)&inst);
		//print to stdout
		//if(g_print_step) puts(disasm_buf);
		//print to log file
		//log_write("%s\n", disasm_buf);
	//}

	#ifdef CONFIG_FTRACE
	/* trace(2):function trace*/
	extern char * elf_file;
	opcode = BITS(inst, 6, 0);
	if(opcode == JAL || opcode == JALR){
		ftrace_check(opcode ,prev_pc, dnpc, inst);
	}
	#endif

}

/* cpu single cycle in exec */
static void exec_once(){
	nvboard_update();
	single_cycle();
}

void cpu_exec(uint32_t n){
	//max inst to print to stdout
	g_print_step = (n < MAX_INST_TO_PRINT);
	while(n > 0){
		prev_pc = cpu->rootp -> ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__ifu__DOT__pc__DOT__pcReg;
		snpc = pc + 4;

  		uint64_t timer_start = get_time();
		exec_once();
  		uint64_t timer_end = get_time();
  		g_timer += timer_end - timer_start;

		//if(cpu.rootp -> ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__ifu__DOT__nextStateC == 3)
			//inst = cpu.rootp -> ysyxSoCFull__DOT__flash__DOT__rdata;
		pc = cpu->rootp -> ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__ifu__DOT__pc__DOT__pcReg;
		dnpc = cpu->rootp -> ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__ifu__DOT__pc__DOT__pcReg;
		get_reg();
		g_nr_guest_inst ++;
		waveCounter ++;
		if(waveCounter >= 1000000){
			close_wave();
			if(remove("builds/waveform.vcd") != 0) assert(0);
			init_wave();
			waveCounter = 0;
		}
		trace_and_difftest();
		if(cpu->rootp -> ysyxSoCFull__DOT__asic__DOT__lmrom__DOT___mrom_rdata != 0)
			n--;
	}
}

static void statistic() {
  Log("total cycle = %llu", g_nr_guest_inst);
  Log("host time spent = %llu us", g_timer);
  Log("1 clock = %lf us",(double)g_timer/ (double)g_nr_guest_inst);
  if (g_timer > 0) Log("simulation frequency = %llu inst/s", g_nr_guest_inst * 1000000 / g_timer);
  else Log("Finish running in less than 1 us and can not calculate the simulation frequency");
}

extern "C" void npc_trap(){
	nvboard_quit();
	dump_wave_inc();
	close_wave();
	bool success;
	int code = isa_reg_str2val("$a0",&success);
	if(code == 0)
		printf("\033[1;32mHIT GOOD TRAP\033[0m");
	else
		printf("\033[1;31mHIT BAD TRAP\033[0m exit code = %d",code);
	printf(" trap in %#x\n",pc);
	statistic();
	exit(0);
}
