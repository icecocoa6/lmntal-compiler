package runtime;

import java.util.ArrayList;
import java.util.Iterator;

import runtime.systemRuleset.GlobalSystemRuleset;

/**
 * �����ƥ�롼�륻�åȤ�������� static ���饹
 * @author Mizuno
 */
public final class SystemRulesets {
	private static ArrayList<Ruleset> all = new ArrayList<Ruleset>();
	private static ArrayList<Ruleset> userDefined = new ArrayList<Ruleset>();
	static {
		clear();
	}
	/** ���������Ͽ���줿�桼������������ƥ�롼�륻�åȤ����롣 */
	public static void clear() {
		all.clear();
		userDefined.clear();
		all.add(GlobalSystemRuleset.getInstance());
	}
	/**
	 * �桼������������ƥ�롼�륻�åȤ���Ͽ���롣
	 * @param rs ��Ͽ����롼�륻�å�
	 */
	public static void addUserDefinedSystemRuleset(Ruleset rs) {
		userDefined.add(rs);
		all.add(rs);
	}
	/**
	 * �桼������������ƥ�롼�륻�åȤΥ��ƥ졼�����������
	 * @return �桼������������ƥ�롼�륻�åȤΥ��ƥ졼��
	 */
	public static Iterator<Ruleset> userDefinedSystemRulesetIterator() {
		return userDefined.iterator();
	}
	
	/**
	 * �����ƥ�롼�륻�åȤΥ��ƥ졼�����������
	 * @return �����ƥ�롼�륻�åȤΥ��ƥ졼��
	 */
	public static Iterator<Ruleset> iterator() {
		return all.iterator();
	}

	/**
	 * ���Ƴ�ƥ��Ȥˤ�륷���ƥ�롼���Ŭ�Ѥ��ߤ롣
	 * @return Ŭ�Ѥ�������true
	 */
	public static boolean react(Membrane mem, boolean nondeterministic) {
		boolean flag = false;
		int debugvalue = Env.debug; // todo spy��ǽ���������
		if (Env.debug < Env.DEBUG_SYSTEMRULESET) Env.debug = 0;
		Iterator<Ruleset> itsys = SystemRulesets.iterator();
		while (itsys.hasNext()) {
			if (itsys.next().react(mem, nondeterministic)) {
				flag = true;
				break;
			}
		}
		Env.debug = debugvalue;
		return flag;
	}
}
