#include <common.h>
#include <BP.h>
FILE *fp = NULL;
FILE* parse_args(int argc, char **argv) {
    FILE *fp = NULL;
    if (argc == 2) {
        std::string command = "bzcat " + std::string(argv[1]);
        fp = popen(command.c_str(), "r");
        if (!fp) {
            std::cerr << "Failed to run command." << std::endl;
            exit(1);
        }
    } else {
        std::cout << "Usage: ./bzcat <file>" << std::endl;
        exit(1);
    }
    return fp;
}

int main(int argc, char **argv) {
    fp = parse_args(argc, argv);
    if (!fp) assert(0);
    BTFNPredictor *btfnbp = new BTFNPredictor("./build/btfn.log", 4);
    AlwaysTakenPredictor *atbp = new AlwaysTakenPredictor("./build/alwaysTaken.log", 4);
    AlwaysNotTakenPredictor *antbp = new AlwaysNotTakenPredictor("./build/alwaysNotTaken.log", 2);
    uint32_t inst;
    uint32_t pc;
    int temp;
    bool is_taken;

    while(fscanf(fp, "%u %u %d\n", &pc, &inst, &temp) != EOF){
        if(temp == 1){is_taken = true;}
        else if(temp == 0){is_taken = false;}
        else assert(0);
        btfnbp->check(btfnbp->predict(pc, inst), is_taken);
        atbp->check(atbp->predict(pc, inst), is_taken);
        antbp->check(antbp->predict(pc, inst), is_taken);
    }
    delete atbp;
    delete antbp;
    delete btfnbp;
    return 0;
}
