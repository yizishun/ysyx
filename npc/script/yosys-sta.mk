PROJ_PATH = $(shell pwd)

YOSYS_PATH = $(PROJ_PATH)/yosys-sta
YOSYS_SCRIPT = $(YOSYS_PATH)/scripts
DESIGN ?= $(TOPNAME)
SDC_FILE ?= $(PROJ_PATH)/vsrc/$(TOPNAME).sdc
RTL_FILES ?= $(shell find $(PROJ_PATH)/vsrc -name "*.v" -or -name "*.sv")
export CLK_FREQ_MHZ ?= 500

RESULT_DIR = $(PROJ_PATH)/result/$(DESIGN)-$(CLK_FREQ_MHZ)MHz
NETLIST_V  = $(RESULT_DIR)/$(DESIGN).netlist.v
TIMING_RPT = $(RESULT_DIR)/$(DESIGN).rpt

init:
	bash -c "$$(wget -O - https://ysyx.oscc.cc/slides/resources/scripts/init-yosys-sta.sh)"

syn: $(NETLIST_V)
$(NETLIST_V): $(RTL_FILES) $(YOSYS_SCRIPT)/yosys.tcl $(YOSYS_SCRIPT)/pic.ys
	mkdir -p $(@D)
	echo tcl $(YOSYS_SCRIPT)/yosys.tcl $(DESIGN) \"$(RTL_FILES)\" $(NETLIST_V) | yosys -l $(@D)/yosys.log -s -
	yosys -s $(YOSYS_SCRIPT)/pic.ys
	dot -Tpng $(TOPNAME).dot -o $(TOPNAME).png

sta: $(TIMING_RPT)
$(TIMING_RPT): $(SDC_FILE) $(NETLIST_V)
	LD_LIBRARY_PATH=bin/ ./bin/iSTA $(YOSYS_SCRIPT)/sta.tcl $(DESIGN) $(SDC_FILE) $(NETLIST_V)

cleany:
	-rm -rf result/

.PHONY: init syn sta cleany
