#include <nterm.h>
#include <stdarg.h>
#include <unistd.h>
#include <string.h>
#include <SDL.h>

char handle_key(SDL_Event *ev);
extern "C" int setenv(const char *__name, const char *__value, int __overwrite);

static void sh_printf(const char *format, ...) {
  static char buf[256] = {};
  va_list ap;
  va_start(ap, format);
  int len = vsnprintf(buf, 256, format, ap);
  va_end(ap);
  term->write(buf, len);
}

static void sh_banner() {
  sh_printf("YZS Shell in NTerm (NJU Terminal)\n\n");
}

static void sh_prompt() {
  sh_printf("ysh> ");
}

static int cmd_echo(char *args){
  if(args == NULL) return 0;
  sh_printf("%s\n",args);
  return 0;
}

static struct {
  const char *name;
  const char *description;
  int (*handler) (char *);
} cmd_table [] = {
  { "echo", "echo", cmd_echo },
};

#define NR_CMD (sizeof(cmd_table)/sizeof(cmd_table[0]))

static void sh_handle_cmd(const char *cmd) {
  int i;
  char *argv[16];
  int argc = 0;
  int size = strlen(cmd);
  char *str = (char *)malloc(size);
  for(i = 0;cmd[i] != '\n';i++){
    str[i] = cmd[i];
  }
  str[i] = '\0';
  char *str_end = str + strlen(str);

  const char split[2] = " ";
  char* token;

  token = strtok(str, split);
  while (token != NULL) {
    argv[argc++] = token;
    token = strtok(NULL, split);
  }
  argv[argc] = NULL;

  char *command = strtok(str, " ");
  char *args = command + strlen(command) + 1;
  if (args >= str_end) {
    args = NULL;
  }

  for (i = 0; i < NR_CMD; i ++) {
    if (strcmp(command, cmd_table[i].name) == 0) {
      if (cmd_table[i].handler(args) < 0) { sh_printf("%s command fail",command); }
      return;
    }
  }
  if(argv[0] != NULL){
    if(execvp(argv[0], argv) == -1){
      sh_printf("ysh: command not found: %s\n",command);
    }
  }
}

void builtin_sh_run() {
  sh_banner();
  sh_prompt();
  setenv("PATH", "/bin", 0);

  while (1) {
    SDL_Event ev;
    if (SDL_PollEvent(&ev)) {
      if (ev.type == SDL_KEYUP || ev.type == SDL_KEYDOWN) {
        const char *res = term->keypress(handle_key(&ev));
        if (res) {
          sh_handle_cmd(res);
          sh_prompt();
        }
      }
    }
    refresh_terminal();
  }
}
