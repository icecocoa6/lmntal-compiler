/* 
 * ������: 2003/10/24
 * 
 */
package compile;

import java.io.StringReader;

import runtime.Env;
import runtime.InterpretedRuleset;

import compile.parser.*;
import compile.structure.*;

/**
 * �¹Ի��ǡ�����¤����롼�륻�åȤ�����ƥ���
 * 
 * ���ä����ʳ�
 * @author hara
 *
 */
public class RuleSetGeneratorTest {
	
	/**
	 * �ƥ����Ѥᤤ��
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		//test("v");
		//test("v,v,w");
		//test("{}");
		//test("{v}");
		//test("a(X), b(X), c(d)");
		//test("a(X,Y), b(X,Y)");
		//test("(a:-b)");
		//test("(a:-b), (c:-d)");
		test("a(X), { b(X) }");
	}
	
	/**
	 * �ƥ��Ȥ���
	 * @param src
	 */
	public static void test(String src) {
		try {
			// thnx to ���Ľ�Ĺ
			LMNParser lp = new LMNParser(new StringReader(src));
			Membrane m = lp.parse();
			
			//Membrane m = getTestStructure1();
			//Membrane m = getTestStructure2();
			Env.p(m);
			Membrane root = RuleSetGenerator.runStartWithNull(m);
			InterpretedRuleset ir = (InterpretedRuleset)root.ruleset;
			
			Env.p("");
			Env.p("Compiled Membrane :");
			root.showAllRuleset();
			root.showAllRule();
			
			//Env.p("");
			//Env.p("Generated InterpretedRuleset :");
			//Env.p(ir);
			//ir.showDetail();
		} catch (ParseException e) {
			Env.p(e);
		}
	}
	
	/**
	 * �ǥХå����칽¤��Ĥ��롣
	 * 
	 * ( :- v, w, ( v :- w, w ) )
	 * 
	 * @return RuleStructure
	 */
	static Membrane getTestStructure1() {
		// �롼����ο���� null
		RuleStructure rs = new RuleStructure();
		rs.parent = new Membrane(null);
		
		Membrane m = new Membrane(null);
		
		// �롼��
		RuleStructure r = getTestRule();
		
		m.atoms.add( new Atom(m, "v", 0) );
		m.atoms.add( new Atom(m, "w", 0) );
		m.rules.add(r);
		r.parent = m;
		
		rs.leftMem = new Membrane(null);
		rs.rightMem = m;
		
		rs.parent.rules.add(rs);
		return rs.parent;
	}
	
	/**
	 * �ǥХå����칽¤��Ĥ��롣2
	 * 
	 * ( v :- w, w )
	 * 
	 * @return RuleStructure
	 */
	static Membrane getTestStructure2() {
		// �롼��
		RuleStructure rs = getTestRule();
		rs.parent = new Membrane(null);
		
		rs.parent.rules.add(rs);
		return rs.parent;
	}
	
	/**
	 * �ǥХå��ѥ롼�빽¤��Ĥ��롣
	 * 
	 * ( v :- w, w )
	 * 
	 * @return RuleStructure
	 */
	static RuleStructure getTestRule() {
		// �롼����ο���� null
		RuleStructure rs = new RuleStructure();
		Membrane m = new Membrane(null);
		
		// �롼��
		RuleStructure r = new RuleStructure();
		r.leftMem.atoms.add( new Atom(m, "v", 0) );
		r.rightMem.atoms.add( new Atom(m, "w", 0) );
		r.rightMem.atoms.add( new Atom(m, "w", 0) );
		return r;
	}
}

