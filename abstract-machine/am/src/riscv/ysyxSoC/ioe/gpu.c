#include <am.h>
#include <klib-macros.h>
#include "../ysyxSoC.h"

#define WIDTH 640
#define HIGH 480

void __am_gpu_config(AM_GPU_CONFIG_T *cfg){
    cfg->present = true;
    cfg->has_accel = false;
    cfg->width = WIDTH;
    cfg->height = HIGH;
    cfg->vmemsz = 0;
}
void __am_gpu_fbdraw(AM_GPU_FBDRAW_T *ctl){
    int i,j;
    ctl->sync = true;
    uint32_t *p = (uint32_t *)ctl->pixels; 
    uint32_t *fb = (uint32_t *)(uintptr_t)(VGA + (ctl->y*WIDTH + ctl->x) * sizeof(uint32_t));
    for(i = 0;i < ctl->h;i++){
        for(j = 0;j < ctl->w;j++)
            fb[j] = *p++;
        fb += WIDTH;
    }
}

void __am_gpu_status(AM_GPU_STATUS_T *status){
  status->ready = true;
}