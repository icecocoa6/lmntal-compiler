/*
 * ������: 2003/12/16
 *
 */
package runtime;

import java.io.*;
import java.util.*;

/**
 * ����饤�������<BR>
 * 
 * <UL>
 * <LI>"/��inline��/" �ǻϤޤ�ե��󥯥�̾����ĥ��ȥ�򥤥�饤�󥳡��ɤȤ��ư�����
 * 
 * <LI>"/��inline_define��/" �ǻϤޤ�ե��󥯥�̾����ĥ��ȥ�򥤥�饤����������ɤȤ��ư�����
 * 
 * <LI>����饤����������ɤϡ��ޤȤ�����Ū���������롣���饹����ʤɡ����Ū�ʤ�ΤϤ����˽񤱤롣
 * 
 * <LI>���륤��饤�󥳡��ɤμ¹Ԥϡ�
 * ���Υ����ɤ����դ˴ޤޤ��롼���Ŭ�Ѥ���ľ��Υ����ߥ󥰤Ǽ¹Ԥ���롣
 * 
 * <LI>�б��������̿��� INLINE �Ǥ��롣
 * 
 * <LI>�����Υ��ȥ�򤤤����褦�ˡ����٤Ƥ� NEWATOM, LINK �ʤɤ�������äƤ���
 * INLINE ̿���ȯ�Ԥ��롣
 * 
 * <LI>���̿�����
 * </UL>
 * <PRE>
 *   NEWATOM [1, 0, abc_0]
 *   ...������
 *   INLINE  [1, 0]
 * </PRE>
 * 
 * @author hara
 */
public class Inline {
	// InlineUnit.name -> InlineUnit
	public static Map inlineSet = new HashMap();
	
	/** ����饤�󥯥饹�����Ѳ�ǽ�λ������Υ��֥������Ȥ����롣*/
	public static InlineCode inlineCode;
	
	/** ����ѥ���ץ����� */
	static Process cp;
	
	
	static List classPath = new ArrayList();
	static {
		classPath.add(".");
		classPath.add("lmntal.jar");
	}
	
	/**
	 * ����饤���Ȥ�����ν�������¹Ի��˸Ƥ֡�
	 *
	 */
	public static void initInline() {
		try {
			if(cp!=null) {
				// ����ѥ��뤷�Ƥ�ץ����Υ��顼���Ϥ������
				// ����򤷤ʤ��ȡ����顼���������󤢤�Ȥ��ǥåɥ�å��ˤʤäƻߤޤ롪
				BufferedReader br = new BufferedReader(new InputStreamReader(cp.getErrorStream()));
				String el;
				while( (el=br.readLine())!=null ) {
					System.err.println(el);
				}
				cp.waitFor();
				Env.d("Compile result :  "+cp.exitValue());
				cp = null;
			}
			// jar �ǽ����Ϥ�ư����ȡ�����ʥե����뤫�饯�饹����ɤ��뤳�Ȥ��Ǥ��ʤ��ߤ�����
			ClassLoader cl = new FileClassLoader();
			Object o = cl.loadClass("MyInlineCode").newInstance();
			if (o instanceof InlineCode) {
				inlineCode = (InlineCode)o;
			}
			//inlineCode = (InlineCode)Class.forName("MyInlineCode").newInstance();
			//Env.d(Class.forName("MyInlineCode").getField("version"));
		} catch (Exception e) {
			//Env.e("!! catch !! "+e.getMessage()+"\n"+Env.parray(Arrays.asList(e.getStackTrace()), "\n"));
		}
		if (inlineCode != null) { Env.d("MyInlineCode Loaded"); }
		else if (inlineCode == null) { Env.d("Failed in loading MyInlineCode"); }
	}
	
	/**
	 * LMNtal �������ե�����̾���б����륯�饹̾���֤�
	 * @param lmn LMNtal �������ե����롣�ѥ���ޤ�Ǥ�褤��
	 * @return
	 */
	public static String className_of_lmntalFilename(String lmn) {
		// �ѥ������
		String path = lmn.replaceFirst("([\\/])[^\\/]+$", "$1");
		
		String o = lmn.replaceAll("^.*?[\\/]([^\\/]+)$", "$1");
		o = o.replaceAll("\\.lmn$", "");
		// ���饹̾�˻Ȥ��ʤ�ʸ������
		o = o.replaceAll("\\-", "");
		o = "Inline"+o;
		return o;
	}
	/**
	 * ���ꤷ���ե��󥯥�̾����ĥ�����ID ���֤���
	 * @param codeStr
	 * @return codeID
	 */
	public static int getCodeID(String unitName, String codeStr) {
		return getUnit(unitName).getCodeID(codeStr);
	}
	
	/**
	 * �ѡ�����˥��ȥब�ФƤ���ȸƤФ�롣
	 * ������ɬ�פ˱����ƥ���饤��̿�����Ͽ���롣
	 * @param unitName
	 * @param funcName
	 */
	public static void add(String unitName, String funcName) {
		if(funcName==null) return;
		
		int type=0;
		if(funcName.startsWith("/*inline*/")) {
			type = InlineUnit.EXEC;
		} else if(funcName.startsWith("/*inline_define*/")) {
			type = InlineUnit.DEFINE;
		} else {
			return;
		}
		
		//��Ͽ
		getUnit(unitName).register(funcName, type);
	}
	
	static InlineUnit getUnit(String unitName) {
		if(!inlineSet.containsKey(unitName)) {
			inlineSet.put(unitName, new InlineUnit(unitName));
		}
		return (InlineUnit)inlineSet.get(unitName);
	}
	
	public static void compile() {
	}
	
	/**
	 * �����ɤ��������롣
	 * TODO java �ե������̾���򡢥���ѥ��뤹��ե�����̾��Ʊ���ˤ��롣oneLiner �� REPL �λ��� "-"
	 * TODO ��������Ƥ��Ȥ���������ѥ��뤹�롣
	 */
	public static void makeCode() {
		Iterator it = inlineSet.values().iterator();
		while(it.hasNext()) {
			InlineUnit u = (InlineUnit)it.next();
			u.makeCode();
		}
		
		try {
			
			// ��Ʊ�����̥ץ����ǥ���ѥ��뤷�ʤ��顢���ߤΥץ����Ǥۤ��λ����롣
			// OS �Ȥ��ˤ�äƥ��饹�ѥ��ζ��ڤ�ʸ���� ; ���ä��� : ���ä��ꤹ��Τ�ưŪ�˼���
			StringBuffer path = new StringBuffer("");
			String sep = System.getProperty("path.separator");
			Iterator ci=classPath.iterator();
			while(ci.hasNext()) {
				path.append(ci.next());
				path.append(sep);
			}
			StringBuffer srcs = new StringBuffer("");
			Iterator ik = inlineSet.keySet().iterator();
			while(ik.hasNext()) {
				InlineUnit u = getUnit((String)ik.next());
				if(!u.isCached()) srcs.append(className_of_lmntalFilename(u.name)+".java ");
			}
			String cmd = "javac -classpath "+path+" "+srcs;
			Env.d("Compile command line: "+cmd);
			cp = Runtime.getRuntime().exec(cmd);
		} catch (Exception e) {
			Env.d("!!! "+e+Arrays.asList(e.getStackTrace()));
		}
		
	}
	
	/**
	 * ����饤��̿���¹Ԥ��롣
	 * @param atom �¹Ԥ��٤����ȥ�̾����ĥ��ȥ�
	 */
	public static void callInline(Atom atom, String unitName, int codeID) {
		getUnit(unitName).callInline(atom, codeID);
	}
}
