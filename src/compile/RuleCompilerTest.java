/*
 * ������: 2003/10/24
 *
 */
package compile;

import java.util.*;
import runtime.Env;

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
		Membrane m = getTestStructure();
		Env.p(m);
		RuleCompiler rc = new RuleCompiler(m);
		rc.compile();
	}
	
	/**
	 * �ǥХå��ѥǡ�����¤��Ĥ��롣
	 * 
	 * v, w, ( v :- w, w )
	 * 
	 * @return �ޤ�
	 */
	static Membrane getTestStructure() {
		// �롼����ο���� null
		Membrane m = new Membrane(null);
		
		// �롼��
		RuleStructure r = new RuleStructure();
		r.leftMem.atoms.add( new Atom(m, "v", 0) );
		r.rightMem.atoms.add( new Atom(m, "w", 0) );
		r.rightMem.atoms.add( new Atom(m, "w", 0) );
		
		m.atoms.add( new Atom(m, "v", 0) );
		m.atoms.add( new Atom(m, "w", 0) );
		m.rules.add(r);
		return m;
	}
}

