/* We use the POSIX regex functions to process regular expressions.
 * Type 'man regex' for more information about POSIX regex functions.
 */
#include <regex.h>
#include <common.h>
#include <memory.h>
#define MAXOP 10

//static uint32_t eval(int ,int ) __attribute__((naked));
enum {
  TK_NOTYPE = 256, TK_EQ, TK_NUMD , TK_NUMH , TK_REG, DEREF ,


};

static struct rule {
  const char *regex;
  int token_type;
} rules[] = {

  /* 
   * Pay attention to the precedence level of different rules.
   */

  {" +", TK_NOTYPE},    // spaces
  {"\\+", '+'},         // plus
  {"-",'-'},            // sub
  {"\\*",'*'},          // mul
  {"/",'/'},            // div
  {"\\(",'('},          // lp
  {"\\)",')'},          // rp
  {"\\$[a-z]{1,2}[0-9]{0,2}",TK_REG}, // reg                                      
  {"0(x|X)([0-9]|[A-F]|[a-f]){1,}",TK_NUMH},  //number hex       
  {"[0-9]{1,}",TK_NUMD},   //number dec  
  {"==", TK_EQ},        // equal
  
};

#define NR_REGEX ARRLEN(rules)

static regex_t re[NR_REGEX] = {};

/* Rules are used for many times.
 * Therefore we compile them only once before any usage.
 */
void init_regex() {
  int i;
  char error_msg[128];
  int ret;

  for (i = 0; i < NR_REGEX; i ++) {
    ret = regcomp(&re[i], rules[i].regex, REG_EXTENDED);
    if (ret != 0) {
      regerror(ret, &re[i], error_msg, 128);
      //panic("regex compilation failed: %s\n%s", error_msg, rules[i].regex);
      assert(0);
    }
  }
}

typedef struct token {
  int type;
  char str[64];
} Token;

static Token tokens[1024] __attribute__((used)) = {};
static int nr_token __attribute__((used))  = 0;

static bool make_token(char *e) {
  int position = 0;
  int i;
  regmatch_t pmatch;

  nr_token = 0;

  while (e[position] != '\0') {
    /* Try all rules one by one. */
    for (i = 0; i < NR_REGEX; i ++) {
      if (regexec(&re[i], e + position, 1, &pmatch, 0) == 0 && pmatch.rm_so == 0) {
        char *substr_start = e + position;
        int substr_len = pmatch.rm_eo;

        //printf("match rules[%d] = \"%s\" at position %d with len %d: %.*s",
            //i, rules[i].regex, position, substr_len, substr_len, substr_start);

        position += substr_len;

        /* TODO: Now a new token is recognized with rules[i]. Add codes
         * to record the token in the array `tokens'. For certain types
         * of tokens, some extra actions should be performed.
         */
	
	
        switch (rules[i].token_type) {
	  case '+':case'-':case '/':case '(':case ')': case TK_EQ:
	    tokens[nr_token++].type = rules[i].token_type;
	    break;
	  case TK_NOTYPE:break;
	  case TK_NUMD:case TK_NUMH:case TK_REG:
	    tokens[nr_token++].type = rules[i].token_type;
	    strncpy(tokens[nr_token-1].str,substr_start,substr_len);
	    break;         
	  case '*':
			if(nr_token > 0&&(
			tokens[nr_token-1].type == ')'||
			tokens[nr_token-1].type == TK_NUMD || 
			tokens[nr_token-1].type == TK_NUMH || 
			tokens[nr_token-1].type == TK_REG)){
			tokens[nr_token++].type = rules[i].token_type;}
			else {tokens[nr_token++].type = DEREF;}
    }

    if (i == NR_REGEX) {
      printf("no match at position %d\n%s\n%*.s^\n", position, e, position, "");
      return false;
    }
  }}}
  return true;
}
bool check_parentheses2(int p,int q){
  if(p > q) assert(0);
  int lp = 0;
  for(;p <= q;p++){
    if(tokens[p].type == '(')
      lp += 1;
    else if(tokens[p].type == ')'){
      if(lp == 0) return false;
      else lp -= 1;
    }
}
  if(lp != 0) return false;
  else return true;



}
bool check_parentheses(int p,int q){

  if(check_parentheses2(p , q) == false)
    assert(0);
  if((tokens[p].type == '(')&&(tokens[q].type == ')')){
    p+=1;
    q-=1;
    return check_parentheses2(p , q);}
  return false;
}

