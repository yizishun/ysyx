TOPNAME = ysyxSoCFull

NXDC_FILES = constr/top.nxdc #nvboard pin file
BUILD_DIR = $(NPC_HOME)/builds
OBJ_DIR = $(BUILD_DIR)/obj_dir #generated c++ and mk files
SV_GEN_DIR = $(BUILD_DIR)/sv-gen #generated systemVerilog files
BIN = $(BUILD_DIR)/$(TOPNAME)

PF_TRACE_TOOL = $(NPC_HOME)/tools/pf-trace/pft.py
PF_TRACE_TOOL2 = $(NPC_HOME)/tools/pf-trace/pft2.py
PF_TRACE1 = $(BUILD_DIR)/trace.csv
PF_TRACE2 = $(BUILD_DIR)/trace2.csv
REWRITE = $(NPC_HOME)/scripts/rewrite.mk

#ysyxSoC
YSYXSoC = ../ysyxSoC

PRJ = playground

VERILATOR = verilator
#verilator flag
VERILATOR_INC = -I$(YSYXSoC)/perip/uart16550/rtl -I$(YSYXSoC)/perip/spi/rtl
VERILATOR_FLAGS += -MMD -cc -O3 --x-assign fast --x-initial fast --noassert -j 5 -autoflush
VERILATOR_FLAGS += --trace
VERILATOR_FLAGS += --timescale "1ns/1ns" --no-timing
VERILATOR_FLAGS += $(VERILATOR_INC)

# constraint file
SRC_AUTO_BIND = $(abspath $(BUILD_DIR)/auto_bind.cpp)
$(SRC_AUTO_BIND): $(NXDC_FILES)
	python3 $(NVBOARD_HOME)/scripts/auto_pin_bind.py $^ $@

#source code
VSRCS = $(shell find $(abspath $(SV_GEN_DIR)) -name "*.v" -or -name "*.sv")
VSRCS += $(shell find $(abspath $(YSYXSoC)/perip) -name "*.v")
VSRCS += $(shell find $(abspath $(YSYXSoC)/build) -name "*.v")
CSRCS = $(shell find $(abspath ./csrc) -name "*.c" -or -name "*.cc" -or -name "*.cpp")
CSRCS += $(SRC_AUTO_BIND)

# rules for NVBoard
include $(NVBOARD_HOME)/scripts/nvboard.mk

#mill
test:
	mill -i $(PRJ).test

verilog:
	$(call git_commit, "generate verilog")
	mkdir -p $(BUILD_DIR)
	mkdir -p buildsTemp
	mill -i $(PRJ).runMain Elaborate --target-dir ./buildsTemp
	rm -rf ./buildsTemp

help:
	mill -i $(PRJ).runMain Elaborate --help

reformat:
	mill -i __.reformat

checkformat:
	mill -i __.checkFormat

clean:
	-rm -rf $(BUILD_DIR)

.PHONY: test verilog help reformat checkformat clean

#verilator
$(BIN): $(VSRCS) $(CSRCS) $(NVBOARD_ARCHIVE)
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "$(COLOR_YELLOW)[VERILATE]$(COLOR_NONE) $(notdir $(BUILD_DIR))/$(notdir $(BIN))"
	@rm -rf $(OBJ_DIR)
	@mkdir -p $(BUILD_DIR)
	$(VERILATOR) $(VERILATOR_FLAGS) \
		--top-module $(TOPNAME) $^ \
		--Mdir $(OBJ_DIR) --exe -o $(abspath $(BIN))
	@make -s -C $(OBJ_DIR) -f $(REWRITE)

override ARGS ?= --log=$(BUILD_DIR)/npc-log.txt --diff=$(NPC_HOME)/tools/nemu-diff/riscv32-nemu-interpreter-so
NPC_EXEC := $(BIN) $(ARGS) $(IMG)
RUN_PYTHON_SCRIPT = 0
run: $(BIN)	
	@echo "$(COLOR_YELLOW)[Run CPU]$(COLOR_NONE)"
	@rm -f $(PF_TRACE1)
	@rm -f $(PF_TRACE2)
	@if [ "$(RUN_PYTHON_SCRIPT)" -eq "1" ]; then \
		echo "Running Python script..."; \
		python3 $(PF_TRACE_TOOL2) $(PF_TRACE1) $(PF_TRACE2) & \
	else \
		echo "Skipping Python script."; \
	fi
	$(NPC_EXEC)
#wave
wave:
	gtkwave $(BUILD_DIR)/waveform.vcd $(BUILD_DIR)/save.gtkw &
#pf_trace
pft:
	python3 $(PF_TRACE_TOOL) $(PF_TRACE1) $(PF_TRACE2)
#perf
MICROBENCH_DIR = $(AM_KERNEL_HOME)/benchmarks/microbench
perf:
	make -C $(MICROBENCH_DIR) ARCH=riscv32e-ysyxsoc run mainargs=train

COLOR_RED   		= \033[1;31m
COLOR_GREEN 		= \033[1;32m
COLOR_YELLOW 		= \033[33m
COLOR_NONE  		= \033[0m

-include ../Makefile
