/*
 * ������: 2003/11/18
 *
 */
package compile;

import java.util.*;
import runtime.Env;
import compile.structure.*;
import runtime.InterpretedRuleset;

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
//		Module.genInstruction(root);
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
		// ������ˤ���롼�빽¤��롼�륪�֥������Ȥ˥���ѥ��뤹��
		ArrayList rules = new ArrayList();
		it = mem.rules.listIterator();
		while (it.hasNext()) {
			RuleStructure rs = (RuleStructure)it.next();
			// �롼��α�����ʲ��ˤ���ҥ롼���롼�륻�åȤ˥���ѥ��뤹��
			processMembrane(rs.leftMem); // ������դ�
			processMembrane(rs.rightMem);
			//
			RuleCompiler rc = new RuleCompiler(rs);
			rc.compile();
			rules.add(rc.theRule);
		}
		// ���������롼�륪�֥������ȤΥꥹ�Ȥ�롼�륻�åȡʤΥ��åȡˤ˥���ѥ��뤹��
		if (!rules.isEmpty()) {
			if (Env.fRandom) {
				it = rules.iterator();
				while (it.hasNext()) {
					InterpretedRuleset ruleset = new InterpretedRuleset();
					ruleset.rules.add(it.next());
					//ruleset.compile();
					mem.rulesets.add(ruleset);
				}
			} else {
				InterpretedRuleset ruleset = new InterpretedRuleset();
				it = rules.iterator();
				while (it.hasNext()) {
					ruleset.rules.add(it.next());
				}
				//ruleset.compile();
				mem.rulesets.add(ruleset);
			}
		}	
	}
}
