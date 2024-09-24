#include <common.h>
#include <cache.h>

extern void ra_FIFO(Cache *, cacheSet_t& s, int tag);
extern void ra_LRU(Cache *, cacheSet_t& s, int tag);
FILE *fp = NULL;
FILE *logfp = NULL;
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
    logfp = fopen("./build/log.txt", "w");

    return fp;
}

paddr_t get_inst_addr(){
    paddr_t addr;
    fscanf(fp, "%u\n", &addr);
    //printf("%#x\n",addr);
    return addr;
}

int main(int argc, char **argv) {
    fp = parse_args(argc, argv);
    if (!fp) assert(0);
    paddr_t addr;

    Cache cache(ra_FIFO, 4, 1, 8); // ra set way bsz
    while(fscanf(fp, "%u\n", &addr) != EOF){  
        #ifdef CONFIG_TRACE
        fprintf(logfp, "---------------\n");
        fprintf(logfp, "req\naddr = %#x, set = %d, tag = %d, offset = %d\n", addr, cache.INDEX(addr), cache.TAG(addr), cache.OFFSET(addr));
        #endif
        cache.replace(cache.decode(addr), addr);
    }
    cache.statistic();
    return 0;
}
