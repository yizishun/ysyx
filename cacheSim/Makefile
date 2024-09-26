BUILD_DIR = $(CACHESIM_HOME)/build
TRACE_DIR = $(CACHESIM_HOME)/trace

ITRACE_FILE_BIG = $(TRACE_DIR)/itrace.log
ITRACE_FILE = $(TRACE_DIR)/itrace.log.bz2
MTRACE_FILE_BIG = $(TRACE_DIR)/mtrace.log
MTRACE_FILE = $(TRACE_DIR)/mtrace.log.bz2
SRCS = $(shell find $(CACHESIM_HOME)/src/ -name "*.cpp")
SRCS += $(shell find $(CACHESIM_HOME)/src/ -name "*.c")
CPPFLAGS = -std=c++17 -O2
#CPPFLAGS += -DCONFIG_TRACE
INCLUDE_FLAG = -I$(CACHESIM_HOME)/include

bin = $(BUILD_DIR)/cachesim

$(bin): $(SRCS)
	@mkdir -p $(BUILD_DIR)
	@g++ $(CPPFLAGS) -o $@ $(INCLUDE_FLAG) $^

run: $(bin)
	@echo "#[Run CacheSim(icache)]"
	$(bin) $(ITRACE_FILE)
icache: $(bin)
	@echo "#[Run CacheSim(icache)]"
	$(bin) $(ITRACE_FILE)
dcache: $(bin)
	@echo "#[Run CacheSim(dcache)]"
	$(bin) $(MTRACE_FILE)
zipi:
	pbzip2 -p4 -kv $(ITRACE_FILE_BIG)
zipm:
	pbzip2 -p4 -kv $(MTRACE_FILE_BIG)
clean:
	rm -rf $(BUILD_DIR)

