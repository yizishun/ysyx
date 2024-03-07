#testbench
CTB = $(shell find $(abspath $(NPC_HOME)/csrc/tb) -name "*.c" -or -name "*.cc" -or -name "*.cpp")
override ARGS ?= --log=$(BUILD_DIR)/npc-log.txt --diff=$(NPC_HOME)/tools/nemu-diff/riscv32-nemu-interpreter-so

NPC_EXEC := $(BIN) $(ARGS) $(IMG)
#sim
$(BIN): $(VSRCS) $(CTB)
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "$(COLOR_YELLOW)[VERILATE]$(COLOR_NONE) $(notdir $(BUILD_DIR))/$(notdir $(BIN))"
	@rm -rf $(OBJ_DIR)
	@mkdir -p $(BUILD_DIR)
	@$(VERILATOR) $(VERILATOR_FLAGS) \
		--top-module $(TOPNAME) $^ \
		--Mdir $(OBJ_DIR) --exe -o $(abspath $(BIN))
	@make -s -C $(OBJ_DIR) -f $(REWRITE)

run: $(BIN)	
	@echo "$(COLOR_YELLOW)[Run CPU]$(COLOR_NONE)" $(notdir $(BUILD_DIR))/$(notdir $(BIN))
	$(NPC_EXEC)
