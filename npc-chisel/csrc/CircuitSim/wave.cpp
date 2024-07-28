#include <circuit.h>
static VerilatedVcdC* m_trace = nullptr;
static VerilatedContext* contextp = nullptr;

void init_wave(){
    Verilated::traceEverOn(true);
	contextp = new VerilatedContext;	
	m_trace = new VerilatedVcdC;
	cpu->trace(m_trace, 5);
	m_trace->open("builds/waveform.vcd");
} 

void dump_wave_inc(){
    m_trace->dump(contextp -> time());
	contextp -> timeInc(1);
} 

void close_wave(){
    m_trace -> close();
}