/*
 * ������: 2003/11/18
 *
 */
package compile;

import java.util.*;
import runtime.Env;
import runtime.InlineUnit;
import runtime.Rule;
import runtime.Ruleset;
import runtime.SystemRuleset;
import compile.structure.*;
import runtime.InterpretedRuleset;

/**
 * �롼�륻�åȤ����������֤���
 * @author hara
 * 
 */
public class RulesetCompiler {
	/**
	 * Ϳ����줿�칽¤����������react�᥽�åɤ��������롼�륻�åȤ��������롣
	 * �᥽�åɼ¹��桢�칽¤�����ˤ���롼�빽¤���Ƶ�Ū�˥롼�륻�åȤ˥���ѥ��뤵��롣
	 * 
	 * @param unitName �ե�����̾
	 * @param m �칽¤
	 * @return (:-m)�Ȥ����롼��1�Ĥ�������ʤ�롼�륻�å�
	 */
	public static Ruleset compileMembrane(Membrane m, String unitName) {
		return (Ruleset)compileMembraneToGeneratingMembrane(m, unitName).rulesets.get(0);
	}
	
	public static Ruleset compileMembrane(Membrane m) {
		return compileMembrane(m, InlineUnit.DEFAULT_UNITNAME);
	}
	
	/**
	 * Ϳ����줿�칽¤����������롼��1�Ĥ��������Ǥ˻�������������롣
	 * ������Τ˸����ȡ�Ϳ����줿�칽¤�����Ƥ��б�����ץ�����1�������������react�᥽�åɤ�
	 * ��������롼�륻�åȤ�ͣ��Υ롼�륻�åȤȤ��ƻ����칽¤���������롣
	 * �᥽�åɼ¹��桢�칽¤�����ˤ���롼�빽¤���Ƶ�Ū�˥롼�륻�åȤ˥���ѥ��뤵��롣
	 * @param m �칽¤
	 * @return ���������롼�륻�åȤ�����칽¤
	 */
	protected static Membrane compileMembraneToGeneratingMembrane(Membrane m) {
		return compileMembraneToGeneratingMembrane(m, InlineUnit.DEFAULT_UNITNAME);
	}
	protected static Membrane compileMembraneToGeneratingMembrane(Membrane m, String unitName) {
		Env.c("RulesetGenerator.runStartWithNull");
		// ��������������
		Membrane root = new Membrane(null);
		RuleStructure rs = new RuleStructure(root);
		rs.fSuppressEmptyHeadWarning = true;
		rs.leftMem  = new Membrane(null);
		rs.rightMem = m;
		root.rules.add(rs);
		processMembrane(root, unitName);
		Module.resolveModules(root);
		return root;
	}
	
	/**
	 * Ϳ����줿��γ��ز��ˤ������Ƥ� RuleStructure �ˤĤ��ơ�
	 * �б����� Rule ���������Ƥ�����Υ롼�륻�åȤ��ɲä��롣
	 * <p>�롼�����礦��1�Ļ�����ˤϤ��礦��1�ĤΥ롼�륻�åȤ��ɲä���롣
	 * @param mem �оݤȤʤ���
	 */
	public static void processMembrane(Membrane mem) {
		processMembrane(mem, InlineUnit.DEFAULT_UNITNAME);
	}
	public static void processMembrane(Membrane mem, String unitName) {
		Env.c("RulesetCompiler.processMembrane");
		// ����ˤ���롼���롼�륻�åȤ˥���ѥ��뤹��
		Iterator it = mem.mems.listIterator();
		while (it.hasNext()) {
			Membrane submem = (Membrane)it.next();
			processMembrane(submem, unitName);
		}
		// ������ˤ���롼�빽¤��롼�륪�֥������Ȥ˥���ѥ��뤹��
		ArrayList rules = new ArrayList();
		it = mem.rules.listIterator();
		while (it.hasNext()) {
			RuleStructure rs = (RuleStructure)it.next();
			// �롼��α�����ʲ��ˤ���ҥ롼���롼�륻�åȤ˥���ѥ��뤹��
			processMembrane(rs.leftMem, unitName); // ������դ�
			processMembrane(rs.rightMem, unitName);
			//
			RuleCompiler rc = new RuleCompiler(rs, unitName);
			rc.compile();
			rules.add(rc.theRule);
		}
		// ���������롼�륪�֥������ȤΥꥹ�Ȥ�롼�륻�åȡʤΥ��åȡˤ˥���ѥ��뤹��
		InterpretedRuleset ruleset;
		if (!rules.isEmpty()) {
			if (Env.shuffle >= Env.SHUFFLE_RULES) {
				it = rules.iterator();
				while (it.hasNext()) {
					ruleset = new InterpretedRuleset();
					ruleset.rules.add(it.next());
					//ruleset.compile();
					mem.rulesets.add(ruleset);
				}
			} else {
				ruleset = new InterpretedRuleset();
				it = rules.iterator();
				while (it.hasNext()) {
					ruleset.rules.add(it.next());
				}
				//ruleset.compile();
				mem.rulesets.add(ruleset);
			}
		}
		// ɬ�פʤ饷���ƥ�롼�륻�åȤ���Ͽ
		if(mem.is_system_ruleset) {
			//Env.p("Use system_ruleset "+mem);
			Iterator ri = mem.rulesets.iterator();
			while(ri.hasNext()) {
				InterpretedRuleset ir = (InterpretedRuleset)ri.next();
				Iterator rii = ir.rules.iterator();
				while(rii.hasNext()) {
					Rule r = (Rule)rii.next();
					SystemRuleset.ruleset.rules.add(r);
				}
			}
		}
	}
}
