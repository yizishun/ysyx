#include <common.h>
#include "syscall.h"
#include <fs.h>

__attribute__((unused)) static void strace(int no, int a1, int a2, int a3){
  char s0[128];
  char *s = s0;
  s += sprintf(s ,"STRACE(nanos) ");
  switch (no)
  {
  case SYS_yield:
    s += sprintf(s ,"yield");
    break;
  case SYS_exit:
    s += sprintf(s ,"exit( %d(status) )",a1);
    break;
  case SYS_open:
    s += sprintf(s ,"open( %s(pathname) )",(char *)a1);
    break;
  case SYS_close:
    s += sprintf(s ,"close( %s(fd=%d) )",fd2pathname(a1),a1);
    break;
  case SYS_lseek:
    s += sprintf(s ,"lseek( %s(fd=%d), %d(offset), %d(whence))",fd2pathname(a1),a1,a2,a3);
    break;
  case SYS_read:
    s += sprintf(s ,"read( %s(fd=%d), (buf), %d(count)) ",fd2pathname(a1), a1,a3);
    break;
  case SYS_write:
    s += sprintf(s ,"write( %s(fd=%d), (buf), %d(count))",fd2pathname(a1), a1,a3);
    break;
  case SYS_brk:
    s += sprintf(s ,"brk  ");
    break;
  default:
    s += sprintf(s ,"UNKOWN syscall");
    break;
  }
  printf("%s\n\n",s0);
}

int sys_open(char *pathname, int flags, int mode){
  return fs_open((const char *)pathname, flags, mode);
}

int sys_close(int fd){
  return fs_close(fd);
}

size_t sys_write(int fd ,void *buf ,size_t count){
  return fs_write(fd, (const void *)buf, count);
}

size_t sys_read(int fd, void *buf, size_t count){
  return fs_read(fd ,buf, count);
}

size_t sys_lseek(int fd, size_t offset, int whence){
  return fs_lseek(fd, offset, whence);
}

void do_syscall(Context *c) {
  uintptr_t a[4];
  a[0] = c->GPR1;
  a[1] = c->GPR2;
  a[2] = c->GPR3;
  a[3] = c->GPR4;
  #ifdef CONFIG_STRACE
  strace(a[0], a[1], a[2], a[3]);
  #endif
  switch (a[0]) {
    case SYS_yield:
      yield();
      c->GPRx = 0;
      break;
    case SYS_open:
      c->GPRx = sys_open((char *)a[1] ,a[2] ,a[3]);
      break;
    case SYS_close:
      c->GPRx = sys_close(a[1]);
      break;
    case SYS_lseek:
      c->GPRx = sys_lseek(a[1] ,a[2] ,a[3]);
      break;
    case SYS_read:
      c->GPRx = sys_read(a[1] ,(void *)a[2] ,a[3]);
      break;
    case SYS_write:
      c->GPRx = sys_write(a[1] ,(void *)a[2] ,a[3]);
      break;
    case SYS_brk:
      c->GPRx = 0;
      break;
    case SYS_exit:
      halt(a[1]);
      break;
    default: panic("Unhandled syscall ID = %d", a[0]);
  }
}
