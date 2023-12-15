#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <memory>
#include <verilated.h>
#include <Vtop.h>
#include "verilated_vcd_c.h"
int main(int argc,char** argv) {
  if(false && argc && argv){}
	Verilated::mkdir("logs");
	const std::unique_ptr<VerilatedContext> contextp{new VerilatedContext};
	contextp ->debug(0);
	contextp ->randReset(2);
	contextp ->traceEverOn(true);
  contextp ->commandArgs(argc,argv);

	VerilatedVcdC* tfp = new VerilatedVcdC;
	const std::unique_ptr<Vtop> top{new Vtop{contextp.get(),"TOP"}};
	top ->trace(tfp,0);
	tfp ->open("logs/wave.vcd");
	while(1){
		int a = rand() & 1;
		int b = rand() & 1;
		top->a = a;
		top->b = b;
		top->eval();
		printf("a = %d, b = %d, f = %d\n", a, b, top->f);
		tfp->dump(contextp->time());
		contextp->timeInc(1);
		assert(top->f == (a ^ b));
	}
	tfp ->close();
	top->final();
  return 0;
}
