package runtime;

import java.util.ArrayList;
import java.util.List;

/**
 * compile.RulesetCompiler �ˤ�ä���������롣
 * @author hara, nakajima, n-kato
 */
public final class InterpretedRuleset extends Ruleset
{
	/** ���Υ롼�륻�åȤΥ�����ID */
	private int id;
	private static int lastId = 600;

	/** �Ȥꤢ�����롼�������Ȥ��Ƽ��� */
	public List<Rule> rules;
	
	/** �Ԥ߾夲���̿���� */
	public MergedBranchMap branchmap;
	public MergedBranchMap systemrulemap;
	
	/** ���߼¹���Υ롼�� */
	public Rule currentRule;
	
	private int backtracks, lockfailure;

	/**
	 * RuleCompiler �Ǥϡ��ޤ��������Ƥ���ǡ�����������ࡣ
	 * �Τǡ��äˤʤˤ⤷�ʤ�
	 */
	public InterpretedRuleset()
	{
		rules = new ArrayList<Rule>();
		id = ++lastId;
		branchmap = null;
		systemrulemap = null;
	}

	/** �����Х�롼�륻�å�ID��̤����ξ���null��*/
	private String globalRulesetID;

	/**���Υ롼�륻�åȤΥ�����ID��������롣*/
	public int getId()
	{
		return id;
	}

	/**�ʲ���*/
	// 061129 okabe runtimeid �ѻߤˤ��
//	public String getGlobalRulesetID() {
//		// todo ��󥿥���ID��ͭ�����֤�ľ��
//		if (globalRulesetID == null) {
//			globalRulesetID = Env.theRuntime.getRuntimeID() + ":" + id;
////			IDConverter.registerRuleset(globalRulesetID, this);
//		}
//		return globalRulesetID;
//	}

	public String toString()
	{
		String ret = "@" + id;
		if (Env.verbose >= Env.VERBOSE_EXPANDRULES)
		{
			ret += dumpRules();
		}
		return ret;
	}
	
	public String dumpRules()
	{
		StringBuilder s = new StringBuilder();
		for (Rule rule : rules)
		{
			s.append(" ");
			s.append(rule);
		}
		return s.toString();
	}

	public String encode()
	{
		StringBuilder s = new StringBuilder();
		boolean isFirst = true;
		for (Rule rule : rules)
		{
			s.append(rule.getFullText().replace("\\n", "").replace("\\r", ""));
			if (isFirst)
			{
				s.append(", ");
				isFirst = false;
			}
		}
		return s.toString();
	}

	public String[] encodeRulesIndividually()
	{
		String[] result = new String[rules.size()];
		int i = 0;
		for (Rule rule : rules)
		{
			result[i] = rule.getFullText().replace("\\n", "").replace("\\r", "");
			if (2 < result[i].length())
			{
				result[i] = result[i].substring(1, result[i].length() - 1);
			}
			i++;
		}
		return result;
	}

	public void showDetail()
	{
		if (Env.compileonly)
		{
			if (isSystemRuleset)
			{
				Env.p("Compiled SystemRuleset @" + id + dumpRules());
			}
			else
			{
				Env.p("Compiled Ruleset @" + id + dumpRules());
			}
		}
		for (Rule rule : rules)
		{
			rule.showDetail();
		}
		if (Env.slimcode) Env.p("");
	}
}
