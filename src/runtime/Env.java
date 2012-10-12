package runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import util.Util;

public final class Env
{
	public static final String LMNTAL_VERSION = "1.21.20111226 dev";

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
	public static boolean compileonly = true;

	/** SLIM�Ѥ����̿�������Ϥ���⡼�ɡ� */
	public static boolean slimcode = false;
	
	/** ����Ĥ�findatom��ޤ����̿�������Ϥ���⡼�ɡ� */
	public static boolean findatom2 = false;

	/** ���Ƴ�ƥ��ȥ���꡼�Υ⡼�ɡ� */
	public static boolean memtestonly = false;

	/** ��������̤�Ǿ������� */
	public static boolean fMemory = true;

	public static boolean fThread = true;

	/**
	 * <p>�������{@code swaplink}̿�����Ѥ��롣</p>
	 */
	public static boolean useSwapLink = false;

	/**
	 * <p>�������{@code cyclelinks}̿�����Ѥ��롣</p>
	 */
	public static boolean useCycleLinks = false;

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

	public static boolean showlongrulename = false;

	// PROXY ��ɽ�������ʤ� 2005/02/03 T.Nagata ���ץ���� --hideproxy
	// �ǥե���Ȥ�ͭ�� 2005/10/14 mizuno
	public static boolean hideProxy = true;

	/**
	 * �ץ�����Ϳ�������
	 */
	public static List<String> argv = new ArrayList<String>();

	/**
	 * �������ե�����
	 */
	public static List<String> srcs = new ArrayList<String>();

	/**
	 * ̤����ѥ���饤�֥������Ѥ���
	 */
	public static boolean fUseSourceLibrary = false;

	/** �ǥХå��¹ԥ��ץ�����̵ͭ by inui */
	public static boolean debugOption = false;

	/** ɸ�����Ϥ��� LMNtal �ץ������ɤ߹��४�ץ���� 2006.07.11 inui */
	public static boolean stdinLMN = false;

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

	public static boolean preProcess0 = false;

	/** ���ȥ�̾��ɽ������Ĺ�� */
	public static int printLength = 14;

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
		Util.errPrintln(getIndent(depth) + o);
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
		if(debug > 0) Util.println(getIndent(depth) + o);
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
		Util.println(getIndent(depth) + o);
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
	public static<E> String parray(Collection<E> l, String delim) {
		StringBuffer s = new StringBuffer();
		for(Iterator<E> i=l.iterator();i.hasNext();) {
			s.append( i.next().toString()+(i.hasNext() ? delim:"") );
		}
		return s.toString();
	}
	public static<E> String parray(Collection<E> l) {
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

	/**
	 * ��ĥ���ޥ�ɥ饤������򤳤�˳�Ǽ����
	 */
	public static Map<String, String> extendedOption = new HashMap<String, String>();
	public static String getExtendedOption(String key) {
		if(!extendedOption.containsKey(key)) return "";
		return extendedOption.get(key);
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

	/** ������̤�ɽ�� */
	public static boolean flgShowConstraints = false;

	////////////////////////////////////////////////////////////////

	private static int nErrors = 0;
	private static int nWarnings = 0;

	public static void clearErrors()
	{
		nErrors = 0;
		nWarnings = 0;
	}

	public static void error(String text)
	{
		Util.errPrintln(text);
		nErrors++;
	}

	public static void warning(String text)
	{
		Util.errPrintln(text);
		nWarnings++;
	}

	public static int getErrorCount()
	{
		return nErrors;
	}

	public static int getWarningCount()
	{
		return nWarnings;
	}

	//�Ԥ߾夲
	public static boolean fMerging = false;

	/** ��ĤΥ롼��Υ���ѥ����Ԥ� (for SLIM model checking mode) */
	public static boolean compileRule = false;

	/** hyperlink */
	public static boolean hyperLink    = false;//seiji
	public static boolean hyperLinkOpt = false;//seiji
}
