#include <common.h>
#include <cacheType.h>
#include <math.h>
class Cache;
typedef void (*ra)(Cache *, cacheSet_t&, int);
class Cache{
    private:
        cache_t cache;
        int set = 16;
        int way = 1;
        int block_size = 4;
        ra replaceAlgo;
        Report report;
    public:
        Cache(ra r, int s = 16, int w = 1, int bsz = 4);
        ~Cache();
        inline int get_set(){return set;}
        inline int get_way(){return way;}
        inline void incr_hit(){report.hit++;}
        inline uint64_t get_hit(){return report.hit;}
        inline void incr_miss(){report.miss++;}
        inline uint64_t get_miss(){return report.miss;}
        inline void incr_req(){report.reqCnt++;}
        inline uint64_t get_req(){return report.reqCnt;}
        inline int M(){return log2(set); }
        inline int N(){ return log2(block_size); }
        inline int INDEX(paddr_t addr){ return (addr>>N())&((1<<M())-1); }
        inline int TAG(paddr_t addr) { return (addr>>(N()+M()))&((1<<(32-N()-M()))-1); }
        inline int OFFSET(paddr_t addr) { return addr&((1<<N())-1); }
        inline cacheSet_t& decode(paddr_t addr) { return cache[INDEX(addr)]; }
        void statistic();
        void replace(cacheSet_t& set, paddr_t addr);
        bool has_empty(cacheSet_t set);
        friend void ra_FIFO(Cache *, cacheSet_t& s, int tag);
        friend void ra_LRU(Cache *, cacheSet_t& s, int tag);
};