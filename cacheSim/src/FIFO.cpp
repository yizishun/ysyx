#include <common.h>
#include <cacheType.h>
#include <cache.h>
#include <algorithm>
extern FILE *logfp;
bool is_hit(cacheSet_t set, int tag, int w){
    for(int i = 0; i < w; i++){
        if(set[i].valid && set[i].tag == tag){
            return true;
        }
    }
    return false;
}


void ra_FIFO(Cache *cache, cacheSet_t& s, int tag){
    #ifdef CONFIG_TRACE
    for(auto it = s.begin(); it != s.end(); it++){
        fprintf(logfp, "set\nvalid: %d, tag: %d, %s:%llu\n",it->valid, it->tag, is_hit(s, tag, cache->get_way()) ? "hit" : "miss", cache->get_hit());
    }
    #endif
    cache->incr_req();
    if(is_hit(s, tag, cache->get_way())){
        cache->incr_hit();
        return;
    }
    else{
        cache->incr_miss();
        for(auto it = s.begin(); it != s.end(); it++){
            if(!it->valid){
                it->valid = true;
                it->tag = tag;
                break;
            }
            if(it == s.end() - 1){
                std::rotate(s.begin(), s.begin() + 1, s.end());
                s.back().valid = true;
                s.back().tag = tag;
            }
        }
    }

}