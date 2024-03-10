#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

#define MAXDEC 32
static char *__out;
void sputch(char ch){*__out++ = ch;}

int vprintf( void(*gputch)(char) , const char *fmt, va_list ap){
	int i;
	char *s;
	int d;
	int pos = 0;
	for( ;*fmt != '\0';fmt++){
		if(*fmt != '%'){
			gputch(*fmt);pos++;
		}
		else{
			switch(*(++fmt)){
				case 's':  //%s
					s = va_arg(ap , char *);
					for(i = 0; s[i] != '\0'; i++){
						gputch(s[i]);pos++;
					}
					break;
				case 'd':  //%d
					d = va_arg(ap , int);
					if(d < 0){
						d = -d;
						gputch('-');pos++;
					}
					if(d == 0){
						gputch('0');pos++;
					};
					char invert[MAXDEC];
					i = 0;
					for( ; d != 0 ; i++ , d/=10){
						invert[i] = d%10 + '0';
					}
					for(i-=1 ;i >= 0 ; i--){
						gputch(invert[i]);pos++;
					}
					break;
			}
		}
	}
	return pos;
}

int printf(const char *fmt, ...) {
	va_list ap;
	va_start(ap, fmt);
	int res = vprintf(putch , fmt , ap);
	va_end(ap);
	return res;
}

int vsprintf(char *out, const char *fmt, va_list ap) {
  panic("Not implemented");
}

int sprintf(char *out, const char *fmt, ...) {
	va_list ap;
	va_start(ap, fmt);
	__out = out;
	int res = vprintf(sputch , fmt , ap);
	sputch('\0');
	va_end(ap);
	return res++;
}

int snprintf(char *out, size_t n, const char *fmt, ...) {
  panic("Not implemented");
}

int vsnprintf(char *out, size_t n, const char *fmt, va_list ap) {
  panic("Not implemented");
}

#endif
