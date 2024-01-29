#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

#define MAXDEC 32

int printf(const char *fmt, ...) {
  panic("Not implemented");
}

int vsprintf(char *out, const char *fmt, va_list ap) {
  panic("Not implemented");
}

int sprintf(char *out, const char *fmt, ...) {
	va_list ap;
	int i,j,k;
	char *s;
	int d;
	char a[MAXDEC];
	va_start(ap, fmt);
	for(i = 0, j = 0; fmt[i] != '\0' ; i++ , j++){
		if(fmt[i] != '%')
			out[j] = fmt[i];
		else{
			switch(fmt[++i]){
				case 's':
					s = va_arg(ap , char *);
					for(k = 0; s[k] != '\0'; k++ , j++)
						out[j] = s[k];
					j--;
					break;
				case 'd':
					d = va_arg(ap , int);
					k = 0;
					if(d < 0){
						d = -d;
						out[j++] = '-';
					}
					if(d == 0)
						a[k++] = '0';
					for( ; d != 0 ; k++ , d/=10){
						a[k] = d%10 + '0';
					}
					for(k-- ;k >= 0 ; k-- , j++)
						out[j] = a[k];
					j--;
					break;
			}
		}
	}
	va_end(ap);
	out[j] = '\0';
	return j;
}

int snprintf(char *out, size_t n, const char *fmt, ...) {
  panic("Not implemented");
}

int vsnprintf(char *out, size_t n, const char *fmt, va_list ap) {
  panic("Not implemented");
}

#endif
