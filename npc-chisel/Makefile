TOPNAME = ysyxSoCFull

BUILD_DIR = ./builds
OBJ_DIR = $(BUILD_DIR)/obj_dir #generated c++ and mk files
SV_GEN_DIR = $(BUILD_DIR)/sv-gen #generated systemVerilog files
BIN = $(BUILD_DIR)/$(TOPNAME)
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
#source code
VSRCS = $(shell find $(abspath $(SV_GEN_DIR)) -name "*.v" -or -name "*.sv")
VSRCS += $(shell find $(abspath $(YSYXSoC)/perip) -name "*.v")
VSRCS += $(shell find $(abspath $(YSYXSoC)/build) -name "*.v")
CSRCS = $(shell find $(abspath ./csrc) -name "*.c" -or -name "*.cc" -or -name "*.cpp")

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
$(BIN): $(VSRCS) $(CSRCS)
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
run: $(BIN)	
	@echo "$(COLOR_YELLOW)[Run CPU]$(COLOR_NONE)"
	$(NPC_EXEC)
#wave
wave:
	gtkwave $(BUILD_DIR)/waveform.vcd $(BUILD_DIR)/save.gtkw &

COLOR_RED   		= \033[1;31m
COLOR_GREEN 		= \033[1;32m
COLOR_YELLOW 		= \033[33m
COLOR_NONE  		= \033[0m

-include ../Makefile
