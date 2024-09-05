TOPNAME = ysyxSoCFull

#ysyxSoC
YSYXSoC = ../ysyxSoC

NXDC_FILES = constr/ysyxsoc.nxdc #nvboard pin file
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
CSRCS += $(SRC_AUTO_BIND)
