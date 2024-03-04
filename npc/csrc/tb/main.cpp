#include <Vysyx_23060171_cpu.h>
#include <verilated.h>
#include <verilated_vcd_c.h>
#include <svdpi.h>
#include <Vysyx_23060171_cpu__Dpi.h>
#include <memory.h>
Vysyx_23060171_cpu cpu;
VerilatedVcdC* m_trace = nullptr;
VerilatedContext* contextp = nullptr;
void init_monitor(int, char *[]);
uint32_t isa_reg_str2val(const char *s, bool *success);
void isa_reg_display();

void single_cycle(){
	cpu.clk=0;cpu.eval();
	cpu.clk=1;cpu.eval();
}

static void reset(int n) {
	cpu.rst = 1;
 	while (n -- > 0) single_cycle();
	cpu.rst = 0;
	m_trace->dump(contextp -> time());
	contextp -> timeInc(1);
}
extern "C" void npc_trap(){
	m_trace->dump(contextp -> time());
	contextp -> timeInc(1);
	m_trace -> close();
	bool success;
	int code = isa_reg_str2val("$a0",&success);
	if(code == 0)
		printf("\033[1;32mHIT GOOD TRAP\033[0m");
	else
		printf("\033[1;31mHIT BAD TRAP\033[0m exit code = %d",code);
	printf(" trap in %#x\n",cpu.pc);
	exit(0);
}
int main(int argc, char *argv[]){
	init_monitor(argc,argv);

	Verilated::traceEverOn(true);
	contextp = new VerilatedContext;	
	m_trace = new VerilatedVcdC;
	cpu.trace(m_trace, 5);
	m_trace->open("builds/waveform.vcd");
	
	reset(10);
	while(1){
		cpu.inst = pmem_read(cpu.pc);
		cpu.eval();
		single_cycle();
		m_trace->dump(contextp -> time());
		contextp -> timeInc(1);
	}
	m_trace -> close();
}
