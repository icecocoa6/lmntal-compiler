/*
 * ������: 2003/11/18
 *
 */
package compile;

import java.util.*;
import runtime.Env;
import runtime.InterpretedRuleset;
import compile.structure.*;

/**
 * �롼�륻�åȤ����������֤���
 * @author hara
 * 
 */
public class RuleSetGenerator {
	
	/**
	 * Ϳ����줿�����������롼�����ä�����ꡢ
	 * ����ľ°�����Ƥ� RuleStructure �ˤĤ��ơ�
	 * �б����� Rule ���������Ƥ�����Υ롼�륻�åȤ��ɲä��롣
	 * 
	 * @param m �оݤȤʤ���
	 * @return ���ꤷ����Υ롼�륻�å�
	 */
	public static Membrane runStartWithNull(Membrane m) {
		Env.c("RuleSetGenerator.runStartWithNull");
		// ��������������
		Membrane root = new Membrane(null);
		RuleStructure rs = new RuleStructure();
		rs.leftMem  = new Membrane(null);
		rs.rightMem = m;
		root.rules.add(rs);
		rs.parent = root;
		processMembrane(root);
		listupModules(root);
		Env.p("\n=== Modules = \n"+modules+"\n\n");
		return root;
	}
	
	public static InterpretedRuleset run(Membrane m) {
		Env.c("RuleSetGenerator.run");
		processMembrane(m);
		return (InterpretedRuleset)m.ruleset;
	}
	
	/**
	 * Ϳ����줿��ľ°�����Ƥ� RuleStructure �ˤĤ��ơ�
	 * �б����� Rule ���������Ƥ�����Υ롼�륻�åȤ��ɲä��롣
	 * @param m �оݤȤʤ���
	 */
	public static void processMembrane(Membrane m) {
		Env.c("RuleSetGenerator.processMembrane");
		
		Iterator i = m.rules.listIterator();
		while(i.hasNext()) {
			RuleStructure rs = (RuleStructure)i.next();
			RuleCompiler rc = new RuleCompiler(rs);
			rc.compile();
		}
	}
	
	public static Map modules = new HashMap();
	public static void listupModules(Membrane m) {
		Env.p("listupModules");
		Iterator i;
		i = m.atoms.listIterator();
		while(i.hasNext()) {
			Atom a = (Atom)i.next();
			runtime.Functor f = new runtime.Functor("name", 1);
			if(a.functor.equals(f)) {
				Env.p("Module found : "+a.args[0].atom);
				modules.put(a.args[0].atom.mem, a.args[0].atom.toString());
			}
		}
		i = m.rules.listIterator();
		while(i.hasNext()) {
			RuleStructure rs = (RuleStructure)i.next();
			//Env.p("");
			//Env.p("About rule structure (LEFT): "+rs.leftMem+" of "+rs);
			listupModules(rs.leftMem);
			//Env.p("About rule structure (LEFT): "+rs.rightMem+" of "+rs);
			listupModules(rs.rightMem);
		}
		i = m.mems.listIterator();
		while(i.hasNext()) {
			listupModules((Membrane)i.next());
		}
	}
}
