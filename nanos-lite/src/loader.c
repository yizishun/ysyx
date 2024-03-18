#include <proc.h>
#include <elf.h>
#include <fs.h>

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

static void disk2mem(int fd, ELf_Off offset ,ELf_Addr vaddr ,ELf_Word filesz ,ELf_Word memsz){
  void *buf = malloc(filesz);
  fs_lseek(fd ,offset ,SEEK_SET);
  fs_read(fd, buf ,filesz);
  memcpy((void *)vaddr, buf, filesz);
  memset((void *)(vaddr+filesz), 0, memsz - filesz);
}

static uintptr_t loader(PCB *pcb, const char *filename) {
  size_t ret;
  size_t len;
  size_t offset;
  int fd = fs_open(filename, 0, 0);
  //elf header
	Elf_Ehdr elf_header;				//elf header var
  len = sizeof(elf_header);
  offset = 0;
  fs_lseek(fd, offset, SEEK_SET);
  ret = fs_read(fd ,&elf_header, len);
	assert(ret == len);
	if (elf_header.e_ident[0] != 0x7F ||
			elf_header.e_ident[1] != 'E' ||
			elf_header.e_ident[2] != 'L' ||
			elf_header.e_ident[3] != 'F')
	  panic("ld : no ELF file");

  //phdr
  Elf_Phdr *phdr =(Elf_Phdr *) malloc(sizeof(Elf_Phdr) * elf_header.e_shnum);
  len = sizeof(Elf_Phdr);
  offset = elf_header.e_phoff;
  for(int i = 0;i < elf_header.e_shnum; i++){
    fs_lseek(fd ,offset ,SEEK_SET);
    ret = fs_read(fd, &phdr[i], len);
    assert(ret == len);
    if(phdr[i].p_type == PT_LOAD){
      disk2mem(fd, phdr[i].p_offset ,phdr[i].p_vaddr ,phdr[i].p_filesz ,phdr[i].p_memsz);
    }
    offset += len;
  }
  Log("loader file:%s",filename);
  return elf_header.e_entry;
}

void naive_uload(PCB *pcb, const char *filename) {
  if(get_ramdisk_size() == 0) return;
  uintptr_t entry = loader(pcb, filename);
  Log("Jump to entry = %lu", entry);
  ((void(*)())entry) ();
}

