/*
 * ������: 2003/12/16
 *
 * �����������줿�����Ȥ����������ƥ�ץ졼�Ȥ��ѹ����뤿��
 * ������ɥ� > ���� > Java > ���������� > �����ɤȥ�����
 */
package runtime;

import java.io.*;
import java.util.*;

/**
 * @author pa
 *
 * �����������줿�����Ȥ����������ƥ�ץ졼�Ȥ��ѹ����뤿��
 * ������ɥ� > ���� > Java > ���������� > �����ɤȥ�����
 */
public class Inline {
	/** ����饤�󥯥饹�����Ѳ�ǽ�λ������Υ��֥������Ȥ����롣*/
	public static InlineCode inlineCode;
	
	static Process cp;
	
	static List code = new ArrayList(); 
	
	/**
	 * ����饤���Ȥ�����ν�������¹Ի��˸Ƥ֡�
	 *
	 */
	public static void initInline() {
		try {
			inlineCode = (InlineCode)Class.forName("MyInlineCode").newInstance();
		} catch (Exception e) {
			Env.p(e);
		}
		Env.p("inline = "+inlineCode);
	}
	
	/**
	 * �ѡ�����˥��ȥब�ФƤ���ȸƤФ�롣
	 * ������ɬ�פ˱����ƥ���饤��̿�����Ͽ���롣
	 * @param atom
	 */
	public static void add(String src) {
		//if(src.startsWith("/*inline*/")) {
		if(src.startsWith("a")) {
			//��Ͽ
			Env.p("Register inlineCode : "+src);
			code.add(src);
		}
	}
	
	/**
	 * �����ɤ��������롣
	 *
	 */
	public static void makeCode() {
		try {
			PrintWriter p = new PrintWriter(new FileOutputStream("MyInlineCode.java"));
			Env.p("make inline code "+code);
			
			//p.println("package runtime;");
			p.println("import runtime.*;");
			p.println("public class MyInlineCode implements InlineCode {");
			p.println("\tpublic void run(Atom a) {");
			p.println("\t\tEnv.p(a);");
			p.println("\t\tswitch(a.getName().hashCode()) {");
			Iterator i = code.iterator();
			while(i.hasNext()) {
				String s = (String)i.next();
				p.println("\t\tcase "+s.hashCode()+": ");
				p.println("\t\t\t/*"+s+"*/");
				p.println("\t\t\tSystem.out.println(\"=> call Inline "+s+" \");");
				p.println("\t\t\tbreak;");
			}
			p.println("\t\t}");
			p.println("\t}");
			p.println("}");
			p.close();
			
			cp = Runtime.getRuntime().exec("javac MyInlineCode.java");
			BufferedReader br = new BufferedReader(new InputStreamReader(cp.getErrorStream()));
			String el;
			while( (el=br.readLine())!=null ) {
				System.err.println(el);
			}
			cp.waitFor();
			Env.p("Compile result :  "+cp.exitValue());
			
		} catch (Exception e) {
			Env.p("!!! "+e.getMessage()+e.getStackTrace());
		}
		
	}
	
	/**
	 * ����饤��̿���¹Ԥ��롣
	 * @param atom �¹Ԥ��٤����ȥ�̾����ĥ��ȥ�
	 */
	public static void callInline(Atom atom) {
		if(inlineCode==null) return;
		//Env.p("=> call Inline "+atom.getName());
		inlineCode.run(atom);
	}
}
