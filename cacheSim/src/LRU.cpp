#include <common.h>
#include <cacheType.h>
#include <cache.h>
#include <algorithm>
extern FILE *logfp;
extern bool is_hit(cacheSet_t set, int tag, int w);


void ra_LRU(Cache *cache, cacheSet_t& s, int tag){
    #ifdef CONFIG_TRACE
    fprintf(logfp, "set\n");
    for(auto it = s.begin(); it != s.end(); it++){
        fprintf(logfp, "valid: %d, tag: %d, %s:%llu\n",it->valid, it->tag, is_hit(s, tag, cache->get_way()) ? "hit" : "miss", cache->get_hit());
    }
    #endif
    cache->incr_req();

    bool hit = false;
    int w = cache->get_way();
    //hit:refresh priority
    for(auto it = s.begin(); it < s.end(); it++){
        if(it->valid && it->tag == tag){
            hit = true;
            cache->incr_hit();
            std::rotate(s.begin(), it, it+1);
        }
    }
    if(hit){
    }
    else{
        cache->incr_miss();
        std::rotate(s.begin(), s.end() - 1, s.end());
        s.front().valid = true;
        s.front().tag = tag;
    }




    #ifdef CONFIG_TRACE
    fprintf(logfp, "set after\n");
    for(auto it = s.begin(); it != s.end(); it++){
        fprintf(logfp, "valid: %d, tag: %d, %s:%llu\n",it->valid, it->tag, is_hit(s, tag, cache->get_way()) ? "hit" : "miss", cache->get_hit());
    }
    #endif
}