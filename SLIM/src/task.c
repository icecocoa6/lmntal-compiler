/*
 * task.c
 */


#include <stdio.h>
#include "lmntal.h"
#include "rule.h"
#include "membrane.h"
#include "instruction.h"
#include "read_instr.h"
#include "vector.h"

/* this size should be the maximum size of 'spec' arguments */
/* Or allocated when required */
LmnWord v1[1024];
LmnWord v2[1024];

/* for value tag */
LmnByte k1[1024];
LmnByte k2[1024];

LmnWord *wt, *tv;		/* working table */
LmnByte *at, *tkv;	/* attribute table */

#define SWAP(T,X,Y)       do { T t=(X); (X)=(Y); (Y)=t;} while(0)

struct Entity {
  LmnMembrane	*mem;
  struct Entity	*next;
};

struct MemStack {
  struct Entity	*head, *tail;
} memstack;

static BOOL interpret(LmnRuleInstr instr, LmnRuleInstr *next);

static void memstack_init()
{
  memstack.head = LMN_MALLOC(struct Entity);
  memstack.head->next = NULL;
  memstack.head->mem = NULL;
  memstack.tail = memstack.head;
}

static void memstack_push(LmnMembrane *mem)
{
  struct Entity *ent = LMN_MALLOC(struct Entity);
  ent->mem = mem;
  ent->next = memstack.head->next;
  memstack.head->next = ent;
}

static int memstack_isempty()
{
  return memstack.head->next==NULL;
}

static LmnMembrane* memstack_pop()
{
  struct Entity *ent = memstack.head->next;
  LmnMembrane *mem = ent->mem;
  memstack.head->next = ent->next;
  LMN_FREE(ent);
  return mem;
}

static LmnMembrane* memstack_peek()
{
  return memstack.head->next->mem;
}

static void memstack_printall()
{
  struct Entity *ent;
  for(ent = memstack.head; ent!=NULL; ent = ent->next){
    if(ent->mem!=NULL)printf("%d ", ent->mem->name);
  }
  printf("\n");
}

static BOOL react_ruleset(LmnMembrane *mem, LmnRuleSet *ruleset)
{
  int i;
  LmnRuleInstr dummy;

  wt[0] = (LmnWord)mem;
  for (i = 0; i < ruleset->num; i++) {
    if (interpret(ruleset->rules[i]->instr, &dummy)) return TRUE;
  }
  
  return FALSE;
}

/* ��󥯥��֥������Ȥ����� */
typedef struct LinkObj {
  LmnAtomPtr ap;
  LmnLinkAttr pos;
} LinkObj;

/*
 * ground�����ǺƵ�����Τ���ɬ�פˤʤä�stack
 * �Ȥꤢ���������˺�ä����ɤɤ����褦��
 */
typedef struct StackEntity StackEntity;
struct StackEntity {
  LmnWord keyp;
  StackEntity *next;
};

typedef struct Stack {
  StackEntity *head, *tail;
} Stack;

static void stack_init(Stack *stack)
{
  stack->head = LMN_MALLOC(StackEntity);
  stack->head->next = NULL;
  stack->head->keyp = 0; /* TODO: ����Ǥ����Τ��ġ� */
  stack->tail = stack->head;
}

static void stack_push(Stack* stack, LmnWord keyp)
{
  StackEntity *ent = LMN_MALLOC(StackEntity);
  ent->keyp = keyp;
  ent->next = stack->head->next;
  stack->head->next = ent;
}

static int stack_isempty(Stack* stack)
{
  return stack->head->next==NULL;
}

static LmnWord stack_pop(Stack *stack)
{
  StackEntity *ent = stack->head->next;
  LmnWord keyp = ent->keyp;
  stack->head->next = ent->next;
  LMN_FREE(ent);
  return keyp;
}

static LmnWord stack_peek(Stack *stack)
{
  return stack->head->next->keyp;
}

static void stack_printall(Stack *stack)
{
  StackEntity *ent;
  for(ent = stack->head; ent!=NULL; ent = ent->next){
    printf("%lu ", ent->keyp);
  }
  printf("\n");
}
/* Stack�����ޤ� */

static int exec(LmnMembrane *mem)
{
  RuleSetNode *rs = mem->rulesets;
  int flag = FALSE;
/*   	flag = react(mem, systemrulesets); */
  if (!flag) {
    while (rs != NULL) {
      if (react_ruleset(mem, rs->ruleset)) {
        flag = TRUE;
        break;
      }
      rs = rs->next;
    }
  }
	
  return flag;
}

void run(void)
{
  LmnMembrane *mem;

  /* Initialize for running */
  wt = v1;
  tv = v2;
  at = k1;
  tkv = k2;

  memstack_init();
  
  /* make toplevel membrane */
  
  mem = lmn_mem_make();

  lmn_mem_dump(mem);

  /*     lmn_mem_add_ruleset(mem, lmn_ruleset_table.entry[0]); */

  memstack_push(mem);
  /* call init atom creation rule */
  react_ruleset(mem, lmn_ruleset_table.entry[0]);

  lmn_mem_dump(mem);
  
  while(!memstack_isempty()){
    LmnMembrane *mem = memstack_peek();
    if(!exec(mem))
      memstack_pop();
/*     memstack_printall(); */
  }

  lmn_mem_dump(mem);
}

/* Utility for reading data */

#define READ_DATA_ATOM(dest, attr)              \
  do {                                          \
    switch (attr) {                             \
    case LMN_ATOM_INT_ATTR:                     \
       LMN_IMS_READ(int, instr, (dest));        \
       break;                                   \
     case LMN_ATOM_DBL_ATTR:                    \
     {                                          \
        double *x;                              \
        x = LMN_MALLOC(double);                 \
        LMN_IMS_READ(double, instr, *x);        \
        (dest) = (LmnWord)x;          \
        break;                                  \
     }                                          \
     default:                                   \
       LMN_ASSERT(FALSE);                       \
       break;                                   \
     }                                          \
   } while (0)

#define READ_CONST_DATA_ATOM(dest, attr)        \
  do {                                          \
    switch (attr) {                             \
    case LMN_ATOM_INT_ATTR:                     \
       LMN_IMS_READ(int, instr, (dest));        \
       break;                                   \
     case LMN_ATOM_DBL_ATTR:                    \
       (dest) = (LmnWord)instr;                 \
       instr += sizeof(double);                 \
       break;                                   \
     default:                                   \
       LMN_ASSERT(FALSE);                       \
       break;                                   \
     }                                          \
   } while (0)

