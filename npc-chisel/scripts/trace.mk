
RUN_PYTHON_SCRIPT = 0
PF_TRACE_TOOL = $(NPC_HOME)/tools/pf-trace/pft.py
PF_TRACE_TOOL2 = $(NPC_HOME)/tools/pf-trace/pft2.py
PF_TRACE1 = $(BUILD_DIR)/trace.csv
PF_TRACE2 = $(BUILD_DIR)/trace2.csv
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
