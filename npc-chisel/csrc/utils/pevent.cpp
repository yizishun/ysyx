#include <pevent.h>
char *trace_csv = "builds/trace.csv";
char *trace_csv2 = "builds/trace2.csv";
FILE *perf_fp = NULL;
FILE *perf_time_fp = NULL;
Perf IFUGetInst;
Perf IFUNGetInst;
Perf LSUGetData;
Perf LSUNGetData;
Perf EXUFinCal ;
//IDUFinDec
Perf DECisJump ;
Perf DECisStore;
Perf DECisLoad ;
Perf DECisCal  ;
Perf DECisCsr  ;
Perf DECisOther;
//icache
Perf ICacheHit;
Perf ICacheMiss;

void e_IFUGetInst(int inc, int start, int end);
void e_IFUNGetInst(int inc, int start, int end);
void e_LSUNGetData(int inc, int start, int end);
void e_LSUGetData(int inc, int start, int end);
void e_EXUFinCal(int inc, int start, int end);
void e_IDUFinDec(int inc, int subType, int start, int end, int timeEn);
void d_Jump(int inc, int start, int end, int timeEn);
void d_Store(int inc, int start, int end, int timeEn);
void d_Load(int inc, int start, int end, int timeEn);
void d_Cal(int inc, int start, int end, int timeEn);
void d_Csr(int inc, int start, int end, int timeEn);
void d_Other(int inc, int start, int end, int timeEn);
void e_ICacheHit(int inc, int start, int end);
void e_ICacheMiss(int inc, int start, int end);

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
    case t_ICacheHit:
        e_ICacheHit(inc, start, end);
        break;
    case t_ICacheMiss:
        e_ICacheMiss(inc, start, end);
        break;
    case t_IFUNGetInst:
        e_IFUNGetInst(inc, start, end);
        break;
    case t_LSUNGetData:
        e_LSUNGetData(inc, start, end);
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
    if(start & end) IFUGetInst.time ++;
}

void e_IFUNGetInst(int inc, int start, int end){
    if(inc) IFUNGetInst.cnt ++;
    if(start) IFUNGetInst.switchTime = true;
    if(end) {if(!IFUNGetInst.switchTime)assert(0); IFUNGetInst.switchTime = false;}
    if(IFUNGetInst.switchTime) IFUNGetInst.time ++;
    if(start & end) IFUNGetInst.time ++;
}
void e_LSUGetData(int inc, int start, int end){
    if(inc) LSUGetData.cnt ++;
    if(start) LSUGetData.switchTime = true;
    if(end) {if(!LSUGetData.switchTime)assert(0); LSUGetData.switchTime = false;}
    if(LSUGetData.switchTime) LSUGetData.time ++;
    if(start & end) LSUGetData.time ++;
}

void e_LSUNGetData(int inc, int start, int end){
    if(inc) LSUNGetData.cnt ++;
    if(start) LSUNGetData.switchTime = true;
    if(end) {if(!LSUNGetData.switchTime)assert(0); LSUNGetData.switchTime = false;}
    if(LSUNGetData.switchTime) LSUNGetData.time ++;
    if(start & end) LSUNGetData.time ++;
}
void e_EXUFinCal(int inc, int start, int end){
    if(inc) EXUFinCal.cnt ++;
    if(start) EXUFinCal.switchTime = true;
    if(end) {if(!EXUFinCal.switchTime)assert(0); EXUFinCal.switchTime = false;}
    if(EXUFinCal.switchTime) EXUFinCal.time ++;
    if(start & end) EXUFinCal.time ++;
}

void e_ICacheHit(int inc, int start, int end){
    if(inc) ICacheHit.cnt ++;
    if(start) ICacheHit.switchTime = true;
    if(end) {if(!ICacheHit.switchTime)assert(0); ICacheHit.switchTime = false;}
    if(ICacheHit.switchTime) ICacheHit.time ++;
    if(start & end) ICacheHit.time ++;
}

