/*
 * ������: 2003/10/24
 *
 */
package runtime;
import java.util.*;

import test.GUI.*;
import test3d.*;
import graphic.*;

/**
 * �Ķ����ǥХå��ѡ�
 * @author hara
 */
public final class Env {

	public static final String LMNTAL_VERSION = "0.66.20051105";

	/** -d���ץ���������Υǥե���ȤΥǥХå���٥� */
	static final int DEBUG_DEFAULT = 1;
	/** ����̿��μ¹Ԥ�ȥ졼������ǥХå���٥� */
	static final int DEBUG_TRACE = 2;
	/** �����ƥ�롼�륻�åȤ�̿����¹Ԥ�ɽ������ǥХå���٥�ʲ��� */
	static final int DEBUG_SYSTEMRULESET = 7;
	/** Debug level. */
	public static int debug = 0;

	////////////////////////////////////////////////////////////////

//	/** ̿����Υ���饤�˥󥰤�Ԥ���Ŭ����٥� */
//	public static final int OPTIMIZE_INLINING = 1;
//	/** ��Ĺ��̿��������Ŭ����٥� */
//	public static final int OPTIMIZE_RE = 1;
//	/** ����̿�������夲���Ŭ����٥� */
//	public static final int OPTIMIZE_FF = 1;
//	/** Optimization level. */
//	public static int optimize = 0;
//	
	/** ����Ū��Ŭ�����ץ���󡡥�����,���롼�״ط� sakurai**/
	public static int zoptimize = 0;
	
	/** OCaMNtal�Ѥ����ϥե��������������⡼�� */
	public static boolean compileonly = false;
	////////////////////////////////////////////////////////////////
	
	/** �롼�륻�åȤ����Ƥ�1�����ɽ�������Ĺɽ����٥롡*/
	public static final int VERBOSE_SHOWRULES = 3;
	/** ��ͳ��󥯴������ȥ��ɽ�������Ĺɽ����٥��EXPANDATOMS̤���˸¤��*/
	public static final int VERBOSE_EXPANDPROXIES = 3;
	/** �黻�Ҥ�Ÿ�������Ĺɽ����٥��EXPANDATOMS̤���˸¤�� <pre> X+Y --> '+'(X,Y) </pre> */
	public static final int VERBOSE_EXPANDOPS = 4;
	/** ���ȥ������Ÿ�������Ĺɽ����٥� <pre> a(b) --> a(_2),b(_2) </pre> */
	public static final int VERBOSE_EXPANDATOMS = 5;
	/** �롼�륻�åȤ����Ƥ�Ÿ�������Ĺɽ����٥� */
	public static final int VERBOSE_EXPANDRULES = 6;

	/** -v���ץ����̵������ξ�Ĺɽ����٥� */
	public static final int VERBOSE_INIT = 2;
	/** -v���ץ���������Υǥե���Ȥξ�Ĺɽ����٥� */
	public static final int VERBOSE_DEFAULT = 5;
	/** verbose level. */
	public static int verbose = VERBOSE_INIT;
	
	public static int indent = 0;
	public static boolean dumpEnable = true;

	////////////////////////////////////////////////////////////////

	/** �¹ԥ��ȥॹ���å���Ȥ�ʤ�������¹ԥ�٥� */
	public static final int SHUFFLE_DONTUSEATOMSTACKS = 1;
	/** �롼��˥ޥå����륢�ȥ������������ˤ��������¹ԥ�٥� */
	public static final int SHUFFLE_ATOMS = 2;
	/** �롼��˥ޥå������������������ˤ��������¹ԥ�٥� */
	public static final int SHUFFLE_MEMS = 2;
	/** ����Υ롼�������������ˤ��������¹ԥ�٥� */
	public static final int SHUFFLE_RULES = 3;
	// ** ���Ƥ����롼����ˤ��������¹ԥ�٥��̤������SHUFFLE_TASKS���ȡ�
	//public static final int SHUFFLE_EVERYMEMISROOT = 4;
	// ** �������򥷥�åե뤹�������¹ԥ�٥롣
	// * ���Ƥ��줬�롼����ʤ�С��롼���õ���˹Ԥ�������򤬥�����ˤʤ롣
	// * ������������ˤ���롼��ο����θ������ʤ��ȡ��롼�������ϥ�����ˤϤʤ�ʤ���
	// *��̤�������Ȥ��������ɤΤ褦�˼������٤���������
	//public static final int SHUFFLE_TASKS = 5;

