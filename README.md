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
* [ ] Opt the area to reach the B stage standard(2.5w)(25.1.1 ~ 25.1.10)
* [ ] RV64GC (IMACFA) (25.1.11 ~ 25.2.30)
* [ ] BPU,TLB,(virtually tagged)cache
* [ ] CLINT + PLIC + UART
* [ ] Porting xv6
* [ ] Porting Linux
* [ ] Out-of-Order!
### Loong-core
* [ ] NSCSCC 2025 (25.4 ~ 25.8)
### Riscv-core simulation/synthesis
* [ ] vcs/iverilog (25.1.1 ~ 25.1.10)
* [ ] dc
* [ ] FPGA
### Riscv-emu
* [ ] Porting xv6 (25.2.30 ~ 25.3.15)
* [ ] Porting Linux (25.3.15 ~ 25.3.30)
### software(mainly for os development)
* [ ] Complete the PA4. ⭐️ **<= Focus** (1 week preview, 1 week PA4.1, 1 week PA4.2, 1 week PA4.3)(24.11.1 ~ 24.11.30)
* [ ] Finish jyy lab ⭐️ **<= Focus** (1 video(include corresponding hw)/2 days) (24.11.1 ~ 25.1.10)
* [ ] Finish the xv6 lab (25.1.11 ~ 25.3.30)
### Utils
* [ ] Sdb: freely toggle DiffTest mode
* [ ] Sdb: snapshot
* [ ] Porting xiangshan toolchains

> **notes:** The todo list is not fixed, and will be updated as the project progresses.