void e_ICacheMiss(int inc, int start, int end){
    if(inc) ICacheMiss.cnt ++;
    if(start) ICacheMiss.switchTime = true;
    if(end) {if(!ICacheMiss.switchTime)assert(0); ICacheMiss.switchTime = false;}
    if(ICacheMiss.switchTime) ICacheMiss.time ++;       
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
    if(start & end) DECisJump.time ++;
}

void d_Store(int inc, int start, int end, int timeEn)
{
    if(inc) DECisStore.cnt ++;
    if(start) DECisStore.switchTime = true;
    if(end) {if(!DECisStore.switchTime)assert(0); DECisStore.switchTime = false;}
    if(DECisStore.switchTime && timeEn) DECisStore.time ++;
    if(start & end) DECisStore.time ++;
}

void d_Load(int inc, int start, int end, int timeEn)
{
    if(inc) DECisLoad.cnt ++;
    if(start) DECisLoad.switchTime = true;
    if(end) {if(!DECisLoad.switchTime)assert(0); DECisLoad.switchTime = false;}
    if(DECisLoad.switchTime && timeEn) DECisLoad.time ++;
    if(start & end) DECisLoad.time ++;
}

void d_Cal(int inc, int start, int end, int timeEn)
{   
    if(inc) DECisCal.cnt ++;
    if(start) DECisCal.switchTime = true;
    if(end) {if(!DECisCal.switchTime)assert(0); DECisCal.switchTime = false;}
    if(DECisCal.switchTime && timeEn) DECisCal.time ++;
    if(start & end) DECisCal.time ++;
}

void d_Csr(int inc, int start, int end, int timeEn)
{
    if(inc) DECisCsr.cnt ++;
    if(start) DECisCsr.switchTime = true;
    if(end) {if(!DECisCsr.switchTime)assert(0); DECisCsr.switchTime = false;}
    if(DECisCsr.switchTime && timeEn) DECisCsr.time ++;
    if(start & end) DECisCsr.time ++;
}

void d_Other(int inc, int start, int end, int timeEn)
{
    if(inc) DECisOther.cnt ++;
    if(start) DECisOther.switchTime = true;
    if(end) {if(!DECisOther.switchTime)assert(0); DECisOther.switchTime = false;}
    if(DECisOther.switchTime && timeEn) DECisOther.time ++;
    if(start & end) DECisOther.time ++;
}

void record_perf_trace(uint64_t cycle, uint64_t instCnt){
    if(cycle == 1 && perf_fp == NULL){
        //init
        perf_fp = fopen(trace_csv, "w");
        perf_time_fp = fopen(trace_csv2, "w");
        Assert(perf_time_fp, "Can not open '%s'", trace_csv2);
        Assert(perf_fp, "Can not open '%s'", trace_csv);
        fprintf(perf_fp, "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n", "cpuCycle", "指令数", "IFU获得指令", "EXU结束计算", "LSU获得数据", "跳转指令", "加载指令", "存储指令", "计算指令", "csr指令","其他指令");
        //fflush(perf_fp);
        fprintf(perf_time_fp, "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n", "总时间", "IFU占用时间", "EXU占用时间", "LSU占用时间", "跳转指令占用时间", "加载指令占用时间", "存储指令占用时间", "计算指令占用时间", "csr指令占用时间","其他指令占用时间");
        //fflush(perf_time_fp);
    }
    else{
        uint64_t IDUFinDec = DECisJump.cnt + DECisStore.cnt + DECisLoad.cnt + DECisCal.cnt + DECisCsr.cnt + DECisOther.cnt;
        fprintf(perf_fp, "%llu,%llu,%llu,%llu,%llu,%llu,%llu,%llu,%llu,%llu,%llu\n",cycle, instCnt, IFUGetInst.cnt, EXUFinCal.cnt, LSUGetData.cnt,
        DECisJump.cnt, DECisLoad.cnt, DECisStore.cnt, DECisCal.cnt, DECisCsr.cnt, DECisOther.cnt);
        //fflush(perf_fp);
    }
}
