/*
 * ������: 2006/01/17
 *
 */
package runtime;

import java.io.*;
import java.util.*;

import test.GUI.*;

//class ConsolePrintStream extends PrintStream {
//	private JTextArea jt;
//	
//	public ConsolePrintStream(OutputStream os, JTextArea jt) throws FileNotFoundException {
//		super(os);
//		this.jt = jt;
//	}
//
//	@Override
//	public void println(String s) {
//		super.println(s);
//		jt.append(s+"\n");
//	}
//	
//}

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
	private static Rule currentRule;
	
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
			InterpretedRuleset ruleset = (InterpretedRuleset)itr.next();
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
		try {
			FileReader fr = new FileReader(unitName);
			BufferedReader br = new BufferedReader(fr);
			StringBuffer buf = new StringBuffer();
			String s = null;
			int lineno = 0;
			buf.append("<style>pre {font-size:10px; font-family:monospace;}</style>\n");
			buf.append("<pre>\n");
			while ((s = br.readLine()) != null) {
				buf.append("  "+s.replace(":-", "<font color=red>:-</font>")+"\n");
				lineno++;
			}
			buf.append("</pre>\n");
			s = buf.toString();
			s = s.replaceAll("/\\*", "<font color=green>/*");
			s = s.replaceAll("\\*/", "</font>*/");
			Env.guiDebug.setSourceText(s, lineno);
			Env.gui.repaint();
		} catch (IOException e) {
			System.err.println(e);
		}
		
		//���ƤΥ롼����������
		Debug.rules = new HashSet();
		Membrane rootMem = ((MasterLMNtalRuntime)Env.theRuntime).getGlobalRoot();
		collectAllRules(rootMem);
		
		//ɸ����Ϥ��ڤ��ؤ���
//		try {
//			System.setOut(new ConsolePrintStream(System.out, ((LMNtalDebugFrame)Env.gui).getConsole()));
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
	}
	
	/**
	 * ���ֹ����ꤷ�ƥ֥졼���ݥ���Ȥ��ڤ��ؤ���
	 * @param lineno
	 */
	public static void toggleBreakPointAt(int lineno) {
		//���Ǥ�¸�ߤ���֥졼���ݥ���Ȥʤ���
		for (int i = 0; i < breakPoints.size(); i++) {
			if (((Rule)breakPoints.get(i)).lineno == lineno) {
				breakPoints.remove(i);
				return;
			}
		}
		
		Iterator itr = rules.iterator();
		while (itr.hasNext()) {
			Rule rule = (Rule)itr.next();
			if (rule.lineno == lineno) {
				breakPoints.add(rule);
				System.out.println("Breakpoint "+breakPoints.size()+" at "+rule.name+": file "+unitName+", line "+lineno);
			}
		}
	}
	
	/**
	 * ���֥졼���ݥ���Ȥ��ɤ���Ĵ�٤�
	 * @return
	 */
	public static boolean isBreakPoint() {
		if (state == NEXT) return true;
		Iterator itr = breakPointIterator();
		while (itr.hasNext()) {
			if (currentRule != null && currentRule.equals(itr.next())) {
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
	public static void breakPoint(Rule r, int testType) {
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
		currentRule = null;
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
		if (currentRule == null) return -1;
		return currentRule.lineno;
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
}
