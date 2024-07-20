#include <memory.h>
#include <unistd.h>
#include <getopt.h>
#include <common.h>
#include <circuit.h>
static char * img_file = NULL;
static char *log_file = NULL;
static char *diff_so_file = NULL;
char *elf_file = NULL;

void sdb_set_batch_mode();
void init_sdb();
void init_log(const char *log_file);
void init_ftrace(char *elf_file);
void init_difftest(char *ref_so_file, long img_size);
extern "C" void init_disasm(const char *triple);

static void welcome() {
  #ifdef CONFIG_TRACE
  Log("Trace: %s", ANSI_FMT("ON", ANSI_FG_GREEN));
  #else
  Log("Trace: %s", ANSI_FMT("OFF", ANSI_FG_RED));
  #endif
  Log("If trace is enabled, a log file will be generated "
        "to record the trace. This may lead to a large log file. "
        "If it is not necessary, you can disable it in menuconfig");
  Log("Build time: %s, %s", __TIME__, __DATE__);
  printf("Welcome to %s-NPC!\n", ANSI_FMT("riscv32e", ANSI_FG_YELLOW ANSI_BG_RED));
  printf("For help, type \"help\"\n");
}

static long load_img() {
  if (img_file == NULL) {    
    Log("No image is given. Use the default build-in image.");
    return 72; // built-in image size
  }

  FILE *fp = fopen(img_file, "rb");
  assert(fp);

  fseek(fp, 0, SEEK_END);
  long size = ftell(fp);

  Log("The image is %s, size = %ld", img_file, size);
  fflush(stdout);

  fseek(fp, 0, SEEK_SET);
  int ret = fread(guest_to_host(FLASH_BASE), size, 1, fp);
  assert(ret == 1);

  fclose(fp);
  return size;
}

static int parse_args(int argc,char *argv[]){
  const struct option table[] = {
    {"batch"    , no_argument      , NULL, 'b'},
    {"log"      , required_argument, NULL, 'l'},
    {"help"     , no_argument      , NULL, 'h'},
    {"elf"			,	required_argument, NULL, 'e'},
    {"diff"     , required_argument, NULL, 'd'},
    {0          , 0                , NULL,  0 },
  };
  int o;
  while ( (o = getopt_long(argc, argv, "-bhl:d:e:", table, NULL)) != -1) {
    switch (o) {
      case 'b': sdb_set_batch_mode(); break;
      case 'l': log_file = optarg; break;
      case 'd': diff_so_file = optarg; break;
      case 'e': elf_file = optarg; break;
      case 1: img_file = optarg; return 0;
      default:
        printf("Usage: %s [OPTION...] IMAGE [args]\n\n", argv[0]);
        printf("\t-b,--batch              run with batch mode\n");
        printf("\t-l,--log=FILE           output log to FILE\n");
        printf("\t-d,--diff=REF_SO        run DiffTest with reference REF_SO\n");
        printf("\t-e,--elf=FILE						ftrace load elf file to find sym");
        printf("\n");
        exit(0);
    }
  }
  return 0;
}

void init_monitor(int argc, char *argv[]){

  /* Parse arguments. */
  parse_args(argc, argv);

  init_log(log_file);

  init_mem(0x7fffffff);

  init_flash();

  init_psram();

  init_sdram();

  long img_size = load_img();

  init_ftrace(elf_file);

  init_disasm("riscv32-pc-linux-gnu");

  init_sdb();

  #ifdef CONFIG_DIFFTEST
  init_difftest(diff_so_file , img_size);
  #endif

  nvboard_bind_all_pins(&cpu);
  nvboard_init();

  welcome();
}