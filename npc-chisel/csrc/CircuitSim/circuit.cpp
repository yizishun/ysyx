#include <circuit.h>
#include <memory.h>
#include <common.h>
#include <ftrace.h>
#include <pevent.h>
CPU *cpu;
extern "C" void disassemble(char *str, int size, uint64_t pc, uint8_t *code, int nbyte);
static void statistic();
void difftest_step();
#define MAX_INST_TO_PRINT 10
uint64_t cycle = 0;
uint64_t instCnt = 0;
uint32_t waveCounter = 0;
bool prev_wbu;
bool isIRQ;
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

void difftest(){
	if(isIRQ){
		difftest_step();
		isIRQ = 0;
		return;
	}
	if(prev_wbu){
		if(cpu->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__isIRQ == 1){
			isIRQ = 1;
			return;
		}
		difftest_step();
	}
}

static void trace_and_difftest(){
	/* DiffTest */
	#ifdef CONFIG_DIFFTEST
	difftest();
	#endif

	/* watchpoint check */
	extern int check_w();
  	int no = check_w();
  	if(no != 0){
    	printf("NO.%d watchpoint has been trigger\n",no);
		return;
  	}

	#ifdef CONFIG_TRACE
	/* trace(1):instruction trace */
	char disasm_buf[128] = {0};
	if(cpu->rootp->V_E_GETINST == 1){
		record_inst_trace(disasm_buf,(uint8_t *)&inst);
		//print to stdout
		if(g_print_step) puts(disasm_buf);
		//print to log file
		log_write("%s\n", disasm_buf);
	}
	#endif

	#ifdef CONFIG_FTRACE
	/* trace(2):function trace*/
	extern char * elf_file;
	opcode = BITS(inst, 6, 0);
	if(opcode == JAL || opcode == JALR){
		ftrace_check(opcode ,prev_pc, dnpc, inst);
	}
	#endif

	#ifdef CONFIG_CSV
	/* trace: performance event trace */
	record_perf_trace(cycle, instCnt);
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
		prev_pc = cpu->rootp -> V_PC;
		prev_wbu = cpu->rootp->WBU;
		snpc = pc + 4;
		if(cpu->rootp->V_E_GETINST == 1)
			instCnt ++;
		cycle ++;

  		uint64_t timer_start = get_time();
		exec_once();
  		uint64_t timer_end = get_time();
  		g_timer += timer_end - timer_start;

		if(cpu->rootp->V_INST != 0){
			inst = cpu->rootp->V_INST;
		}
		pc = cpu->rootp->V_PC;
		dnpc = cpu->rootp->V_PC;
		get_reg();
		waveCounter ++;
		if(waveCounter >= 1000000){
			close_wave();
			if(remove("builds/waveform.vcd") != 0) assert(0);
			init_wave();
			waveCounter = 0;
		}
		trace_and_difftest();
		if(cpu->rootp->V_INST != 0)
			n--;
	}
}

