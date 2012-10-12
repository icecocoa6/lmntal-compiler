package runtime;

import java.util.ArrayList;
import java.util.Iterator;

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
		//all.add(GlobalSystemRuleset.getInstance());
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
}
