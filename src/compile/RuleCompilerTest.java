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

/* �Ť�������
	public static void main(String[] args) {
		List ab=genTestAbterms();
		Env.p(ab);
		
		// �ǽ�ˡ�������֤Υ֥Ĥ���������褦�ʥ롼����������롣
		// ( :- world)
		RuleCompiler rc = new RuleCompiler();
		rc.loadProc(rc.r, ab);
		rc.simplify();
		
		Rule r = rc.compile();
		
		// ����
		r.react(Env.rootMembrane);
	}

	static List gen(String s) {
		List l=new ArrayList();
		l.add(new Integer(Instruction.NAME));
		l.add(s);
		return l;
	}
	static List genTestAbterms() {
		List ab=new ArrayList();
		
		ab.add(gen("v"));
		ab.add(gen("w"));
		
		List l=gen(":-");
		l.add(gen("v"));
		List ll=gen(",");
		ll.add(gen("w"));
		ll.add(gen("w"));
		l.add(ll);
		
		ab.add(l);
		return ab;
	}
*/
