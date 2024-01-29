#include <cpu/iringbuf.h>
IRingBuf *irb = NULL;
//create a iringbuf
void init_iringbuf(int length){
	irb = calloc(1 , sizeof(IRingBuf));
	//alloc for buffer
	irb -> buffer = (char **)malloc( sizeof(char *) * (length + 1) );
	for(int i = 0;i < length + 1 ; i++)
		irb -> buffer[i] = malloc( sizeof(char) * MAX_INST_LEN);

	irb -> start = 0;
	irb -> end   = 0;
	irb -> size = length + 1;
}
//write an inst to iringbuf
void iringbuf_write(char *inst){
	strncpy( irb -> buffer[irb -> end] , inst , MAX_INST_LEN); //copy the inst to ring's end
	irb->buffer[irb->end][MAX_INST_LEN - 1] = '\0'; 
	irb -> end = (irb -> end + 1) % (irb -> size); //mode inc end
	if(((irb -> end + 1) % (irb -> size)) == irb -> start) //full condition,make one place empty
		irb -> start = (irb -> start + 1) % (irb -> size); //mode inc start
}
//print the iringbuf
void iringbuf_print(){
	int empty = (irb -> end) % (irb -> size); //find the empty place
	int i;
	for(i = irb -> start; i != empty ; i = (i + 1) % (irb -> size) ){ //mode inc i
		if((i + 1) % (irb -> size) == empty)
			printf("-->  %s\n",irb -> buffer[i]);
		else
			printf("     %s\n",irb -> buffer[i]);
	}
}
//free the iringbuf
void iringbuf_free(){ //so easy ,so i send gpt write
    if (irb == NULL) {
        return; // 如果 irb 已经是 NULL，则不需要释放
    } 
    // 释放每个字符串指针指向的内存
    for (int i = 0; i < irb->size; i++) {
        free(irb->buffer[i]);
    }
    // 释放字符串指针数组本身
    free(irb->buffer);
    // 释放 IRingBuf 结构体的内存
    free(irb);
    // 将 irb 指针设置为 NULL，防止悬挂指针
    irb = NULL;
}
