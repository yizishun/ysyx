#testbench
CTB = $(shell find $(abspath $(NPC_HOME)/csrc/tb) -name "*.c" -or -name "*.cc" -or -name "*.cpp")
LDFLAGS = $(shell llvm-config --ldflags)
LDFLAGS += $(shell llvm-config --libs)
VERILATOR_FLAGS += -CFLAGS "-I$(NPC_HOME)/csrc/tb/include"
VERILATOR_FLAGS += -LDFLAGS "$(LDFLAGS) -lreadline"
override ARGS ?= --log=$(BUILD_DIR)/npc-log.txt

NPC_EXEC := $(BIN) $(ARGS) $(IMG)
#sim
$(BIN): $(VSRCS) $(CTB)
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@rm -rf $(OBJ_DIR)
	@mkdir -p $(BUILD_DIR)
	$(VERILATOR) $(VERILATOR_FLAGS) \
		--top-module $(TOPNAME) $^ \
		--Mdir $(OBJ_DIR) --exe -o $(abspath $(BIN))

run: $(BIN)	
	$(NPC_EXEC)
