package runtime;
import java.util.List;

/** �����ƥ�롼�륻�å�
 * <p>todo ���󥹥��󥹤�ï����������Τ�����</p>
 * 
 * �����ƥ�롼�륻�åȤΥ⥸�塼�벽��
 * system_ruleset ���ȥब�������ľ°�Υ롼�륻�åȤϥ����ƥ�롼�륻�åȤǤ��뤳�Ȥˤ��롣
 * 
 * {system_ruleset, (a:-b)}, {{{{{{a}}}}}}    ==> {{{{{{b}}}}}} 
 * 
 * @author n-kato, hara
 */
public final class SystemRuleset extends Ruleset {
	public static InterpretedRuleset ruleset;
	public String toString() {
		return "System Ruleset Object";
	}
	/**
	 * ���ȥ��Ƴ�ƥ��Ȥ�Ԥ����ޥå������Ŭ�Ѥ���
	 * @return �롼���Ŭ�Ѥ�������true
	 */
	public boolean react(Membrane mem, Atom atom) {
		return false;
	}
	/**
	 * ���Ƴ�ƥ��Ȥ�Ԥ����ޥå������Ŭ�Ѥ���
	 * @return �롼���Ŭ�Ѥ�������true
	 */
	public boolean react(Membrane mem) {
		return ruleset.react(mem);
	}
	/** �����ƥ�롼�륻�åȡʤʤ�������SystemRuleset���֥������ȤǤϤʤ��ˤ��֤� */
	public static Ruleset getInstance() {
		return ruleset;
	}
	static {
		ruleset = new InterpretedRuleset();
		// === System Rule #1 ===
		//    1:$outside(B,A), 3:{2:$inside(B,C), 4:$inside(D,C), $p,@p}, 5:$outside(D,E)
		// :-                  3:{                                $p,@p}, A=E.
		Rule rule = new Rule();
		List insts = rule.memMatch;
		// match
		insts.add(new Instruction(Instruction.SPEC,        6,0));
		insts.add(new Instruction(Instruction.FINDATOM,  1,0,Functor.OUTSIDE_PROXY));
		insts.add(new Instruction(Instruction.DEREFATOM, 2,1,0));
		insts.add(new Instruction(Instruction.LOCKMEM,   3,2));
		insts.add(new Instruction(Instruction.DEREFATOM, 4,2,1));
		insts.add(new Instruction(Instruction.FUNC,        4,Functor.INSIDE_PROXY));
		insts.add(new Instruction(Instruction.DEREFATOM, 5,4,0));
		// react
		insts.add(new Instruction(Instruction.REMOVEATOM,  1,0,Functor.OUTSIDE_PROXY));
		insts.add(new Instruction(Instruction.REMOVEATOM,  2,3,Functor.INSIDE_PROXY));
		insts.add(new Instruction(Instruction.REMOVEATOM,  4,3,Functor.INSIDE_PROXY));
		insts.add(new Instruction(Instruction.REMOVEATOM,  5,0,Functor.OUTSIDE_PROXY));
		insts.add(new Instruction(Instruction.LOCALUNIFY,  1,1,5,1,0));
		insts.add(new Instruction(Instruction.UNLOCKMEM,   3)); // TODO �������������ʤ��褦�ˤ���
		insts.add(new Instruction(Instruction.PROCEED));
		ruleset.rules.add(rule);
		//
		// === System Rule #2 ===
		//    1:$outside(A,B), 2:$outside(C,B), 4:{3:$inside(C,D), 5:$inside(A,E), $p,@p}
		// :-                                   4:{                          D=E,  $p,@p}.
		rule = new Rule();
		insts = rule.memMatch;
		// match		
		insts.add(new Instruction(Instruction.SPEC,        6,0));
		insts.add(new Instruction(Instruction.FINDATOM,  1,0,Functor.OUTSIDE_PROXY));
		insts.add(new Instruction(Instruction.DEREFATOM, 2,1,1));
		insts.add(new Instruction(Instruction.FUNC,        2,Functor.OUTSIDE_PROXY));
		insts.add(new Instruction(Instruction.DEREFATOM, 3,2,0));
		insts.add(new Instruction(Instruction.LOCKMEM,   4,3));
		insts.add(new Instruction(Instruction.DEREFATOM, 5,1,0));
		insts.add(new Instruction(Instruction.TESTMEM,     4,5));
		// react
		insts.add(new Instruction(Instruction.REMOVEATOM,  1,0,Functor.OUTSIDE_PROXY));
		insts.add(new Instruction(Instruction.REMOVEATOM,  2,0,Functor.OUTSIDE_PROXY));
		insts.add(new Instruction(Instruction.REMOVEATOM,  3,4,Functor.OUTSIDE_PROXY));
		insts.add(new Instruction(Instruction.REMOVEATOM,  5,4,Functor.OUTSIDE_PROXY));
		insts.add(new Instruction(Instruction.UNIFY,       3,1,5,1,4)); // memo: �ܼ�Ū�˥�⡼�Ȥ�UNIFY
		insts.add(new Instruction(Instruction.UNLOCKMEM,   4)); // �����Ǥϻ���γ�������ɬ��
		insts.add(new Instruction(Instruction.PROCEED));
		ruleset.rules.add(rule);

		////////////////////////////////////////////////////////////
		//
		// �����ɤ�����ѥ���Ǥ���褦�ˤʤ�ޤǤ���ʬ�δ֡�
		// ���Υ����ߥ󥰤��Ȥ߹��ߥ⥸�塼�����ɤ����Ƥ�餦��
		
		if (true) {
			loadBuiltInRules(ruleset);
		}			
		//ruleset.compile();
	}
	/**
	 * ���᥽�åɡ�
	 * ���ꤵ�줿InterpretedRuleset���Ф��ơ�integer�⥸�塼�����������Ƥ��ɲä��뤿��˻��Ѥ���롣
	 * ����ա������黻��̵���ˤ��륪�ץ���󤬤��äƤ�褤��		
	 */
	static Rule buildBinOpRule(String name, int typechecker, int op) {
		// 1:'+'(X,Y,Res), 2:$x[X], 3:$y[Y] :- int($x),int($y), (4:$z)=$x+$y | 5:$z[Res].
		Rule rule = new Rule();
		List insts = rule.memMatch;
		// match		
		insts.add(new Instruction(Instruction.SPEC,        5,0));
		insts.add(new Instruction(Instruction.FINDATOM,  1,0,new Functor(name,3)));
		insts.add(new Instruction(Instruction.DEREFATOM, 2,1,0));
		insts.add(new Instruction(typechecker,             2));
		insts.add(new Instruction(Instruction.DEREFATOM, 3,1,1));
		insts.add(new Instruction(typechecker,             3));
		insts.add(new Instruction(op,                    4,2,3));
		// react
		insts.add(new Instruction(Instruction.DEQUEUEATOM, 1));
		insts.add(new Instruction(Instruction.REMOVEATOM,  1,0));
		insts.add(new Instruction(Instruction.REMOVEATOM,  2,0));
		insts.add(new Instruction(Instruction.REMOVEATOM,  3,0));
		insts.add(new Instruction(Instruction.LOCALADDATOM,  0,4));
		insts.add(new Instruction(Instruction.RELINK,        4,0,1,2));
		insts.add(new Instruction(Instruction.FREEATOM,      1));
		insts.add(new Instruction(Instruction.FREEATOM,      2));
		insts.add(new Instruction(Instruction.FREEATOM,      3));
		insts.add(new Instruction(Instruction.PROCEED));
		return rule;
	}
	/**
	 * ���᥽�åɡ�
	 */
	static Rule buildUnaryOpRule(String name, int typechecker, int op) {
		// 1:float(X,Res), 2:$x[X] :- int($x), (3:$y)=float($x) | 4:$y[Res].
		Rule rule = new Rule();
		List insts = rule.memMatch;
		// match		
		insts.add(new Instruction(Instruction.SPEC,        4,0));
		insts.add(new Instruction(Instruction.FINDATOM,  1,0,new Functor(name,2)));
		insts.add(new Instruction(Instruction.DEREFATOM, 2,1,0));
		insts.add(new Instruction(typechecker,             2));
		insts.add(new Instruction(op,                    3,2));
		// react
		insts.add(new Instruction(Instruction.DEQUEUEATOM, 1));
		insts.add(new Instruction(Instruction.REMOVEATOM,  1,0));
		insts.add(new Instruction(Instruction.REMOVEATOM,  2,0));
		insts.add(new Instruction(Instruction.LOCALADDATOM,  0,3));
		insts.add(new Instruction(Instruction.RELINK,        3,0,1,1));
		insts.add(new Instruction(Instruction.FREEATOM,      1));
		insts.add(new Instruction(Instruction.FREEATOM,      2));
		insts.add(new Instruction(Instruction.PROCEED));
		return rule;
	}
	
	
	static void loadBuiltInRules(InterpretedRuleset ruleset) {
		Rule rule;
		List insts;		
		
		ruleset.rules.add(buildBinOpRule("+",	Instruction.ISINT,Instruction.IADD));
		ruleset.rules.add(buildBinOpRule("-",	Instruction.ISINT,Instruction.ISUB));
		ruleset.rules.add(buildBinOpRule("*",	Instruction.ISINT,Instruction.IMUL));
		ruleset.rules.add(buildBinOpRule("/",	Instruction.ISINT,Instruction.IDIV));
		ruleset.rules.add(buildBinOpRule("mod",	Instruction.ISINT,Instruction.IMOD));

		ruleset.rules.add(buildBinOpRule("+.",	Instruction.ISFLOAT,Instruction.FADD));
		ruleset.rules.add(buildBinOpRule("-.",	Instruction.ISFLOAT,Instruction.FSUB));
		ruleset.rules.add(buildBinOpRule("*.",	Instruction.ISFLOAT,Instruction.FMUL));
		ruleset.rules.add(buildBinOpRule("/.",	Instruction.ISFLOAT,Instruction.FDIV));

		ruleset.rules.add(buildUnaryOpRule("int",  Instruction.ISFLOAT,Instruction.FLOAT2INT));
		ruleset.rules.add(buildUnaryOpRule("float",Instruction.ISINT,  Instruction.INT2FLOAT));
		
		// 1:cp(A,B,C), 2:$n[A] :- unary($n) | $3:$n[B], $4:$n[C].
		// reuse { 2->4 }
		rule = new Rule();
		insts = rule.memMatch;
		// match		
		insts.add(new Instruction(Instruction.SPEC,        4,0));
		insts.add(new Instruction(Instruction.FINDATOM,  1,0,new Functor("cp",3)));
		insts.add(new Instruction(Instruction.DEREFATOM, 2,1,0));
		insts.add(new Instruction(Instruction.ISUNARY,     2));
		// react
		insts.add(new Instruction(Instruction.DEQUEUEATOM, 1));
		insts.add(new Instruction(Instruction.REMOVEATOM,  1,0));
		insts.add(new Instruction(Instruction.COPYATOM,  3,0,2));
		insts.add(new Instruction(Instruction.RELINK,    2,0,1,1));
		insts.add(new Instruction(Instruction.RELINK,    3,0,1,2));
		insts.add(new Instruction(Instruction.FREEATOM,    1));
		insts.add(new Instruction(Instruction.PROCEED));
		ruleset.rules.add(rule);
	}
}
