/* 
 * ������: 2003/10/24
 * 
 */
package compile;

import runtime.Env;
import runtime.InterpretedRuleset;

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
		Membrane m = getTestStructure1();
		//Membrane m = getTestStructure2();
		Env.p(m);
		InterpretedRuleset ir = RuleSetGenerator.run(m);
		
		Env.p("");
		Env.p("Generated InterpretedRuleset :");
		Env.p(ir);
		ir.showDetail();
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

