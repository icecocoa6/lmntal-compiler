package test;

import runtime.*;
import runtime.Ruleset;
import test.SampleInitRuleset;
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
		System.out.println("before init:");
		Ruleset rule = new SampleInitRuleset();
		LMNtalRuntime mm = new LMNtalRuntime();
		Membrane m = mm.getGlobalRoot();
		rule.react(m);
		System.out.println("before exec:");
		System.out.println(Dumper.dump(m));
//		mm.exec(); // ��
		System.out.println("after exec:");
		System.out.println(Dumper.dump(m));
	}
}
