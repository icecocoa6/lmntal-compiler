/*
 * ������: 2003/11/18
 *
 */
package compile;

import java.util.Iterator;
import runtime.Env;
import runtime.InterpretedRuleset;

/**
 * @author hara
 *
 */
public class RuleSetGenerator {
	
	/**
	 * Ϳ����줿��ľ°�����Ƥ� RuleStructure �ˤĤ��ơ�
	 * �б����� Rule ���������Ƥ�����Υ롼�륻�åȤ��ɲä��롣
	 * 
	 * @param root_mem
	 * @return ���ꤷ����Υ롼�륻�å�
	 */
	public static InterpretedRuleset run(Membrane m) {
		Env.c("RuleSetGenerator.run");
		processMembrane(m);
		return (InterpretedRuleset)m.ruleset;
	}
	
	/**
	 * Ϳ����줿��ľ°�����Ƥ� RuleStructure �ˤĤ��ơ�
	 * �б����� Rule ���������Ƥ�����Υ롼�륻�åȤ��ɲä��롣
	 * @param m ���ܤ�����
	 */
	public static void processMembrane(Membrane m) {
		Env.c("RuleSetGenerator.processMembrane");
		
		Iterator i = m.rules.listIterator();
		while(i.hasNext()) {
			RuleStructure rs = (RuleStructure)i.next();
			RuleCompiler rc = new RuleCompiler(rs);
			rc.compile();
		}
	}
}
