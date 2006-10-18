package util;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import runtime.Functor;
import runtime.Instruction;
import runtime.InstructionList;
import runtime.InterpretedRuleset;
import runtime.Rule;
import runtime.SymbolFunctor;

import compile.Translator;

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
public final class GlobalSystemRulesetGenerator {
	public static InterpretedRuleset ruleset;
	public static void main(String[] args) {
		ruleset = new InterpretedRuleset();
		// === System Rule #1 ===
		//    1:$outside(B,A), 3:{2:$inside(B,C), 4:$inside(D,C), $p,@p}, 5:$outside(D,E)
		// :-                  3:{                                $p,@p}, A=E.
		Rule rule = new Rule("proxy");
		List insts = rule.memMatch;
		// match
		insts.add(new Instruction(Instruction.SPEC,        1,6));
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
		insts.add(new Instruction(Instruction.UNIFY,       1,1,5,1,0)); //n-kato 2006-09-07
		insts.add(new Instruction(Instruction.UNLOCKMEM,   3)); // ��������������ɬ�פϤʤ���quiet�Ǥ褤��
		insts.add(new Instruction(Instruction.PROCEED));
		ruleset.rules.add(rule);
		//
		// === System Rule #2 ===
		//    1:$outside(A,B), 2:$outside(C,B), 4:{3:$inside(C,D), 5:$inside(A,E), $p,@p}
		// :-                                   4:{                          D=E,  $p,@p}.
		rule = new Rule("proxy");
		insts = rule.memMatch;
		// match		
		insts.add(new Instruction(Instruction.SPEC,        1,6));
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
		insts.add(new Instruction(Instruction.REMOVEATOM,  3,4,Functor.INSIDE_PROXY));
		insts.add(new Instruction(Instruction.REMOVEATOM,  5,4,Functor.INSIDE_PROXY));
		insts.add(new Instruction(Instruction.UNIFY,       3,1,5,1,4)); // memo: �ܼ�Ū�˥�⡼�Ȥ�UNIFY
		insts.add(new Instruction(Instruction.ENQUEUEMEM,  4)); // �����Ǥϻ���γ�������ɬ��
		insts.add(new Instruction(Instruction.UNLOCKMEM,   4));
		insts.add(new Instruction(Instruction.PROCEED));
		ruleset.rules.add(rule);

		////////////////////////////////////////////////////////////
		//
		// ��ʬ�δ֡����Υ����ߥ󥰤��Ȥ߹��ߥ⥸�塼�����ɤ����Ƥ�餦��
		
		if (true) { // �����ɺ�Ŭ���郎��������ޤǤϤ������Ȥ�
			loadBuiltInRules(ruleset);
		}
		else {
			String text = "";
			text += " Res=X+Y      :- Z=X+Y      | Res=Z.    \n";
			text += " Res=X-Y      :- Z=X-Y      | Res=Z.    \n";
			text += " Res=X*Y      :- Z=X*Y      | Res=Z.    \n";
			text += " Res=X/Y      :- Z=X/Y      | Res=Z.    \n";
			text += " Res=X mod Y  :- Z=X mod Y  | Res=Z.    \n";
			text += " Res=X+.Y     :- Z=X+.Y     | Res=Z.    \n";
			text += " Res=X-.Y     :- Z=X-.Y     | Res=Z.    \n";
			text += " Res=X*.Y     :- Z=X*.Y     | Res=Z.    \n";
			text += " Res=X/.Y     :- Z=X/.Y     | Res=Z.    \n";
			text += " Res=+X       :- int(X)     | Res=X.    \n";
			text += " Res=-X       :- Z=-X       | Res=Z.    \n";
			text += " Res=+.X      :- float(X)   | Res=X.    \n";
			text += " Res=-.X      :- Z=-.X      | Res=Z.    \n";
			text += " Res=int(X)   :- Z=int(X)   | Res=Z.    \n";
			text += " Res=float(X) :- Z=float(X) | Res=Z.    \n";
//			text += " cp(X,Y,Z)    :- unary(X)   | Y=X, Z=X. \n";
			compileAndLoadRules(ruleset,text);
		}
		if (false) {
			String text = "";
			text += generateUnaryFloatingFunctionRuleText("sin");
			compileAndLoadRules(ruleset,text);
		}
		try {
			new Translator(ruleset, true).translate();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	static String generateUnaryFloatingFunctionRuleText(String func) {
		String text = " Res=" + func + "(X) :- float(X) | [[/*inline*/";
		text += "double x = ((FloatingFunctor)me.nthAtom(0).getFunctor()).value;";
		text += "double y = Math." + func + "(x);";
		text += "Membrane mem = me.getMem();";
		text += "Atom res = mem.newAtom(new FloatingFunctor(y));";
		text += "mem.relinkAtomArgs(res,0,me,1);";
		text += "mem.removeAtom(me.nthAtom(0));";
		text += "mem.removeAtom(me);]](X,Res).\n";
		return text;
	}
	/** 
	 * LMNtal�ץ����ƥ����Ȥ򥳥�ѥ��뤷���ޤޤ��롼�������ꤷ���롼�륻�åȤ��ɲä��롣
	 * @param ruleset �ɲ���Υ롼�륻�å�
	 * @param text �ʥȥåץ�٥�˥롼�����ޤ��LMNtal�ץ����ƥ����� */
	static void compileAndLoadRules(InterpretedRuleset ruleset, String text) {
		Reader src = new StringReader(text);
		compile.parser.LMNParser lp = new compile.parser.LMNParser(src);
		compile.structure.Membrane m = null;
		try {
			m = lp.parse();
		} catch(Exception e){}
		// todo ���������ɤ����餫���Լ����ʤΤǡ�InterpretedRuleset�ʤΥꥹ�Ȥޤ��Ϥ�������
		// ����ѥ�����칽¤�ˤ�������������Υ᥽�åɤ�RulesetCompiler�˺�롣
		InterpretedRuleset ir = (InterpretedRuleset)compile.RulesetCompiler.compileMembrane(m);
		Rule rule = (Rule)ir.rules.get(0);
		Iterator it = rule.body.iterator();
		while (it.hasNext()) {
			Instruction loadruleset = (Instruction)it.next();
			if (loadruleset.getKind() == Instruction.LOADRULESET) {
				InterpretedRuleset ir2 = (InterpretedRuleset)loadruleset.getArg2();
				ruleset.rules.addAll(ir2.rules);
			}
		}
	}
	/**
	 * ���᥽�åɡ�
	 * ���ꤵ�줿InterpretedRuleset���Ф��ơ�integer�⥸�塼�����������Ƥ��ɲä��뤿��˻��Ѥ���롣
	 * ����ա������黻��̵���ˤ��륪�ץ���󤬤��äƤ�褤��	
	 */
	static Rule buildBinOpRule(String name, int typechecker, int op) {
		// 1:'+'(X,Y,Res), 2:$x[X], 3:$y[Y] :- int($x),int($y), (4:$z)=$x+$y | 5:$z[Res].
		Rule rule = new Rule(name);
		rule.bodyLabel = new InstructionList();
		rule.body = rule.bodyLabel.insts;
		
		List insts = rule.memMatch, insts2 = rule.body;
		// match		
		insts.add(new Instruction(Instruction.SPEC,        1,5));
		insts.add(new Instruction(Instruction.FINDATOM,  1,0,new SymbolFunctor(name,3)));
		insts.add(new Instruction(Instruction.DEREFATOM, 2,1,0));
		insts.add(new Instruction(typechecker,             2));
		insts.add(new Instruction(Instruction.DEREFATOM, 3,1,1));
		insts.add(new Instruction(typechecker,             3));
		insts.add(new Instruction(op,                    4,2,3));
		ArrayList mems = new ArrayList();
		mems.add(new Integer(0));
		ArrayList atoms = new ArrayList();
		atoms.add(new Integer(1));
		atoms.add(new Integer(2));
		atoms.add(new Integer(3));
		atoms.add(new Integer(4));
		insts.add(Instruction.jump(rule.bodyLabel, mems, atoms, new ArrayList()));
		// react
		insts2.add(new Instruction(Instruction.SPEC,        5,5));
		insts2.add(new Instruction(Instruction.COMMIT));
		insts2.add(new Instruction(Instruction.DEQUEUEATOM, 1));
		insts2.add(new Instruction(Instruction.REMOVEATOM,  1,0));
		insts2.add(new Instruction(Instruction.REMOVEATOM,  2,0));
		insts2.add(new Instruction(Instruction.REMOVEATOM,  3,0));
		insts2.add(new Instruction(Instruction.ADDATOM,  0,4));
		insts2.add(new Instruction(Instruction.RELINK,        4,0,1,2,0));
		insts2.add(new Instruction(Instruction.FREEATOM,      1));
		insts2.add(new Instruction(Instruction.FREEATOM,      2));
		insts2.add(new Instruction(Instruction.FREEATOM,      3));
		insts2.add(new Instruction(Instruction.PROCEED));
		return rule;
	}
	static Rule f() {
		Rule rule = new Rule();
		List insts = rule.memMatch;
		insts.add(new Instruction(Instruction.SPEC,      1, 0));
		insts.add(new Instruction(Instruction.FINDATOM,  1, 0, new SymbolFunctor("+",2)));
		insts.add(new Instruction(Instruction.DEREFATOM, 2, 1, 0));
		insts.add(new Instruction(Instruction.ISINT,     2));
		insts.add(new Instruction(Instruction.GETFUNC,   3, 2));
		insts.add(new Instruction(Instruction.ALLOCATOMINDIRECT, 4, 3));

		return rule;
	}
	
	/**
	 * ���᥽�åɡ�	
	 */
	static Rule buildUnaryPlusRule(String name, int typechecker) {
		Rule rule = new Rule(name);
		rule.bodyLabel = new InstructionList();
		rule.body = rule.bodyLabel.insts;

		List insts = rule.memMatch, insts2 = rule.body;
		// match		
		insts.add(new Instruction(Instruction.SPEC,        1,5));
		insts.add(new Instruction(Instruction.FINDATOM,  1,0,new SymbolFunctor(name,2)));
		insts.add(new Instruction(Instruction.DEREFATOM, 2,1,0));
		insts.add(new Instruction(typechecker,             2));
		insts.add(new Instruction(Instruction.GETFUNC,   4,2));
		insts.add(new Instruction(Instruction.ALLOCATOMINDIRECT, 3,4));

		ArrayList mems = new ArrayList();
		mems.add(new Integer(0));
		ArrayList atoms = new ArrayList();
		atoms.add(new Integer(1));
		atoms.add(new Integer(2));
		atoms.add(new Integer(3));
		insts.add(Instruction.jump(rule.bodyLabel, mems, atoms, new ArrayList()));
		// react
		insts2.add(new Instruction(Instruction.SPEC,        4,4));
		insts2.add(new Instruction(Instruction.COMMIT));
		insts2.add(new Instruction(Instruction.DEQUEUEATOM, 1));
		insts2.add(new Instruction(Instruction.REMOVEATOM,  1,0));
		insts2.add(new Instruction(Instruction.REMOVEATOM,  2,0));
		insts2.add(new Instruction(Instruction.ADDATOM,  0,3));
		insts2.add(new Instruction(Instruction.RELINK,        3,0,1,1,0));
		insts2.add(new Instruction(Instruction.FREEATOM,      1));
		insts2.add(new Instruction(Instruction.FREEATOM,      2));
		insts2.add(new Instruction(Instruction.PROCEED));
		return rule;
	}

	/**
	 * ���᥽�åɡ�	
	 */
	static Rule buildUnaryOpRule(String name, int typechecker, int op) {
		// 1:float(X,Res), 2:$x[X] :- int($x), (3:$y)=float($x) | 4:$y[Res].
		Rule rule = new Rule(name);
		rule.bodyLabel = new InstructionList();
		rule.body = rule.bodyLabel.insts;

		List insts = rule.memMatch, insts2 = rule.body;
		// match		
		insts.add(new Instruction(Instruction.SPEC,        1,4));
		insts.add(new Instruction(Instruction.FINDATOM,  1,0,new SymbolFunctor(name,2)));
		insts.add(new Instruction(Instruction.DEREFATOM, 2,1,0));
		insts.add(new Instruction(typechecker,             2));
		insts.add(new Instruction(op,                    3,2));

		ArrayList mems = new ArrayList();
		mems.add(new Integer(0));
		ArrayList atoms = new ArrayList();
		atoms.add(new Integer(1));
		atoms.add(new Integer(2));
		atoms.add(new Integer(3));
		insts.add(Instruction.jump(rule.bodyLabel, mems, atoms, new ArrayList()));
		// react
		insts2.add(new Instruction(Instruction.SPEC,        4,4));
		insts2.add(new Instruction(Instruction.COMMIT));
		insts2.add(new Instruction(Instruction.DEQUEUEATOM, 1));
		insts2.add(new Instruction(Instruction.REMOVEATOM,  1,0));
		insts2.add(new Instruction(Instruction.REMOVEATOM,  2,0));
		insts2.add(new Instruction(Instruction.ADDATOM,  0,3));
		insts2.add(new Instruction(Instruction.RELINK,        3,0,1,1,0));
		insts2.add(new Instruction(Instruction.FREEATOM,      1));
		insts2.add(new Instruction(Instruction.FREEATOM,      2));
		insts2.add(new Instruction(Instruction.PROCEED));
		return rule;
	}
	
	/** */
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

		ruleset.rules.add(buildUnaryPlusRule("+",  Instruction.ISINT));
		ruleset.rules.add(buildUnaryPlusRule("+.", Instruction.ISFLOAT));
		ruleset.rules.add(buildUnaryOpRule("-",    Instruction.ISINT,  Instruction.INEG));
		ruleset.rules.add(buildUnaryOpRule("-.",   Instruction.ISFLOAT,Instruction.FNEG));
		ruleset.rules.add(buildUnaryOpRule("int",  Instruction.ISFLOAT,Instruction.FLOAT2INT));
		ruleset.rules.add(buildUnaryOpRule("float",Instruction.ISINT,  Instruction.INT2FLOAT));
		
//		// 1:cp(A,B,C), 2:$n[A] :- unary($n) | $3:$n[B], $4:$n[C].
//		// reuse { 2->4 }
//		rule = new Rule();
//		insts = rule.memMatch;
//		// match		
//		insts.add(new Instruction(Instruction.SPEC,        1,4));
//		insts.add(new Instruction(Instruction.FINDATOM,  1,0,new SymbolFunctor("cp",3)));
//		insts.add(new Instruction(Instruction.DEREFATOM, 2,1,0));
//		insts.add(new Instruction(Instruction.ISUNARY,     2));
//		// react
//		insts.add(new Instruction(Instruction.DEQUEUEATOM, 1));
//		insts.add(new Instruction(Instruction.REMOVEATOM,  1,0));
//		insts.add(new Instruction(Instruction.COPYATOM,  3,0,2));
//		insts.add(new Instruction(Instruction.RELINK,    2,0,1,1,0));
//		insts.add(new Instruction(Instruction.RELINK,    3,0,1,2,0));
//		insts.add(new Instruction(Instruction.FREEATOM,    1));
//		insts.add(new Instruction(Instruction.PROCEED));
//		ruleset.rules.add(rule);
	}
}