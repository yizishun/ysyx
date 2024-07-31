#ifndef PEVENT_H
#define PEVENT_H
#include <common.h>
extern uint64_t cnt_IFUGetInst;
extern uint64_t cnt_LSUGetData;
extern uint64_t cnt_EXUFinCal;
extern uint64_t cnt_DECisJump ;
extern uint64_t cnt_DECisStore;
extern uint64_t cnt_DECisLoad ;
extern uint64_t cnt_DECisCal  ;
extern uint64_t cnt_DECisCsr  ;
enum {
    IFUGetInst,
    LSUGetData,
    EXUFinCal ,
    IDUFinDec 
};
enum {
    isJump,
    isStore,
    isLoad,
    isCal,
    isCsr
};

#endif