include VysyxSoCFull.mk
CPPFLAGS += $(shell llvm-config --cxxflags | tr ' ' '\n' | grep '^-I' | tr '\n' ' ')
CPPFLAGS += -I$(NPC_HOME)/csrc/include
#CPPFLAGS += -DCONFIG_FTRACE=1
#CPPFLAGS += -DCONFIG_TRACE=1
#CPPFLAGS += -DCONFIG_DIFFTEST
CPPFLAGS += -DCONFIG_MTRACE
#CPPFLAGS += -DCONFIG_WAVE
LDFLAGS += $(shell llvm-config --ldflags)
LIBS += $(shell llvm-config --libs) #please make sure you already have llvm-config or in your PATH
LIBS += -lreadline
LINK := g++