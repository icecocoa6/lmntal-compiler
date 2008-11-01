/*
 * ������: 2006/01/17
 *
 */
package debug;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import runtime.Dumper;
import runtime.Env;
import runtime.InterpretedRuleset;
import runtime.Membrane;
import runtime.Rule;
import runtime.Ruleset;
import util.Util;

/**
 * LMNtal�ǥХå�
 * ���ޥ�ɥ饤��ǥХå��ȡ�eclipse �ΥǥХå���ξ���ε�ǽ���ޤޤ�롥
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
	
	//�֥졼���ݥ���Ȥν���
	private static Set<Integer> breakPoints = new HashSet<Integer>();
	
	//���롼��Υ��å�
	private static Set<Rule> rules = new HashSet<Rule>();
	
	//�¹���Υե�����̾(�Ȥꤢ�����ե����뤬1�ĤǤ���Ȳ���)
	private static String unitName;
	
	//�������ץ����
	private static Vector<String> source;
	
	//�Ǹ��ɽ���������ֹ�
	private static int lastLineno;
	private static int listsize = 10;
	
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
		Iterator<Ruleset> itr = mem.rulesetIterator();
		while (itr.hasNext()) {
			Object o = itr.next();
			if (!(o instanceof InterpretedRuleset)) continue;
			InterpretedRuleset ruleset = (InterpretedRuleset)o;
			for(Rule rule : ruleset.rules){
				Debug.rules.add(rule);
			}
		}
		
		Iterator<Membrane> memIterator = mem.memIterator();
		while (memIterator.hasNext()) {
			collectAllRules(memIterator.next());
		}
	}
	
	/**
	 * ���ʤ��Ȥ�ǽ��1��¹Ԥ���
	 */
	public static void init() {
		lastLineno = 1;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(unitName));
			String s = null;
			source = new Vector<String>();
			source.add("** system rule **");
			while ((s = br.readLine()) != null)
				source.add(s);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//���ƤΥ롼����������
		Membrane rootMem = Env.theRuntime.getGlobalRoot();
		collectAllRules(rootMem);
	}
	
	/**
	 * ���ֹ����ꤷ�ƥ֥졼���ݥ���Ȥ��ڤ��ؤ���
	 * @param lineno
	 */
	public static void toggleBreakPointAt(int lineno) {
		//���Ǥ�¸�ߤ���֥졼���ݥ���Ȥʤ���
		if (breakPoints.contains(lineno)) breakPoints.remove(lineno);
		else breakPoints.add(lineno);
	}
	
	/**
	 * ���ֹ����ꤷ�ƥ֥졼���ݥ���Ȥ��ɲä���
	 * @param lineno
	 * @return ���ꤵ�줿�Ԥ˥롼�뤬�ʤ��ä���false
	 */
	public static boolean addBreakPoint(int lineno) {
		for (Rule r : rules) {
			if (r.lineno == lineno) {
				breakPoints.add(lineno);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * �롼��̾����ꤷ�ƥ֥졼���ݥ���Ȥ��ɲä���
	 * @param name �롼��̾
	 * @return ���ꤵ�줿�롼��̾���ʤ��ä���false
	 */
	public static boolean addBreakPoint(String name) {
		for (Rule r : rules) {
			if (r.name != null && r.name.equals(name)) {
				breakPoints.add(r.lineno);
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
		for (Integer lineno : breakPoints) {
			if (currentLineNumber == lineno.intValue()) {
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
		System.out.println(r);
		currentLineNumber = r;
		Debug.testType = testType;
	}
	
	/**
	 * ���åȤ���Ƥ���֥졼���ݥ���Ȥ�iterator���֤��ޤ���
	 * @return iterator
	 */
	public static Iterator<Integer> breakPointIterator() {
		return breakPoints.iterator();
	}
	
	/**
	 * ���ߥ֥졼�����Ƥ�����ֹ���������
	 * @return ���ߥ֥졼�����Ƥ�����ֹ�
	 */
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

	/**
	 * ���ߥ֥졼�����Ƥ���롼��Υƥ��Ȥμ���
	 * @return ���Ƴ�ƥ��� (MEMBRANE_TEST) �����ȥ��Ƴ�ƥ��� (ATOM_TEST) 
	 */
	public static int getTestType() {
		return testType;
	}
	
	/**
	 * �롼��Υ��ƥ졼�����֤��ޤ�
	 */
	public static Iterator<Rule> ruleIterator() {
		if (rules == null) return null;
		return rules.iterator();
	}
	
	/**
	 * �¹Ԥ��Ƥ���ե�����̾���֤�
	 * @return �ե�����̾
	 */
	public static String getUnitName() {
		return unitName;
	}
	
	/**
	 * �ץ�����ɽ�����ޤ�
	 */
	public static void showList() {
		int i;
		for (i = lastLineno; i < Math.min(lastLineno+listsize, source.size()); i++) {
			Util.println(i+"\t"+source.get(i));
		}
		lastLineno = i;
	}

	/**
	 * ɸ�����Ϥ��饳�ޥ�ɤ�����դ��롥
	 * eclipse ���鵯ư���Ƥ���Ȥ��ϥ����å��̿����롥
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
				Util.print("(ldb) ");
//				System.err.println("���饤����Ȥ������³��ݡ���"+requestPort+"���Ԥ��ޤ�");
				String s = requestIn.readLine().trim();
//				System.err.println("'"+s+"'��������ޤ���");
				if (s.equals("")) {
				} else if (s.startsWith("b")) {//�֥졼���ݥ���Ȥ��ڤ��ؤ�
					String[] ss = s.split("[ \t]+");
					if (ss.length < 2) continue;
					try {
						int lineNumber = Integer.parseInt(ss[1]);
						if (addBreakPoint(lineNumber)) Util.println("Breakpoint "+breakPoints.size()+", line"+lineNumber);
						else Util.println("No rlue at line "+lineNumber);
					} catch (NumberFormatException e) {
						if (addBreakPoint(ss[1])) Util.println("Breakpoint "+breakPoints.size()+", "+ss[1]);
						else Util.println("No rlue "+ss[1]);
					}
				} else if (s.startsWith("c")) {//�¹Ԥ�Ƴ�
					Util.println("Continuing.");
					break;
				} else if (s.startsWith("h")) {
					showHelp();
				} else if (s.startsWith("l")) {
					String[] ss = s.split("[ \t]+");
					if (ss.length >= 2) {
						try {
							int lineNumber = Integer.parseInt(ss[1]);
							lastLineno = Math.max(1, lineNumber-listsize/2);
						} catch (NumberFormatException e) {
							Util.println("Rule \""+ss[1]+"\" not defined.");
							continue;
						}
					}
					showList();
				} else if (s.startsWith("n")) {//���ƥå׼¹�
					if (!isRunning)	Util.println("The program is not being run.");
					else isStepping = true;
					break;
				} else if (s.startsWith("p")) {//�������֤�ɽ��
					Membrane memToDump = Env.theRuntime.getGlobalRoot();
					requestOut.println(Dumper.dump(memToDump));
				} else if (s.startsWith("r")) {//�¹Գ���
					if (isRunning) {
						Util.println("The program being debugged has been started already.");
					} else {
						isRunning = true;
						Util.println("Starting program: "+getUnitName()+"\n");
						break;
					}
				} else if (s.startsWith("f")) {//�ե졼������ɽ���ʺ��ϸ��ߤι��ֹ��ɽ����
					Util.println(currentLineNumber);
				} else if (s.startsWith("q")) {//�ǥХå���λ
					System.exit(0);//TODO exit������ä��ɤ����ʡ�
				} else if (s.startsWith("set l")) {
					String[] ss = s.split("[ \t]+");
					if (ss.length < 3) continue;
					try {
						listsize = Integer.parseInt(ss[2]);
					} catch (NumberFormatException e) {
						Util.println("No symbol \""+ss[2]+"\" in current context.");
					}
				} else {
					Util.println("Undefined command: \""+s+"\".  Try \"help\".");
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
				Util.errPrintln("���饤����Ȥ������³��ݡ���"+requestPort+"���Ԥ��ޤ�");
				
				eventSocket = new ServerSocket(eventPort);
				Util.errPrintln("���饤����Ȥ������³��ݡ���"+eventPort+"���Ԥ��ޤ�");
				
				Socket socket1 = requestSocket.accept();
				Util.errPrintln(socket1.getInetAddress() + "������³����դޤ���");
				requestIn = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
				requestOut = new PrintWriter(socket1.getOutputStream(), true);
				
				Socket socket2 = eventSocket.accept();
				Util.errPrintln(socket2.getInetAddress() + "������³����դޤ���");
				eventOut = new PrintWriter(socket2.getOutputStream(), true);
			}
		} catch (IOException e) {}
	}
	
	//�ǥХå���λ����
	public static void terminate() {
		Util.println("");
		eventOut.println("Program exited normally.");
		try {
			requestIn.close();
			eventOut.close();
			requestOut.close();
			if (requestSocket != null) requestSocket.close();
			if (eventSocket != null) eventSocket.close();
		} catch (IOException e) {
			Util.errPrintln(e);
		}
	}
	
	public static void showHelp() {
		Util.println("List of commands:");
		Util.println("");
		Util.println("break -- Set breakpoint at specified line or function");
		Util.println("continue -- Continue program being debugged");
		Util.println("help -- Print list of commands");
		Util.println("list -- List specified line");
		Util.println("next -- Step program");
		Util.println("print -- dump membrane");
		Util.println("run -- Start debugged program");					
		//System.out.println("frame -- Select and print a stack frame");
		Util.println("quit -- Exit ldb");
	}
}