static void statistic() {
	uint64_t total_time = DECisJump.time + DECisCsr.time + DECisCal.time + DECisJump.time + DECisLoad.time + DECisStore.time;
	double Jump_pro = (double)DECisJump.time * 100 / (double)total_time;
	double Csr_pro = (double)DECisCsr.time * 100 / (double)total_time;
	double Cal_pro = (double)DECisCal.time * 100 / (double)total_time;
	double Load_pro = (double)DECisLoad.time * 100 / (double)total_time;
	double Store_pro = (double)DECisStore.time * 100 / (double)total_time;
	double Other_pro = (double)DECisOther.time * 100 / (double)total_time;
	double IFUGetInst_pro = (double)IFUGetInst.time * 100 / (double)cycle;
	double LSUGetData_pro = (double)LSUGetData.time * 100 / (double)cycle;
	double EXUFinCal_pro = (double)EXUFinCal.time * 100 / (double)cycle;
	Log("total cycle      = %llu", cycle);
	Log("total inst       = %llu", instCnt);
	Log("IPC              = %lf", (double)instCnt / (double)cycle);
	Log("CPI              = %lf", (double)cycle / (double)instCnt);
	Log("IFUGetInst Event = %llu", IFUGetInst.cnt);
	Log("IFUGetInst/cycle = %lf", (double)IFUGetInst.time / (double)IFUGetInst.cnt);
	Log("IFUGetInst total time = %llu", IFUGetInst.time);
	Log("LSUGetData Event = %llu", LSUGetData.cnt);
	Log("LSUGetData/cycle = %lf", (double)LSUGetData.time / (double)LSUGetData.cnt);
	Log("LSUGetData total time = %llu", LSUGetData.time);
	Log("EXUFinCal  Event = %llu", EXUFinCal.cnt);
	Log("EXUFinCal/cycle = %lf", (double)EXUFinCal.time / (double)EXUFinCal.cnt);
	Log("EXUFinCal total time = %llu", EXUFinCal.time);
	Log("Jump             = %llu", DECisJump.cnt);
	Log("Jump per cycle   = %lf",  (double)DECisJump.time / (double)DECisJump.cnt);
	Log("Jump total time  = %llu", DECisJump.time);
	Log("Store            = %llu", DECisStore.cnt);
	Log("Store per cycle  = %lf",  (double)DECisStore.time / (double)DECisStore.cnt);
	Log("Store total time = %llu", DECisStore.time);
	Log("Load             = %llu", DECisLoad.cnt);
	Log("Load per cycle   = %lf",  (double)DECisLoad.time / (double)DECisLoad.cnt);
	Log("Load tatal time  = %llu", DECisLoad.time);
	Log("Cal              = %llu", DECisCal.cnt);
	Log("Cal per cycle    = %lf",  (double)DECisCal.time / (double)DECisCal.cnt);
	Log("Cal total time   = %llu", DECisCal.time);
	Log("Csr              = %llu", DECisCsr.cnt);
	Log("Csr per cycle    = %lf",  (double)DECisCsr.time / (double)DECisCsr.cnt);
	Log("Csr total time   = %llu", DECisCsr.time);
	Log("Other            = %llu", DECisOther.cnt);
	Log("Other per cycle  = %lf",  (double)DECisOther.time / (double)DECisOther.cnt);
	Log("Other total time = %llu", DECisOther.time);
	Log("iCache Hit        = %llu", ICacheHit.cnt);
	Log("iCache Hit per cycle  = %lf",  (double)ICacheHit.time / (double)ICacheHit.cnt);
	Log("iCache Hit total time = %llu", ICacheHit.time);
	Log("iCache Miss       = %llu", ICacheMiss.cnt);
	Log("iCache Miss per cycle  = %lf",  (double)ICacheMiss.time / (double)ICacheMiss.cnt);
	Log("iCache Miss total time = %llu", ICacheMiss.time);
	Log("iCache HIT ratio  = %lf", (double)ICacheHit.cnt / (double)IFUGetInst.cnt);
	Log("iCache AMAT = %lf", (double)ICacheHit.time / (double)ICacheHit.cnt + (1-(double)ICacheHit.cnt / (double)IFUGetInst.cnt) * (double)ICacheMiss.time / (double)ICacheMiss.cnt);
	Log("proportion Jump | Store | Load | Cal | Csr | Other");
	Log("   	 %2.2lf%% %2.2lf%% %2.2lf%% %2.2lf%% %2.2lf%%  %2.2lf%%", Jump_pro, Store_pro, Load_pro, Cal_pro, Csr_pro, Other_pro);
	Log("proportion IFUGetInst | LSUGetData | EXUFinCal");
	Log("   	  %2.2lf%%       %2.2lf%%       %2.2lf%%", IFUGetInst_pro, LSUGetData_pro, EXUFinCal_pro);
	Log("host time spent  = %llu us", g_timer);
  #ifdef CONFIG_CSV
	fprintf(perf_time_fp, "%llu,%llu,%llu,%llu,%llu,%llu,%llu,%llu,%llu,%llu\n", cycle, IFUGetInst.time, EXUFinCal.time, LSUGetData.time, DECisJump.time, DECisStore.time, DECisLoad.time, DECisCal.time, DECisCsr.time, DECisOther.time);
	fprintf(perf_time_fp, "%lf,%lf,%lf,%lf,%lf,%lf,%lf,%lf,%lf,%lf\n",(double)cycle/(double)instCnt, (double)IFUGetInst.time / (double)IFUGetInst.cnt, (double)EXUFinCal.time / (double)EXUFinCal.cnt, (double)LSUGetData.time / (double)LSUGetData.cnt, 
	(double)DECisJump.time / (double)DECisJump.cnt, (double)DECisLoad.time / (double)DECisLoad.cnt,(double)DECisStore.time / (double)DECisStore.cnt, (double)DECisCal.time / (double)DECisCal.cnt, (double)DECisCsr.time / (double)DECisCsr.cnt, (double)DECisOther.time/(double)DECisOther.cnt);
  #endif
  if (g_timer > 0) Log("simulation frequency = %llu inst/s", cycle * 1000000 / g_timer);
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
