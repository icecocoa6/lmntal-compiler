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
	
	/** ����ѥ���ץ����� */
	static Process cp;
	
	
	static List classPath = new ArrayList();
	static {
		classPath.add(new File("."));
		classPath.add(new File("lmntal.jar"));
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
		} catch (Exception e) {
			Env.e("!! catch !! "+e.getMessage()+"\n"+Env.parray(Arrays.asList(e.getStackTrace()), "\n"));
		}
		Iterator ui = inlineSet.values().iterator();
		while(ui.hasNext()) {
			InlineUnit u = (InlineUnit)ui.next();
			u.attach();
		}
	}
	
	/**
	 * ����饤�󥳡��ɤΥ������ե�����Υѥ����֤����Ǹ�� / ��ޤࡣ
	 * @param unitName
	 * @return
	 */
	public static File path_of_unitName(String unitName) {
		if(unitName.equals(InlineUnit.DEFAULT_UNITNAME)) return new File("");
		File path = new File(unitName).getParentFile();
		path = new File(path + "/.lmntal_inline/");
		return path;
	}
	
	/**
	 * ���饹̾���֤�
	 * @param unitName
	 * @return
	 */
	public static String className_of_unitName(String unitName) {
		String o = new File(unitName).getName();
		o = o.replaceAll("\\.lmn$", "");
		// ���饹̾�˻Ȥ��ʤ�ʸ������
		o = o.replaceAll("\\-", "");
		o = "SomeInlineCode"+o;
		return o;
	}
	/**
	 * ����饤�󥳡��ɤΥ������ե�����̾���֤����ѥ��ա�
	 * @param unitName
	 * @return
	 */
	public static File fileName_of_unitName(String unitName) {
		return new File(Inline.path_of_unitName(unitName)+"/"+className_of_unitName(unitName)+".java");
	}
	
	/**
	 * ���ꤷ���ե��󥯥�̾����ĥ�����ID ���֤���
	 * @param codeStr
	 * @return codeID
	 */
	public static int getCodeID(String unitName, String codeStr) {
		if(!inlineSet.containsKey(unitName)) return -1;
		return ((InlineUnit)inlineSet.get(unitName)).getCodeID(codeStr);
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
			Iterator iu = inlineSet.values().iterator();
			boolean do_compile = false;
			while(iu.hasNext()) {
				InlineUnit u = (InlineUnit)iu.next();
				if(!u.isCached()) {
					srcs.append(fileName_of_unitName(u.name));
					srcs.append(" ");
					do_compile = true;
				}
			}
			if(do_compile) {
				String cmd = "javac -classpath "+path+" "+srcs;
				Env.d("Compile command line: "+cmd);
				cp = Runtime.getRuntime().exec(cmd);
			}
		} catch (Exception e) {
			Env.d("!!! "+e+Arrays.asList(e.getStackTrace()));
		}
	}
	
	/**
	 * ����饤��̿���¹Ԥ��롣
	 * @param atom �¹Ԥ��٤����ȥ�̾����ĥ��ȥ�
	 */
	public static void callInline(Atom atom, String unitName, int codeID) {
		Env.d("=> call Inline "+unitName);
//		System.out.println(inlineSet);
		if(inlineSet.containsKey(unitName)) {
			((InlineUnit)inlineSet.get(unitName)).callInline(atom, codeID);
		}
	}
}
