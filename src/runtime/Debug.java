/*
 * ������: 2006/01/17
 *
 */
package runtime;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LMNtal�ǥХå�
 * @author inui
 */
public class Debug {
	public static final int INIT = 0;
	public static final int RUN = 1;
	public static final int NEXT = 2;
	public static final int CONTINUE = 3;
	public static final int ATOM = 4;
	public static final int MEMBRANE = 5;
	
	/** ľ����Ŭ�Ѥ��줿�롼�� */
	private static int currentRule;
	
	/** ľ����Ŭ�Ѥ��줿�롼��Υƥ��Ȥμ��� */
	private static int testType;
	
	/** �֥졼���ݥ���� */
	private static List breakPoints = new ArrayList();
	
	/** ���롼�� */
	private static Set rules = null;
	
	/** �¹���Υե�����̾(�ե����뤬1�ĤǤ���Ȳ���) */
	private static String unitName;
	
	/** �ǥХå����� */
	private static int state = INIT;
	
	/**
	 * ����Υ롼���Ƶ�Ū�˼�������
	 * @param mem �롼����
	 */
	private static void collectAllRules(Membrane mem) {
		Iterator itr = mem.rulesetIterator();
		while (itr.hasNext()) {
			Object o = itr.next();
			if (!(o instanceof InterpretedRuleset)) continue;
			InterpretedRuleset ruleset = (InterpretedRuleset)o;
			List rules = ruleset.rules;
			Iterator ruleIterator = rules.iterator();
			while (ruleIterator.hasNext()) {
				Rule rule = (Rule)ruleIterator.next();
				Debug.rules.add(rule);
			}
		}
		
		Iterator memIterator = mem.mems.iterator();
		while (memIterator.hasNext()) {
			collectAllRules((Membrane)memIterator.next());
		}
	}
	
	/**
	 * ���ʤ��Ȥ�ǽ��1��¹Ԥ���
	 */
	public static void init() {
		if (Env.guiDebug.restart) return;
		
		try {
			FileReader fr = new FileReader(unitName);
			BufferedReader br = new BufferedReader(fr);
			StringBuffer buf = new StringBuffer();
			String s = null;
			int lineno = 0;
			buf.append("<style>pre {font-size:"+(Env.fDEMO ? 14 : 10)+"px; font-family:monospace;}</style>\n");
			buf.append("<pre>\n");
			while ((s = br.readLine()) != null) {
				Matcher m = Pattern.compile("(.*)(//|%)(.*)").matcher(s);
				if (m.matches()) {//�����Ȥ��ä��餽��¾�ο��դ��Ϥ��ʤ�
					s = m.group(1)+"<font color=green>"+m.group(2)+m.group(3)+"</font>";
				} else {
					s = s.replace("=", "<font color=fuchsia>=</font>");
					s = s.replace(":-", "<font color=fuchsia>:-</font>");
					s = s.replace("{", "<font color=blue>{</font>");
					s = s.replace("}", "<font color=blue>}</font>");
				}
				buf.append("  "+s+"\n");
				lineno++;
			}
			buf.append("</pre>\n");
			s = buf.toString();
			s = s.replaceAll("/\\*", "<font color=green>/*");
			s = s.replaceAll("\\*/", "*/</font>");
			Env.guiDebug.setSourceText(s, lineno);
			Env.gui.repaint();
		} catch (IOException e) {
			System.err.println(e);
		}
		
		//���ƤΥ롼����������
		Debug.rules = new HashSet();
		Membrane rootMem = ((MasterLMNtalRuntime)Env.theRuntime).getGlobalRoot();
		collectAllRules(rootMem);
	}
	
	/**
	 * ���ֹ����ꤷ�ƥ֥졼���ݥ���Ȥ��ڤ��ؤ���
	 * @param lineno
	 */
	public static void toggleBreakPointAt(int lineno) {
		//���Ǥ�¸�ߤ���֥졼���ݥ���Ȥʤ���
		for (int i = 0; i < breakPoints.size(); i++) {
			if (((Integer)breakPoints.get(i)).intValue() == lineno) {
				breakPoints.remove(i);
				return;
			}
		}
		
		breakPoints.add(lineno);
	}
	
	/**
	 * ���֥졼���ݥ���Ȥ��ɤ���Ĵ�٤�
	 * @return
	 */
	public static boolean isBreakPoint() {
		if (state == NEXT) return true;
		Iterator itr = breakPointIterator();
		while (itr.hasNext()) {
			if (currentRule == ((Integer)itr.next()).intValue()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * �֥졼���ݥ����
	 * @param r Ŭ�Ѥ��줿�롼��
	 * @param testType ���ȥ��Ƴ�ƥ��Ȥ����Ƴ�ƥ��Ȥ���ɽ�����
	 * ATOM, MEMBRANE
	 */
	public static void breakPoint(int r, int testType) {
		currentRule = r;
		Debug.testType = testType;
	}
	
	/**
	 * �֥졼���ݥ���Ȥν�������λ������Ƥ�
	 * @param state ���ΥǥХå�����
	 * NEXT, CONTINUE
	 *
	 */
	public static void endBreakPoint(int state) {
		currentRule = 0;
		Debug.state = state;
	}
	
	/**
	 * ���åȤ���Ƥ���֥졼���ݥ���Ȥ�iterator���֤��ޤ���
	 * @return iterator
	 */
	public static Iterator breakPointIterator() {
		return breakPoints.iterator();
	}
	
	public static int getCurrentRuleLineno() {
		if (currentRule == 0) return -1;
		return currentRule;
	}

	/**
	 * unitName�򥻥åȤ��ޤ�
	 * @param unitName �ե�����̾
	 */
	public static void setUnitName(String unitName) {
		Debug.unitName = unitName;
	}

	public static int getTestType() {
		return testType;
	}
	
	/**
	 * �롼��Υ��ƥ졼�����֤��ޤ�
	 */
	public static Iterator ruleIterator() {
		if (rules == null) return null;
		return rules.iterator();
	}
	
	public static String getUnitName() {
		return unitName;
	}
}
