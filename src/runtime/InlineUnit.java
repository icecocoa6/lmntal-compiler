package runtime;

import java.util.*;
import java.io.*;

/**
 * ���ĤΥ���饤�󥳡��ɤΤޤȤޤ���б����륯�饹��
 * �ޤȤޤ�Ȥϡ��̾� .lmn �Υ饤�֥��ե�����Τ��ȡ�
 * 
 * @author hara
 *
 */
public class InlineUnit {
	/** ����̾�ʥե�����ʤ�ե�����̾���н������λ��� "-"�� */
	String name;
	public static final String DEFAULT_UNITNAME = "-";
	
	/** ����饤�󥯥饹�����Ѳ�ǽ�λ������Υ��֥������Ȥ����롣*/
	public static InlineCode inlineCode;
	
	/** Hash { ����饤�󥳡���ʸ���� => ��դ�Ϣ�� } */
	public static Map codes = new HashMap(); 
	
	/** List ����饤�����������ʸ���� */
	public static List defs = new ArrayList(); 
	
	/** ��դ�Ϣ�֡�����饤�󥳡���ʸ�����1��1 */
	static int codeCount = 0;
	
	/** ����饤��¹ԥ��ȥ� */
	static final int EXEC   = 0;
	
	/** ����饤������ԥ��ȥ� */
	static final int DEFINE = 1;
	
	/****** ����ѥ�����˻Ȥ� ******/
	
	/**
	 * class �ե����뤬�ǿ����ɤ������֤�
	 */
	public boolean isCached() {
		// ?.lmn
		File src = new File(name);
		// ?.class
		File dst = classFile(name);
//		System.out.println(src.lastModified()+" "+src);
//		System.out.println(dst.lastModified()+" "+dst);
		
		// src ��̵���Τϡ�"-" �ΤȤ�
		if(!dst.exists() || !src.exists()) return false;
		return src.lastModified() <= dst.lastModified();
	}
	
	InlineUnit(String name) {
		this.name = name;
	}
	
	/**
	 * �����ɤ��б����� ID ���֤���
	 * @param codeStr
	 * @return
	 */
	public int getCodeID(String codeStr) {
		return codes.containsKey(codeStr) ? ((Integer)codes.get(codeStr)).intValue() : -1;
	}
	
	/**
	 * ����饤�󥢥ȥ����Ͽ���롣
	 * @param code ���ȥ�̾
	 * @param type ����饤��¹ԥ��ȥ� => EXEC ,  ����饤��������ȥ� => DEFINE
	 */
	public void register(String code, int type) {
		switch(type) {
		case EXEC:
			if(Env.debug>=Env.DEBUG_TRACE) Env.d("Register inlineCode : "+code);
			codes.put(code, new Integer(codeCount++));
			break;
		case DEFINE:
			if(Env.debug>=Env.DEBUG_TRACE) Env.d("Register inlineDefineCode : "+code);
			defs.add(code);
			break;
		}
	}
	
	/**
	 * �����ɤ��������롣
	 */
	public void makeCode() {
		if(isCached()) return;
		try {
			if(codes.isEmpty() && defs.isEmpty()) return;
			Iterator i;
			
			String className = className(name);
			File outputFile = srcFile(name);
			if(!outputFile.getParentFile().exists()) {
				outputFile.getParentFile().mkdirs();
			}
			PrintWriter p = new PrintWriter(new FileOutputStream(outputFile));
//			Env.d("make inline code "+name);
			
			//p.println("package runtime;");
			p.println("import runtime.*;");
			p.println("import java.util.*;");
			
			i = defs.iterator();
			while(i.hasNext()) {
				String s = (String)i.next();
				p.println(s);
			}
			p.println("public class "+className+" implements InlineCode {");
			p.println("\tpublic void run(Atom me, int codeID) {");
			p.println("\t\tAbstractMembrane mem = me.getMem();");
			//p.println("\t\tEnv.p(\"-------------------------- \");");
			//p.println("\t\tEnv.d(\"Exec Inline \"+me+codeID);");
			p.println("\t\tswitch(codeID) {");
			i = codes.keySet().iterator();
			while(i.hasNext()) {
				String s = (String)i.next();
				int codeID = ((Integer)(codes.get(s))).intValue();
				p.println("\t\tcase "+codeID+": {");
				//p.println("\t\t\t/*"+s.replaceAll("\\*\\/", "* /").replaceAll("\\/\\*", "/ *")+"*/");
				p.println("\t\t\t"+s);
				p.println("\t\t\tbreak; }");
			}
			p.println("\t\t}");
			p.println("\t}");
			p.println("}");
			p.close();
			
			Env.d("Class "+className+" written to "+outputFile);
		} catch (Exception e) {
			Env.d(e);
		}
	}
	
