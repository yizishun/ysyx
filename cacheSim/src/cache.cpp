#include <cache.h>
//cache -> cacheSet_t(vector<cacheBlock_t>) -> cacheBlock
Cache::Cache(ra r, int s, int w, int bsz)
: set(s), way(w), block_size(bsz), replaceAlgo(r)
{
    cache = new cacheSet_t[set];
    for(int i = 0; i < set; i++){
        cache[i].resize(way);
        for(int j = 0; j < way; j++){
            cache[i][j].valid = false;
        }
    }
    report.hit = 0;
    report.miss = 0;
    report.reqCnt = 0;
}

Cache::~Cache()
{
    delete[] cache;
} 

void Cache::replace(cacheSet_t &set, paddr_t addr)
{
    replaceAlgo(this, set, TAG(addr));
}

bool Cache::has_empty(cacheSet_t set)
{
    bool flag = false;
    for(int i = 0; i < way; i++){
        if(!set[i].valid)
            flag = true;
    }
    return flag;
}

void Cache::statistic(){
    printf("Req count : %llu\n", report.reqCnt);
    printf("Hit count : %llu\n", report.hit);
    printf("Miss count : %llu\n", report.miss);
    printf("Hit rate : %lf\n", (double)report.hit / (double)report.reqCnt);
}
