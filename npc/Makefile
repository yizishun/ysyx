#top name
TOPNAME = ysyx_23060171_cpu

#some build's dir variable
BUILD_DIR = ./builds
OBJ_DIR = $(BUILD_DIR)/obj_dir
BIN = $(BUILD_DIR)/$(TOPNAME)


#verilator variable
ifeq ($(VERILATOR_ROOT),)
VERILATOR = verilator
else
export VERILATOR_ROOT
VERILATOR = $(VERILATOR_ROOT)/bin/verilator
endif

#verilator flag
VERILATOR_FLAGS += -MMD -cc -O3  --build --x-assign fast --x-initial fast --noassert -j 5
VERILATOR_FLAGS += --trace

#source code
VSRCS = $(shell find $(abspath ./vsrc) -name "*.v" -or -name "*.sv")
CSRCS = $(shell find $(abspath ./csrc) -name "*.c" -or -name "*.cc" -or -name "*.cpp")

#sta 
include $(NPC_HOME)/script/yosys-sta.mk

#sim
include $(NPC_HOME)/script/sim.mk

#nvb
include $(NPC_HOME)/script/sim_nvb.mk 

#main makefile in all project
include ../Makefile

.PHONY:clean run

clean:
	-rm -rf builds/* result top.dot top.png vsrc/*.png
