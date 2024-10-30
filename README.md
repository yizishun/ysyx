# Framework Specially For YSYX Flow
---
## RTL Design
In the path `./npc-chisel` , which is a five stage pipline RV32I processor.
use
```bash
make verilog
```
to generate the verilog code.
The details of the design can be found in [riscv-core](https://github.com/yizishun/riscv-core) repo.

## RTL Simulation
Currently, we only support the simulation tool is `verilator`.
You can use
```bash
make run
```
in `./npc-chisel` to build the executable and run it with default img.
Or you can use
```bash
make ARCH=riscv32e-ysyxsoc run
```
in other workloads directory to build the executable and run the workload.

## RTL Synthesis
ysyx use yosys to do the synthesis.And use iEDA to analyze the timming.

## Emulator
In the path `./nemu`, it has a simple emulator to run the riscv binary.
The usage is as same as npc except you should use `ARCH=riscv32-nemu` or `ARCH=riscv32e-nemu`
The details of this emulator can be found in [riscv-emu](https://github.com/yizishun/riscv-emu) repo.

## The HAL(Abstract Machine)
AbstractMachine is a minimal, modularized, and machine-independent abstraction layer of the computer hardware:

* physical memory and direct execution (The "Turing Machine");
* basic model for input and output devices (I/O Extension);
* interrupt/exception and processor context management (Context Extension);
* virtual memory and protection (Virtual Memory Extension);
* multiprocessing (Multiprocessing Extension).

## Software
All the software acctually run in the abstract machine,which means that it use the abstract machine's interface to interact with the hardware.
I use this to test CPU, build a simple OS, etc.
The easy way to use it is
```bash
make ARCH=riscv32-nemu run
or
make ARCH=riscv32e-npc run
```
It will use the corresponding hardware to run the workload(An am software).
### AM-kernels
Lot of tests are in this repo.
### Nanos-lite
A simple OS which is the content of NJU programming assignment.(PA3-PA4)
### fceux-am
Nintendo Entertainment System模拟器

## TODO LIST
### Riscv-core
* [ ] Opt the area to reach the B stage standard(2.5w)
* [ ] RV64GC (IMACFA)
* [ ] BPU,TLB,(virtually tagged)cache
* [ ] CLINT + PLIC + UART
* [ ] Porting xv6
* [ ] Porting Linux
* [ ] Out-of-Order!
### Riscv-core simulation/synthesis
* [ ] vcs/iverilog
* [ ] dc
* [ ] FPGA
### Riscv-emu
* [ ] Porting xv6
* [ ] Porting Linux
### software(mainly for os development)
* [ ] Complete the PA4. **<= Focus**
* [ ] Finish jyy lab
* [ ] Finish the xv6 lab
### Utils
* [ ] Sdb: freely toggle DiffTest mode
* [ ] Sdb: snapshot
* [ ] Porting xiangshan toolchains

> **notes:** The todo list is not fixed, and will be updated as the project progresses.