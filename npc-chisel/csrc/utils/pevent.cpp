#include <pevent.h>
Perf IFUGetInst;
Perf LSUGetData;
Perf EXUFinCal ;
//IDUFinDec
Perf DECisJump ;
Perf DECisStore;
Perf DECisLoad ;
Perf DECisCal  ;
Perf DECisCsr  ;
Perf DECisOther;

void e_IFUGetInst(int inc, int start, int end);
void e_LSUGetData(int inc, int start, int end);
void e_EXUFinCal(int inc, int start, int end);
void e_IDUFinDec(int inc, int subType, int start, int end, int timeEn);
void d_Jump(int inc, int start, int end, int timeEn);
void d_Store(int inc, int start, int end, int timeEn);
void d_Load(int inc, int start, int end, int timeEn);
void d_Cal(int inc, int start, int end, int timeEn);
void d_Csr(int inc, int start, int end, int timeEn);
void d_Other(int inc, int start, int end, int timeEn);

extern "C" void peventWrapper(int type, int inc, int subType, int start, int end, int timeEn){
    switch (type)
    {
    case t_IFUGetInst:
        e_IFUGetInst(inc, start, end);
        break;
    case t_LSUGetData:
        e_LSUGetData(inc, start, end);
        break;
    case t_EXUFinCal:
        e_EXUFinCal(inc, start, end);
        break;
    case t_IDUFinDec:
        e_IDUFinDec(inc, subType, start, end, timeEn);
        break;
    
    default:
        break;
    }
    return;
}

void e_IFUGetInst(int inc, int start, int end){
    if(inc) IFUGetInst.cnt ++;
    if(start) IFUGetInst.switchTime = true;
    if(end) {if(!IFUGetInst.switchTime)assert(0); IFUGetInst.switchTime = false;}
    if(IFUGetInst.switchTime) IFUGetInst.time ++;
}

void e_LSUGetData(int inc, int start, int end){
    if(inc) LSUGetData.cnt ++;
    if(start) LSUGetData.switchTime = true;
    if(end) {if(!LSUGetData.switchTime)assert(0); LSUGetData.switchTime = false;}
    if(LSUGetData.switchTime) LSUGetData.time ++;
}

void e_EXUFinCal(int inc, int start, int end){
    if(inc) EXUFinCal.cnt ++;
    if(start) EXUFinCal.switchTime = true;
    if(end) {if(!EXUFinCal.switchTime)assert(0); EXUFinCal.switchTime = false;}
    if(EXUFinCal.switchTime) EXUFinCal.time ++;
}

void e_IDUFinDec(int inc, int subType, int start, int end, int timeEn){
    bool isJump, isStore, isLoad, isCal, isCsr;
    isJump = (bool)((subType >> 0) & 1);
    isStore = (bool)((subType >> 1) & 1);
    isLoad = (bool)((subType >> 2) & 1);
    isCal = (bool)((subType >> 3) & 1);
    isCsr = (bool)((subType >> 4) & 1);
    if(isJump) d_Jump(inc, start, end, timeEn);
    if(isStore) d_Store(inc, start, end, timeEn);
    if(isLoad) d_Load(inc, start, end, timeEn);
    if(isCal) d_Cal(inc, start, end, timeEn);
    if(isCsr) d_Csr(inc, start, end, timeEn);
    if(!isJump && !isStore && !isLoad && !isCal && !isCsr)  d_Other(inc, start, end, timeEn);
}

void d_Jump(int inc, int start, int end, int timeEn)
{
    if(inc) DECisJump.cnt ++;
    if(start) DECisJump.switchTime = true;
    if(end) {if(!DECisJump.switchTime)assert(0); DECisJump.switchTime = false;}
    if(DECisJump.switchTime && timeEn) DECisJump.time ++;
}

void d_Store(int inc, int start, int end, int timeEn)
{
    if(inc) DECisStore.cnt ++;
    if(start) DECisStore.switchTime = true;
    if(end) {if(!DECisStore.switchTime)assert(0); DECisStore.switchTime = false;}
    if(DECisStore.switchTime && timeEn) DECisStore.time ++;
}

void d_Load(int inc, int start, int end, int timeEn)
{
    if(inc) DECisLoad.cnt ++;
    if(start) DECisLoad.switchTime = true;
    if(end) {if(!DECisLoad.switchTime)assert(0); DECisLoad.switchTime = false;}
    if(DECisLoad.switchTime && timeEn) DECisLoad.time ++;
}

void d_Cal(int inc, int start, int end, int timeEn)
{   
    if(inc) DECisCal.cnt ++;
    if(start) DECisCal.switchTime = true;
    if(end) {if(!DECisCal.switchTime)assert(0); DECisCal.switchTime = false;}
    if(DECisCal.switchTime && timeEn) DECisCal.time ++;
}

void d_Csr(int inc, int start, int end, int timeEn)
{
    if(inc) DECisCsr.cnt ++;
    if(start) DECisCsr.switchTime = true;
    if(end) {if(!DECisCsr.switchTime)assert(0); DECisCsr.switchTime = false;}
    if(DECisCsr.switchTime && timeEn) DECisCsr.time ++;
}

void d_Other(int inc, int start, int end, int timeEn)
{
    if(inc) DECisOther.cnt ++;
    if(start) DECisOther.switchTime = true;
    if(end) {if(!DECisOther.switchTime)assert(0); DECisOther.switchTime = false;}
    if(DECisOther.switchTime && timeEn) DECisOther.time ++;
}
