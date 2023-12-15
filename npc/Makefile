ifeq ($(VERILATOR_ROOT),)
VERILATOR = verilator
else
export VERILATOR_ROOT
VERILATOR = $(VERILATOR_ROOT)/bin/verilator
endif

VERILATOR_FLAGS += -cc --exe --build -Wall
VERILATOR_FLAGS += --trace

VERILATOR_INPUT = $(NPC_HOME)/csrc/main.cpp $(NPC_HOME)/vsrc/top.v
all:
	@echo "Write this Makefile by your self."

sim:
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	#@echo "Write this Makefile by your self."
	$(VERILATOR) $(VERILATOR_FLAGS) $(VERILATOR_INPUT)
	@rm -rf logs
	@mkdir -p logs
	obj_dir/Vtop +trace
clean:
	-rm -rf obj_dir
include ../Makefile
