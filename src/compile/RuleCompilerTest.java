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
public class RuleCompilerTest {
	/**
	 * �ƥ����Ѥᤤ��
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		RuleStructure rs = getTestStructure();
		Env.p(rs);
		RuleCompiler rc = new RuleCompiler(rs);
		rc.simplify();
		InterpretedRuleset r = rc.compile();
	}
	
	/**
	 * �ǥХå��ѥǡ�����¤��Ĥ��롣
	 * 
	 * v, w, ( v :- w, w )
	 * 
	 * @return �ޤ�
	 */
	static RuleStructure getTestStructure() {
		// �롼����ο���� null
		RuleStructure rs = new RuleStructure();
		Membrane m = new Membrane(null);
		
		// �롼��
		RuleStructure r = new RuleStructure();
		r.leftMem.atoms.add( new Atom(m, "v", 0) );
		r.rightMem.atoms.add( new Atom(m, "w", 0) );
		r.rightMem.atoms.add( new Atom(m, "w", 0) );
		
		m.atoms.add( new Atom(m, "v", 0) );
		m.atoms.add( new Atom(m, "w", 0) );
		m.rules.add(r);
		
		rs.leftMem = new Membrane(null);
		rs.rightMem = m;
		return rs;
	}
}

