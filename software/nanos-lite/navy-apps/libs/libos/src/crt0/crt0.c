#include <stdint.h>
#include <stdlib.h>
#include <assert.h>
#include <stdio.h>

int main(int argc, char *argv[], char *envp[]);
extern char **environ;
void call_main(uintptr_t *args) {
  int argc = *(int *)args++;
  char **argv = (char **)args;
  args += argc + 1;
  char **envp = (char **)args; 
  environ = envp;
  printf("argc = %d\n",argc);
  for (int i = 0; i < argc; i++) {
    printf("argv[%d] = %s\n", i, argv[i]);
  }
  for (int i = 0; envp[i]; i++) {
    printf("envp[%d] = %s\n", i, envp[i]);
  }
  exit(main(argc, argv, envp));
  assert(0);
}
