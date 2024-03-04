#include <circuit.h>
#include <memory.h>
Vysyx_23060171_cpu cpu;
void single_cycle(){  //  0 --> 0 > 1 --> 1 > 0 this is a cycle in cpu
	cpu.clk=0;   //negedge 1->0 no
    cpu.eval();  //process 0->0 refresh combination logic and make them stable
	cpu.clk=1;   //posedge 0->1 refresh sequential logic
    cpu.eval();  //process 1->1 refresh sequential logic(sim)
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

void reset(int n) {
	cpu.rst = 1;
 	while (n -- > 0) single_cycle();
	cpu.rst = 0;
	dump_wave_inc();
}