#ifndef __COMMOM_H__
#define __COMMOM_H__
#include <macro.h>

#include <inttypes.h>
#include <stdbool.h>
#include <stdio.h>
#include <string.h>

#include <assert.h>
#include <stdlib.h>


typedef uint32_t word_t;
#define FMT_WORD "0x%08x"

typedef word_t vaddr_t;
typedef uint32_t paddr_t;
#include <debug.h>
#endif