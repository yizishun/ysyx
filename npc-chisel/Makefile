TOPNAME = GCD

BUILD_DIR = ./builds
OBJ_DIR = $(BUILD_DIR)/obj_dir #generated c++ and mk files
SV_GEN_DIR = $(BUILD_DIR)/sv-gen #generated systemVerilog files

PRJ = playground

VERILATOR = verilator
#verilator flag
VERILATOR_FLAGS += -MMD -cc -O3 --x-assign fast --x-initial fast --noassert -j 5
VERILATOR_FLAGS += --trace
#source code
VSRCS = $(shell find $(abspath $(SV_GEN_DIR)) -name "*.v" -or -name "*.sv")
CSRCS = $(shell find $(abspath ./csrc) -name "*.c" -or -name "*.cc" -or -name "*.cpp")

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

sim: $(VSRCS)
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "$(COLOR_YELLOW)[VERILATE]$(COLOR_NONE) $(notdir $(BUILD_DIR))/$(notdir $(BIN))"
	@rm -rf $(OBJ_DIR)
	@mkdir -p $(BUILD_DIR)
	$(VERILATOR) $(VERILATOR_FLAGS) \
		--top-module $(TOPNAME) $^ \
		--Mdir $(OBJ_DIR)

COLOR_RED   		= \033[1;31m
COLOR_GREEN 		= \033[1;32m
COLOR_YELLOW 		= \033[33m
COLOR_NONE  		= \033[0m

-include ../Makefile
