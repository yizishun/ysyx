#include <circuit.h>
#include <memory.h>
#include "sdb.h"
#include <readline/readline.h>
#include <readline/history.h>

static int is_batch_mode = false;
void init_regex();
void init_wp_pool();

static char* rl_gets() {
  static char *line_read = NULL;

  if (line_read) {
    free(line_read);
    line_read = NULL;
  }

  line_read = readline("(yCPU) ");

  if (line_read && *line_read) {
    add_history(line_read);
  }

  return line_read;
}

static int cmd_c(char *args) {
  cpu_exec(-1);
  return 0;
}

static int cmd_si(char *args){
  int n;
  if(args == NULL) n = 1;
  else n = atoi(args);
  cpu_exec(n);
  return 0;
}

static int cmd_q(char *args) {
	exit(0);
}

static int cmd_info(char *args){
  if(args == NULL) return 0;
  switch(*args){
    case 'r':
	    isa_reg_display();
	    break;	
    case 'w':
      show_w();
	    break;	    
    default:
  	    printf("info r/w\n");}
  return 0;

}

static int cmd_x(char *args){
  char *arg1 = strtok(NULL," ");
  char *arg2 = strtok(NULL," ");
  if(arg2 == NULL || arg1 == NULL){
    printf("usage : x <size> <addr>\n");
    return 0;
  }
  int n = strtol(arg1,NULL,10);
  int addr = strtol(arg2,NULL,16);
  uint8_t *raddr = (uint8_t *)guest_to_host(addr);
  for(int i =0;i < n;i++ ,addr+=8,raddr++)
	  printf("0x%x    %02x\n",addr,*raddr);  
  return 0;
}

static int cmd_p(char *args){
  bool seccess = true;
  int val;
  if(args == NULL) return 0;
  val = expr(args,&seccess);
  if(seccess == false){
    printf("make_token false\n");
    return 0;
  }
  if(val >= 0x80000000)
    printf("%#x\n",val);
  else printf("%u\n",val);
  return 0;
}

static int cmd_w(char *args){
  bool seccess = true;
  int val;
  WP *wp;
  if(args == NULL) return 0;
  val = expr(args,&seccess);
  if(seccess == false){
    printf("make_token false\n");
    return 0;
  }
  wp = new_wp();
  wp -> val = val;
  strcpy(wp -> expr,args);
  return 0;
}

static int cmd_d(char *args){
  int n = atoi(args);
  del_w(n);
  return 0;
}

static int cmd_help(char *args);

static struct {
  const char *name;
  const char *description;
  int (*handler) (char *);
} cmd_table [] = {
  { "help", "Display information about all supported commands", cmd_help },
  { "c", "Continue the execution of the program", cmd_c },
  { "q", "Exit NEMU", cmd_q },
  { "si", "step [step n]", cmd_si},
  { "info", "info r / info w",cmd_info},
  { "x", "x <size> <addr>",cmd_x},
  { "p", "p expr",cmd_p},
  { "w", "w expr",cmd_w},
  { "d", "d NO.", cmd_d}
  /* TODO: Add more commands */

};

#define NR_CMD ARRLEN(cmd_table)

static int cmd_help(char *args) {
  /* extract the first argument */
  char *arg = strtok(NULL, " ");
  int i;

  if (arg == NULL) {
    /* no argument given */
    for (i = 0; i < NR_CMD; i ++) {
      printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
    }
  }
  else {
    for (i = 0; i < NR_CMD; i ++) {
      if (strcmp(arg, cmd_table[i].name) == 0) {
        printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
        return 0;
      }
    }
    printf("Unknown command '%s'\n", arg);
  }
  return 0;
}

void sdb_set_batch_mode() {
  is_batch_mode = true;
}

void sdb_mainloop() {
  if (is_batch_mode) {
    cmd_c(NULL);
    return;
  }

  for (char *str; (str = rl_gets()) != NULL; ) {
    char *str_end = str + strlen(str);

    /* extract the first token as the command */
    char *cmd = strtok(str, " ");
    if (cmd == NULL) { continue; }

    /* treat the remaining string as the arguments,
     * which may need further parsing
     */
    char *args = cmd + strlen(cmd) + 1;
    if (args >= str_end) {
      args = NULL;
    }

    int i;
    for (i = 0; i < NR_CMD; i ++) {
      if (strcmp(cmd, cmd_table[i].name) == 0) {
        if (cmd_table[i].handler(args) < 0) { return; }
        break;
      }
    }

    if (i == NR_CMD) { printf("Unknown command '%s'\n", cmd); }
  }
}

void init_sdb() {
  /* Compile the regular expressions. */
  init_regex();

  /* Initialize the watchpoint pool. */
  init_wp_pool();
}