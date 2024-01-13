#include "./monitor/sdb/sdb.h"
void test_expr(){
FILE* fp = NULL;
int ref_result;
word_t dut_result;
char buf[200];
bool success;
fp = fopen("./input","r");
if(fscanf(fp,"%d",&ref_result));
if(fscanf(fp,"%s",buf));
dut_result = expr(buf,&success);
if(!success) assert(0);
if(dut_result != ref_result) assert(0);
fclose(fp);
}
int main(){
	test_expr();
	return 0;
}
