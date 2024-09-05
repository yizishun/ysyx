
TOPNAME = NPC

NXDC_FILES = constr/npc.nxdc #nvboard pin file
#verilator flag
VERILATOR_FLAGS += -MMD -cc -O3 --x-assign fast --x-initial fast --noassert -j 5 -autoflush
VERILATOR_FLAGS += --trace
VERILATOR_FLAGS += --timescale "1ns/1ns" --no-timing

#source code
VSRCS = $(shell find $(abspath $(SV_GEN_DIR)) -name "*.v" -or -name "*.sv")
CSRCS = $(shell find $(abspath ./csrc) -name "*.c" -or -name "*.cc" -or -name "*.cpp")
CSRCS += $(SRC_AUTO_BIND)