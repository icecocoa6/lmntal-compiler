/*
 * ������: 2003/10/24
 *
 */
package runtime;
import graphic.LMNtalGFrame;
import toolkit.LMNtalTFrame; //todo

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * �Ķ����ǥХå��ѡ�
 * @author hara
 */
public final class Env {

	public static final String LMNTAL_VERSION = "0.86.20061218";

	/** -d���ץ���������Υǥե���ȤΥǥХå���٥� */
	static final int DEBUG_DEFAULT = 1;
	/** ����̿��μ¹Ԥ�ȥ졼������ǥХå���٥� */
	static final int DEBUG_TRACE = 2;
	/** �����ƥ�롼�륻�åȤ�̿����¹Ԥ�ɽ������ǥХå���٥�ʲ��� */
	static final int DEBUG_SYSTEMRULESET = 7;
	/** Debug level. */
	public static int debug = 0;
	
	////////////////////////////////////////////////////////////////

	/** ���̿�������Ϥ���⡼�ɡ�Java �ؤ��Ѵ���¹ԤϹԤ�ʤ��� */
	public static boolean compileonly = false;
	
	/** ��������̤�Ǿ������� */
	public static boolean fMemory = true;

	/** �̾�μ¹� */
	public static final int ND_MODE_D = 0;
	/** �������������� */
	public static final int ND_MODE_ND_ALL = 1;
	/** ���ĤȤ��η���Τ� */
	public static final int ND_MODE_ND_ANSCESTOR = 2;
	/** ���ڹԤ�ʤ� */
	public static final int ND_MODE_ND_NOTHING= 3;
	/** �����ŪLMNtal�⡼�� */
	public static int ndMode = ND_MODE_D;
	/** �����ŪLMNtal�ˤ����륤�󥿥饯�ƥ��֥⡼��*/
	public static boolean fInteractive = false;
	
	public static boolean fThread = true;
	
	////////////////////////////////////////////////////////////////
	
	/** ��󥯤�ɽ����L[����]��ɽ�������Ĺɽ����٥롡<pre> a(_2) --> a(L2) </pre> */
	public static final int VERBOSE_SIMPLELINK = 1;
	/** �롼�륻�åȤ����Ƥ�1�����ɽ�������Ĺɽ����٥롡*/
	public static final int VERBOSE_SHOWRULES = 3;
//	/** ��ͳ��󥯴������ȥ��ɽ�������Ĺɽ����٥��EXPANDATOMS̤���˸¤��*/
//	public static final int VERBOSE_EXPANDPROXIES = 3;
	/** �黻�Ҥ�Ÿ�������Ĺɽ����٥��EXPANDATOMS̤���˸¤�� <pre> X+Y --> '+'(X,Y) </pre> */
	public static final int VERBOSE_EXPANDOPS = 4;
	/** ���ȥ������Ÿ�������Ĺɽ����٥� <pre> a(b) --> a(_2),b(_2) </pre> */
	public static final int VERBOSE_EXPANDATOMS = 5;
	/** �롼�륻�åȤ����Ƥ�Ÿ�������Ĺɽ����٥� */
	public static final int VERBOSE_EXPANDRULES = 6;

	/** -v���ץ����̵������ξ�Ĺɽ����٥� */
	public static final int VERBOSE_INIT = 1;
	/** -v���ץ���������Υǥե���Ȥξ�Ĺɽ����٥� */
	public static final int VERBOSE_DEFAULT = 5;
	/** verbose level. */
	public static int verbose = VERBOSE_INIT;
	
	public static int indent = 0;
	
	public static boolean showrule = true;

	public static boolean showruleset = true;

	// PROXY ��ɽ�������ʤ� 2005/02/03 T.Nagata ���ץ���� --hideproxy
	// �ǥե���Ȥ�ͭ�� 2005/10/14 mizuno
	public static boolean hideProxy = true;
	
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
	
	/**
	 * �ץ�����Ϳ�������
	 */
	public static List<String> argv = new ArrayList<String>();
	
	/**
	 * �������ե�����
	 */
	public static List<String> srcs = new ArrayList<String>();
	
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
	
	/** �ǥХå��¹ԥ��ץ�����̵ͭ by inui */
	public static boolean debugOption = false;
	
	/** ɸ�����Ϥ��� LMNtal �ץ������ɤ߹��४�ץ���� 2006.07.11 inui */
	public static boolean stdinLMN = false;
	
	/** ɸ�����Ϥ��� ���̿������ɤ߹��४�ץ���� 2006.07.11 inui */
	public static boolean stdinTAL = false;
		
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
	
