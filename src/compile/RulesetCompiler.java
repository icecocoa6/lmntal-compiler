/*
 * ������: 2003/11/18
 *
 */
package compile;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import runtime.Env;
import runtime.InlineUnit;
import runtime.InterpretedRuleset;
import runtime.MergedBranchMap;
import runtime.Rule;
import runtime.Ruleset;
import runtime.SystemRulesets;

import compile.parser.LMNParser;
import compile.parser.ParseException;
import compile.structure.Atom;
import compile.structure.Membrane;
import compile.structure.RuleStructure;

/**
 * �롼�륻�åȤ����������֤���
 * @author hara
 * 
 */
public class RulesetCompiler
{
	private static boolean recursived = false;

	/**
	 * Ϳ����줿�칽¤����������react�᥽�åɤ��������롼�륻�åȤ��������롣
	 * �᥽�åɼ¹��桢�칽¤�����ˤ���롼�빽¤���Ƶ�Ū�˥롼�륻�åȤ˥���ѥ��뤵��롣
	 * 
	 * @param unitName �ե�����̾
	 * @param m �칽¤
	 * @return (:-m)�Ȥ����롼��1�Ĥ�������ʤ�롼�륻�å�
	 */
	public static Ruleset compileMembrane(Membrane m, String unitName)
	{
		return compileMembraneToGeneratingMembrane(m, unitName).rulesets.get(0);
	}

	public static Ruleset compileMembrane(Membrane m)
	{
		return compileMembrane(m, InlineUnit.DEFAULT_UNITNAME);
	}

	/**
	 * Ϳ����줿�칽¤����������롼��1�Ĥ��������Ǥ˻�������������롣
	 * ������Τ˸����ȡ�Ϳ����줿�칽¤�����Ƥ��б�����ץ�����1�������������react�᥽�åɤ�
	 * ��������롼�륻�åȤ�ͣ��Υ롼�륻�åȤȤ��ƻ����칽¤���������롣
	 * �᥽�åɼ¹��桢�칽¤�����ˤ���롼�빽¤���Ƶ�Ū�˥롼�륻�åȤ˥���ѥ��뤵��롣
	 * @param m �칽¤
	 * @param unitName
	 * @return ���������롼�륻�åȤ�����칽¤
	 */
	protected static Membrane compileMembraneToGeneratingMembrane(Membrane m, String unitName)
	{
		Env.c("RulesetGenerator.runStartWithNull");

		// �����Х�롼����
		Membrane root = new Membrane(null);

		// �����¤����������롼��
		RuleStructure rs = new RuleStructure(root, "(initial rule)");
		rs.fSuppressEmptyHeadWarning = true;
		rs.leftMem  = new Membrane(null);
		rs.rightMem = m;
		root.rules.add(rs);

		processMembrane(root, unitName);

		if (Env.fUseSourceLibrary)
		{
			Module.resolveModules(root);
		}
		return root;
	}

	/**
	 * Ϳ����줿��γ��ز��ˤ������Ƥ� RuleStructure �ˤĤ��ơ�
	 * �б����� Rule ���������Ƥ�����Υ롼�륻�åȤ��ɲä��롣
	 * <p>�롼�����礦��1�Ļ�����ˤϤ��礦��1�ĤΥ롼�륻�åȤ��ɲä���롣
	 * @param mem �оݤȤʤ���
	 */
	public static void processMembrane(Membrane mem)
	{
		processMembrane(mem, InlineUnit.DEFAULT_UNITNAME);
	}

	/**
	 * Ϳ����줿��γ��ز��ˤ������Ƥ� {@code RuleStructure} �ˤĤ��ơ��б����� {@code Rule} ���������Ƥ�����Υ롼�륻�åȤ��ɲä��롣
	 * <p>�롼�����礦��1�Ļ�����ˤϤ��礦��1�ĤΥ롼�륻�åȤ��ɲä���롣</p>
	 * @param mem �оݤȤʤ���
	 */
	public static void processMembrane(Membrane mem, String unitName)
	{
		// ����ˤ���롼���롼�륻�åȤ˥���ѥ��뤹��
		for (Membrane submem : mem.mems)
		{
			processMembrane(submem, unitName);
		}

		List<Rule> rules = new ArrayList<Rule>();

		// ������ˤ���롼�빽¤��롼�륪�֥������Ȥ˥���ѥ��뤹��
		for (RuleStructure rs : mem.rules)
		{
			// �롼��α�����ʲ��ˤ���ҥ롼���롼�륻�åȤ˥���ѥ��뤹��
			processMembrane(rs.leftMem, unitName); // ������դ�
			processMembrane(rs.rightMem, unitName);

			RuleCompiler rc = null;
			try
			{
				rc = new RuleCompiler(rs, unitName);
				rc.compile();
				//2006.1.22 Rule�˹��ֹ���Ϥ� by inui
				rc.theRule.lineno = rs.lineno;
			}
			catch (CompileException e)
			{
				Env.p("    in " + rs.toString() + "\n");
			}
			rules.add(rc.theRule);
		}

		// �Ԥ߾夲��Ԥ�
		Merger merger = new Merger();
		MergedBranchMap mbm = null;
		MergedBranchMap systemmbm = null;
		if (Optimizer.fMerging)
		{
			mbm = merger.Merging(rules, false);
			merger.clear();
			systemmbm = merger.createSystemRulesetsMap();
		}

		// ���������롼�륪�֥������ȤΥꥹ�Ȥ�롼�륻�åȡʤΥ��åȡˤ˥���ѥ��뤹��
		if (!rules.isEmpty())
		{
			InterpretedRuleset ruleset = new InterpretedRuleset();
			for (Rule r : rules)
			{
				ruleset.rules.add(r);
			}
			ruleset.branchmap = mbm;
			ruleset.systemrulemap = systemmbm;
			Ruleset compiledRuleset = compileRuleset(ruleset);
			mem.rulesets.add(ruleset);
		}
		// ɬ�פʤ饷���ƥ�롼�륻�åȤ���Ͽ
		boolean isSystemRuleset = false;
		for (Atom atom : mem.atoms)
		{
			if (atom.functor.getName().equals("system_ruleset"))
			{
				isSystemRuleset = true;
				break;
			}
		}
		if (isSystemRuleset)
		{
			for (Ruleset r : mem.rulesets)
			{
				InterpretedRuleset ir = (InterpretedRuleset)r;
				SystemRulesets.addUserDefinedSystemRuleset(ir);
				ir.isSystemRuleset = true;
			}
		}
	}

	public static Ruleset compileRuleset(InterpretedRuleset rs)
	{
		return rs; //�֤��롼�륻�åȤϤ��Τޤޡ��ɤ�����Τ��ɤ��Τ�������
	}
}
