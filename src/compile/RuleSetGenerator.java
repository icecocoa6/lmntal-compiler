/*
 * ������: 2003/11/18
 *
 */
package compile;

import java.util.*;
import runtime.Env;
import runtime.Instruction;
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
	 * Ϳ����줿�칽¤����������롼��1�Ĥ��������Ǥ˻�������������롣
	 * ������Τ˸����ȡ�Ϳ����줿�칽¤���б��������1�������������react�᥽�åɤ�
	 * ��������롼�륻�åȤ���ä��칽¤���������롣
	 * �᥽�åɼ¹��桢�칽¤�����ˤ���롼�빽¤���롼�륻�åȤ˥���ѥ��뤵��롣
	 * @param m �칽¤
	 * @return ���������롼�륻�åȤ�����칽¤
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
		fixupLoadRuleset(root);
		Env.d("\n=== Modules = \n"+modules+"\n\n");
		return root;
	}
	
//	public static InterpretedRuleset run(Membrane m) {
//		Env.c("RuleSetGenerator.run");
//		processMembrane(m);
//		return (InterpretedRuleset)m.ruleset;
//	}
	
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
		ArrayList rules = new ArrayList();
//		runtime.Ruleset ruleset = mem.ruleset;
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
			rules.add(rc.theRule);
//			((runtime.InterpretedRuleset)ruleset).rules.add(rc.theRule);
		}
		if (Env.fRandom) {
			it = rules.iterator();
			while (it.hasNext()) {
				runtime.Ruleset ruleset = new runtime.InterpretedRuleset();
				((runtime.InterpretedRuleset)ruleset).rules.add(it.next());
				//ruleset.compile();
				mem.rulesets.add(ruleset);
			}
		} else {
			if (rules.size() > 0) {
				runtime.Ruleset ruleset = new runtime.InterpretedRuleset();
				it = rules.iterator();
				while (it.hasNext()) {
					((runtime.InterpretedRuleset)ruleset).rules.add(it.next());
				}
				//ruleset.compile();
				mem.rulesets.add(ruleset);
			}
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
				modules.put(a.args[0].buddy.atom.functor.getName(), a.args[0].atom.mem);
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
	public static void fixupLoadRuleset(Membrane m) {
		//Env.d("fixupLoadRuleset");
		
		Iterator i;
		i = ((InterpretedRuleset)m.ruleset).rules.listIterator();
		while(i.hasNext()) {
			runtime.Rule rule = (runtime.Rule)i.next();
			ListIterator ib = rule.body.listIterator();
			while(ib.hasNext()) {
				Instruction inst = (Instruction)ib.next();
				// �����ʤ���
				if(inst.getKind()==Instruction.LOADRULESET && inst.getArg2() instanceof String) {
					//Env.p("module solved : "+modules.get(inst.getArg2()));
					ib.set(new Instruction(Instruction.LOADRULESET, inst.getIntArg1(), 
						((Membrane)modules.get(inst.getArg2())).ruleset ));
				}
			}
		}
		i = m.mems.listIterator();
		while(i.hasNext()) {
			fixupLoadRuleset((Membrane)i.next());
		}
	}
}
