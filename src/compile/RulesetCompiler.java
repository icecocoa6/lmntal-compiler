/*
 * ������: 2003/11/18
 *
 */
package compile;

import java.util.*;
import runtime.Env;
import compile.structure.*;

/**
 * �롼�륻�åȤ����������֤���
 * @author hara
 * 
 */
public class RulesetCompiler {
	
	/**
	 * Ϳ����줿�칽¤����������롼��1�Ĥ��������Ǥ˻�������������롣
	 * ������Τ˸����ȡ�Ϳ����줿�칽¤���б��������1�������������react�᥽�åɤ�
	 * ��������롼�륻�åȤ���ä��칽¤���������롣
	 * �᥽�åɼ¹��桢�칽¤�����ˤ���롼�빽¤���롼�륻�åȤ˥���ѥ��뤵��롣
	 * @param m �칽¤
	 * @return ���������롼�륻�åȤ�����칽¤
	 */
	public static Membrane runStartWithNull(Membrane m) {
		Env.c("RulesetGenerator.runStartWithNull");
		// ��������������
		Membrane root = new Membrane(null);
		RuleStructure rs = new RuleStructure(root);
		rs.leftMem  = new Membrane(null);
		rs.rightMem = m;
		root.rules.add(rs);
		processMembrane(root);
		Module.genInstruction(root);
		return root;
	}
	
//	public static InterpretedRuleset run(Membrane m) {
//		Env.c("RulesetCompiler.run");
//		processMembrane(m);
//		return (InterpretedRuleset)m.ruleset;
//	}
	
	/**
	 * Ϳ����줿��γ��ز��ˤ������Ƥ� RuleStructure �ˤĤ��ơ�
	 * �б����� Rule ���������Ƥ�����Υ롼�륻�åȤ��ɲä��롣
	 * @param mem �оݤȤʤ���
	 */
	public static void processMembrane(Membrane mem) {
		Env.c("RulesetCompiler.processMembrane");
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
	
}
