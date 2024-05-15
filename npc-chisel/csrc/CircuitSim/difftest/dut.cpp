#include <dlfcn.h>
#include <memory.h>
#include <common.h>
#include <circuit.h>
struct CPU_state {
  word_t gpr[REGNUM];
  word_t pc;
  word_t csr[4];
};
bool is_skip_diff = false;
void (*ref_difftest_memcpy)(paddr_t addr, void *buf, size_t n, bool direction) = NULL;
void (*ref_difftest_regcpy)(void *dut, bool direction) = NULL;
void (*ref_difftest_exec)(uint64_t n) = NULL;
void (*ref_difftest_raise_intr)(uint64_t NO) = NULL;

enum { DIFFTEST_TO_DUT, DIFFTEST_TO_REF };

void init_difftest(char *ref_so_file, long img_size) {
  if(ref_so_file == NULL) return;

  void *handle;
  handle = dlopen(ref_so_file, RTLD_LAZY);
  assert(handle);

  ref_difftest_memcpy = (void(*)(paddr_t,void *,size_t,bool))dlsym(handle, "difftest_memcpy");
  assert(ref_difftest_memcpy);

  ref_difftest_regcpy = (void(*)(void *,bool))dlsym(handle, "difftest_regcpy");
  assert(ref_difftest_regcpy);

  ref_difftest_exec = (void(*)(uint64_t))dlsym(handle, "difftest_exec");
  assert(ref_difftest_exec);

  ref_difftest_raise_intr = (void(*)(uint64_t))dlsym(handle, "difftest_raise_intr");
  assert(ref_difftest_raise_intr);

  void (*ref_difftest_init)() = (void(*)())dlsym(handle, "difftest_init");
  assert(ref_difftest_init);
  #ifdef CONFIG_TRACE
  #ifdef CONFIG_DIFFTEST
  Log("Differential testing: %s", ANSI_FMT("ON", ANSI_FG_GREEN));
  Log("The result of every instruction will be compared with %s. "
      "This will help you a lot for debugging, but also significantly reduce the performance. "
      "If it is not necessary, you can turn it off in menuconfig.", ref_so_file);
  #else
  Log("Differential testing: %s", ANSI_FMT("OFF", ANSI_FG_RED));
  #endif
  #endif
  ref_difftest_init();
  ref_difftest_memcpy(RESET_VECTOR, (void *)guest_to_host(RESET_VECTOR), img_size, DIFFTEST_TO_REF);
  //get dut reg into CPU_state struct
  CPU_state dut_r;
  dut_r.pc = 0x80000000;
  for(int i = 0;i < REGNUM;i++)
    dut_r.gpr[i] = gpr[i];
  for(int i = 0;i < 4;i++)
    dut_r.csr[i] = csr[i];
  ref_difftest_regcpy(&dut_r, DIFFTEST_TO_REF);
}

bool static checkregs(struct CPU_state *ref_r){
  bool flag = true;
  int i;
  if(ref_r -> pc != pc) flag = false;
  for(i = 0;i < REGNUM;i++){
    if(ref_r -> gpr[i] != gpr[i])
      flag = false;
  }
  for(i = 0;i < 4;i++){
    if(ref_r->csr[i] != csr[i])
      flag = false;
  }
  if(flag == false){
    printf("ref-pc=%x\n",ref_r -> pc);
    for(i = 0;i < REGNUM;i++){
    if(ref_r -> gpr[i] >= 0x80000000){
        printf("ref-%3s = %-#11x",regs[i],ref_r -> gpr[i]);
        if(i % 3 == 0) printf("\n");
        }
    else{
        printf("ref-%3s = %-11d",regs[i],ref_r -> gpr[i]);
        if(i % 3 == 0) printf("\n");
        } 
    }
    printf("\n");
    printf("ref-mstatus = %#x\n",ref_r->csr[0]);
    printf("ref-mtvec = %#x\n",ref_r->csr[1]);
    printf("ref-mepc = %#x\n",ref_r->csr[2]);
    printf("ref-mcause = %#x\n",ref_r->csr[3]);
  }
  return flag;
}

void difftest_step() {
  if(ref_difftest_memcpy == NULL) return;
  if(is_skip_diff == true){
    is_skip_diff = false;
    int i;
    //get dut reg into CPU_state struct
    CPU_state dut_r;
    dut_r.pc = pc;
    for(i = 0;i < REGNUM;i++)
      dut_r.gpr[i] = gpr[i];
    for(i = 0;i < 4;i++)
      dut_r.csr[i] = csr[i];
    //copy reg to ref to skip this inst
    ref_difftest_regcpy(&dut_r, DIFFTEST_TO_REF);
    return;
  }
  CPU_state ref_r;
  ref_difftest_exec(1);
  ref_difftest_regcpy(&ref_r, DIFFTEST_TO_DUT);

  if(!checkregs(&ref_r)){
    isa_reg_display();
    assert(0);
  }
}