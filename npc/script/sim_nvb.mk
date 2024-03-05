#testbench in nvb
CTB_NVB = $(shell find $(abspath $(NPC_HOME)/csrc/tb_nvb) -name "*.c" -or -name "*.cc" -or -name "*.cpp" -or -name "*.cc")

#nvb gcc compile flag
INCFLAGS = $(addprefix -I, $(INC_PATH))
CXXFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(TOPNAME)\""

#nvboard interface
NXDC_FILES = constr/$(TOPNAME).nxdc
SRC_AUTO_BIND = $(abspath $(BUILD_DIR)/auto_bind.cpp)
$(SRC_AUTO_BIND): $(NXDC_FILES)
	python3 $(NVBOARD_HOME)/scripts/auto_pin_bind.py $^ $@
include $(NVBOARD_HOME)/scripts/nvboard.mk

#nvb
nvb: $(VSRCS) $(CTB_NVB) $(SRC_AUTO_BIND) $(NVBOARD_ARCHIVE)
	$(call git_commit, "sim in NVB")
	@rm -rf $(OBJ_DIR)
	$(VERILATOR) $(VERILATOR_FLAGS) \
		--top-module $(TOPNAME) $^ \
		$(addprefix -CFLAGS , $(CXXFLAGS)) $(addprefix -LDFLAGS , $(LDFLAGS))	\
		--Mdir $(OBJ_DIR) --exe -o $(abspath $(BIN)) 
	$(BIN) +trace
