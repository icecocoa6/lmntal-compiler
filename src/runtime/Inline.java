package runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import util.Util;

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
 *   INLINE  [1, <unitName>, 0]
 * </PRE>
 * 
 * @author hara
 */
public class Inline {
	// InlineUnit.name -> InlineUnit
	public static Map inlineSet = new HashMap();
	
	/** ����ѥ���ץ����� */
	static Process cp;
	
	/** LMntal �饤�֥���õ���ѥ� */
	static List classPath = new ArrayList();
	static {
		classPath.add(new File(System.getProperty("java.class.path")));
//		classPath.add(new File("."));
//		classPath.add(new File("lmntal.jar"));
	}
	
	/**
	 * �ۤ��˥���ѥ��뤹�٤��ե����롣
	 * //# �Ԥˤ���̥ե�����˽��Ϥ��줿java�ե����뤬��������롣
	 */
	public static ArrayList othersToCompile = new ArrayList();
	
	/****** ����ѥ�����˻Ȥ� ******/
	
	/**
	 * InlineUnit ��̾���ȥ��饹�ե�������Ȥ���Ϥ��롣
	 */
	public static void showInlineList() {
		System.out.println("Inline");
		Iterator it = inlineSet.keySet().iterator();
		while (it.hasNext()) {
			System.out.println(Util.quoteString((String)it.next(), '"'));
		}
	}
	
	/**
	 * ����ѥ��뤵�줿����饤�󥳡��ɤ��ɤ߹���� InlineUnit �˴�Ϣ�դ���
	 */
	public static void initInline() {
		try {
			if(cp!=null) {
				// ����ѥ��뤷�Ƥ�ץ����Υ��顼���Ϥ������
				// ����򤷤ʤ��ȡ����顼���������󤢤�Ȥ��ǥåɥ�å��ˤʤäƻߤޤ롪
				BufferedReader br = new BufferedReader(new InputStreamReader(cp.getErrorStream()));
				String el;
				while( (el=br.readLine())!=null ) Env.p(el);
				cp.waitFor();
				Env.d("Compile result :  "+cp.exitValue());
				if(cp.exitValue()==1) {
					System.err.println("Failed in compiling.");
					//���Ϥ���Ĺ�ˤʤ�Τǥ����ȥ����Ȥ��ޤ�����2006.07.05 inui
//					System.err.println("Failed in compiling. Commandline was :");
//					System.err.println(compileCommand);
				} 
				cp = null;
			}
		} catch (Exception e) {
			Env.d(e);
		}
		for(Iterator ui = inlineSet.values().iterator();ui.hasNext();) {
			InlineUnit u = (InlineUnit)ui.next();
			u.attach();
		}
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
	 * ������ɬ�פ˱����ƥ���饤��̿�����Ͽ���롣
	 * ���٤ƤΥ��ȥ���Ф��Ƥ����Ƥ֤٤���
	 * @param unitName
	 * @param funcName
	 */
	public static void register(String unitName, String funcName) {
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
		if(!inlineSet.containsKey(unitName)) inlineSet.put(unitName, new InlineUnit(unitName));
		((InlineUnit)inlineSet.get(unitName)).register(funcName, type);
	}
	
	public static void terminate() {
		if(cp!=null) cp.destroy();
	}
	
	public static String getCode(int atomID, String unitName, int codeID) {
		if(!Inline.inlineSet.containsKey(unitName)) return null;
		InlineUnit iu = (InlineUnit)Inline.inlineSet.get(unitName);
		return iu.getCode(codeID);
	}
	
	static List compileCommand = new ArrayList();
	/**
	 * ɬ�פ˱����ƥ����ɤ������ȥ���ѥ����Ԥ���
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
			for(Iterator ci=classPath.iterator();ci.hasNext();) {
				path.append(ci.next());
				path.append(sep);
			}
			compileCommand.clear();			// n-kato
//			compileCommand.add("javaca");	// hara
			compileCommand.add("javac");	// n-kato
			compileCommand.add("-classpath");
			compileCommand.add(path);
			
			StringBuffer srcs = new StringBuffer("");
			boolean do_compile = false;
			for(Iterator iu = inlineSet.values().iterator();iu.hasNext();) {
				InlineUnit u = (InlineUnit)iu.next();
				if(!u.isCached()) {
					compileCommand.add(InlineUnit.srcFile(u.name));
					InlineUnit.classFile(u.name).delete();
					do_compile = true;
				}
			}
			compileCommand.addAll(othersToCompile);
			if(do_compile) {
				Env.d("Compile command line: "+compileCommand);
				String cmd[] = new String[compileCommand.size()];
				for(int i=0;i<compileCommand.size();i++) {
					cmd[i] = compileCommand.get(i).toString();
				}
				cp = Runtime.getRuntime().exec(cmd);
			}
		} catch (java.io.IOException e) {
			System.err.println("\n*** Compile failed. javac not found on PATH. javac is necessary for compiling inline code. ***\n");
			Env.d(e);
		}
	}
	
	/****** �¹Ի��˻Ȥ� ******/
	
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
	/**
	 * �����ɥ���饤��̿���¹Ԥ��롣
	 * @param obj
	 * @return
	 */
	public static boolean callGuardInline(String guardID, Membrane mem, Object obj) {
//		System.out.println(Inline.inlineSet);
		Iterator it = Inline.inlineSet.values().iterator();
		boolean res = false;
		while(it.hasNext()) {
			InlineUnit iu = (InlineUnit)it.next();
//System.out.println("iu.name "+iu.name);
			try {
//				res = iu.inlineCode.runGuard(guardID, mem, obj);
				if(iu.customGuard!=null) {
					res = iu.customGuard.run(guardID, mem, obj);
					return res;
				}
//				System.out.println("GUARD result = "+res);
			} catch(GuardNotFoundException e) {
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}
