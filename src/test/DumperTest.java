package test;

import runtime.LMNtalRuntime;
import runtime.Dumper;
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
		/*		LMNtalRuntime rt = new LMNtalRuntime(rule);
			System.out.println("before exec:");
		Dumper.dump(rt.getRootMem());
		rt.exec();
		System.out.println("after exec:");
		Dumper.dump(rt.getRootMem());
*/	}
}