	/** dump�򥫥顼�ˤ���⡼�� */
	public static boolean colorMode = false;//2006.11.13 inui
	
	////////////////////////////////////////////////////////////////
	
	/** ����åɤ��ȤΥ��ȥ��Ƴ�ƥ��ȡ����Ƴ�ƥ��Ȥμ¹Ի���¬�� */
	public static final int PROFILE_BYDRIVEN = 0;
	/** �롼�뤴�Ȥμ¹Ի��֡���Բ����Ŭ�Ѳ����¬�� */
	public static final int PROFILE_BYRULE = 1;
	/** �롼�뤴�Ȥμ¹Ի��֡���Բ����Ŭ�Ѳ�����Хå��ȥ�å���������å����Բ����¬�� */
	public static final int PROFILE_BYRULEDETAIL = 2;
	/** �롼�뤴�Ȥˡ�����å��衢�ƥ��Ȥμ������¬�� */
	public static final int PROFILE_ALL = 3;

	/** -profile���ץ����̵������Υץ�ե�����ܺ��٥�٥� */
	public static final int PROFILE_INIT  = -1;
	/** -profile���ץ���������Υǥե���ȤΥץ�ե�����ܺ��٥�٥� */
	public static final int PROFILE_DEFAULT = 0;
	/** ������¹ԥ�٥� */
	public static int profile = PROFILE_INIT;

	////////////////////////////////////////////////////////////////

	public static int majorVersion = 0;
	public static int minorVersion = 0;
	
	/**
	 * �롼�뺸�դ˽и����롢����åɿ��ξ�¤�����
	 */
	public static int threadMax = 128;
	
	/**
	 * ����å�ۣ��������Ѥ����Ȥ��Ρ��Ѵ���Υ롼�������
	 */
	public static boolean dumpConvertedRules = false;
	
	
	
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public static boolean fTool = false;
	public static LMNtalTFrame LMNtool;
	
	public static void initTool(){
		if(!Env.fTool) return;
		LMNtool = new LMNtalTFrame();
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
	 * �� GUI �⡼�ɡ�
	 */
	public static boolean fGUI = false;
	public static gui.LMNtalFrame gui;
	public static void initGUI(){
		if(!Env.fGUI){ return; }
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		gui = new gui.LMNtalFrame();
	}
	
	/**
	 * CGI �⡼��
	 */
	public static boolean fCGI = false;
	
	/**
	 * REMAIN �⡼��
	 */
	public static boolean fREMAIN = false;
	public static LMNtalRuntime remainedRuntime;
	
	/**
	 * REPL �⡼��
	 */
	public static boolean fREPL = false;
	
	/**
	 * 060804
	 * safe mode
	 */
	public static boolean safe = false;
	
	/**
	 * 060804
	 * safe mode
	 */
	public static int maxStep = 1000;
	
	/**
	 * 060804
	 * safe mode
	 */
	public static int counter = 0;

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
	
	/** LMNtalRuntime�Υ��󥹥��� */
	public static LMNtalRuntime theRuntime;

	/** @return �롼�륹��åɤμ¹Ԥ��³���Ƥ褤���ɤ��� */
	public static boolean guiTrace() {
		if(gui == null) return true;
		if(null != gui){
			gui.onTrace();
		}
		
		return true;
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
	/* type inference �ط� by kudo */
	/** �������ƥ� on/off */
	public static boolean fType = false;
	
	/** �ƿ�����ͭ����̵�� */
	public static boolean flgOccurrenceInference = false;
	public static boolean flgQuantityInference = true;
	public static boolean flgArgumentInference = true;

	/** ��������򺮤��Ƹ��̤�Ŭ�� */
	public static final int COUNT_MERGEANDAPPLY = 0;
	/** �������줴�Ȥ˸��̤�Ŭ�Ѥ��Ƥ��麮���� */
	public static final int COUNT_APPLYANDMERGE = 1;
	/** �������줴�Ȥ�Ŭ�Ѳ������� */
	public static final int COUNT_APPLYANDMERGEDETAIL = 2;
	/** default */
	public static final int COUNT_DEFAULT = COUNT_APPLYANDMERGEDETAIL;
	
	/** �Ŀ��β��ϤΥ�٥� */
	public static int quantityInferenceLevel = COUNT_DEFAULT;
	
	/** �������줿���Ƥξ����ɽ�� */
	public static boolean flgShowAllConstraints = false;
	
	/** ������̤�ɽ�� */
	public static boolean flgShowConstraints = false;
	
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
	
	//�Ԥ߾夲
	public static boolean fMerging = false;
}
