/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include "local-include/reg.h"
#include <cpu/cpu.h>
#include <cpu/ifetch.h>
#include <cpu/decode.h>
#include <cpu/ftrace.h>
#include <stdio.h>
#include <isa.h>
#include <ysyxsoc.h>

#define R(i) gpr(i)
#define SR(i) csr(i)
#define Mr vaddr_read
#define Mw vaddr_write
#define XLEN 32

#ifdef CONFIG_RVE
#define GPR1 15
#else
#define GPR1 17
#endif
enum {
  TYPE_I, TYPE_U, TYPE_S,
  TYPE_N, TYPE_J, TYPE_R,
  TYPE_B 
};

void ftrace_check(int type,Decode *s,word_t imm, int rd){
	#ifndef CONFIG_FTRACE
	return;
	#endif
  uint32_t i = s->isa.inst.val;
  int rs1 = BITS(i, 19, 15);
	//printf("pc = %#x\n",s->pc);
	char *prev_fname = ftrace_find_symbol(s->pc);
	//printf("dnpc = %#x\n",s->dnpc);
	char *now_fname  = ftrace_find_symbol(s->dnpc);
	//printf("prev = %s\n",prev_fname);
	//printf("now  = %s\n",now_fname);
	if(strcmp(prev_fname,now_fname) == 0)	return;
	//printf("rs1 = %u imm = %u rd = %u\n",rs1,imm,rd);
	if(type == JAL) ftrace_write(CALL,now_fname,s->dnpc,s->pc);
	else if(type == JALR){
		if(rs1 == 1 && imm == 0 && rd == 0)
			ftrace_write(RET,prev_fname,s->dnpc,s->pc);
		else 
			ftrace_write(CALL,now_fname,s->dnpc,s->pc);
	}
}

void etrace(word_t NO, vaddr_t epc){
  #ifdef CONFIG_ETRACE
  char s[20];
  switch (NO)
  {
  case EVENT_YIELD:
    sprintf(s ,"EVENT_YIELD  ");
    break;
  case EVENT_SYSCALL:
    sprintf(s ,"EVENT_SYSCALL");
    break;
  default:
    sprintf(s ,"UNKOWN EVENT id = %d",NO);
    break;
  }
  printf("ETRACE: %s   pc = %#x\n",s,epc); //if you convert printf to log_write,you can write trace to log.
  #endif
}
#define src1R() do { *src1 = R(rs1); } while (0)
#define src2R() do { *src2 = R(rs2); } while (0)
#define immI() do { *imm = SEXT(BITS(i, 31, 20), 12); } while(0)
#define immU() do { *imm = SEXT(BITS(i, 31, 12), 20) << 12; } while(0)
#define immS() do { *imm = (SEXT(BITS(i, 31, 25), 7) << 5) | BITS(i, 11, 7); } while(0)
//#define immJ() do { *imm = SEXT((BITS(i,31,31) << 19) + (BITS(i,19,12) << 18) + (BITS(i,20,20) << 10) + (BITS(i,30,21)),20) << 1;} while(0)
#define immJ() do { *imm = ((SEXT(BITS(i, 31, 31), 1) << 19) | BITS(i, 19, 12) << 11 | BITS(i, 20 , 20) << 10 | BITS(i , 30 , 21)) << 1; } while(0)
#define immB() do { *imm = ((SEXT(BITS(i, 31, 31), 1) << 11) | BITS(i, 7, 7) << 10 | BITS(i, 30 , 25) << 4 | BITS(i , 11 , 8)) << 1; } while(0)

static void decode_operand(Decode *s, int *rd, word_t *src1, word_t *src2, word_t *imm, int *csri, int type) {
  uint32_t i = s->isa.inst.val;
  int rs1 = BITS(i, 19, 15);
  int rs2 = BITS(i, 24, 20);
  *rd     = BITS(i, 11, 7);
  *csri   = BITS(i, 31, 20);
  switch (type) {
    case TYPE_I: src1R();          immI(); break;
    case TYPE_U:                   immU(); break;
    case TYPE_S: src1R(); src2R(); immS(); break;
    case TYPE_J:		   						 immJ(); break;
    case TYPE_R: src1R(); src2R();		 	 ; break;
    case TYPE_B: src1R(); src2R(); immB(); break;
  }
}


