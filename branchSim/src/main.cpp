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
    AlwaysTakenPredictor *atbp = new AlwaysTakenPredictor("./build/alwaysTaken.log");
    AlwaysNotTakenPredictor *antbp = new AlwaysNotTakenPredictor("./build/alwaysNotTaken.log");
    uint32_t inst;
    uint32_t pc;
    int temp;
    bool is_taken;

    while(fscanf(fp, "%u %u %d\n", &pc, &inst, &temp) != EOF){
        if(temp == 1){is_taken = true;}
        else if(temp == 0){is_taken = false;}
        else assert(0);
//        #ifdef CONFIG_TRACE
//        fprintf(logfp, "---------------\n");
//        fprintf(logfp, "REF PC = %#x inst = %u %s\n", pc, inst, is_taken ? "taken" : "not taken");
//        #endif
        atbp->check(atbp->predict(inst), is_taken);
        antbp->check(antbp->predict(inst), is_taken);
    }
    delete atbp;
    delete antbp;
    return 0;
}
