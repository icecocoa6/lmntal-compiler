/*
 * ������: 2006/01/17
 *
 */
package debug;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import runtime.Dumper;
import runtime.Env;
import runtime.InterpretedRuleset;
import runtime.MasterLMNtalRuntime;
import runtime.Membrane;
import runtime.Rule;

/**
 * LMNtal�ǥХå�
 * @author inui
 */
public class Debug {
	/** ���ȥ��Ƴ�ƥ��Ȥ�ɽ�����  */
	public static final int ATOM = 1;
	
	/** ���Ƴ�ƥ��Ȥ�ɽ�����  */
	public static final int MEMBRANE = 2;
	
	//ľ����Ŭ�Ѥ��줿�롼��ι��ֹ� */
	private static int currentLineNumber;
	
	//ľ����Ŭ�Ѥ��줿�롼��Υƥ��ȥ�����
	private static int testType;
	
	//�֥졼���ݥ���ȤΥꥹ��
	private static List breakPoints = new ArrayList();
	
	//���롼��Υ��å�
	private static Set rules = new HashSet();
	
	//�¹���Υե�����̾(�Ȥꤢ�����ե����뤬1�ĤǤ���Ȳ���)
	private static String unitName;
	
	//�������ץ����
	private static Vector source;
	
	//�ݡ����ֹ�
	private static int requestPort = -1;
	private static int eventPort = -1;
	
	//�����å�
	private static ServerSocket requestSocket = null;
	private static ServerSocket eventSocket = null;
	
	//����
	private static boolean isRunning = false;
	private static boolean isStepping = false;
	
	//�̿�
	private static BufferedReader requestIn;
	private static PrintWriter requestOut;
	private static PrintWriter eventOut;
	
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
		
