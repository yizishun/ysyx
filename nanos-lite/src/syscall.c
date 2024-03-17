#include <common.h>
#include "syscall.h"

static void strace(int no){
  char s0[50];
  char *s = s0;
  s += sprintf(s ,"STRACE(nanos) ");
  switch (no)
  {
  case SYS_yield:
    s += sprintf(s ,"SYS_yield");
    break;
  case SYS_exit:
    s += sprintf(s ,"SYS_exit ");
    break;
  case SYS_write:
    s += sprintf(s ,"SYS_write");
    break;
  case SYS_brk:
    s += sprintf(s ,"SYS_brk  ");
    break;
  default:
    s += sprintf(s ,"UNKOWN syscall");
    break;
  }
  printf("%s\n",s0);
}

size_t sys_write(int fd ,void *buf ,size_t count){
  int i;
  char *c = (char *)buf;
  switch (fd)
  {
  case 1:
  case 2:
    for(i = 0;i < count;i++)
      putch(*c++);
    break;
  default:
    panic("invalid fd : %d",fd);
    break;
  }
  return c - (char *)buf;
}

void do_syscall(Context *c) {
  uintptr_t a[4];
  a[0] = c->GPR1;
  a[1] = c->GPR2;
  a[2] = c->GPR3;
  a[3] = c->GPR4;
  strace(a[0]);
  switch (a[0]) {
    case SYS_yield:
      yield();
      c->GPRx = 0;
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
