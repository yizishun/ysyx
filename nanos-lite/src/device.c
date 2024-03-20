#include <common.h>

#if defined(MULTIPROGRAM) && !defined(TIME_SHARING)
# define MULTIPROGRAM_YIELD() yield()
#else
# define MULTIPROGRAM_YIELD()
#endif

#define NAME(key) \
  [AM_KEY_##key] = #key,

static const char *keyname[256] __attribute__((used)) = {
  [AM_KEY_NONE] = "NONE",
  AM_KEYS(NAME)
};

size_t serial_write(const void *buf, size_t offset, size_t len) {
  int i;
  char *c = (char *)buf;
  for(i = 0;i < len;i ++){
    putch(*c++);
  }
  return c - (char *)buf;
}

size_t events_read(void *buf, size_t offset, size_t len) {
  int count = 0, i;
  char *s = (char *)malloc(20);
  assert(s != NULL);
  char *e = s;
  AM_INPUT_KEYBRD_T kb = io_read(AM_INPUT_KEYBRD);
  if(kb.keycode == AM_KEY_NONE) return 0;
  e += sprintf(e, "%s %s\n", kb.keydown ? "kd":"ku", keyname[kb.keycode]);
  count = e - s;
  e = (char *)buf;
  for(i = 0;i < len && i < count;i ++){
    *e++ = *s++;
  }
  free(s - count);
  s = NULL;e = NULL;
  return i;
}

size_t dispinfo_read(void *buf, size_t offset, size_t len) {
  int count = 0,i;
  AM_GPU_CONFIG_T cfg = io_read(AM_GPU_CONFIG);
  char *s = (char *)malloc(40);
  assert(s != NULL);
  char *e = s;
  e += sprintf(e, "WIDTH : %lu\n",cfg.width);
  e += sprintf(e, "HEIGHT: %lu\n",cfg.height);
  count = e - s;
  e = (char *)buf;
  for(i = 0;i < len && i < count;i ++){
    *e++ = *s++;
  }
  free(s - count);
  s = NULL;e = NULL;
  return i;
}
//write len bytes in buf to offset in fb
size_t fb_write(void *buf, size_t offset, size_t len) {
  AM_GPU_CONFIG_T cfg = io_read(AM_GPU_CONFIG);
  int x = offset % cfg.width;
  int y = offset / cfg.width;
  io_write(AM_GPU_FBDRAW, x, y, buf, len, 1, true);
  return len;
}

void init_device() {
  Log("Initializing devices...");
  ioe_init();
}
