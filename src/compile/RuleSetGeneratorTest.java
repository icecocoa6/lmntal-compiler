/* 
 * ������: 2003/10/24
 * 
 */
package compile;

import runtime.Env;
import runtime.InterpretedRuleset;

/**
 * �¹Ի��ǡ�����¤��̿����ʲ��� foo �ȸƤ֡ˤ��Ѵ����롣
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
		//RuleStructure rs = getTestStructure2();
		Env.p(m);
		RuleSetGenerator.run(m);
		
		Env.p("Membrane with RuleSet.");
		Env.p(m);
		//r.showDetail();
	}
	
	/**
	 * �ǥХå��ѥǡ�����¤��Ĥ��롣
	 * 
	 * v, w, ( v :- w, w )
	 * 
	 * @return RuleStructure
	 */
	static Membrane getTestStructure1() {
		// �롼����ο���� null
		RuleStructure rs = new RuleStructure();
		rs.parent = new Membrane(null);
		
		Membrane m = new Membrane(null);
		
		// �롼��
		RuleStructure r = getTestStructure2();
		
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
	 * �ǥХå��ѥǡ�����¤��Ĥ��롣2
	 * 
	 * ( v :- w, w )
	 * 
	 * @return RuleStructure
	 */
	static RuleStructure getTestStructure2() {
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

