/*
 * �쐬��: 2003/10/24
 *
 */
package runtime;

import java.util.*;

/**
 * abterms �𖽗ߗ�i���� foo �ƌĂԁj�ɕϊ�����B
 * 
 * ��������i�K
 * @author hara
 *
 */
public class RuleCompilerTest {
	/**
	 * �e�X�g�p�߂���
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		/**
		 * v, w, ( v :- w, w )
		 * [
		 *     [:name, "v"], 
		 *     [:name, "w"], 
		 *     [:name, ":-", 
		 *         [:name, "v"], 
		 *         [:name, ",", [:name, "w"], [:name, "w"] ]
		 *     ]
		 * ]
		 */
		List ab=genTestAbterms();
		Env.p(ab);
		
		// �ŏ��ɁA������Ԃ̃u�c�𐶐�����悤�ȃ��[���𐶐�����B
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
}
