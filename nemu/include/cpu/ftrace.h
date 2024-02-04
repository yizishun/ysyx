#include <memory/vaddr.h>
#ifndef __FTRACE_H__
#define __FTRACE_H__
#define MAX_FUNC 40 
enum {
	JAL  ,JALR,
	CALL ,RET 
};
void init_ftrace(char *elf_file);
char *ftrace_find_symbol(vaddr_t addr);
void ftrace_write(int type,char *fname, vaddr_t caddr, vaddr_t addr);
#endif