#define READ_CMP_DATA_ATOM(attr, x, result)            \
       do {                                            \
          switch(attr) {                               \
          case LMN_ATOM_INT_ATTR:                      \
            {                                          \
              int t;                                   \
              LMN_IMS_READ(int, instr, t);             \
              (result) = ((int)(x) == t);              \
              break;                                   \
            }                                          \
          case LMN_ATOM_DBL_ATTR:                      \
            {                                          \
              double t;                                \
              LMN_IMS_READ(double, instr, t);          \
              (result) = (*(double*)(x) == t);         \
              break;                                   \
            }                                          \
          default:                                     \
            LMN_ASSERT(FALSE);                         \
            break;                                     \
          }                                            \
          } while (0)

static BOOL interpret(LmnRuleInstr instr, LmnRuleInstr *next)
{
  LmnInstrOp op;
  LmnRuleInstr start = instr;

  while (TRUE) {
    LMN_IMS_READ(LmnInstrOp, instr, op);
    fprintf(stderr, "op: %d %d\n", op, instr - start - 2);
/*     lmn_mem_dump((LmnMembrane*)wt[0]); */
    switch (op) {
    case INSTR_SPEC:
      /* ignore spec, because wt is initially large enough. */
      instr += sizeof(LmnInstrVar)*2;
      break;
    case INSTR_INSERTCONNECTORSINNULL:
    {
      /* TODO ���ɤ����� */
      LmnInstrVar seti, listi;
      LMN_IMS_READ(LmnInstrVar, instr, seti);
      LMN_IMS_READ(LmnInstrVar, instr, listi);
      break;
    }
    case INSTR_JUMP:
    {
      LmnInstrVar num, i, n;
      LmnJumpOffset offset;

      i = 0;
      /* atom */
      LMN_IMS_READ(LmnInstrVar, instr, num);
      for (; num--; i++) {
        LMN_IMS_READ(LmnInstrVar, instr, n);
        tv[i] = wt[n];
        tkv[i] = at[n];
      }
      /* mem */
      LMN_IMS_READ(LmnInstrVar, instr, num);
      for (; num--; i++) {
        LMN_IMS_READ(LmnInstrVar, instr, n);
        tv[i] = wt[n];
        tkv[i] = at[n];
      }
      /* vars */
      LMN_IMS_READ(LmnInstrVar, instr, num);
      for (; num--; i++) {
        LMN_IMS_READ(LmnInstrVar, instr, n);
        tv[i] = wt[n];
        tkv[i] = at[n];
      }

      LMN_IMS_READ(LmnJumpOffset, instr, offset);
      instr += offset;

      SWAP(LmnWord*, wt, tv);
      SWAP(LmnLinkAttr*, at, tkv);
      if (interpret(instr, &instr)) return TRUE;
      else {
        SWAP(LmnWord*, wt, tv);
        SWAP(LmnLinkAttr*, at, tkv);
        return FALSE;
      }
    }
    case INSTR_COMMIT:
      instr += sizeof(lmn_interned_str) + sizeof(LmnLineNum);
      break;
    case INSTR_FINDATOM:
    {
      LmnInstrVar atomi, memi;
      LmnLinkAttr attr;

      LMN_IMS_READ(LmnInstrVar, instr, atomi);
      LMN_IMS_READ(LmnInstrVar, instr, memi);
      LMN_IMS_READ(LmnLinkAttr, instr, attr);
      
      if (LMN_ATTR_IS_DATA(attr)) {
        fprintf(stderr, "I can not find data atoms.\n");
        assert(FALSE);
      } else { /* symbol atom */
        LmnFunctor f;
        AtomSetEntry *atomlist_ent;
        LmnAtomPtr atom;
        
        LMN_IMS_READ(LmnFunctor, instr, f);
        atomlist_ent = lmn_mem_get_atomlist((LmnMembrane*)wt[memi], f);
        if (atomlist_ent) {
          at[atomi] = LMN_ATTR_MAKE_LINK(0);
          for (atom = atomlist_ent->head;
               atom != lmn_atomset_end(atomlist_ent);
               atom = LMN_ATOM_GET_NEXT(atom)) {
            wt[atomi] = (LmnWord)atom;
            if (interpret(instr, &instr)) return TRUE;
          }
        }
       return FALSE;
      }
      break;
    }
    case INSTR_ANYMEM:
    {
      LmnInstrVar mem1, mem2, memt, memn; /* dst, parent, type, name */
      LmnMembrane* mp;

      LMN_IMS_READ(LmnInstrVar, instr, mem1);
      LMN_IMS_READ(LmnInstrVar, instr, mem2);
      LMN_IMS_READ(LmnInstrVar, instr, memt);
      LMN_IMS_READ(lmn_interned_str, instr, memn);

      mp = ((LmnMembrane*)wt[mem2])->child_head;
      while (mp) {
        wt[mem1] = (LmnWord)mp;
        if (mp->name == memn && interpret(instr, &instr)) return TRUE;
        mp = mp->next;
      }
      return FALSE;
      break;
    }
    /* INSTR_GETMEM
       ���ȥब��°��ؤλ��Ȥ���äƤ��ʤ��ΤǤ���̿����Ψ�ɤ�
       �����Ǥ��ʤ�
    */
    /* INSTR_GETPARENT
       ���ȥ��ư�ƥ��ȤǻȤ���
    */
    /*----------------------------------------------------------------------
     * ��˴ؤ�����Ϥ��ʤ����ܥ�����̿��
     */
    /* INSTR_TESTMEM
       ���ȥ��ư�ƥ��ȤΤߡ�
    */
    case INSTR_NORULES:
    {
      LmnInstrVar memi;
      RuleSetList *rp;

      LMN_IMS_READ(LmnInstrVar, instr, memi);
      rp = ((LmnMembrane *)wt[memi])->rulesets;
      if(rp) return FALSE;
      break;
    }
    case INSTR_NFREELINKS:
    {
      LmnInstrVar memi, count;

      LMN_IMS_READ(LmnInstrVar, instr, memi);
      LMN_IMS_READ(LmnInstrVar, instr, count);

      if (!lmn_mem_nfreelinks((LmnMembrane *)wt[memi], count)) return FALSE;
      break;
    }
    case INSTR_NATOMS:
    {
      LmnInstrVar memi, count;
      LMN_IMS_READ(LmnInstrVar, instr, memi);
      LMN_IMS_READ(LmnInstrVar, instr, count);
      if (!lmn_mem_natoms((LmnMembrane*)wt[memi], count)) return FALSE;
      break;
    }
    case INSTR_NATOMSINDIRECT:
    {
      LmnInstrVar memi, counti;
      LMN_IMS_READ(LmnInstrVar, instr, memi);
      LMN_IMS_READ(LmnInstrVar, instr, counti);
      if(!lmn_mem_natoms((LmnMembrane*)wt[memi], wt[counti])) return FALSE;
      break;
    }
    case INSTR_NMEMS:
    {
      LmnInstrVar memi, count;

      LMN_IMS_READ(LmnInstrVar, instr, memi);
      LMN_IMS_READ(LmnInstrVar, instr, count);

      if(!lmn_mem_nmems((LmnMembrane*)wt[memi], count)) return FALSE;
      break;
    }
    case INSTR_EQMEM:
    {
      LmnInstrVar mem1, mem2;

      LMN_IMS_READ(LmnInstrVar, instr, mem1);
      LMN_IMS_READ(LmnInstrVar, instr, mem2);
      if ((LmnMembrane *)wt[mem1] != (LmnMembrane *)wt[mem2]) return FALSE;
      break;
    }
    case INSTR_NEQMEM:
    {
      LmnInstrVar mem1, mem2;

      LMN_IMS_READ(LmnInstrVar, instr, mem1);
      LMN_IMS_READ(LmnInstrVar, instr, mem2);
      if ((LmnMembrane *)wt[mem1] == (LmnMembrane *)wt[mem2]) return FALSE;
      break;
    }
    /* INSTR_STABLE
       need no
    */
    /*----------------------------------------------------------------------
     * ���ȥ�˴ط�������Ϥ��ʤ����ܥ�����̿�� 
     */
    case INSTR_FUNC:
    {
      LmnInstrVar atomi;
      LmnFunctor f;
      LmnLinkAttr attr;
      LMN_IMS_READ(LmnInstrVar, instr, atomi);
      LMN_IMS_READ(LmnLinkAttr, instr, attr);

      if (LMN_ATTR_IS_DATA(at[atomi]) == LMN_ATTR_IS_DATA(attr)) {
        if(LMN_ATTR_IS_DATA(at[atomi])) {
          BOOL eq;
          if(at[atomi] != attr) return FALSE; /* comp attr */
          READ_CMP_DATA_ATOM(attr, wt[atomi], eq);
          if (!eq) return FALSE;
        }
        else /* symbol atom */
          {
            LMN_IMS_READ(LmnFunctor, instr, f);
            if (LMN_ATOM_GET_FUNCTOR(LMN_ATOM(wt[atomi])) != f) {
              return FALSE;
            }
          }
      }
      else { /* LMN_ATTR_IS_DATA(at[atomi]) != LMN_ATTR_IS_DATA(attr) */
        return FALSE;
      }
      fprintf(stderr, "instr_func successed\n");
      break;
    }
    case INSTR_NOTFUNC:
    {
      LmnInstrVar atomi;
      LmnFunctor f;
      LmnLinkAttr attr;
      LMN_IMS_READ(LmnInstrVar, instr, atomi);
      LMN_IMS_READ(LmnLinkAttr, instr, attr);

      if (LMN_ATTR_IS_DATA(at[atomi]) == LMN_ATTR_IS_DATA(attr)) {
        if(LMN_ATTR_IS_DATA(at[atomi])) {
          if(at[atomi] == attr) {
            BOOL eq;
            READ_CMP_DATA_ATOM(attr, wt[atomi], eq);
            if (eq) return FALSE;
          }
        }
        else { /* symbol atom */
          LMN_IMS_READ(LmnFunctor, instr, f);
          if (LMN_ATOM_GET_FUNCTOR(LMN_ATOM(wt[atomi])) == f) return FALSE;
        }
      }
      break;
    }
    case INSTR_EQATOM:
    {
      LmnInstrVar atom1, atom2;
      LMN_IMS_READ(LmnInstrVar, instr, atom1);
      LMN_IMS_READ(LmnInstrVar, instr, atom2);

      if(LMN_ATOM(wt[atom1]) != LMN_ATOM(wt[atom2])) return FALSE;
      break;
    }
    case INSTR_NEQATOM:
    {
      LmnInstrVar atom1, atom2;
      LMN_IMS_READ(LmnInstrVar, instr, atom1);
      LMN_IMS_READ(LmnInstrVar, instr, atom2);
      LMN_ASSERT(!LMN_ATTR_IS_DATA(at[atom1]));
      if(LMN_ATOM(wt[atom1]) == LMN_ATOM(wt[atom2])) return FALSE;
      break;
    }
    case INSTR_SAMEFUNC:
    {
      LmnInstrVar atom1, atom2;

      LMN_IMS_READ(LmnInstrVar, instr, atom1);
      LMN_IMS_READ(LmnInstrVar, instr, atom2);

      if(LMN_ATTR_IS_DATA(at[atom1]) == LMN_ATTR_IS_DATA(at[atom2])) {
        if (LMN_ATTR_IS_DATA(at[atom1])) {
          if(at[atom1] != at[atom2]) { /* comp attr */
            return FALSE;
          }
          /* TODO: double*�ʤɥݥ��󥿤ξ����ͤ����ԡ�����Ƥ����礬���� */
          else if(wt[atom1] != wt[atom2]) { /* comp value */
            return FALSE;
          }
        }
        else if(LMN_ATOM_GET_FUNCTOR((LmnAtomPtr)wt[atom1]) != LMN_ATOM_GET_FUNCTOR((LmnAtomPtr)wt[atom2])) {
          return FALSE;
        }
      }
      else { /* LMN_ATTR_IS_DATA(at[atom1] != LMN_ATTR_IS_DATA(at[atom2]) */
        return FALSE;
      }
      break;
    }
    /*----------------------------------------------------------------------
     * �ե��󥯥��˴ط�����̿��
     */
    case INSTR_DEREFFUNC:
    {
      LmnInstrVar funci, atomi, pos;
      LmnLinkAttr attr;
      
      LMN_IMS_READ(LmnInstrVar, instr, funci);
      LMN_IMS_READ(LmnInstrVar, instr, atomi);
      LMN_IMS_READ(LmnLinkAttr, instr, pos);

      attr = LMN_ATOM_GET_LINK_ATTR(LMN_ATOM(wt[atomi]), pos);
      if (LMN_ATTR_IS_DATA(attr)) {
        wt[funci] = LMN_ATOM_GET_LINK(LMN_ATOM(wt[atomi]), pos);
      }
      else { /* symbol atom */
        wt[funci] = LMN_ATOM_GET_FUNCTOR(LMN_ATOM_GET_LINK(LMN_ATOM(wt[atomi]), pos));
      }
      at[funci] = attr;
      break;
    }
    case INSTR_GETFUNC:
    {
      LmnInstrVar funci, atomi;

      LMN_IMS_READ(LmnFunctor, instr, funci);
      LMN_IMS_READ(LmnInstrVar, instr, atomi);
      if(LMN_ATTR_IS_DATA(at[atomi])) {
        wt[funci]=wt[atomi];
      }
      else {
        wt[funci] = (LmnWord)LMN_ATOM_GET_FUNCTOR(wt[atomi]);
      }
      at[funci] = at[atomi];

      break;
    }
    case INSTR_LOADFUNC:
    {
      LmnInstrVar funci;
      LmnLinkAttr attr;

      LMN_IMS_READ(LmnFunctor, instr, funci);
      LMN_IMS_READ(LmnInstrVar, instr, attr);
      at[funci] = attr;
      if(LMN_ATTR_IS_DATA(attr)) {
        READ_CONST_DATA_ATOM(wt[funci], attr);
      }
      else {
        LmnFunctor f;
        
        LMN_IMS_READ(LmnFunctor, instr, f);
        wt[funci] = (LmnWord)f;
      }
      break;
    }
    case INSTR_EQFUNC:
    {
      LmnInstrVar func1, func2;

      LMN_IMS_READ(LmnInstrVar, instr, func1);
      LMN_IMS_READ(LmnInstrVar, instr, func2);
      if ((LmnFunctor)wt[func1] != (LmnFunctor)wt[func2]) return FALSE;
      break;
    }
    case INSTR_NEQFUNC:
    {
      LmnInstrVar func1, func2;

      LMN_IMS_READ(LmnInstrVar, instr, func1);
      LMN_IMS_READ(LmnInstrVar, instr, func2);
      if ((LmnFunctor)wt[func1] == (LmnFunctor)wt[func2]) return FALSE;
      break;
    }
    /*----------------------------------------------------------------------
     * ���ȥ��������ܥܥǥ�̿��
     */
    case INSTR_REMOVEATOM:
    {
      LmnInstrVar atomi, memi;
      LmnAtomPtr atom;

      LMN_IMS_READ(LmnInstrVar, instr, atomi);
      LMN_IMS_READ(LmnInstrVar, instr, memi);

      if (LMN_ATTR_IS_DATA(at[atomi])) {
        /* TODO: freeatom �����뤫�餳����FREE��ɬ����ΤǤ� ? */
        switch (at[atomi]) {
        case LMN_ATOM_INT_ATTR: /* notiong */  break;
        case LMN_ATOM_DBL_ATTR:
          LMN_FREE((double*)wt[atomi]);
          break;
        default:
          assert(FALSE);
        }
      } else { /* symbol atom */
        atom = (LmnAtomPtr)wt[atomi];
        if (! LMN_ATTR_IS_DATA(at[atomi])){
          lmn_mem_remove_atom((LmnMembrane*)wt[memi], (LmnAtomPtr)wt[atomi]);
        }
      }
      break;
    }
    case INSTR_NEWATOM:
    {
      LmnInstrVar atomi, memi;
      LmnAtomPtr ap;
      LmnLinkAttr attr;

      LMN_IMS_READ(LmnInstrVar, instr, atomi);
      LMN_IMS_READ(LmnInstrVar, instr, memi);
      LMN_IMS_READ(LmnLinkAttr, instr, attr);
      at[atomi] = attr;
      if (LMN_ATTR_IS_DATA(attr)) {
        READ_DATA_ATOM(wt[atomi], attr);
      } else { /* symbol atom */
        LmnFunctor f;
        LMN_IMS_READ(LmnFunctor, instr, f);
        ap = lmn_new_atom(f);
        lmn_mem_push_atom((LmnMembrane*)wt[memi], ap);
        wt[atomi] = (LmnWord)ap;
      }
      break;
    }
    case INSTR_NEWATOMINDIRECT:
    {
      LmnInstrVar atomi, memi;
      LmnAtomPtr ap;
      LmnFunctor funci;

      LMN_IMS_READ(LmnInstrVar, instr, atomi);
      LMN_IMS_READ(LmnInstrVar, instr, memi);
      LMN_IMS_READ(LmnInstrVar, instr, funci);
      at[atomi] = at[funci];
      if (LMN_ATTR_IS_DATA(at[atomi])) {
        wt[atomi] = wt[funci];
      } else { /* symbol atom */
        ap = lmn_new_atom(wt[funci]);
        lmn_mem_push_atom((LmnMembrane*)wt[memi], ap);
        wt[atomi] = (LmnWord)ap;
      }
      break;
    }
    case INSTR_ENQUEUEATOM:
    {
      LmnInstrVar atom;
	
      LMN_IMS_READ(LmnInstrVar, instr, atom);
      break;
    }
    case INSTR_DEQUEUEATOM:
    {
      LmnInstrVar atom;
	
      LMN_IMS_READ(LmnInstrVar, instr, atom);
      break;
    }
    case INSTR_FREEATOM:
    {
      LmnInstrVar atomi;

      LMN_IMS_READ(LmnInstrVar, instr, atomi);

      if (LMN_ATTR_IS_DATA(at[atomi])) {
        switch (at[atomi]) {
        case LMN_ATOM_INT_ATTR:
          break;
        case LMN_ATOM_DBL_ATTR:
          LMN_FREE((double*)wt[atomi]);
          break;
        default:
          LMN_ASSERT(FALSE);
          break;
        }
      }
      else { /* symbol atom */
        lmn_delete_atom((LmnAtomPtr)wt[atomi]);
      }
      break;
    }
    case INSTR_ALTERFUNC:
    {
      /* TODO: incompletely implemented */
      LMN_ASSERT(FALSE);
/*       LmnInstrVar atomi; */
/*       LmnLinkAttr attr; */

/*       LMN_IMS_READ(LmnInstrVar, instr, atomi); */
/*       LMN_IMS_READ(LmnLinkAttr, instr, attr); */

/*       if (LMN_ATTR_IS_DATA(attr)) { */
/*         READ_DATA_ATOM(wt[atomi], attr); */
/*       } */
    }
    case INSTR_ALTERFUNCINDIRECT:
    {
      /* TODO: incompletely implemented */
      LMN_ASSERT(FALSE);
/*       LmnInstrVar atomi; */
/*       LmnLinkAttr attr; */

/*       LMN_IMS_READ(LmnInstrVar, instr, atomi); */
/*       LMN_IMS_READ(LmnLinkAttr, instr, attr); */

/*       if (LMN_ATTR_IS_DATA(attr)) { */
/*         READ_DATA_ATOM(wt[atomi], attr); */
/*       } */
    }
    /*----------------------------------------------------------------------
     * ���ȥ�����뷿�դ���ĥ��̿��
     */
    case INSTR_ALLOCATOM:
    {
      LmnInstrVar atomi;
      LmnAtomPtr ap;
      LmnLinkAttr attr;

      LMN_IMS_READ(LmnInstrVar, instr, atomi);
      LMN_IMS_READ(LmnLinkAttr, instr, attr);
      at[atomi] = attr;
      if (LMN_ATTR_IS_DATA(attr)) {
        READ_DATA_ATOM(attr, wt[atomi]);
      } else { /* symbol atom */
        LmnFunctor f;
        fprintf(stderr, "symbol atom can't be created in GUARD\n");
        LMN_IMS_READ(LmnFunctor, instr, f);
        wt[atomi] = (LmnWord)ap;
      }
      break;
    }
    case INSTR_ALLOCATOMINDIRECT:
    {
      LmnInstrVar atomi;
      LmnFunctor funci;

      LMN_IMS_READ(LmnInstrVar, instr, atomi);
      LMN_IMS_READ(LmnInstrVar, instr, funci);
      at[atomi] = at[funci];
      if (LMN_ATTR_IS_DATA(at[funci])) {
        READ_DATA_ATOM(at[funci], wt[atomi]);
      } else { /* symbol atom */
        fprintf(stderr, "symbol atom can't be created in GUARD\n");
      }
      break;
    }
    case INSTR_COPYATOM:
    {
      LmnInstrVar atom1, memi, atom2;

      LMN_IMS_READ(LmnInstrVar, instr, atom1);
      LMN_IMS_READ(LmnInstrVar, instr, memi);
      LMN_IMS_READ(LmnInstrVar, instr, atom2);

      if (LMN_ATTR_IS_DATA(at[atom2])) {
        switch (at[atom2]) {
        case LMN_ATOM_INT_ATTR:
          wt[atom1] = wt[atom2];
          break;
        case LMN_ATOM_DBL_ATTR:
          wt[atom1] = (LmnWord)LMN_MALLOC(double);
          *(double *)wt[atom1] = *(double*)wt[atom2];
          break;
        default:
          LMN_ASSERT(FALSE);
          break;
        }
      } else { /* symbol atom */
        LmnFunctor f = LMN_ATOM_GET_FUNCTOR(LMN_ATOM(wt[atom2]));
        wt[atom1] = (LmnWord)lmn_new_atom(f);
        memcpy((void *)wt[atom1], (void *)wt[atom2], LMN_WORD_BYTES*LMN_ATOM_WORDS(f));
        at[atom1] = at[atom2];
      }
      break;
    }
    case INSTR_ADDATOM:
    {
      LmnInstrVar memi, atomi;

      LMN_IMS_READ(LmnInstrVar, instr, memi);
      LMN_IMS_READ(LmnInstrVar, instr, atomi);
      lmn_mem_push_atom((LmnMembrane *)wt[memi], LMN_ATOM(wt[atomi]));
      break;
    }
    /*----------------------------------------------------------------------
     * ���������ܥܥǥ�̿��
     */
    case INSTR_REMOVEMEM:
    {
      LmnInstrVar memi;
      LmnMembrane *mp;

      LMN_IMS_READ(LmnInstrVar, instr, memi);
      instr += sizeof(LmnInstrVar); /* ingnore parent */

      mp = (LmnMembrane*)wt[memi];
      if(mp->parent->child_head == mp) mp->parent->child_head = mp->next;
      if(mp->prev) mp->prev->next = mp->next;
      if(mp->next) mp->next->prev = mp->prev;
      break;
    }
    case INSTR_NEWMEM:
    {
      LmnInstrVar newmemi, parentmemi, memf;
      LmnMembrane *mp;

      LMN_IMS_READ(LmnInstrVar, instr, newmemi);
      LMN_IMS_READ(LmnInstrVar, instr, parentmemi);
      LMN_IMS_READ(LmnInstrVar, instr, memf);

      mp = lmn_mem_make(); /*lmn_new_mem(memf);*/
      lmn_mem_push_mem((LmnMembrane*)wt[parentmemi], mp);
      wt[newmemi] = (LmnWord)mp;
      memstack_push(mp);
      break;
    }
    case INSTR_ALLOCMEM:
    {
      LmnInstrVar memi;

      LMN_IMS_READ(LmnInstrVar, instr, memi);
      wt[memi] = (LmnWord)lmn_mem_make();
      break;
    }
    /* INSTR_NEWROOT
       no need
    */
    case INSTR_MOVECELLS:
    {
      LmnInstrVar destmemi, srcmemi;

      LMN_IMS_READ(LmnInstrVar, instr, destmemi);
      LMN_IMS_READ(LmnInstrVar, instr, srcmemi);

      
      break;
    }
    case INSTR_ALLOCLINK:
    {
      LmnInstrVar link, atom, n;
      
      LMN_IMS_READ(LmnInstrVar, instr, link);
      LMN_IMS_READ(LmnInstrVar, instr, atom);
      LMN_IMS_READ(LmnInstrVar, instr, n);

      if (LMN_ATTR_IS_DATA(at[atom])) {
        wt[link] = wt[atom];
        at[link] = at[atom];
      } else { /* link to atom */
        wt[link] = (LmnWord)LMN_ATOM(wt[atom]);
        at[link] = LMN_ATTR_MAKE_LINK(n);
      }
      break;
    }
    case INSTR_UNIFYLINKS:
    {
      LmnInstrVar link1, link2, mem;
      
      LMN_IMS_READ(LmnInstrVar, instr, link1);
      LMN_IMS_READ(LmnInstrVar, instr, link2);
      LMN_IMS_READ(LmnInstrVar, instr, mem);

      if (LMN_ATTR_IS_DATA(at[link1]) && LMN_ATTR_IS_DATA(at[link2])) {
        #ifdef DEBUG
        fprintf(stderr, "Two data atoms are connected each other.\n");
        #endif
      }
      else if (LMN_ATTR_IS_DATA(at[link1])) {
        /* TODO at�򥳥ԡ�����ɬ�פϤʤ�����? */
           
        LMN_ATOM_SET_LINK(LMN_ATOM(wt[link2]), LMN_ATTR_GET_VALUE(at[link2]), wt[link1]);
        LMN_ATOM_SET_LINK_ATTR(LMN_ATOM(wt[link2]),
                               LMN_ATTR_GET_VALUE(at[link2]),
                               at[link1]);
      }
      else if (LMN_ATTR_IS_DATA(at[link2])) {
        LMN_ATOM_SET_LINK(LMN_ATOM(wt[link1]), LMN_ATTR_GET_VALUE(at[link1]), wt[link2]);
        LMN_ATOM_SET_LINK_ATTR(LMN_ATOM(wt[link1]),
                               LMN_ATTR_GET_VALUE(at[link1]),
                               at[link2]);
      }
      else {
        LMN_ATOM_SET_LINK(LMN_ATOM(wt[link1]), LMN_ATTR_GET_VALUE(at[link1]), wt[link2]);
        LMN_ATOM_SET_LINK(LMN_ATOM(wt[link2]), LMN_ATTR_GET_VALUE(at[link2]), wt[link1]);
        LMN_ATOM_SET_LINK_ATTR(LMN_ATOM(wt[link1]),
                               LMN_ATTR_GET_VALUE(at[link1]),
                               at[link2]);
        LMN_ATOM_SET_LINK_ATTR(LMN_ATOM(wt[link2]),
                               LMN_ATTR_GET_VALUE(at[link2]),
                               at[link1]);
      }
        break;
    }
		case INSTR_NEWLINK:
		{
			LmnInstrVar atom1, atom2, pos1, pos2, memi;

			LMN_IMS_READ(LmnInstrVar, instr, atom1);
			LMN_IMS_READ(LmnInstrVar, instr, pos1);
			LMN_IMS_READ(LmnInstrVar, instr, atom2);
			LMN_IMS_READ(LmnInstrVar, instr, pos2);
			LMN_IMS_READ(LmnInstrVar, instr, memi);

			if (LMN_ATTR_IS_DATA(at[atom1]) && LMN_ATTR_IS_DATA(at[atom2])) {
				#ifdef DEBUG
				fprintf(stderr, "Two data atoms are connected each other.\n");
				#endif
			}
			else if (LMN_ATTR_IS_DATA(at[atom1])) {
				LMN_ATOM_SET_LINK(LMN_ATOM(wt[atom2]), pos2, wt[atom1]);
				LMN_ATOM_SET_LINK_ATTR(LMN_ATOM(wt[atom2]), pos2, at[atom1]);
			}
			else if (LMN_ATTR_IS_DATA(at[atom2])) {
				LMN_ATOM_SET_LINK(LMN_ATOM(wt[atom1]), pos1, wt[atom2]);
				LMN_ATOM_SET_LINK_ATTR(LMN_ATOM(wt[atom1]), pos1, at[atom2]);
			}
			else {
				LMN_ATOM_SET_LINK(LMN_ATOM(wt[atom1]), pos1, wt[atom2]);
				LMN_ATOM_SET_LINK_ATTR(LMN_ATOM(wt[atom1]), pos1, pos2);
				LMN_ATOM_SET_LINK(LMN_ATOM(wt[atom2]), pos2, wt[atom1]); 
				LMN_ATOM_SET_LINK_ATTR(LMN_ATOM(wt[atom2]), pos2, pos1);
			}
			break;
		}
		case INSTR_RELINK:
		{
			LmnInstrVar atom1, atom2, pos1, pos2, memi;
			LmnAtomPtr ap;
			LmnByte attr;

			LMN_IMS_READ(LmnInstrVar, instr, atom1);
			LMN_IMS_READ(LmnInstrVar, instr, pos1);
			LMN_IMS_READ(LmnInstrVar, instr, atom2);
			LMN_IMS_READ(LmnInstrVar, instr, pos2);
			LMN_IMS_READ(LmnInstrVar, instr, memi);

			ap = LMN_ATOM(LMN_ATOM_GET_LINK(wt[atom2], pos2));
			attr = LMN_ATOM_GET_LINK_ATTR(wt[atom2], pos2);

			if(LMN_ATTR_IS_DATA(at[atom1]) && LMN_ATTR_IS_DATA(attr)) {
			  #ifdef DEBUG
				fprintf(stderr, "Two data atoms are connected each other.\n");
				#endif 
			}
			else if (LMN_ATTR_IS_DATA(at[atom1])) {
				LMN_ATOM_SET_LINK(ap, attr, wt[atom1]);
				LMN_ATOM_SET_LINK_ATTR(ap, attr, at[atom1]);
			}
			else if (LMN_ATTR_IS_DATA(attr)) {
				LMN_ATOM_SET_LINK(LMN_ATOM(wt[atom1]), pos1, (LmnWord)ap);
				LMN_ATOM_SET_LINK_ATTR(LMN_ATOM(wt[atom1]), pos1, attr);
			}
			else {
				LMN_ATOM_SET_LINK(ap, attr, wt[atom1]);
				LMN_ATOM_SET_LINK_ATTR(ap, attr, pos1);
				LMN_ATOM_SET_LINK(LMN_ATOM(wt[atom1]), pos1, (LmnWord)ap);
				LMN_ATOM_SET_LINK_ATTR(LMN_ATOM(wt[atom1]), pos1, attr);
			}
			break;
		}
		case INSTR_INHERITLINK:
		{
			LmnInstrVar atomi, posi, linki, memi;
			LMN_IMS_READ(LmnInstrVar, instr, atomi);
			LMN_IMS_READ(LmnInstrVar, instr, posi);
			LMN_IMS_READ(LmnInstrVar, instr, linki);
			LMN_IMS_READ(LmnInstrVar, instr, memi);

			if(LMN_ATTR_IS_DATA(at[atomi]) && LMN_ATTR_IS_DATA(at[linki])) {
				#ifdef DEBUG
				fprintf(stderr, "Two data atoms are connected each other.\n");
				#endif
			}
			else if(LMN_ATTR_IS_DATA(at[atomi])) {
				LMN_ATOM_SET_LINK(LMN_ATOM(wt[linki]), at[linki], wt[atomi]);
				LMN_ATOM_SET_LINK_ATTR(LMN_ATOM(wt[linki]), at[linki], at[atomi]);
			}
			else if(LMN_ATTR_IS_DATA(at[linki])) {
				LMN_ATOM_SET_LINK(LMN_ATOM(wt[atomi]), posi, wt[linki]);
				LMN_ATOM_SET_LINK_ATTR(LMN_ATOM(wt[atomi]), posi, at[linki]);
			}
			else {
				LMN_ATOM_SET_LINK(LMN_ATOM(wt[atomi]), posi, wt[linki]);
				LMN_ATOM_SET_LINK_ATTR(LMN_ATOM(wt[atomi]), posi, at[linki]);
				LMN_ATOM_SET_LINK(LMN_ATOM(wt[linki]), at[linki], wt[atomi]);
				LMN_ATOM_SET_LINK_ATTR(LMN_ATOM(wt[linki]), at[linki], posi);
			}
			break;
		}
		case INSTR_GETLINK:
		{
			LmnInstrVar linki, atomi, posi;
			LMN_IMS_READ(LmnInstrVar, instr, linki);
			LMN_IMS_READ(LmnInstrVar, instr, atomi);
			LMN_IMS_READ(LmnInstrVar, instr, posi);

			wt[linki] = LMN_ATOM_GET_LINK(wt[atomi], posi);
			at[linki] = LMN_ATOM_GET_LINK_ATTR(wt[atomi], posi);
			break;
		}
		case INSTR_UNIFY:
		{
			LmnInstrVar atom1, pos1, atom2, pos2, memi;
			LmnAtomPtr ap1, ap2;
			LmnByte attr1, attr2;
		
			LMN_IMS_READ(LmnInstrVar, instr, atom1);
			LMN_IMS_READ(LmnInstrVar, instr, pos1);
			LMN_IMS_READ(LmnInstrVar, instr, atom2);
			LMN_IMS_READ(LmnInstrVar, instr, pos2);
			LMN_IMS_READ(LmnInstrVar, instr, memi);

			ap1 = LMN_ATOM(LMN_ATOM_GET_LINK(wt[atom1], pos1));
			attr1 = LMN_ATOM_GET_LINK_ATTR(wt[atom1], pos1);
			ap2 = LMN_ATOM(LMN_ATOM_GET_LINK(wt[atom2], pos2));
			attr2 = LMN_ATOM_GET_LINK_ATTR(wt[atom2], pos2);

			if(LMN_ATTR_IS_DATA(attr1) && LMN_ATTR_IS_DATA(attr2)) {
				#ifdef DEBUG
				fprintf(stderr, "Two data atoms are connected each other.\n");
				#endif
			}
			else if (LMN_ATTR_IS_DATA(attr1)) {
				LMN_ATOM_SET_LINK(ap2, attr2, (LmnWord)ap1);
				LMN_ATOM_SET_LINK_ATTR(ap2, attr2, attr1);
			}
			else if (LMN_ATTR_IS_DATA(attr2)) {
				LMN_ATOM_SET_LINK(ap1, attr1, (LmnWord)ap2);
				LMN_ATOM_SET_LINK_ATTR(ap1, attr1, attr2);
			}
			else {
				LMN_ATOM_SET_LINK(ap2, attr2, (LmnWord)ap1);
				LMN_ATOM_SET_LINK_ATTR(ap2, attr2, attr1);
				LMN_ATOM_SET_LINK(ap1, attr1, (LmnWord)ap2);
				LMN_ATOM_SET_LINK_ATTR(ap1, attr1, attr2);
			}
			break;
		}
    case INSTR_PROCEED:
      *next = instr;
      return TRUE;
    case INSTR_FREEMEM:
    {
      LmnInstrVar memi;
      LmnMembrane *mp;

      LMN_IMS_READ(LmnInstrVar, instr, memi);

      mp = (LmnMembrane*)wt[memi];
      lmn_mem_free(mp);
      break;
    }
    case INSTR_LOADRULESET:
    {
      LmnInstrVar memi;
      LmnRulesetId id;
      LMN_IMS_READ(LmnInstrVar, instr, memi);
      LMN_IMS_READ(LmnRulesetId, instr, id);
      
      lmn_mem_add_ruleset((LmnMembrane*)wt[memi], LMN_RULESET_ID(id));
      break;
    }
    case INSTR_DEREFATOM:
    {
      LmnInstrVar atom1, atom2, posi;
      LMN_IMS_READ(LmnInstrVar, instr, atom1);
      LMN_IMS_READ(LmnInstrVar, instr, atom2);
      LMN_IMS_READ(LmnInstrVar, instr, posi);
            
      wt[atom1] = (LmnWord)LMN_ATOM(LMN_ATOM_GET_LINK(wt[atom2], posi));
      at[atom1] = LMN_ATOM_GET_LINK_ATTR(wt[atom2], posi);
      break;
    }
    case INSTR_DEREF:
    {
      LmnInstrVar atom1, atom2, pos1, pos2;
      LmnAtomPtr ap;
      LmnByte attr;
      LMN_IMS_READ(LmnInstrVar, instr, atom1);
      LMN_IMS_READ(LmnInstrVar, instr, atom2);
      LMN_IMS_READ(LmnInstrVar, instr, pos1);
      LMN_IMS_READ(LmnInstrVar, instr, pos2);

      ap = LMN_ATOM(LMN_ATOM_GET_LINK(wt[atom2], pos1));
      attr = LMN_ATOM_GET_LINK_ATTR(wt[atom2], pos1);

      if (LMN_ATTR_IS_DATA(at[atom2])) {
#ifdef DEBUG
        fprintf(stderr, "Can't deref from data atom.\n");
#endif
      }
      else if (LMN_ATTR_IS_DATA(attr)) {
        switch (attr) {
        case LMN_ATOM_INT_ATTR:
          {
            wt[atom1] = (LmnWord)ap;
            break;
          }
        case LMN_ATOM_DBL_ATTR:
          {
           wt[atom1] = (LmnWord)ap;
            break;
          }
        default:
          LMN_ASSERT(FALSE);
        }
        at[atom1] = attr;
      }
      else {
        if (attr != pos2)
          return FALSE;
        wt[atom1] = (LmnWord)ap;
        at[atom1] = LMN_ATTR_MAKE_LINK(0);
      }
      break;
    }
    case INSTR_ISGROUND:
    {
      LmnInstrVar funci, srclisti, avolisti;
      unsigned int i, c;
      int argi;
      Vector *srcvec, *avovec; /* ���Ǥϥ�� */
      Vector *roots;
      HashSet *avoset;
      HashSet *atoms; /* �����Ѥߥ��ȥ� */
      Stack *links;   /* �Ƶ��ѥ����å� */

      LMN_IMS_READ(LmnInstrVar, instr, funci);
      LMN_IMS_READ(LmnInstrVar, instr, srclisti);
      LMN_IMS_READ(LmnInstrVar, instr, avolisti);

      srcvec = (Vector*) wt[srclisti];
      avovec = (Vector*) wt[avolisti];
      roots = vec_make(avovec->num);
      for(i = 0; i < roots->cap; i++) {
        roots->tbl[i] = 0;
      }
      roots->tbl[0] = TRUE;
      
      hashset_init(avoset, 16);
      hashset_init(atoms, 256);
      for(i = 0; i < avovec->num; i++) {
        hashset_add(avoset, avovec->tbl[i]);
      }
      stack_init(links);
      stack_push(links, vec_get(srcvec, 0));

      /* method: isGround */
      while(!stack_isempty(links)) {
        LinkObj *linko = (LinkObj *)stack_pop(links);
        LinkObj *nextlinko;
        LmnAtomPtr ap = linko->ap;
        if(hashset_contains(atoms, (HashKeyType)ap)) /* �����Ѥߥ��ȥ� */
          continue;
        if(hashset_contains(avoset, LMN_ATOM_GET_LINK(ap, linko->pos)))
          return FALSE;
        argi = vec_indexof(srcvec, LMN_ATOM_GET_LINK(ap, linko->pos));
        if(argi >= 0) { /* ������ã������� */
          roots->tbl[argi] = TRUE;
          continue;
        }
        if(LMN_IS_PROXY_FUNCTOR(LMN_ATOM_GET_FUNCTOR(ap))) /* ����Ǥ��� */
          return FALSE;
        c++;
        hashset_add(atoms, (LmnWord)&ap);
        for(i = 0; i < LMN_ATOM_GET_ARITY(ap); i++) {
          if(i == linko->pos) /* �ƤؤΥ�� */
            continue;
          nextlinko->ap = (LmnAtomPtr)LMN_ATOM_GET_LINK(ap, i);
          nextlinko->pos = LMN_ATOM_GET_LINK_ATTR(ap, i);
          stack_push(links, (LmnWord)&nextlinko); 
        }
      }
      for(i = 0; i < srcvec->num; i++) {
        if(roots->tbl[i] == TRUE) continue;
        else return -1;
      }
      printf("instr_isground: success\n");
    }
    case INSTR_ISUNARY:
    {
      LmnInstrVar atomi;
      LMN_IMS_READ(LmnInstrVar, instr, atomi);

      if(LMN_ATOM_GET_ARITY((LmnAtomPtr)wt[atomi]) != 1) return FALSE;
      break;
    }
    case INSTR_ISINT:
    {
      LmnInstrVar atomi;
      LMN_IMS_READ(LmnInstrVar, instr, atomi);

      if (at[atomi] != LMN_ATOM_INT_ATTR)
        return FALSE;
      break;
    }
    case INSTR_ISFLOAT:
    {
      LmnInstrVar atomi;
      LMN_IMS_READ(LmnInstrVar, instr, atomi);

      if(at[atomi] != LMN_ATOM_DBL_ATTR)
        return FALSE;
      break;
    }
    case INSTR_ISINTFUNC:
    {
      LmnInstrVar funci;
      LMN_IMS_READ(LmnInstrVar, instr, funci);

      if(at[funci] != LMN_ATOM_INT_ATTR)
        return FALSE;
      break;
    }
    case INSTR_ISFLOATFUNC:
	  {
      LmnInstrVar funci;
      LMN_IMS_READ(LmnInstrVar, instr, funci);

      if(at[funci] != LMN_ATOM_DBL_ATTR)
        return FALSE;
      break;
    }
    case INSTR_NEWLIST:
    {
      LmnInstrVar listi;
      LMN_IMS_READ(LmnInstrVar, instr, listi);
      wt[listi] = (LmnWord)vec_make_default();
      break;
    }
    case INSTR_ADDTOLIST:
    {
      LmnInstrVar listi, srci;
      LinkObj linko;
      LMN_IMS_READ(LmnInstrVar, instr, listi);
      LMN_IMS_READ(LmnInstrVar, instr, srci);
      /* �����餯ADDTOLIST���ɲä�������Ǥμ���ϥ�󥯤Τ� */
      linko.ap = (LmnAtomPtr)wt[srci];
      linko.pos = (at[srci]);
      vec_add((Vector*)wt[listi], (LmnWord)&linko);
      break;
    }
    case INSTR_IADD:
    {
      LmnInstrVar dstatom, atom1, atom2;
      LMN_IMS_READ(LmnInstrVar, instr, dstatom);
      LMN_IMS_READ(LmnInstrVar, instr, atom1);
      LMN_IMS_READ(LmnInstrVar, instr, atom2);

      wt[dstatom] = (LmnWord)((int)wt[atom1] + (int)wt[atom2]);
      at[dstatom] = LMN_ATOM_INT_ATTR;
      break;
    }
    case INSTR_ISUB:
    {
      LmnInstrVar dstatom, atom1, atom2;
      LMN_IMS_READ(LmnInstrVar, instr, dstatom);
      LMN_IMS_READ(LmnInstrVar, instr, atom1);
      LMN_IMS_READ(LmnInstrVar, instr, atom2);

      wt[dstatom] = (LmnWord)((int)wt[atom1] - (int)wt[atom2]);
      at[dstatom] = LMN_ATOM_INT_ATTR;
      break;
    }
    case INSTR_IMUL:
    {
      LmnInstrVar dstatom, atom1, atom2;
      LMN_IMS_READ(LmnInstrVar, instr, dstatom);
      LMN_IMS_READ(LmnInstrVar, instr, atom1);
      LMN_IMS_READ(LmnInstrVar, instr, atom2);

      wt[dstatom] = (LmnWord)((int)wt[atom1] * (int)wt[atom2]);
      at[dstatom] = LMN_ATOM_INT_ATTR;
      break;
    }
    case INSTR_IDIV:
    {
      LmnInstrVar dstatom, atom1, atom2;
      LMN_IMS_READ(LmnInstrVar, instr, dstatom);
      LMN_IMS_READ(LmnInstrVar, instr, atom1);
      LMN_IMS_READ(LmnInstrVar, instr, atom2);

      wt[dstatom] = (LmnWord)((int)wt[atom1] / (int)wt[atom2]);
      at[dstatom] = LMN_ATOM_INT_ATTR;
      break;
    }
    case INSTR_ILT:
    {
      LmnInstrVar atom1, atom2;
      LMN_IMS_READ(LmnInstrVar, instr, atom1);
      LMN_IMS_READ(LmnInstrVar, instr, atom2);

      if(!((int)wt[atom1] < (int)wt[atom2])) return FALSE;
      break;
    }
    case INSTR_ILE:
    {
      LmnInstrVar atom1, atom2;
      LMN_IMS_READ(LmnInstrVar, instr, atom1);
      LMN_IMS_READ(LmnInstrVar, instr, atom2);

      if(!((int)wt[atom1] <= (int)wt[atom2])) return FALSE;
      break;
    }
    case INSTR_IGT:
    {
      LmnInstrVar atom1, atom2;
      LMN_IMS_READ(LmnInstrVar, instr, atom1);
      LMN_IMS_READ(LmnInstrVar, instr, atom2);

      if(!((int)wt[atom1] > (int)wt[atom2])) return FALSE;
      break;
    }
    case INSTR_IGE:
    {
      LmnInstrVar atom1, atom2;
      LMN_IMS_READ(LmnInstrVar, instr, atom1);
      LMN_IMS_READ(LmnInstrVar, instr, atom2);

      if(!((int)wt[atom1] >= (int)wt[atom2])) return FALSE;
      break;
    }
    case INSTR_IEQ:
    {
      LmnInstrVar atom1, atom2;
      LMN_IMS_READ(LmnInstrVar, instr, atom1);
      LMN_IMS_READ(LmnInstrVar, instr, atom2);

      if(!((int)wt[atom1] == (int)wt[atom2])) return FALSE;
      break;
    }
    case INSTR_INE:
    {
      LmnInstrVar atom1, atom2;
      LMN_IMS_READ(LmnInstrVar, instr, atom1);
      LMN_IMS_READ(LmnInstrVar, instr, atom2);

      if(!((int)wt[atom1] != (int)wt[atom2])) return FALSE;
      break;
    }
    default:
      fprintf(stderr, "interpret: Unknown operation %d\n", op);
      exit(1);
    }
  }
}
