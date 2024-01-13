#include <nvboard.h>
#include <Vtop.h>
#include <verilated_vcd_c.h>
static TOP_NAME dut;
void nvboard_bind_all_pins(Vtop* top);

void single_cycle(){
	dut.clk=0;dut.eval();
	dut.clk=1;dut.eval();
}

static void reset(int n) {
	dut.rst = 1;
 	while (n -- > 0) single_cycle();
	dut.rst = 0;
}


int main(){
	Verilated::traceEverOn(true);
	VerilatedContext* contextp = new VerilatedContext;
	nvboard_bind_all_pins(&dut);
	VerilatedVcdC *m_trace = new VerilatedVcdC;
	dut.trace(m_trace, 5);
	m_trace->open("builds/waveform.vcd");
	nvboard_init();
	reset(20);
	while(1){
		nvboard_update();
		single_cycle();
		m_trace->dump(contextp -> time());
		contextp ->timeInc(1);
	}
	m_trace ->close();
	nvboard_quit();
}
