#testbench
CTB = $(shell find $(abspath $(NPC_HOME)/csrc/tb) -name "*.c" -or -name "*.cc" -or -name "*.cpp")

#sim
sim: $(VSRCS) $(CTB)
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@rm -rf $(OBJ_DIR)
	@mkdir -p $(BUILD_DIR)
	$(VERILATOR) $(VERILATOR_FLAGS) \
		--top-module $(TOPNAME) $^ \
		--Mdir $(OBJ_DIR) --exe -o $(abspath $(BIN))
	$(BIN) +trace	