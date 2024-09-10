BUILD_DIR = $(CACHESIM_HOME)/build
ITRACE_DIR = $(CACHESIM_HOME)/itrace

ITRACE_FILE_BIG = $(ITRACE_DIR)/itrace.log
ITRACE_FILE = $(ITRACE_DIR)/itrace.log.bz2
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
	@echo "#[Run CacheSim]"
	$(bin) $(ITRACE_FILE)
zip:
	pbzip2 -p4 -kv $(ITRACE_FILE_BIG)
clean:
	rm -rf $(BUILD_DIR)

