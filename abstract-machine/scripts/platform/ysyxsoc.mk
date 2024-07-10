AM_SRCS := riscv/ysyxSoC/start.S \
           riscv/ysyxSoC/trm.c \
		   riscv/ysyxSoC/bootloader.c \
           riscv/ysyxSoC/ioe.c \
           riscv/ysyxSoC/timer.c \
           riscv/ysyxSoC/input.c \
           riscv/ysyxSoC/cte.c \
           riscv/ysyxSoC/trap.S \
           platform/dummy/vme.c \
           platform/dummy/mpe.c


CFLAGS    += -fdata-sections -ffunction-sections
LDFLAGS   += -T $(AM_HOME)/scripts/linkersoc.ld \
						 --defsym=_flash_start=0x30000000 --defsym=_entry_offset=0x0 \
						 --defsym=_sram_start=0x0f000000 --defsym=_sram_size=0x2000 \
						 --defsym=_psram_start=0x80000000 --defsym=_psram_size=0x400000
LDFLAGS   += --gc-sections -e _start
CFLAGS += -DMAINARGS=\"$(mainargs)\"
NPCFLAGS += -l $(shell dirname $(IMAGE).bin)/npc_log.txt
#NPCFLAGS += -e $(IMAGE).elf
NPCFLAGS += -d $(NPC_HOME)/tools/nemu-diff/riscv32-nemu-interpreter-so
NPCFLAGS += -b
.PHONY: $(AM_HOME)/am/src/riscv/npc/trm.c

image: $(IMAGE).elf
	@$(OBJDUMP) -d $(IMAGE).elf > $(IMAGE).txt
	@echo + OBJCOPY "->" $(IMAGE_REL).bin
	@$(OBJCOPY) --verbose -S --set-section-flags .bss=alloc \
	-O binary $(IMAGE).elf $(IMAGE).bin

run: image
	make -C $(NPC_HOME) run ARGS="$(NPCFLAGS)" IMG="$(IMAGE).bin"