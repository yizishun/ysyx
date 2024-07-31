#include <pevent.h>
uint64_t cnt_IFUGetInst = 0;
uint64_t cnt_LSUGetData = 0;
uint64_t cnt_EXUFinCal  = 0;
//IDUFinDec
uint64_t cnt_DECisJump  = 0;
uint64_t cnt_DECisStore = 0;
uint64_t cnt_DECisLoad  = 0;
uint64_t cnt_DECisCal   = 0;
uint64_t cnt_DECisCsr   = 0;

void e_IFUGetInst(int inc);
void e_LSUGetData(int inc);
void e_EXUFinCal(int inc);
void e_IDUFinDec(int inc, int subType);
extern "C" void peventWrapper(int type, int inc, int subType){
    switch (type)
    {
    case IFUGetInst:
        e_IFUGetInst(inc);
        break;
    case LSUGetData:
        e_LSUGetData(inc);
        break;
    case EXUFinCal:
        e_EXUFinCal(inc);
        break;
    case IDUFinDec:
        e_IDUFinDec(inc, subType);
        break;
    
    default:
        break;
    }
    return;
}

void e_IFUGetInst(int inc){
    if(inc) cnt_IFUGetInst ++;
}

void e_LSUGetData(int inc){
    if(inc) cnt_LSUGetData ++;
}

void e_EXUFinCal(int inc){
    if(inc) cnt_EXUFinCal ++;
}

void e_IDUFinDec(int inc, int subType){
    bool isJump, isStore, isLoad, isCal, isCsr;
    isJump = (bool)((subType >> 0) & 1);
    isStore = (bool)((subType >> 1) & 1);
    isLoad = (bool)((subType >> 2) & 1);
    isCal = (bool)((subType >> 3) & 1);
    isCsr = (bool)((subType >> 4) & 1);
    if(inc){
        if(isJump) cnt_DECisJump ++;
        if(isStore) cnt_DECisStore ++;
        if(isLoad) cnt_DECisLoad ++;
        if(isCal) cnt_DECisCal ++;
        if(isCsr) cnt_DECisCsr ++;
    }
}