
PRJ = playground
#mill
test:
	mill -i $(PRJ).test

verilog:
	$(call git_commit, "generate verilog")
	mkdir -p $(BUILD_DIR)
	mkdir -p buildsTemp
	mill -i $(PRJ).runMain Elaborate --target-dir ./buildsTemp
	rm -rf ./buildsTemp

help:
	mill -i $(PRJ).runMain Elaborate --help

reformat:
	mill -i __.reformat

checkformat:
	mill -i __.checkFormat

clean:
	-rm -rf $(BUILD_DIR)

cleanv:
	-rm -rf $(BUILD_DIR)/sv-gen

.PHONY: test verilog help reformat checkformat clean