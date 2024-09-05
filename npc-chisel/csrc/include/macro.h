#ifndef __MACRO_H__
#define __MACRO_H__

// calculate the length of an array
#define ARRLEN(arr) (int)(sizeof(arr) / sizeof(arr[0]))
#define STR_HELPER(x) #x
#define STR(x) STR_HELPER(x)
#endif