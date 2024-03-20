#include <fs.h>

size_t get_ramdisk_size();
size_t ramdisk_read(void *buf, size_t offset, size_t len);
size_t ramdisk_write(const void *buf, size_t offset, size_t len);

typedef size_t (*ReadFn) (void *buf, size_t offset, size_t len);
typedef size_t (*WriteFn) (const void *buf, size_t offset, size_t len);

typedef struct {
  char *name;
  size_t size;
  size_t disk_offset;
  ReadFn read;
  WriteFn write;
  bool open;
  size_t open_offset;
} Finfo;

enum {FD_STDIN, FD_STDOUT, FD_STDERR, FD_EVENT, FD_DISPINFO, FD_FB};

size_t serial_write(const void *buf, size_t offset, size_t len);
size_t events_read(void *buf, size_t offset, size_t len);
size_t dispinfo_read(void *buf, size_t offset, size_t len);
size_t fb_write(const void *buf, size_t offset, size_t len);

size_t invalid_read(void *buf, size_t offset, size_t len) {
  panic("should not reach here");
  return 0;
}

size_t invalid_write(const void *buf, size_t offset, size_t len) {
  panic("should not reach here");
  return 0;
}

/* This is the information about all files in disk. */
static Finfo file_table[] __attribute__((used)) = {
  [FD_STDIN]  = {"stdin", 0, 0, invalid_read, invalid_write},
  [FD_STDOUT] = {"stdout", 0, 0, invalid_read, serial_write},
  [FD_STDERR] = {"stderr", 0, 0, invalid_read, serial_write},
  [FD_EVENT]  = {"/dev/events", 0, 0, events_read, invalid_write},
  [FD_DISPINFO]={"/proc/dispinfo", 0, 0,dispinfo_read, invalid_write},
  [FD_FB]     = {"/dev/fb", 0, 0,invalid_read, fb_write},
#include "files.h"
};

static void init_OpenState(){
  int fd = 5;
  for(;file_table[fd].name != NULL;fd ++){
    file_table[fd].open = false;
  }
}

int str2num(int *num, char *buf, int offset){
  int i = offset;
  int number = 0;
  int digit;
  for(;buf[i] >= '0' && buf[i] <= '9';i++);
  digit = i - offset;
  int n = 1;
  for(int j = 0;j < digit-1;j ++) n*=10;
  for(i = offset; i - offset < digit ;i ++){
    number += (buf[i] - '0') * n;
    n /= 10;
  }
  *num = number;
  return offset + digit;
}

void init_fs() {
  // TODO: initialize the size of /dev/fb
  init_OpenState();

  AM_GPU_CONFIG_T cfg = io_read(AM_GPU_CONFIG);
  file_table[FD_FB].size = cfg.width * cfg.height;
}


//---some utils---------
static int pathname2fd(const char *pathname){
  int fd;
  for(fd = 0;file_table[fd].name != NULL;fd ++){
    if(strcmp(pathname, file_table[fd].name) == 0){
      return fd;
    }
  }
  panic("fs : no filename is %s\n",pathname);
}

char * fd2pathname(int fd){
  return file_table[fd].name;
}

static void check_bound(int fd, size_t count){
  if(file_table[fd].name == NULL)
    panic("fs : unkonwn file descripter(checkbound)");
  if(file_table[fd].open == false)
    panic("fs : you are trying to operate an unopened file descripter(checkbound)");
}

static int regular_read(int fd, void *buf, int len){
  int count = len;
  char *c = (char *)buf;
  c += ramdisk_read(buf, file_table[fd].disk_offset + file_table[fd].open_offset, len);
  count = c - (char *)buf;
  return count;
}

static int regular_write(int fd,const void *buf, int len){
  int count = len;
  char *c = (char *)buf;
  c += ramdisk_write(buf ,file_table[fd].disk_offset + file_table[fd].open_offset ,len);
  count = c - (char *)buf;
  return count;
}

//-----some file api---------
int fs_open(const char *pathname, int flags, int mode){
  int fd = pathname2fd(pathname);
  file_table[fd].open = true;
  file_table[fd].open_offset = 0;
  return fd;
}

int fs_close(int fd){
  file_table[fd].open_offset = 0;
  file_table[fd].open = false;
  return 0;
}

size_t fs_write(int fd ,const void *buf ,size_t count){
  int len;
  if(file_table[fd].write != NULL){
    len = file_table[fd].write(buf, file_table[fd].open_offset, count);
  }
  else{
    check_bound(fd, count);
    if(file_table[fd].open_offset + count > file_table[fd].size){
      panic("fs(write): offset out of bound.(checkbound), offset = %lu\n",file_table[fd].open_offset + count);
    }
    len = regular_write(fd, buf, count);
  }
  file_table[fd].open_offset += len;
  assert(len != -1);
  return len;
}

size_t fs_read(int fd, void *buf, size_t count){
  int len = -1;
  if(file_table[fd].read != NULL){
    len = file_table[fd].read(buf, file_table[fd].open_offset, count);
  }
  else{
    if(file_table[fd].open_offset + count > file_table[fd].size){ //reach EOF
      len = file_table[fd].size - file_table[fd].open_offset;
      len = regular_read(fd, buf, len);
    }
    else{
      check_bound(fd, count);
      len = regular_read(fd, buf, count);
    }
  }
  file_table[fd].open_offset += len;
  assert(len != -1);
  return len;
}

size_t fs_lseek(int fd, size_t offset, int whence){
  if(file_table[fd].name == NULL) panic("fs : unkonwn file descripter(lseek)");

  size_t foffset = whence == SEEK_SET ?  0: 
                  (whence == SEEK_CUR ? file_table[fd].open_offset : file_table[fd].size);
  file_table[fd].open_offset = foffset; //modify open_offset before plus offset

  check_bound(fd, offset);
  if(file_table[fd].open_offset + offset > file_table[fd].size){
    panic("fs(lseek): offset out of bound.(checkbound), offset = %lu\n",file_table[fd].open_offset + offset);
  }

  file_table[fd].open_offset += offset;

  return file_table[fd].open_offset;
}