#include <circuit.h>
#include <memory.h>
#include <utils.h>
void init_monitor(int, char *[]);
void sdb_mainloop();

int main(int argc, char** argv, char** env){
	cpu = new CPU;
	get_time();
	init_monitor(argc,argv);
	init_wave();
	Verilated::commandArgs(argc, argv);
	reset(25);
	sdb_mainloop();
	close_wave();
}
