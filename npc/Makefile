#top name
TOPNAME = ysyx_23060171_cpu

#some build's dir variable
BUILD_DIR = ./builds
OBJ_DIR = $(BUILD_DIR)/obj_dir
BIN = $(BUILD_DIR)/$(TOPNAME)

REWRITE = $(NPC_HOME)/script/rewrite.mk

#verilator variable
ifeq ($(VERILATOR_ROOT),)
VERILATOR = verilator
else
export VERILATOR_ROOT
VERILATOR = $(VERILATOR_ROOT)/bin/verilator
endif

#verilator flag
VERILATOR_FLAGS += -MMD -cc -O3 --x-assign fast --x-initial fast --noassert -j 5
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

COLOR_RED   		= \033[1;31m
COLOR_GREEN 		= \033[1;32m
COLOR_YELLOW 		= \033[33m
COLOR_NONE  		= \033[0m

.PHONY:clean run 

clean:
	-rm -rf builds result top.dot top.png vsrc/*.png


