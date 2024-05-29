#include <circuit.h>
#include <memory.h>
#include <utils.h>
void init_monitor(int, char *[]);
void sdb_mainloop();
extern "C" void flash_read(int addr, int *data) { assert(0); }

int main(int argc, char *argv[]){
	Verilated::commandArgs(argc, argv);
	get_time();
	init_monitor(argc,argv);
	init_wave();
	reset(10);
	sdb_mainloop();
	close_wave();
}
