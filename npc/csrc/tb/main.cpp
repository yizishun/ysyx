#include <Vysyx_23060171_cpu.h>
#include <verilated.h>
#include <verilated_vcd_c.h>
#include <svdpi.h>
#include <Vysyx_23060171_cpu__Dpi.h>
#include <memory.h>
static Vysyx_23060171_cpu dut;
VerilatedVcdC* m_trace = nullptr;
VerilatedContext* contextp = nullptr;
void single_cycle(){
	dut.clk=0;dut.eval();
	dut.clk=1;dut.eval();
}

static void reset(int n) {
	dut.rst = 1;
 	while (n -- > 0) single_cycle();
	dut.rst = 0;
}
extern "C" void npc_trap(){
	m_trace->dump(contextp -> time());
	contextp -> timeInc(1);
	m_trace -> close();
	printf("trap in %#x\n",dut.pc);
	exit(0);
}
int main(){
	init_mem(100000);

	Verilated::traceEverOn(true);
	contextp = new VerilatedContext;	
	m_trace = new VerilatedVcdC;
	dut.trace(m_trace, 5);
	m_trace->open("builds/waveform.vcd");
	
	reset(10);
	while(1){
		dut.inst = pmem_read(dut.pc);
		dut.eval();
		single_cycle();
		m_trace->dump(contextp -> time());
		contextp -> timeInc(1);
	}
	m_trace -> close();
}
