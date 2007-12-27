package test;

import runtime.Dumper;
import runtime.LMNtalRuntime;
import runtime.Membrane;
import runtime.Ruleset;
import util.Util;
/**
 * LMNtal �Υᥤ��
 * 
 * 
 * ������: 2003/10/22
 */
public class DumperTest {
	/**
	 * �¹ԡ����ϥƥ��ȤΤ��ᡢSampleInitRuleset�������롼��Ȥ��Ƽ¹ԡ�Dump��Ԥ���
	 */
	public static void main(String[] args) {
		Util.println("before init:");
		Ruleset rule = new SampleInitRuleset();
		LMNtalRuntime mm = new LMNtalRuntime();
		Membrane m = mm.getGlobalRoot();
		rule.react(m);
		Util.println("before exec:");
		Util.println(Dumper.dump(m));
//		mm.exec(); // ��
		Util.println("after exec:");
		Util.println(Dumper.dump(m));
	}
}