	/** -s���ץ����̵������Υ�����¹ԥ�٥� */
	public static final int SHUFFLE_INIT  = 0;
	/** -s���ץ���������Υǥե���ȤΥ�����¹ԥ�٥� */
	public static final int SHUFFLE_DEFAULT = 3;
	/** ������¹ԥ�٥� */
	public static int shuffle = SHUFFLE_INIT;
	
	////////////////////////////////////////////////////////////////
	
	public static List argv = new ArrayList();
	
	/**
	 * ���¹�
	 */
	public static boolean fInterpret = false;

	/**
	 * �饤�֥����Jar�ե���������
	 */
	public static boolean fLibrary = false;
	
	/**
	 * ̤����ѥ���饤�֥������Ѥ���
	 */
	public static boolean fUseSourceLibrary = false;
	
	/**
	 * �ȥ졼���¹�
	 */
	public static boolean fTrace = false;
	
	/**
	 * REPL �ǡ�ʸ��¹Ԥ��뤿��Υ��������
	 *  null_line : null �Ԥ������Ȥ��˼¹ԡ�Enter �򣲲󲡤����Ȥˤʤ��
	 *  immediate : ʸ�ιԤ������Ȥ��˼¹ԡ�Enter �򣱲󲡤����Ȥˤʤ��
	 * hara
	 */
	public static String replTerm = "null_line";
	
	/**
	 * REPL �ǡ��ü쥳�ޥ�ɤˤĤ���٤��ץ�ե��å���
	 * �㡧������ + "q" �ǽ�λ
	 * hara
	 */
	public static String replCommandPrefix = ":";
	
	/**
	 * one liner
	 */
	public static String oneLiner;
	/**
	 * 3D mode��ͭ��
	 */
	public static boolean f3D = false;
	public static LMNtal3DFrame threed;
	
	public static void init3D(){
		if(!Env.f3D)return;
		atomSize = Env.fDEMO ? 40 : 16;
		Env.threed = new LMNtal3DFrame();
		
	}
	/**
	 * Graphic Mode ͭ����nakano 
	 */
	public static boolean fGraphic = false;
	public static LMNtalGFrame LMNgraphic;
	
	public static void initGraphic(){
		if(!Env.fGraphic) return;
		LMNgraphic = new LMNtalGFrame();
	}
	/**
	 * GUI ͭ������������
	 */
	public static boolean fGUI = false;
	public static LMNtalFrame gui;
	public static boolean fDEMO;
	public static int atomSize;
	
	public static void initGUI() {
		if(!Env.fGUI) return;
		atomSize = Env.fDEMO ? 40 : 16;
		Env.gui = new LMNtalFrame();
	}
	
	/**
	 * CGI �⡼��
	 */
	public static boolean fCGI = false;
	
	/**
	 * REMAIN �⡼��
	 */
	public static boolean fREMAIN = false;
	public static MasterLMNtalRuntime remainedRuntime;
	
	/**
	 * REPL �⡼��
	 */
	public static boolean fREPL = false;
	
	public static boolean preProcess0 = false;
	
	/** ���ȥ�̾��ɽ������Ĺ�� */
	public static int printLength = 14;
	
	////////////////////////////////////////////////////////////////
	// ʬ��
	
	/** start LMNtalDaemon.*/
	public static boolean startDaemon = false;
	
	/**The debug level of LMNtalDaemon.*/
	public static int debugDaemon = 0;
	
	/** The default port number that LMNtalDaemon listens on.*/
	static final int DAEMON_DEFAULT_LISTENPORT = 60000;
	
