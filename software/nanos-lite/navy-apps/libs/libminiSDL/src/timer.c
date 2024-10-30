#include <NDL.h>
#include <sdl-timer.h>
#include <stdio.h>

SDL_TimerID SDL_AddTimer(uint32_t interval, SDL_NewTimerCallback callback, void *param) {
  TODO();
  return NULL;
}

int SDL_RemoveTimer(SDL_TimerID id) {
  TODO();
  return 1;
}

uint32_t SDL_GetTicks() {
  return NDL_GetTicks()/1000;
}

void SDL_Delay(uint32_t ms) {
}