static int decode_exec(Decode *s) {
  int rd = 0;
  int csri = 0;
  word_t src1 = 0, src2 = 0, imm = 0;
  bool is_jump = false;
  s->dnpc = s->snpc;

#define INSTPAT_INST(s) ((s)->isa.inst.val)
#define INSTPAT_MATCH(s, name, type, ... /* execute body */ ) { \
  decode_operand(s, &rd, &src1, &src2, &imm, &csri, concat(TYPE_, type)); \
  __VA_ARGS__ ; \
}

  INSTPAT_START(); 
  INSTPAT("??????? ????? ????? ??? ????? 00101 11", auipc  , U, R(rd) = s->pc + imm);
  INSTPAT("??????? ????? ????? ??? ????? 01101 11", lui    , U, R(rd) = imm);
  INSTPAT("??????? ????? ????? 100 ????? 00000 11", lbu    , I, R(rd) = Mr(src1 + imm, 1));
  INSTPAT("??????? ????? ????? 000 ????? 01000 11", sb     , S, Mw(src1 + imm, 1, src2));
  INSTPAT("??????? ????? ????? 001 ????? 01000 11", sh     , S, Mw(src1 + imm, 2, src2));
  INSTPAT("??????? ????? ????? 010 ????? 01000 11", sw     , S, Mw(src1 + imm, 4, src2));
  INSTPAT("??????? ????? ????? 010 ????? 00000 11", lw     , I, R(rd) = Mr(src1 + imm, 4));
  INSTPAT("??????? ????? ????? 001 ????? 00000 11", lh     , I, R(rd) = SEXT(Mr(src1 + imm, 2), 16));
  INSTPAT("??????? ????? ????? 000 ????? 00000 11", lb     , I, R(rd) = SEXT(Mr(src1 + imm, 1), 8));
  INSTPAT("??????? ????? ????? 101 ????? 00000 11", lhu    , I, R(rd) = Mr(src1 + imm, 2));
  INSTPAT("??????? ????? ????? 000 ????? 00100 11", addi   , I, R(rd) = src1 + imm);
  INSTPAT("??????? ????? ????? 011 ????? 00100 11", sltiu  , I, R(rd) = (src1 < imm) ? 1:0);
  INSTPAT("??????? ????? ????? 010 ????? 00100 11", slti   , I, R(rd) = ((int32_t)src1 < (int32_t)imm) ? 1:0);
  INSTPAT("??????? ????? ????? 100 ????? 00100 11", xori   , I, R(rd) = src1 ^ imm);
  INSTPAT("??????? ????? ????? 110 ????? 00100 11", ori    , I, R(rd) = src1 | imm);
  INSTPAT("??????? ????? ????? 111 ????? 00100 11", andi   , I, R(rd) = src1 & imm);
  INSTPAT("0000000 ????? ????? 001 ????? 00100 11", slli   , I, R(rd) = src1 << BITS(imm, 4 , 0));
  INSTPAT("0000000 ????? ????? 101 ????? 00100 11", srli   , I, R(rd) = src1 >> BITS(imm, 4 , 0));
  INSTPAT("0100000 ????? ????? 101 ????? 00100 11", srai   , I, R(rd) = (int32_t)src1 >> BITS(imm, 4 , 0));
  INSTPAT("??????? ????? ????? ??? ????? 11011 11", jal    , J, s->dnpc = s->pc + imm; R(rd) = s->snpc; ftrace_check(JAL,s,imm, rd); is_jump = true);
  INSTPAT("??????? ????? ????? 000 ????? 11001 11", jalr   , I, s->dnpc = (imm + src1) & ~1ull; R(rd) = s->snpc; ftrace_check(JALR,s,imm, rd); is_jump = true);
  INSTPAT("0000000 ????? ????? 000 ????? 01100 11", add    , R, R(rd) = src1 + src2); 
  INSTPAT("0000000 ????? ????? 010 ????? 01100 11", slt    , R, R(rd) = (int32_t)src1 < (int32_t)src2? 1: 0); 
  INSTPAT("0000000 ????? ????? 011 ????? 01100 11", sltu   , R, R(rd) = src1 < src2? 1 : 0); 
  INSTPAT("0000000 ????? ????? 100 ????? 01100 11", xor    , R, R(rd) = src1 ^ src2); 
  INSTPAT("0000000 ????? ????? 110 ????? 01100 11", or     , R, R(rd) = src1 | src2); 
  INSTPAT("0000000 ????? ????? 111 ????? 01100 11", and    , R, R(rd) = src1 & src2); 
  INSTPAT("0000000 ????? ????? 001 ????? 01100 11", sll    , R, R(rd) = src1 << BITS(src2, 4 , 0)); 
  INSTPAT("0000000 ????? ????? 101 ????? 01100 11", srl    , R, R(rd) = src1 >> BITS(src2, 4 , 0)); 
  INSTPAT("0100000 ????? ????? 101 ????? 01100 11", sra    , R, R(rd) = (int32_t)src1 >> BITS(src2, 4 , 0)); 
  INSTPAT("0100000 ????? ????? 000 ????? 01100 11", sub    , R, R(rd) = src1 - src2);
  INSTPAT("0000001 ????? ????? 000 ????? 01100 11", mul    , R, R(rd) = src1 * src2);
  INSTPAT("0000001 ????? ????? 001 ????? 01100 11", mulh   , R, R(rd) = ((int64_t)SEXT(src1, 32) * (int64_t)SEXT(src2, 32)) >> XLEN);
  INSTPAT("0000001 ????? ????? 010 ????? 01100 11", mulhsu , R, R(rd) = ((int64_t)SEXT(src1, 32) * SEXT(src2, 32)) >> XLEN);
  INSTPAT("0000001 ????? ????? 011 ????? 01100 11", mulhu  , R, R(rd) = ((uint64_t)src1 * (uint32_t)src2) >> XLEN);
  INSTPAT("0000001 ????? ????? 100 ????? 01100 11", div    , R, R(rd) = (int32_t)src1 / (int32_t)src2);
  INSTPAT("0000001 ????? ????? 101 ????? 01100 11", divu   , R, R(rd) = src1 / src2);
  INSTPAT("0000001 ????? ????? 110 ????? 01100 11", rem    , R, R(rd) = (int32_t)src1 % (int32_t)src2);
  INSTPAT("0000001 ????? ????? 111 ????? 01100 11", remu   , R, R(rd) = src1 % src2);
  INSTPAT("??????? ????? ????? 000 ????? 11000 11", beq    , B, s->dnpc = (src1 == src2)?s->pc + imm : s->dnpc; is_jump = true);
  INSTPAT("??????? ????? ????? 001 ????? 11000 11", bne    , B, s->dnpc = (src1 != src2)?s->pc + imm : s->dnpc; is_jump = true);
  INSTPAT("??????? ????? ????? 100 ????? 11000 11", blt    , B, s->dnpc = ((int32_t)src1 <(int32_t)src2)?s->pc + imm : s->dnpc; is_jump = true);
  INSTPAT("??????? ????? ????? 101 ????? 11000 11", bge    , B, s->dnpc = ((int32_t)src1 >=(int32_t)src2)?s->pc + imm : s->dnpc; is_jump = true);
  INSTPAT("??????? ????? ????? 110 ????? 11000 11", bltu   , B, s->dnpc = (src1 < src2)?s->pc + imm : s->dnpc; is_jump = true);
  INSTPAT("??????? ????? ????? 111 ????? 11000 11", bgeu   , B, s->dnpc = (src1 >= src2)?s->pc + imm : s->dnpc; is_jump = true);
  INSTPAT("0000000 00001 00000 000 00000 11100 11", ebreak , N, NEMUTRAP(s->pc, R(10))); // R(10) is $a0
  INSTPAT("0000000 00000 00000 000 00000 11100 11", ecall  , N, etrace((R(15) == -1 ? EVENT_YIELD:EVENT_SYSCALL),s->pc);s->dnpc = isa_raise_intr((R(15) == -1 ? 11:11), s->pc));
  INSTPAT("0011000 00010 00000 000 00000 11100 11", mret   , R, s->dnpc = SR(MEPC));
  INSTPAT("??????? ????? ????? 001 ????? 11100 11", csrrw  , I, int t = SR(csri); SR(csri) = src1;R(rd) = t;);
  INSTPAT("??????? ????? ????? 010 ????? 11100 11", csrrs  , I, int t = SR(csri); SR(csri) = src1 | t;R(rd) = t);
  INSTPAT("??????? ????? ????? ??? ????? ????? ??", inv    , N, INV(s->pc));
  INSTPAT_END();
  #ifndef CONFIG_TARGET_SHARE
  if(is_jump){
    write_btrace(s->isa.inst.val, s->dnpc != s->snpc, s->snpc - 4);
  }
  #endif

  R(0) = 0; // reset $zero to 0

  return 0;
}

int isa_exec_once(Decode *s) {
  s->isa.inst.val = inst_fetch(&s->snpc, 4);
  return decode_exec(s);
}
