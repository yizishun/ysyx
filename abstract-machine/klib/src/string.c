#include <klib.h>
#include <klib-macros.h>
#include <stdint.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

size_t strlen(const char *s) {
	size_t len;
	for(len = 0; s[len] != '\0' ; len++);
	return len;
}

char *strcpy(char *dst, const char *src) { 
  size_t i;
	for (i = 0; src[i] != '\0'; i++)
  	dest[i] = src[i];
  dest[i] = '\0';
  return dest;
}

char *strncpy(char *dst, const char *src, size_t n) {
  size_t i;
	for (i = 0; i < n && src[i] != '\0'; i++)
  	dest[i] = src[i];
  for ( ; i < n; i++)
    dest[i] = '\0';
  return dest;
}

char *strcat(char *dst, const char *src) {
  size_t dest_len = strlen(dest);
  size_t i;
  for (i = 0 ; src[i] != '\0' ; i++)
  	dest[dest_len + i] = src[i];
  dest[dest_len + i] = '\0';
	return dest;
}

int strcmp(const char *s1, const char *s2) {
  int flag,i;
	for(i = 0; s1[i] != '\0' && s2[i] != '\0' ; i++){
		flag = s1[i] - s2[i];

	}
}

int strncmp(const char *s1, const char *s2, size_t n) {
  panic("Not implemented");
}

void *memset(void *s, int c, size_t n) {
  panic("Not implemented1");
}

void *memmove(void *dst, const void *src, size_t n) {
  panic("Not implemented");
}

void *memcpy(void *out, const void *in, size_t n) {
  panic("Not implemented");
}

int memcmp(const void *s1, const void *s2, size_t n) {
  panic("Not implemented1");
}

#endif
