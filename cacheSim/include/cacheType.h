#ifndef CACHE_TYPE_H
#define CACHE_TYPE_H

#include <common.h>
#include <vector>

typedef uint32_t paddr_t;
typedef struct{
    uint64_t reqCnt;
    uint64_t hit;
    uint64_t miss;
}Report;

typedef struct{
    uint32_t tag;
    uint32_t index;
    bool valid;
}cacheBlock_t;

typedef std::vector<cacheBlock_t> cacheSet_t;
typedef cacheSet_t* cache_t;
#endif