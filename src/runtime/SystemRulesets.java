package runtime;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * �����ƥ�롼�륻�åȤ�������� static ���饹
 * @author Mizuno
 */
public final class SystemRulesets {
	private static ArrayList all = new ArrayList();
	private static ArrayList userDefined = new ArrayList();
	static {
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
	public static Iterator userDefinedSystemRulesetIterator() {
		return userDefined.iterator();
	}
	
	/**
	 * �����ƥ�롼�륻�åȤΥ��ƥ졼�����������
	 * @return �����ƥ�롼�륻�åȤΥ��ƥ졼��
	 */
	public static Iterator iterator() {
		return all.iterator();
	}
}
