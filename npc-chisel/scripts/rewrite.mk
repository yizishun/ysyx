MAKE_FILE = V$(TOPNAME).mk
include $(MAKE_FILE)
CPPFLAGS += $(shell llvm-config --cxxflags | tr ' ' '\n' | grep '^-I' | tr '\n' ' ')
CPPFLAGS += -I$(NPC_HOME)/csrc/include
CPPFLAGS += -I$(NVBOARD_HOME)/usr/include
CPPFLAGS += $(shell sdl2-config --cflags)
#CPPFLAGS += -DCONFIG_FTRACE=1
CPPFLAGS += -DCONFIG_TRACE=1
#CPPFLAGS += -DCONFIG_DIFFTEST
CPPFLAGS += -DCONFIG_MTRACE
CPPFLAGS += -DCONFIG_WAVE
CPPFLAGS += -DCONFIG_WAVESIZE=200
CPPFLAGS += -D$(TOPNAME)
CPPFLAGS += -DARCH=$(ARCH)
WRITE_TO_CSV := 0
ifeq ($(WRITE_TO_CSV), 1)
	CPPFLAGS += -DCONFIG_CSV
endif
LDFLAGS += $(shell llvm-config --ldflags)
LDFLAGS += $(shell sdl2-config --libs) -lSDL2_image -lSDL2_ttf
LIBS += $(shell llvm-config --libs) #please make sure you already have llvm-config or in your PATH
LIBS += -lreadline
LINK := g++