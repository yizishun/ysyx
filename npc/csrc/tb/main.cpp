#include <circuit.h>
#include <memory.h>
void init_monitor(int, char *[]);

int main(int argc, char *argv[]){
	init_monitor(argc,argv);
	init_wave();
	reset(10);
	while(1){
		cpu.inst = pmem_read(cpu.pc);
		single_cycle();
		dump_wave_inc();
	}
	close_wave();
}
