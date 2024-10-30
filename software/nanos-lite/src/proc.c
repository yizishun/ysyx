#include <proc.h>
#include <memory.h>

#define MAX_NR_PROC 4
uintptr_t naive_uload(PCB *pcb, const char *filename); 

static PCB pcb[MAX_NR_PROC] __attribute__((used)) = {};
static PCB pcb_boot = {};
PCB *current = NULL;

void switch_boot_pcb() {
  current = &pcb_boot;
}

void hello_fun(void *arg) {
  int j = 1;
  while (1) {
    Log("Hello World from Nanos-lite with arg '%d' for the %dth time!", (uintptr_t)arg, j);
    j ++;
    yield();
  }
}

void context_kload(PCB *pcb, void (*entry)(void *), void *arg){
  pcb->cp = kcontext((Area) { pcb->stack, pcb + 1 }, entry, arg); 
}
/*
|               |
+---------------+ <---- ustack.end
|  Unspecified  |
+---------------+
|               | <----------+
|    string     | <--------+ |
|     area      | <------+ | |
|               | <----+ | | |
|               | <--+ | | | |
+---------------+    | | | | |
|  Unspecified  |    | | | | |
+---------------+    | | | | |
|     NULL      |    | | | | |
+---------------+    | | | | |
|    ......     |    | | | | |
+---------------+    | | | | |
|    envp[1]    | ---+ | | | |
+---------------+      | | | |
|    envp[0]    | -----+ | | |
+---------------+        | | |
|     NULL      |        | | |
+---------------+        | | |
| argv[argc-1]  | -------+ | |
+---------------+          | |
|    ......     |          | |
+---------------+          | |
|    argv[1]    | ---------+ |
+---------------+            |
|    argv[0]    | -----------+
+---------------+
|      argc     |
+---------------+ <---- cp->GPRx
|               |*/

int context_uload(PCB *pcb, const char *filename, char * argv[], char * envp[]){
  int argc, envpc;
  int i, len;
  char *cur;
  uintptr_t entry = naive_uload(pcb, filename);
  if(entry == -1) return -1;
  pcb->cp = ucontext(NULL, (Area) { pcb->stack, pcb + 1 }, (void *)entry);

//pass parameter to user
  for(argc = 0;argv[argc] != NULL;argc++);
  for(envpc = 0;envp[envpc] != NULL;envpc++);
  cur = (char *)new_page(8);
  //copy string to ustack's string area
  for(i = 0;argv[i] != NULL;i++){
    len = strlen(argv[i]) + 1; // +'\0'
    cur -= len;
    strncpy(cur, argv[i], len); //best is strpncpy,but I have not completed this func in klib
    argv[i] = cur;
  }
  for(i = 0;envp[i] != NULL;i++){
    len = strlen(envp[i]) + 1; // +'\0'
    cur -= len;
    strncpy(cur, envp[i], len);
    envp[i] = cur;
  }
  //copy envp and argv to ustack
  len = sizeof(uintptr_t);
  for(i = envpc;i >= 0;i--){
    cur -= len;
    memcpy(cur, &envp[i], len);
  }
  for(i = argc;i >= 0;i--){
    cur -= len;
    memcpy(cur, &argv[i], len);
  }
  //copy argc to ustack
  len = sizeof(int);
  cur -= len;
  memcpy(cur, &argc, len);

  pcb->cp->GPRx = (uintptr_t)cur;
  return 0;
}

void init_proc() {
  char * argv[] = {"/bin/nterm",NULL};
  char * envp[] = { NULL};
  context_kload(&pcb[0], hello_fun, (void *)'a');
  context_uload(&pcb[1], "/bin/nterm", argv, envp);
  switch_boot_pcb();

  Log("Initializing processes...");
}

Context *schedule(Context *prev) {
  current->cp = prev;
  current = (current == &pcb[0] ? &pcb[1] : &pcb[0]);
  return current->cp;
}
