/* 
 * ������: 2003/10/24
 * 
 */
package compile;

import java.io.StringReader;
import java.util.List;

import javax.swing.JOptionPane;

import runtime.Env;

import compile.parser.*;
import compile.structure.*;

/**
 * �¹Ի��ǡ�����¤����롼�륻�åȤ�����ƥ���
 * 
 * ���ä����ʳ�
 * @author hara
 *
 */
public class RuleSetCompilerTest {
	
	public static void gui() {
		Env.d( JOptionPane.showInputDialog(null, "Hello, Verno") );
	}
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
		//test("a(X), { b(X) }");
		gui();
	}
	
	/**
	 * �ƥ��Ȥ���
	 * @param src
	 */
	public static List test(String src) {
		try {
			// thnx to ���Ľ�Ĺ
			LMNParser lp = new LMNParser(new StringReader(src));
			Membrane m = lp.parse();
			
			//Membrane m = getTestStructure1();
			//Membrane m = getTestStructure2();
			Env.d(m);
			Membrane root = RulesetCompiler.runStartWithNull(m);
			List ir = root.rulesets;
			
			Env.d("");
			Env.d("Compiled Membrane :");
			root.showAllRuleset();
			root.showAllRule();
			
			return ir;
			
			//Env.d("");
			//Env.d("Generated InterpretedRuleset :");
			//Env.d(ir);
			//ir.showDetail();
		} catch (ParseException e) {
			Env.d(e);
		}
		return null;
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
		RuleStructure rs = new RuleStructure(new Membrane(null));
		
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
		RuleStructure rs = new RuleStructure(null);
		Membrane m = new Membrane(null);
		
		// �롼��
		RuleStructure r = new RuleStructure(null);
		r.leftMem.atoms.add( new Atom(m, "v", 0) );
		r.rightMem.atoms.add( new Atom(m, "w", 0) );
		r.rightMem.atoms.add( new Atom(m, "w", 0) );
		return r;
	}
}