	/****** �¹Ի��˻Ȥ� ******/
	
	/**
	 * ��ʬ���б����륤��饤�󥳡��ɥ��饹���ɤ߹��ࡣ
	 */
	public void attach() {
		// jar �ǽ����Ϥ�ư����ȡ�����ʥե����뤫�饯�饹����ɤ��뤳�Ȥ��Ǥ��ʤ��ߤ�����
		String cname = className(name);
		FileClassLoader cl = new FileClassLoader();
		Env.d("Try loading "+classFile(name));
		try {
			Object o = cl.loadClass(name).newInstance();
			if (o instanceof InlineCode) {
				inlineCode = (InlineCode)o;
			}
		} catch (Exception e) {
			//Env.e("!! catch !! "+e.getMessage()+"\n"+Env.parray(Arrays.asList(e.getStackTrace()), "\n"));
		}
		if (inlineCode == null) {
			Env.d("Failed in loading "+cname);
		} else {
			Env.d(cname+" Loaded");
		}
	}
	
	/**
	 * ����饤��̿���¹Ԥ��롣
	 * @param atom �¹Ԥ��٤����ȥ�̾����ĥ��ȥ�
	 */
	public void callInline(Atom atom, int codeID) {
		//Env.d(atom+" "+codeID);
		Env.d(" => call Inline "+atom.getName()+" "+codeID);
		if(inlineCode==null) return;
		inlineCode.run(atom, codeID);
	}

	/**
	 * ����饤�󥳡��ɤΥ������ե�����Υѥ����֤����Ǹ�� / �ϴޤޤʤ���
	 * @param unitName
	 * @return PATH/
	 */
	public static File srcPath(String unitName) {
		File path;
		if(unitName.equals(DEFAULT_UNITNAME)) {
			path = new File(".");
		} else {
			path = new File(unitName).getParentFile();
			if(path==null) path = new File(".");
		}
		path = new File(path + "/.lmntal_inline");
		return path;
	}

	/**
	 * ���饹̾���֤�
	 * @param unitName
	 * @return SomeClass
	 */
	public static String className(String unitName) {
		// ���䤷��
		String o = new File(unitName).getName();
		if(unitName.endsWith(".lmn") || unitName.equals(InlineUnit.DEFAULT_UNITNAME)) {
			o = o.replaceAll("\\.lmn$", "");
			// ���饹̾�˻Ȥ��ʤ�ʸ������
			o = o.replaceAll("\\-", "");
			o = "SomeInlineCode"+o;
		}
		return o;
	}
	
	/**
	 * ����饤�󥳡��ɤΥ������ե�����̾���֤����ѥ��ա�
	 * @param unitName
	 * @return PATH/SomeClass.java
	 */
	public static File srcFile(String unitName) {
		return new File(srcPath(unitName)+"/"+className(unitName)+".java");
	}

	/**
	 * ����饤�󥳡��ɤΥ��饹�ե�����̾���֤����ѥ��ա�
	 * @param unitName
	 * @return
	 */
	public static File classFile(String unitName) {
		return new File(srcPath(unitName) + "/" + className(unitName) + ".class");
	}
}
