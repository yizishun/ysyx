#include <memory.h>
#include <unistd.h>
#include <getopt.h>
#include <common.h>
static char * img_file = NULL;
void sdb_set_batch_mode();
void init_sdb();
extern "C" void init_disasm(const char *triple);
static long load_img() {
  if (img_file == NULL) {
    printf("No image is given. Use the default build-in image.\n");
    return 40; // built-in image size
  }

  FILE *fp = fopen(img_file, "rb");
  assert(fp);

  fseek(fp, 0, SEEK_END);
  long size = ftell(fp);

  printf("The image is %s, size = %ld\n", img_file, size);
  fflush(stdout);

  fseek(fp, 0, SEEK_SET);
  int ret = fread(guest_to_host(RESET_VECTOR), size, 1, fp);
  assert(ret == 1);

  fclose(fp);
  return size;
}

static int parse_args(int argc,char *argv[]){
  const struct option table[] = {
    {"batch"    , no_argument      , NULL, 'b'},
    {"help"     , no_argument      , NULL, 'h'},
    {0          , 0                , NULL,  0 },
  };
  int o;
  while ( (o = getopt_long(argc, argv, "-bh", table, NULL)) != -1) {
    switch (o) {
      case 'b': sdb_set_batch_mode(); break;
      case 1: img_file = optarg; return 0;
      default:
        printf("Usage: %s [OPTION...] IMAGE [args]\n\n", argv[0]);
        printf("\t-b,--batch              run with batch mode\n");
        printf("\n");
        exit(0);
    }
  }
  return 0;
}

void init_monitor(int argc, char *argv[]){

  /* Parse arguments. */
  parse_args(argc, argv);

  init_mem(3000);

  long img_size = load_img();

  init_disasm("riscv32-pc-linux-gnu");

  init_sdb();
}