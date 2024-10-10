#include <BP.h>
#include <assert.h>

bool BTFNPredictor::predict(uint32_t pc, uint32_t inst)
{
    if(findInBTB(pc)){
        string op;
        uint32_t dest;
        bool legal = parse_inst(pc, inst, &op, &dest);
        if(!legal) assert(0);
        if(dest <= pc){
            return true;
        }else{
            return false;
        }
    }else{
        updateBTB(pc, 0); //btb's dest is dont care in simulator
        return false;
    }
}