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
 * TODO RuleSetGenerator�Ϥ�����RulesetCompiler��̾���ѹ�����
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
		RuleStructure rs = new RuleStructure(root);
		rs.leftMem  = new Membrane(null);
		rs.rightMem = m;
		root.rules.add(rs);
		processMembrane(root);
		listupModules(root);
		Env.d("\n=== Modules = \n"+modules+"\n\n");
		return root;
	}
	
	public static InterpretedRuleset run(Membrane m) {
		Env.c("RuleSetGenerator.run");
		processMembrane(m);
		return (InterpretedRuleset)m.ruleset;
	}
	
	/**
	 * Ϳ����줿��γ��ز��ˤ������Ƥ� RuleStructure �ˤĤ��ơ�
	 * �б����� Rule ���������Ƥ�����Υ롼�륻�åȤ��ɲä��롣
	 * @param mem �оݤȤʤ���
	 */
	public static void processMembrane(Membrane mem) {
		Env.c("RuleSetGenerator.processMembrane");
		// ����ˤ���롼���롼�륻�åȤ˥���ѥ��뤹��
		Iterator it = mem.mems.listIterator();
		while (it.hasNext()) {
			Membrane submem = (Membrane)it.next();
			processMembrane(submem);
		}
		// ������ˤ���롼���롼�륻�åȤ˥���ѥ��뤹��
		runtime.Ruleset ruleset = mem.ruleset;
		it = mem.rules.listIterator();
		while (it.hasNext()) {
			RuleStructure rs = (RuleStructure)it.next();
			// �롼��α�����ʲ��ˤ���ҥ롼���롼�륻�åȤ˥���ѥ��뤹��
			processMembrane(rs.leftMem); // ������դ�
			processMembrane(rs.rightMem);
			//
			RuleCompiler rc = new RuleCompiler(rs);
			rc.compile();
			// �� ������ ruleset.add(rc.theRule); �ˤ��������褤
			((runtime.InterpretedRuleset)ruleset).rules.add(rc.theRule);
		}
		// ruleset.compile();
	}
	
	public static Map modules = new HashMap();
	public static void listupModules(Membrane m) {
		//Env.d("listupModules");
		runtime.Functor f = new runtime.Functor("name", 1);
		
		Iterator i;
		i = m.atoms.listIterator();
		while(i.hasNext()) {
			Atom a = (Atom)i.next();
			if(a.functor.equals(f)) {
				Env.d("Module found : "+a.args[0].atom);
				modules.put(a.args[0].atom.mem, a.args[0].buddy.atom.toString());
			}
		}
		i = m.rules.listIterator();
		while(i.hasNext()) {
			RuleStructure rs = (RuleStructure)i.next();
			//Env.d("");
			//Env.d("About rule structure (LEFT): "+rs.leftMem+" of "+rs);
			listupModules(rs.leftMem);
			//Env.d("About rule structure (LEFT): "+rs.rightMem+" of "+rs);
			listupModules(rs.rightMem);
		}
		i = m.mems.listIterator();
		while(i.hasNext()) {
			listupModules((Membrane)i.next());
		}
	}
}