		Iterator memIterator = mem.memIterator();
		while (memIterator.hasNext()) {
			collectAllRules((Membrane)memIterator.next());
		}
	}
	
	/**
	 * ���ʤ��Ȥ�ǽ��1��¹Ԥ���
	 */
	public static void init() {
		if (Env.debugFrame != null && Env.debugFrame.restart) {
			currentLineNumber = -1;
			Env.debugFrame.repaint();
			return;
		}
		
		//TODO DebugFrame��Ǥ����٤�����
		try {
			FileReader fr = new FileReader(unitName);
			BufferedReader br = new BufferedReader(fr);
			StringBuffer buf = new StringBuffer();
			String s = null;
			int lineno = 0;
			buf.append("<style>pre {font-size:"+(Env.fDEMO ? 14 : 10)+"px; font-family:monospace;}</style>\n");
			buf.append("<pre>\n");
			source = new Vector();
			source.add("*System Rule*");
			while ((s = br.readLine()) != null) {
				source.add(s);
				Matcher m = Pattern.compile("(.*)(//|%)(.*)").matcher(s);
				if (m.matches()) {//�����Ȥ��ä��餽��¾�ο��դ��Ϥ��ʤ�
					s = m.group(1)+"<font color=green>"+m.group(2)+m.group(3)+"</font>";
				} else {
					s = s.replace("=", "<font color=fuchsia>=</font>");
					s = s.replace(":-", "<font color=fuchsia>:-</font>");
					s = s.replace("|", "<font color=fuchsia>|</font>");
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
			if (Env.debugFrame != null) Env.debugFrame.setSourceText(s, lineno);
			if (Env.gui != null) Env.gui.repaint();
		} catch (IOException e) {
			System.err.println(e);
		}
		
		//���ƤΥ롼����������
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
		Iterator iter = ruleIterator();
		while (iter.hasNext()) {
			int r = ((Rule)iter.next()).lineno;
			if (r == lineno) {
				breakPoints.add(new Integer(lineno));
				break;
			}
		}
	}
	
	/**
	 * ���ֹ����ꤷ�ƥ֥졼���ݥ���Ȥ��ɲä���
	 * @param lineno
	 * @return ���ꤵ�줿�Ԥ˥롼�뤬�ʤ��ä���false
	 */
	public static boolean addBreakPoint(int lineno) {
		Iterator iter = ruleIterator();
		while (iter.hasNext()) {
			int r = ((Rule)iter.next()).lineno;
			if (r == lineno) {
				breakPoints.add(new Integer(lineno));
				return true;
			}
		}
		return false;
	}
	
	/**
	 * ���֥졼���ݥ���Ȥ��ɤ���Ĵ�٤�
	 * @return
	 */
	public static boolean isBreakPoint() {
		if (isStepping) return true;
		Iterator itr = breakPointIterator();
		while (itr.hasNext()) {
			if (currentLineNumber == ((Integer)itr.next()).intValue()) {
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
		currentLineNumber = r;
		Debug.testType = testType;
	}
	
	/**
	 * ���åȤ���Ƥ���֥졼���ݥ���Ȥ�iterator���֤��ޤ���
	 * @return iterator
	 */
	public static Iterator breakPointIterator() {
		return breakPoints.iterator();
	}
	
	public static int getCurrentRuleLineno() {
		if (currentLineNumber == 0) return -1;
		return currentLineNumber;
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

	/**
	 * ���ޥ�ɼ���
	 */
	public static void inputCommand() {
		if (isRunning) {
			if (!isStepping) eventOut.println("Breakpoint at "+getUnitName()+":"+currentLineNumber);
			//else out.println("step "+currentRule);
			eventOut.println(currentLineNumber+" "+source.get(currentLineNumber));	
		}
		
		isStepping = false;
		try {
			while (true) {
				System.out.print("(ldb) ");
//				System.err.println("���饤����Ȥ������³��ݡ���"+requestPort+"���Ԥ��ޤ�");
				String s = requestIn.readLine().trim();
//				System.err.println("'"+s+"'��������ޤ���");
				if (s.equals("")) {
				} else if (s.startsWith("b")) {//�֥졼���ݥ���Ȥ��ڤ��ؤ�
					String[] ss = s.split("[ \t]+");
					int lineNumber = Integer.parseInt(ss[1]);
					if (addBreakPoint(lineNumber)) System.out.println("Breakpoint "+breakPoints.size()+", line"+lineNumber);
					else System.out.println("No rlue at line "+lineNumber);
				} else if (s.startsWith("c")) {//�¹Ԥ�Ƴ�
					System.out.println("Continuing.");
					break;
				} else if (s.startsWith("n")) {//���ƥå׼¹�
					if (!isRunning)	System.out.println("The program is not being run.");
					else isStepping = true;
					break;
				} else if (s.startsWith("p")) {//�������֤�ɽ��
					Membrane memToDump = ((MasterLMNtalRuntime)Env.theRuntime).getGlobalRoot();
					requestOut.println(Dumper.dump(memToDump));
				} else if (s.startsWith("r")) {//�¹Գ���
					if (isRunning) {
						System.out.println("The program being debugged has been started already.");
					} else {
						isRunning = true;
						System.out.println("Starting program: "+getUnitName()+"\n");
						break;
					}
				} else if (s.startsWith("f")) {//�ե졼������ɽ���ʺ��ϸ��ߤι��ֹ��ɽ����
					System.out.println(currentLineNumber);
				} else if (s.startsWith("q")) {//�ǥХå���λ
					System.exit(0);//TODO exit������ä��ɤ����ʡ�
				} else {
					System.out.println("Undefined command: \""+s+"\".  Try \"help\".");
				}
			}
		} catch(IOException e){
			e.printStackTrace();
		}
		currentLineNumber = 0;
	}
	
	/**
	 * requestPort�򥻥åȤ��ޤ���
	 * @param requestPort
	 */
	public static void setRequestPort(int requestPort) {
		Debug.requestPort = requestPort;
	}
	
	/**
	 * eventPort�򥻥åȤ��ޤ���
	 * @param eventPort
	 */
	public static void setEventPort(int eventPort) {
		Debug.eventPort = eventPort;
	}
	
	/**
	 * �����åȤ򳫤�
	 */
	public static void openSocket() {
		try{
			if (requestPort == -1) {
				requestIn = new BufferedReader(new InputStreamReader(System.in));
				eventOut = new PrintWriter(System.out, true);
				requestOut = new PrintWriter(System.out, true);
			} else {
				//�����С������åȤ�����
				requestSocket = new ServerSocket(requestPort);
				System.err.println("���饤����Ȥ������³��ݡ���"+requestPort+"���Ԥ��ޤ�");
				
				eventSocket = new ServerSocket(eventPort);
				System.err.println("���饤����Ȥ������³��ݡ���"+eventPort+"���Ԥ��ޤ�");
				
				Socket socket1 = requestSocket.accept();
				System.err.println(socket1.getInetAddress() + "������³����դޤ���");
				requestIn = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
				requestOut = new PrintWriter(socket1.getOutputStream(), true);
				
				Socket socket2 = eventSocket.accept();
				System.err.println(socket2.getInetAddress() + "������³����դޤ���");
				eventOut = new PrintWriter(socket2.getOutputStream(), true);
			}
		} catch (IOException e) {}
	}
	
	//�ǥХå���λ����
	public static void terminate() {
		System.out.println();
		eventOut.println("Program exited normally.");
		try {
			requestIn.close();
			eventOut.close();
			requestOut.close();
			if (requestSocket != null) requestSocket.close();
			if (eventSocket != null) eventSocket.close();
		} catch (IOException e) {
			System.err.println(e);
		}
	}
	
	///////////////////////////////////////////////////
	//DebugFrameg������
	
	public static void step() {
		isStepping = true;
	}
	
	public static void doContinue() {
		isStepping = false;
	}
}
