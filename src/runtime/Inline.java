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
	/** ����饤�󥯥饹�����Ѳ�ǽ�λ������Υ��֥������Ȥ����롣*/
	public static InlineCode inlineCode;
	
	/** ����ѥ���ץ����� */
	static Process cp;
	
	/** Hash { ����饤�󥳡���ʸ���� -> ��դ�Ϣ�� } */
	public static Map code = new HashMap(); 
	
	/** List ����饤�����������ʸ���� */
	public static List defs = new ArrayList(); 
	
	/** ��դ�Ϣ�� */
	static int codeCount = 0;
	
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
			Env.d(e);
		}
		Env.d("inline = "+inlineCode);
	}
	
	/**
	 * ���ꤷ���ե��󥯥�̾����ĥ�����ID ���֤���
	 * @param name
	 * @return codeID
	 */
	public static int getCodeID(String name) {
		try {
			return ((Integer)code.get(name)).intValue();
		} catch (Exception e) {
			return -1;
		}
	}
	/**
	 * �ѡ�����˥��ȥब�ФƤ���ȸƤФ�롣
	 * ������ɬ�פ˱����ƥ���饤��̿�����Ͽ���롣
	 * @param atom
	 */
	public static void add(String src) {
		if(src.startsWith("/*inline*/")) {
		//if(src.startsWith("a")) {
			//��Ͽ
			Env.d("Register inlineCode : "+src);
			code.put(src, new Integer(codeCount++));
		} else if(src.startsWith("/*inline_define*/")) {
			//��Ͽ
			Env.d("Register inlineDefineCode : "+src);
			defs.add(src);
		}
	}
	
	/**
	 * �����ɤ��������롣
	 *
	 */
	public static void makeCode() {
		try {
			if(code.isEmpty() && defs.isEmpty()) return;
			Iterator i;
			
			PrintWriter p = new PrintWriter(new FileOutputStream("MyInlineCode.java"));
			Env.d("make inline code "+code);
			
			//p.println("package runtime;");
			p.println("import runtime.*;");
			
			i = defs.iterator();
			while(i.hasNext()) {
				String s = (String)i.next();
				p.println(s);
			}
			p.println("public class MyInlineCode implements InlineCode {");
			p.println("\tpublic static String version=\"static string.\";");
			p.println("\tpublic void run(Atom me, int codeID) {");
			//p.println("\t\tEnv.p(\"-------------------------- \");");
			//p.println("\t\tEnv.d(\"Exec Inline \"+me+codeID);");
			p.println("\t\tswitch(codeID) {");
			i = code.keySet().iterator();
			while(i.hasNext()) {
				String s = (String)i.next();
				int codeID = ((Integer)(code.get(s))).intValue();
				p.println("\t\tcase "+codeID+": ");
				//p.println("\t\t\t/*"+s.replaceAll("\\*\\/", "* /").replaceAll("\\/\\*", "/ *")+"*/");
				p.println("\t\t\t"+s);
				p.println("\t\t\tbreak;");
			}
			p.println("\t\t}");
			p.println("\t}");
			p.println("}");
			p.close();
			
			// ��Ʊ�����̥ץ����ǥ���ѥ��뤷�ʤ��顢���ߤΥץ����Ǥۤ��λ����롣
			cp = Runtime.getRuntime().exec("javac -classpath .;lmntal.jar MyInlineCode.java");
		} catch (Exception e) {
			Env.d("!!! "+e.getMessage()+Arrays.asList(e.getStackTrace()));
		}
		
	}
	
	/**
	 * ����饤��̿���¹Ԥ��롣
	 * @param atom �¹Ԥ��٤����ȥ�̾����ĥ��ȥ�
	 */
	public static void callInline(Atom atom, int codeID) {
		//Env.d(atom+" "+codeID);
		if(inlineCode==null) return;
		//Env.d("=> call Inline "+atom.getName());
		inlineCode.run(atom, codeID);
	}
}
