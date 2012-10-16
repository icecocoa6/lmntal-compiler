package runtime;

import java.util.ArrayList;
import java.util.List;

import runtime.functor.Functor;

/**
 * �롼��ν��硣
 * ���ߤϥ롼�������Ȥ���ɽ�����Ƥ��뤬������Ū�ˤ�ʣ���Υ롼��Υޥå��󥰤�
 * ���ĤΥޥå��󥰥ƥ��ȤǹԤ��褦�ˤ��롣
 */
public abstract class Ruleset
{
	/**
	 * new«�����줿̾���ζ����ͤ��Ǽ��������
	 */
	protected Functor[] holes;

	public List<Rule> compiledRules = new ArrayList<Rule>();
	public boolean isRulesSetted = false;
	public boolean isSystemRuleset = false;

	public abstract String toString();
	public abstract String encode();

	public String[] encodeRulesIndividually()
	{
		return null;
	}

	/**
	 * new«�����줿̾���ζ����ͤ���ꤷ�ƿ�����Ruleset��������롣
	 * @return ������Ruleset
	 */
	public Ruleset fillHoles(Functor[] holes)
	{
		return null;
	}
}
