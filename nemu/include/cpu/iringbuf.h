#include <common.h>
#ifndef IRINGBUF_H
#define IRINGBUF_H
#define MAX_INST_LEN 128
typedef struct{
	char **buffer;
	int start;
	int end;
	int size;
} IRingBuf;
void init_iringbuf(int length);
void iringbuf_write(char *inst);
void iringbuf_print();
void iringbuf_free();
#endif
