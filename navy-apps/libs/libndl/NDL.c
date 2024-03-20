#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/time.h>
#include <sys/file.h>
#include <assert.h>

static int evtdev = -1;
static int fbdev = -1;
static int screen_w = 0, screen_h = 0;

uint32_t NDL_GetTicks() {
  struct timeval now;
  gettimeofday(&now, NULL);
  uint64_t us = now.tv_sec * 1000000 + now.tv_usec;
  return (uint32_t)us;
}

int NDL_PollEvent(char *buf, int len) {
  int fd = open("/dev/events", 0, 0);
  int ret = 0; 
  ret = read(fd, buf, len);
  if(ret > 0)
    return 1;
  else 
    return 0;
}

int str2num(int *num, char *buf, int offset){
  int i = offset;
  int number = 0;
  int digit,d;
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

int GetVgaSize(int *width, int *height){
  int i;
  char *buf = (char *)malloc(40);
  int fd = open("/proc/dispinfo", 0, 0);
  int ret = read(fd, buf, 40);
  for(i = 0;buf[i] != '\n';i ++){
    if(buf[i] >= '0' && buf[i] <= '9'){
      i += str2num(width, buf, i);
      break;
    }
    else
      continue;
  }
  for(i += 1 ;buf[i] != '\n';i ++){
    if(buf[i] >= '0' && buf[i] <= '9'){
      i += str2num(height, buf, i);
      break;
    }
    else
      continue;
  }
  return 1;
}

void NDL_OpenCanvas(int *w, int *h) {
  if (getenv("NWM_APP")) {
    int fbctl = 4;
    fbdev = 5;
    screen_w = *w; screen_h = *h;
    char buf[64];
    int len = sprintf(buf, "%d %d", screen_w, screen_h);
    // let NWM resize the window and create the frame buffer
    write(fbctl, buf, len);
    while (1) {
      // 3 = evtdev
      int nread = read(3, buf, sizeof(buf) - 1);
      if (nread <= 0) continue;
      buf[nread] = '\0';
      if (strcmp(buf, "mmap ok") == 0) break;
    }
    close(fbctl);
  }
  screen_w = *w; screen_h = *h;
  int width,height;
  GetVgaSize(&width, &height);
  if(*w > width || *h > height){
    printf("NDL(NDL_OpenCansvas) fail:size erron");
    assert(0);
  }
}

void NDL_DrawRect(uint32_t *pixels, int x, int y, int w, int h) {
  int fd = open("/dev/fb", 0 ,0);
  uint32_t *p = pixels;
  int offset,width,height,i;
  GetVgaSize(&width, &height);
  int basex = (width - screen_w)/2;
  int basey = (height - screen_h)/2;
  x = basex + x;
  y = basey + y;
  offset = x + y * width;
  lseek(fd, offset, SEEK_SET);
  for(i = 0;i < h;i ++){
    write(fd, p, w);
    lseek(fd, width - w, SEEK_CUR);
    p += w;
  }
}

void NDL_OpenAudio(int freq, int channels, int samples) {
}

void NDL_CloseAudio() {
}

int NDL_PlayAudio(void *buf, int len) {
  return 0;
}

int NDL_QueryAudio() {
  return 0;
}

int NDL_Init(uint32_t flags) {
  if (getenv("NWM_APP")) {
    evtdev = 3;
  }
  return 0;
}

void NDL_Quit() {
}
