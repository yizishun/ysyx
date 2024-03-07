include Vysyx_23060171_cpu.mk
CPPFLAGS += $(shell llvm-config --cxxflags | tr ' ' '\n' | grep '^-I' | tr '\n' ' ')
CPPFLAGS += -I$(NPC_HOME)/csrc/tb/include
LDFLAGS += $(shell llvm-config --ldflags)
LIBS += $(shell llvm-config --libs) #please make sure you already have llvm-config or in your PATH
LIBS += -lreadline
LINK := g++