/*
 * ������: 2003/10/24
 *
 */
package runtime;
import java.util.*;

/**
 * �Ķ����ǥХå��ѡ�
 * @author hara
 */
public final class Env {
	/**
	 * Debug level.
	 */
	public static int debug = 0;
	
	/** �����ƥ�롼�륻�åȤ�̿����¹Ԥ�ɽ������ */
	static final int DEBUG_SYSTEMRULESET = 7;
	/** �ǥե���ȤΥǥХå���٥� */
	static final int DEBUG_DEFAULT = 1;
	
	/**
	 * Optimization level.
	 */
	public static int optimize = 0;
	
	public static final int VERBOSE_DEFAULT = 2;
	/**
	 * ���ȥ������Ÿ�������Ĺ��٥� */
	public static final int VERBOSE_EXPANDATOMS = 3;
	/**
	 * �롼�륻�åȤ����Ƥ�Ÿ�������Ĺ��٥� */
	public static final int VERBOSE_EXPANDRULES = 4;
	/**
	 * verbose level.
	 */
	public static int verbose = VERBOSE_DEFAULT;
	
	/**
	 * ������¹�
	 */
	public static boolean fRandom = false;

	/**
	 * �ȥ졼���¹�
	 */
	public static boolean fTrace = false;
		
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
	
	/**
	 * General dumper for debug
	 * @param o Object to print
	 */
	public static void d(Object o) {
		d(o, 0);
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
}
