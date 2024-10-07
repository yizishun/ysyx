#ifndef PEVENT_H
#define PEVENT_H
#include <common.h>
struct Perf {
    uint64_t cnt = 0;
    uint64_t time = 0;
    bool switchTime = false;
};
extern Perf IFUGetInst;
extern Perf IFUNGetInst;
extern Perf LSUGetData;
extern Perf LSUNGetData;
extern Perf EXUFinCal;
extern Perf DECisJump ;
extern Perf DECisStore;
extern Perf DECisLoad ;
extern Perf DECisCal  ;
extern Perf DECisCsr  ;
extern Perf DECisOther;
extern Perf ICacheHit;
extern Perf ICacheMiss;
enum {
    t_IFUGetInst,
    t_IFUNGetInst,
    t_LSUGetData,
    t_LSUNGetData,
    t_EXUFinCal ,
    t_IDUFinDec ,
    t_ICacheHit ,
    t_ICacheMiss,
};
enum {
    isJump,
    isStore,
    isLoad,
    isCal,
    isCsr
};
void record_perf_trace(uint64_t cycle, uint64_t instCnt);
extern char *trace_csv;
extern FILE *perf_time_fp;
extern FILE *perf_fp;
#endif