	/** The port number that LMNtalDaemon listens on.*/
	public static int daemonListenPort = DAEMON_DEFAULT_LISTENPORT;
	
	////////////////////////////////////////////////////////////////
	
	// PROXY ��ɽ�������ʤ� 2005/02/03 T.Nagata ���ץ���� --hideproxy
	// �ǥե���Ȥ�ͭ�� 2005/10/14 mizuno
	public static boolean hideProxy = true;
	
	/**
	 * General error report
	 * @param o
	 */
	public static void e(Object o) {
		e(o, 0);
	}
	
	/**
	 * General error report with indent
	 * @param o
	 * @param depth ����ǥ�Ȥο���
	 */
	public static void e(Object o, int depth) {
		System.err.println(getIndent(depth) + o);
	}
	public static void e(Exception e) {
		e.printStackTrace(System.err);
	}
	
	/**
	 * General dumper for debug
	 * @param o Object to print
	 */
	public static void d(Object o) {
		d(o, 0);
	}
	
	/**
	 * Exception dumper for debug
	 * @param e
	 */
	public static void d(Exception e) {
		if(debug > 0) e.printStackTrace();
	}
	
	/**
	 * General dumper for debug with indent
	 * @param o
	 * @param depth ����ǥ�Ȥο���
	 */
	public static void d(Object o, int depth) {
		if(debug > 0) System.out.println(getIndent(depth) + o);
	}
	
	/**
	 * General dumper
	 * @param o Object to print
	 */
	public static void p(Object o) {
		p(o, 0);
	}
	
	/**
	 * General dumper with indent
	 * @param o
	 * @param depth ����ǥ�Ȥο���
	 */
	public static void p(Object o, int depth) {
		System.out.println(getIndent(depth) + o);
	}
	
	/**
	 * Debug output when some method called
	 * @param o method name
	 */
	public static void c(Object o) {
		//d(">>> "+o);
	}
	/**
	 * Debug output when new some object: write at constructor.
	 * @param o Class name
	 */
	public static void n(Object o) {
		d(">>> new "+o);
	}
	
	/**
	 * Better list dumper : No comma output
	 */
	public static String parray(Collection l, String delim) {
		StringBuffer s = new StringBuffer();
		for(Iterator i=l.iterator();i.hasNext();) {
			s.append( i.next().toString()+(i.hasNext() ? delim:"") );
		}
		return s.toString();
	}
	public static String parray(Collection l) {
		return parray(l, " ");
	}
	
	/**
	 * ���ꤷ�����Υ���ǥ�Ȥ��֤�
	 * @param depth
	 * @return
	 */
	public static String getIndent(int depth) {
		String indent="";
		for(int i=0;i<depth;i++) {
			indent += "\t";
		}
		return indent;
	}
	
	/** LocalLMNtalRuntime�Υ��󥹥��� */
	public static LocalLMNtalRuntime theRuntime;

	/** @return �롼�륹��åɤμ¹Ԥ��³���Ƥ褤���ɤ��� */
	public static boolean guiTrace() {
		if(gui==null) return true;
		return gui.onTrace();
	}
	/**graphic�� nakano �롼�륹��åɤμ¹Ԥ��³���Ƥ褤���ɤ���*/
	public static boolean graphicTrace() {
		if(LMNgraphic==null) return true;
		return LMNgraphic.onTrace();
	}
	
	/**
	 * ��ĥ���ޥ�ɥ饤������򤳤�˳�Ǽ����
	 */
	public static Map extendedOption = new HashMap();
	public static String getExtendedOption(Object key) {
		if(!extendedOption.containsKey(key)) return "";
		return extendedOption.get(key).toString();
	}
	
	////////////////////////////////////////////////////////////////
	
	public static int nErrors = 0;
	public static int nWarnings = 0;
	public static void clearErrors() {
		nErrors = 0;
		nWarnings = 0;
	}
	public static void error(String text) {
		System.err.println(text);
		nErrors++;
	}
	public static void warning(String text) {
		System.err.println(text);
		nWarnings++;
	}
}
