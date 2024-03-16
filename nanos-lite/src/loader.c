#include <proc.h>
#include <elf.h>

size_t ramdisk_read(void *buf, size_t offset, size_t len);
size_t ramdisk_write(const void *buf, size_t offset, size_t len);
size_t get_ramdisk_size();

#ifdef __LP64__
# define Elf_Ehdr Elf64_Ehdr
# define Elf_Phdr Elf64_Phdr
# define ELf_Off  Elf64_Off
# define ELf_Word Elf64_Word
# define ELf_Addr Elf64_Addr
#else
# define Elf_Ehdr Elf32_Ehdr
# define Elf_Phdr Elf32_Phdr
# define ELf_Off  Elf32_Off
# define ELf_Word Elf32_Word
# define ELf_Addr Elf32_Addr
#endif

static void disk2mem(ELf_Off offset ,ELf_Addr vaddr ,ELf_Word filesz ,ELf_Word memsz){
  void *buf = malloc(filesz);
  ramdisk_read(buf ,offset ,filesz);
  memcpy((void *)vaddr ,buf ,filesz);
  memset((void *)(vaddr+filesz) ,0 ,memsz - filesz);
}

static uintptr_t loader(PCB *pcb, const char *filename) {
  size_t ret;
  size_t len;
  size_t offset;
  //elf header
	Elf_Ehdr elf_header;				//elf header var
  len = sizeof(elf_header);
  offset = 0;
  ret = ramdisk_read(&elf_header ,offset ,len);
	assert(ret == len);
	if (elf_header.e_ident[0] != 0x7F ||
			elf_header.e_ident[1] != 'E' ||
			elf_header.e_ident[2] != 'L' ||
			elf_header.e_ident[3] != 'F')
	  assert(0);

  //phdr
  Elf_Phdr *phdr =(Elf_Phdr *) malloc(sizeof(Elf_Phdr) * elf_header.e_shnum);
  len = sizeof(Elf_Phdr);
  offset = elf_header.e_phoff;
  for(int i = 0;i < elf_header.e_shnum; i++){
    ret = ramdisk_read(&phdr[i] ,offset ,len);
    assert(ret == len);
    if(phdr[i].p_type == PT_LOAD){
      disk2mem(phdr[i].p_offset ,phdr[i].p_vaddr ,phdr[i].p_filesz ,phdr[i].p_memsz);
    }
    offset += len;
  }
  return elf_header.e_entry;
}

void naive_uload(PCB *pcb, const char *filename) {
  uintptr_t entry = loader(pcb, filename);
  Log("Jump to entry = %lu", entry);
  ((void(*)())entry) ();
}

