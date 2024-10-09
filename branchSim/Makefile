BUILD_DIR = $(BRANCHSIM_HOME)/build
TRACE_DIR = $(BRANCHSIM_HOME)/trace

BTRACE_FILE_BIG = $(TRACE_DIR)/btrace.log
BTRACE_FILE = $(TRACE_DIR)/btrace.log.bz2
SRCS = $(shell find $(BRANCHSIM_HOME)/src/ -name "*.cpp")
SRCS += $(shell find $(BRANCHSIM_HOME)/src/ -name "*.c")
CPPFLAGS = -std=c++17 -O2
CPPFLAGS += -DCONFIG_TRACE
INCLUDE_FLAG = -I$(BRANCHSIM_HOME)/include

bin = $(BUILD_DIR)/branchsim

$(bin): $(SRCS)
	@mkdir -p $(BUILD_DIR)
	@g++ $(CPPFLAGS) -o $@ $(INCLUDE_FLAG) $^

run: $(bin)
	@echo "#[Run BranchSim]"
	$(bin) $(BTRACE_FILE)
zip:
	pbzip2 -p4 -kv $(BTRACE_FILE_BIG)
clean:
	rm -rf $(BUILD_DIR)

