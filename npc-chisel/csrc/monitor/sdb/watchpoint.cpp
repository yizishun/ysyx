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

#include "sdb.h"

#define NR_WP 32

//typedef struct watchpoint {
//  int NO;
//  struct watchpoint *next;
//  int val;
//  char *expr;
//  /* TODO: Add more members if necessary */

//} WP;

static WP wp_pool[NR_WP] = {};
static WP *head = NULL, *free_ = NULL;

WP *new_wp(){
  WP *free_2 = free_;
  WP *last = head;
  free_ = free_ -> next;
  if(head == NULL){
    head = free_2;
    head -> next = NULL;
    head -> NO = 1;
  }
  else{
    for(;last -> next;last = last -> next)
      ;
    last -> next = free_2;
    free_2 -> NO = (last -> NO)+1;
    free_2 -> next = NULL;
  }
  return free_2;
}
void free_wp(WP *wp){
  WP *last1;
  WP *last2;
  int no = wp -> NO;
  for(last2 = NULL,last1 = head;last1;last2 = last1,last1 = last1 -> next){
    if(last1 -> NO == no){
      if(last2 != NULL)
        last2 -> next = last1 -> next;
      else 
				head = last1 -> next;
    }
    if(last1 -> NO > no)
      (last1 -> NO)--;
  }
  wp -> next =free_;
  free_ = wp;
}

void del_w(int n){
  WP *p = head;
  for(;p;p = p -> next){
    if(p -> NO == n){
      free_wp(p);
      printf("NO.%d has been free\n",n);break;
      if(head == NULL) break;
    }
  } 
}

void show_w(){
  WP *p = head;
  for(;p;p = p -> next){
    printf("NO:%d ",p -> NO);
    printf("expr:%s= ",p -> expr);
    if(p -> val >= 0x80000000)
      printf("%#x ",p -> val);
    else     
      printf("%u ",p ->val);
    if(p -> next == NULL) printf("\n");
    else printf("next = %d\n",p -> next ->NO);
  }
}

int check_w(){
  WP *p = head;
  for(;p;p = p -> next){
    bool success = true;
    uint32_t val = expr(p -> expr,&success);
    if(success == false) assert(0);
    if(val != p -> val){
      p -> val= val; 
      return p -> NO;}
  }
  return 0;
}

void init_wp_pool() {
  int i;
  for (i = 0; i < NR_WP; i ++) {
    wp_pool[i].NO = i;
    wp_pool[i].next = (i == NR_WP - 1 ? NULL : &wp_pool[i + 1]);
  }

  head = NULL;
  free_ = wp_pool;
}

/* TODO: Implement the functionality of watchpoint */

