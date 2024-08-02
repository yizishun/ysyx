#ifndef PEVENT_H
#define PEVENT_H
#include <common.h>
struct Perf {
    uint64_t cnt = 0;
    uint64_t time = 0;
    bool switchTime = false;
};
extern Perf IFUGetInst;
extern Perf LSUGetData;
extern Perf EXUFinCal;
extern Perf DECisJump ;
extern Perf DECisStore;
extern Perf DECisLoad ;
extern Perf DECisCal  ;
extern Perf DECisCsr  ;
extern Perf DECisOther;
enum {
    t_IFUGetInst,
    t_LSUGetData,
    t_EXUFinCal ,
    t_IDUFinDec 
};
enum {
    isJump,
    isStore,
    isLoad,
    isCal,
    isCsr
};
#endif