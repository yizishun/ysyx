#ifndef __FTRACE_H__
#define __FTRACE_H__
#define MAX_FUNC 40 
#define JAL  0b1101111
#define JALR 0b1100111
enum {
	CALL ,RET 
};
void init_ftrace(char *elf_file);
void ftrace_check(uint8_t type,paddr_t prev_pc,paddr_t pc,word_t i);
#endif
