#include <BP.h>
#include <common.h>
#include <cassert>
#define BITMASK(bits) ((1ull << (bits)) - 1)
#define BITS(x, hi, lo) (((x) >> (lo)) & BITMASK((hi) - (lo) + 1)) // similar to x[hi:lo] in verilog
#define SEXT(x, len) ({ struct { int64_t n : len; } __x = { .n = x }; (uint64_t)__x.n; })

bool BranchPredictor::parse_inst(uint32_t pc, uint32_t i, string* op, uint32_t* dest)
{
    int opcode = BITS(i, 6, 0);
    int func3 = BITS(i, 14, 12);
    int64_t immB = ((SEXT((int64_t)BITS(i, 31, 31), 1) << 11) | BITS(i, 7, 7) << 10 | BITS(i, 30 , 25) << 4 | BITS(i , 11 , 8)) << 1;

    int64_t immJ = ((SEXT((int64_t)BITS(i, 31, 31), 1) << 19) | BITS(i, 19, 12) << 11 | BITS(i, 20 , 20) << 10 | BITS(i , 30 , 21)) << 1;
    int64_t immI = SEXT((int64_t)BITS(i, 31, 20), 12);
    switch (opcode)
    {
        case 0b1100111: //jalr
            *op = "jalr";
            *dest = 0;
            break;
        case 0b1101111: //jal
            *op = "jal";
            *dest = pc + immJ;
            break;
        case 0b1100011: //branch
            *dest = pc + immB;
            switch (func3)
            {
                case 0b000:
                    *op = "beq";
                    break;
                case 0b001:
                    *op = "bne";
                    break;
                case 0b100:
                    *op = "blt";
                    break;
                case 0b101:
                    *op = "bge";
                    break;
                case 0b110:
                    *op = "bltu";
                    break;
                case 0b111:
                    *op = "bgeu";
                    break;
                default:
                    return false;
            }
            break;
        default:
            return false;
    }
    return true;
}

int BranchPredictor::getIndex(uint32_t pc){
    return (pc>>N())&((1<<M())-1);
}

int BranchPredictor::getTag(uint32_t pc){
    return (pc>>(N()+M()))&((1<<(32-N()-M()))-1);
}

bool BranchPredictor::findInBTB(uint32_t pc){
    int tag = getTag(pc);
    int index = getIndex(pc);
    assert(index < set);
    if(btb[index].tag == tag) return true;
    return false;
}

void BranchPredictor::updateBTB(uint32_t pc, uint32_t dest)
{
    int tag = getTag(pc);
    int index = getIndex(pc);
    if(findInBTB(pc)){return;}
    btb[index].tag = tag;
    btb[index].dest = dest;
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