#ifndef BP_H_
#define BP_H_
#include <string>
#include <iostream>
using std::string;

class BranchPredictor
{
    private:
        FILE *logfp;
        uint64_t count = 0;
        uint64_t correctCnt = 0;
        uint64_t incorrectCnt = 0;
        bool parse_inst(uint32_t inst, uint32_t pc, string& op, uint32_t& dest);
    public:
        BranchPredictor(const char* logFile) {
            logfp = fopen(logFile, "w");
            if (!logfp) {
                std::cerr << "Failed to open log file: " << logFile << std::endl;
            }
        }
        virtual ~BranchPredictor(){
            if(logfp){
                fclose(logfp);
            }
        }
        virtual bool predict(uint32_t inst) = 0;
        inline bool check(bool pred, bool actual){
            count++;
            if(pred == actual){
                correctCnt++;
                return true;
            }
            else{
                incorrectCnt++;
                return false;
            }
        }
        virtual void statistic();
        void log(const char* format, ...) {
            if (logfp) {
                va_list args;
                va_start(args, format);         // 初始化 va_list
                vfprintf(logfp, format, args);  // 使用 vfprintf 写入日志文件
                va_end(args);                   // 清理 va_list
                fflush(logfp);                  // 确保数据立即写入文件
            }
        } 
};

class BTFNPredictor : public BranchPredictor
{
    public:
    BTFNPredictor(const char* logFile) : BranchPredictor(logFile){}
    bool predict(uint32_t inst) override;
    void statistic() override{
        log("========== BTFN Predictor Statistic ==========\n");
        printf("========== BTFN Predictor Statistic ==========\n");
        BranchPredictor::statistic();
    }
    ~BTFNPredictor(){
        statistic();   
    }
};

class AlwaysTakenPredictor : public BranchPredictor
{
    public:
    AlwaysTakenPredictor(const char* logFile) : BranchPredictor(logFile){}
    bool predict(uint32_t inst) override{
        return true;
    }
    void statistic() override{
        log("========== Always Taken Predictor Statistic ==========\n");
        printf("========== Always Taken Predictor Statistic ==========\n");
        BranchPredictor::statistic();
    }
    ~AlwaysTakenPredictor(){
        statistic();   
    }
};

class AlwaysNotTakenPredictor : public BranchPredictor
{
    public:
    AlwaysNotTakenPredictor(const char* logFile) : BranchPredictor(logFile){}
    bool predict(uint32_t inst) override{
        return false;
    }
    void statistic() override{
        log("========== Always Not Taken Predictor Statistic ==========\n");
        printf("========== Always Not Taken Predictor Statistic ==========\n");
        BranchPredictor::statistic();
    }
    ~AlwaysNotTakenPredictor(){
        statistic();
    }
};

#endif