TOPNAME ?= ysyxSoCFull

BUILD_DIR = $(NPC_HOME)/builds
OBJ_DIR = $(BUILD_DIR)/obj_dir #generated c++ and mk files
SV_GEN_DIR = $(BUILD_DIR)/sv-gen #generated systemVerilog files
BIN = $(BUILD_DIR)/$(TOPNAME)

REWRITE = $(NPC_HOME)/scripts/rewrite.mk

VERILATOR = verilator

# ARCH logic
ARCH ?= riscv32e-ysyxsoc
# Check: environment variable `$ARCH` must be in the supported list
ARCHS = $(basename $(notdir $(shell ls $(AM_HOME)/scripts/*.mk)))
ifeq ($(filter $(ARCHS), $(ARCH)), )
  $(error Expected $$ARCH in {$(ARCHS)}, Got "$(ARCH)")
endif
# Print build info message
$(info # NPC Type is [$(ARCH)])
-include $(NPC_HOME)/scripts/arch/$(ARCH).mk

# rules for NVBoard
include $(NVBOARD_HOME)/scripts/nvboard.mk

# rules for mill
include $(NPC_HOME)/scripts/chisel_build.mk

# rules for trace
include $(NPC_HOME)/scripts/trace.mk

# constraint file
SRC_AUTO_BIND = $(abspath $(BUILD_DIR)/auto_bind.cpp)
$(SRC_AUTO_BIND): $(NXDC_FILES)
	python3 $(NVBOARD_HOME)/scripts/auto_pin_bind.py $^ $@

#verilator
$(BIN): $(VSRCS) $(CSRCS) $(NVBOARD_ARCHIVE)
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	$(info # NPC Type is [$(ARCH)])
	@echo "$(COLOR_YELLOW)[VERILATE]$(COLOR_NONE) $(notdir $(BUILD_DIR))/$(notdir $(BIN))"
	@rm -rf $(OBJ_DIR)
	@mkdir -p $(BUILD_DIR)
	$(VERILATOR) $(VERILATOR_FLAGS) \
		--top-module $(TOPNAME) $^ \
		--Mdir $(OBJ_DIR) --exe -o $(abspath $(BIN))
	@make -s -C $(OBJ_DIR) -f $(REWRITE) TOPNAME=$(TOPNAME) ARCH=$(ARCH)

# run npc
override ARGS ?= --log=$(BUILD_DIR)/npc-log.txt --diff=$(NPC_HOME)/tools/nemu-diff/riscv32-nemu-interpreter-so
NPC_EXEC := $(BIN) $(ARGS) $(IMG)
run: $(BIN)	
	@echo "$(COLOR_YELLOW)[Run CPU]$(COLOR_NONE)"
	$(info # NPC Type is [$(ARCH)])
	@rm -f $(PF_TRACE1)
	@rm -f $(PF_TRACE2)
	@if [ "$(RUN_PYTHON_SCRIPT)" -eq "1" ]; then \
		echo "Running Python script..."; \
		python3 $(PF_TRACE_TOOL2) $(PF_TRACE1) $(PF_TRACE2) & \
	else \
		echo "Skipping Python script."; \
	fi
	$(NPC_EXEC)
-include ../Makefile
