#include <BP.h>
#include <common.h>
#include <cassert>

bool BranchPredictor::parse_inst(uint32_t inst, uint32_t pc, string& op, uint32_t& dest)
{
    return true;
}

void BranchPredictor::statistic()
{
    log("count           = %llu\n", count);
    log("correct count   = %llu\n", correctCnt);
    log("incorrect count = %llu\n", incorrectCnt);
    log("accuracy        = %.2f%%\n", 100.0 * correctCnt / count);
    printf("count           = %llu\n", count);
    printf("correct count   = %llu\n", correctCnt);
    printf("incorrect count = %llu\n", incorrectCnt);
    printf("accuracy        = %.2f%%\n", 100.0 * correctCnt / count);
}