static int find_main_op(int p,int q){
  int plus[MAXOP] = {-1}, plusptr = 0;
  int sub[MAXOP] = {-1},subptr = 0;
  int mul[MAXOP] = {-1}, mulptr = 0;
  int div[MAXOP] ={-1}, divptr = 0;
  int equl[MAXOP] ={-1},equlptr =0;
	int deref1[MAXOP] ={-1},deref1ptr = 0;
  int lp = 0;
  int op = 0;
  for(;p < q;p++){
    if(tokens[p].type == '(') lp++;
    if(tokens[p].type == ')') lp--;
    if(lp != 0) continue;
    switch(tokens[p].type){
			case DEREF:
				deref1[deref1ptr++] = p;
				break;
      case TK_EQ:
				equl[equlptr++] = p;
				break;
      case '+' : 
        plus[plusptr++] = p;
				break;
      case '-' :
				sub[subptr++] = p;
				break;
      case '*' :
				mul[mulptr++] = p;
				break;
      case '/' :
				div[divptr++] = p;
				break;
      default : continue;
}}
    if(equl[0]!=-1) op = equl[--equlptr];
    else{
    if(plus[0] != -1){ op = plus[--plusptr];}
    if(sub[0] != -1)
      if(sub[--subptr] > op) op = sub[subptr];
    if((plus[0] == -1) &&(sub[0] == -1)){
      if(mul[0] != -1) op = mul[--mulptr];
      if(div[0] != -1)
        if(div[--divptr] > op) op = div[divptr];
			if((mul[0] == -1)&&(div[0] == -1)){
				if(deref1[0]!=-1) op = deref1[--deref1ptr];
			}
    }
		}
  return op;
} 

static uint32_t deref(int addr){
  uint32_t m;
  uint8_t *raddr = (uint8_t *)guest_to_host(addr);  
  m = *raddr++;
  m += *raddr++*256;
  m += *raddr++*256*256;
  m += *raddr*256*256*256;
  return m;
}

int check_zero(int val1,int val2){
	if(val2 == 0){
	printf("%d/%d < %s\n",val1,val2,"\033[1;31mdivid zero exception\033[0m");
	assert(0);
	} 
	else return val1/val2;
}

static uint32_t eval(int p,int q){
  int op;
  int val1,val2;
  if(p > q)
    assert(0);
  else if(p == q){
    if(tokens[p].type == TK_REG){
			int n;
			bool success = false;
			n = isa_reg_str2val(tokens[p].str,&success);
			if(success == true)
	  		return n;
			else{
				printf("%s\n",tokens[p].str);
				printf("%d",n);
	  		printf("isa_reg f\n");
    	}}
    else if(tokens[p].type == DEREF)
      return 0;
    return strtol(tokens[p].str,NULL,0);
  }    
  else if(check_parentheses(p,q) == true)
    return eval(p + 1, q - 1);
  else{
    op = find_main_op(p,q);
		if(tokens[op].type == DEREF){
			val1 = eval(op , op);
			val2 = eval(op + 1,q);}
    else{
			val1 = eval(p , op -1);
    	val2 = eval(op + 1 ,q);}
    switch(tokens[op].type){
			case DEREF:return deref(val2);
      case TK_EQ:return (val1 == val2);
      case '+':return val1 + val2;
      case '-':return val1 - val2;
      case '*':return val1 * val2;
      case '/':return check_zero(val1,val2);
      default :assert(0);
    }
  }
}	

uint32_t expr(char *e, bool *success) {
  for (int i = 0; i < 1024; ++i){
    memset(tokens[i].str,0,sizeof(tokens[i].str));
  }
  
  if (!make_token(e)) {
    *success = false;
    return 0;
  }
  /* TODO: Insert codes to evaluate the expression. */
  return eval(0,nr_token-1);
}
