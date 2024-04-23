#include <elf.h>
#include <common.h>
#include <ftrace.h>
#include <circuit.h>
struct func{
	vaddr_t saddr;
	vaddr_t eaddr;
	char symbol[40];
}funcs[MAX_FUNC];
int count = 0;
int space = 0;
static char *ftrace_find_symbol(vaddr_t addr);
static void ftrace_write(int type,char *fname, vaddr_t caddr, vaddr_t addr);
/* ftrace */
void ftrace_check(uint8_t type,paddr_t pc,paddr_t dnpc,word_t i){
  	extern char * elf_file;
  	if(elf_file == NULL) return;

  	int rs1 = BITS(i, 19, 15);
  	int rd = BITS(i, 11, 7);
	uint32_t imm;
	if(type == JAL)
  		imm = ((SEXT(BITS(i, 31, 31), 1) << 19) | BITS(i, 19, 12) << 11 | BITS(i, 20 , 20) << 10 | BITS(i , 30 , 21)) << 1;
	else if(type == JALR)
		imm = SEXT(BITS(i, 31, 20), 12);
	//printf("pc = %#x\n",pc);
	char *prev_fname = ftrace_find_symbol(pc);
	//printf("dnpc = %#x\n",s->dnpc);
	char *now_fname  = ftrace_find_symbol(dnpc);
	//printf("prev = %s\n",prev_fname);
	//printf("now  = %s\n",now_fname);
	if(strcmp(prev_fname,now_fname) == 0)	return;
	//printf("inst = %#x\n",i);
	//printf("rs1 = %u imm = %u rd = %u\n",rs1,imm,rd);
	if(type == JAL) ftrace_write(CALL , now_fname, dnpc, pc);
	else if(type == JALR){
		if(rs1 == 1 && imm == 0 && rd == 0)
			ftrace_write(RET,prev_fname,dnpc,pc);
		else 
			ftrace_write(CALL,now_fname,dnpc,pc);
	}
}

static char *ftrace_find_symbol(vaddr_t addr){
	//printf("pcin = %#x\n",addr);
	int i;
//	for(i = 0;i < count;i++){
//		printf("funcs[%d].saddr = %#x\n",i,funcs[i].saddr);
//		printf("funcs[%d].eaddr = %#x\n",i,funcs[i].eaddr);
//		printf("funcs[%d].symbol = %s\n",i,funcs[i].symbol);
//	}
	for(i = 0;i < count; i++){
		if(addr >= funcs[i].saddr && addr< funcs[i].eaddr){
//			printf("hahaha=%s\n",funcs[i].symbol);
			return funcs[i].symbol;
			}
	}
	return NULL;
}
static void ftrace_write(int type,char *fname, vaddr_t caddr, vaddr_t addr){
	int i;
	if(type == CALL){
		printf("%#x:",addr);
		for(i = 0;i < space*2;i++)
			printf(" ");
		printf("call [%s@%#x]\n", fname, caddr);
		space++;
		}
	else if(type == RET){
		printf("%#x:",addr);
		space--;
		for(i = 0;i < space*2;i++)
			printf(" ");
		printf("ret  [%s]\n", fname);
		}
	//printf("space=%d\n",space);
	//if(space< 0)  assert(0);
}
void init_ftrace(char *elf_file){ 
	if(elf_file == NULL) { 
		Log("No elf is given. Have turned off ftrace autometically");
		return;
	}
	Log("The ELF is %s", elf_file);
	
	FILE *fp = fopen(elf_file, "rb");		//open and check
  Assert(fp, "Can not open '%s'", elf_file);

	//elf header
	Elf32_Ehdr elf_header;				//elf header var
	int ret = fread(&elf_header ,sizeof(elf_header), 1, fp);		//write elf-header from file to var
	assert(ret == 1);
	if (elf_header.e_ident[0] != 0x7F ||
			elf_header.e_ident[1] != 'E' ||
			elf_header.e_ident[2] != 'L' ||
			elf_header.e_ident[3] != 'F')
		assert(0);
	//section header 
	Elf32_Shdr *shdr =(Elf32_Shdr *) malloc(sizeof(Elf32_Shdr) * elf_header.e_shnum);	//section header var
	assert(shdr != NULL);
	ret = fseek(fp, elf_header.e_shoff, SEEK_SET);		//go to offset of section header of file
	assert(ret == 0);
	ret = fread(shdr, sizeof(Elf32_Shdr) * elf_header.e_shnum, 1, fp);		//write section header from file to var
 	assert(ret == 1);
	//section header 's string table
	char shstrtab[shdr[elf_header.e_shstrndx].sh_size];		//section header 's string table var   size = index(e_shstrndx) of shdr 's sh_size
	ret = fseek(fp, shdr[elf_header.e_shstrndx].sh_offset, SEEK_SET);
	assert(ret == 0);
	ret = fread(shstrtab,shdr[elf_header.e_shstrndx].sh_size, 1, fp);		//write real sh string table to var
	assert(ret == 1);
	//find .symtab and .strtab offset
	Elf32_Off symoff = 0, stroff = 0;
	uint32_t symsize = 0, strsize = 0;
	char *temp;
	int i;
	for(i = 0;i < elf_header.e_shnum;i++){
		temp = shstrtab + shdr[i].sh_name;		//sh_name is the index in string table
		if (strcmp(temp, ".symtab") == 0){
			symoff = shdr[i].sh_offset;
			symsize = shdr[i].sh_size;
		}
		if (strcmp(temp, ".strtab") == 0){
			stroff = shdr[i].sh_offset;
			strsize = shdr[i].sh_size;
			}
	}
	//symbol table
	Elf32_Sym *symtab = (Elf32_Sym *)malloc(symsize);		//malloc a symbol table struct var
	ret = fseek(fp, symoff, SEEK_SET);
	assert(ret == 0);
	ret = fread(symtab, symsize, 1, fp);		//write real symbol table from file to var
	assert(ret == 1);
	//string table
	char strtab[strsize];
	ret = fseek(fp, stroff, SEEK_SET);
	assert(ret == 0);
	ret = fread(strtab, strsize, 1, fp);
	assert(ret == 1);
	//find func entry in symtab
	for(i = 0;i < symsize/sizeof(Elf32_Sym); i++){
		if(ELF32_ST_TYPE(symtab[i].st_info) == STT_FUNC){
			funcs[count].saddr = symtab[i].st_value;
			funcs[count].eaddr = symtab[i].st_value + symtab[i].st_size;
			char *temp = strtab + symtab[i].st_name;
			strcpy(funcs[count].symbol,temp);
			count++;
		}
	}
	fclose(fp);
}
