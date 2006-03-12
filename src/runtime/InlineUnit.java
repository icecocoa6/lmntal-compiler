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
	public InlineCode inlineCode;
	
	/** �������६���ɥ��饹�����Ѳ�ǽ�λ������Υ��֥������Ȥ����롣*/
	public CustomGuard customGuard;
	
	/** Hash { ����饤�󥳡���ʸ���� => ��դ�Ϣ�� } */
	public Map codes = new HashMap(); 
	/** codes �ε� */
	public List code_of_id = new ArrayList(); 
	
	/** List ����饤�����������ʸ���� */
	public List defs = new ArrayList(); 
	
	/** ��դ�Ϣ�֡�����饤�󥳡���ʸ�����1��1 */
	int codeCount = 0;
	
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
	
	public InlineUnit(String name) {
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
	 * ID ���б����륳���ɤ��֤���
	 */
	public String getCode(int id) {
		if(id<0 || code_of_id.size() <= id) return null;
		return (String)code_of_id.get(id);
	}
	
	/**
	 * ����饤�󥢥ȥ����Ͽ���롣
	 * @param code ���ȥ�̾
	 * @param type ����饤��¹ԥ��ȥ� => EXEC ,  ����饤��������ȥ� => DEFINE
	 */
	public void register(String code, int type) {
		switch(type) {
		case EXEC:
			if(Env.debug>=Env.DEBUG_TRACE) Env.d("Register inlineCode to "+name+" : "+code);
			codes.put(code, new Integer(codeCount));
			code_of_id.add(code);
			codeCount++;
			break;
		case DEFINE:
			if(Env.debug>=Env.DEBUG_TRACE) Env.d("Register inlineDefineCode to "+name+" : "+code);
			defs.add(code);
			break;
		}
	}

	/**
	 * �����ɤ��������롣���¹Ի������Ѥ��롣
	 */
	public void makeCode() {
		if(isCached()) return;
		try {
			String className = className(name);
			File outputFile = srcFile(name);
			if(!outputFile.getParentFile().exists()) {
				outputFile.getParentFile().mkdirs();
			}
	//		Env.d("make inline code "+name);
	
			makeCode(null, className, outputFile, true);
		} catch (Exception e) {
			Env.d(e);
		}
	}
	
	/**
	 * �����ɤ��������롣
	 */
	public void makeCode(String packageName, String className, File outputFile, boolean interpret) throws IOException {
		if(codes.isEmpty() && defs.isEmpty()) return;
		Iterator i;
		PrintWriter p = new PrintWriter(new FileOutputStream(outputFile));

		//p.println("package runtime;");
		if (packageName != null) {
			p.println("package " + packageName + ";");
		}
		p.println("import runtime.*;");
		p.println("import java.util.*;");
		
		i = defs.iterator();
		PrintWriter defaultPW = p;
		while(i.hasNext()) {
			String s = (String)i.next();
			s = s.replaceAll("\\/\\*__UNITNAME__\\*\\/", className(name));
			if (packageName != null) {
				s = s.replaceAll("\\/\\*__PACKAGE__\\*\\/", "package " + packageName + ";");
			}
//			System.out.println(s);
//			p.println(s);
			BufferedReader obr = new BufferedReader(new StringReader(s));
			String ss;
			while((ss=obr.readLine())!=null) {
				// //# �ǻϤޤ�Ԥϡ����ιԤΤ���ʹߤ����Ƥ�ե�����̾�Ȥߤʤ������ι԰ʹߤ���ꤵ�줿�ե�����˽��Ϥ���롣
				// ���κݡ�__UNITNAME__, __PACKAGE__ �Ϥ��줾������Ƥ��ִ�����롣
				if(ss.startsWith("//#")) {
					if(p!=defaultPW) p.close();
					String fname = ss.substring(3);
					if(fname.equals("")) {
						p = defaultPW;
					} else {
						File ofile = new File(outputFile.getParentFile()+"/"+fname);
						Inline.othersToCompile.add(ofile.toString());
//						System.out.println(ofile);
						p = new PrintWriter(new FileOutputStream(ofile));
					}
				}
				p.println(ss);
			}
		}
		p = defaultPW;
		
		if (interpret) {
			p.println("public class "+className+" implements InlineCode {");
		} else {
			p.println("public class "+className+" {");
		}
		p.println("\tpublic boolean runGuard(String guardID, Membrane mem, Object obj) throws GuardNotFoundException {");
		p.println("\t\ttry {");
		p.println("\t\tString name = \""+className(name)+"CustomGuardImpl\";\n");
//		p.println("\t\tSystem.out.println(\"Load \"+name);\n");
		p.println("\t\t	CustomGuard cg=(CustomGuard)Class.forName(name).newInstance();\n");
//		p.println("\t\t	System.out.println(\"CG\"+cg);");
		p.println("\t\t	if(cg==null) throw new GuardNotFoundException();\n");
		p.println("\t\t	return cg.run(guardID, mem, obj);\n");
		p.println("\t\t} catch(GuardNotFoundException e) {");
		p.println("\t\t	throw new GuardNotFoundException();\n");
		p.println("\t\t} catch(ClassNotFoundException e) {");
		p.println("\t\t} catch(InstantiationException e) {");
		p.println("\t\t} catch(IllegalAccessException e) {");
		p.println("\t\t} catch(Exception e) {\n");
		p.println("\t\t	e.printStackTrace();\n");
		p.println("\t\t}\n");
		p.println("\t\tthrow new GuardNotFoundException();\n");
		p.println("\t}");
		
		if (interpret) {
			//InlineCode ���饹�Υ��󥹥��󥹤Ȥ��ƻȤ�
			p.println("\tpublic void run(Atom me, int codeID) {");
		} else {
			//ľ�ܸƤӽФ��Τǡ�static �Ǥ褤
			p.println("\tpublic static void run(Atom me, int codeID) {");
		}
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
	}
	
	/****** �¹Ի��˻Ȥ� ******/
	
	/**
	 * ��ʬ���б����륤��饤�󥳡��ɥ��饹���ɤ߹��ࡣ
	 */
	public void attach() {
		// jar �ǽ����Ϥ�ư����ȡ�����ʥե����뤫�饯�饹����ɤ��뤳�Ȥ��Ǥ��ʤ��ߤ�����
		String cname = className(name);
		FileClassLoader.addPath(srcPath(name));
		FileClassLoader cl = new FileClassLoader();
		Env.d("Try loading "+className(name));
		Object o;
		o = newInstance(cl, className(name));
		if (o instanceof InlineCode) inlineCode = (InlineCode)o;
		o = newInstance(cl, className(name)+"CustomGuardImpl");
		if (o instanceof CustomGuard) customGuard = (CustomGuard)o;
		o = newInstance(cl, "translated."+className(name)+"CustomGuardImpl");
		if (o instanceof CustomGuard) customGuard = (CustomGuard)o;
		o = newInstance(cl, "translated.module_"+FileNameWithoutExt(name)+"."+className(name)+"CustomGuardImpl");
		if (o instanceof CustomGuard) customGuard = (CustomGuard)o;
		
		if (inlineCode == null) {
			Env.d("Failed in loading "+cname);
		} else {
			Env.d(cname+" Loaded");
		}
	}
	private Object newInstance(FileClassLoader cl, String name) {
		try {
//			System.out.print(name+"   ");
			Object o = cl.loadClass(name).newInstance();
//			System.out.println("OK");
			return o;
		} catch (Exception e) {
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//		} catch (IllegalAccessException e) {
//		} catch (InstantiationException e) {
//		} catch (NullPointerException e) {
		}
//		System.out.println("Fail");
		return null;
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
	
	public static String FileNameWithoutExt(String unitName) {
		// ���䤷��
		String o = new File(unitName).getName();
		if(unitName.endsWith(".lmn") || unitName.equals(InlineUnit.DEFAULT_UNITNAME)) {
			o = o.replaceAll("\\.lmn$", "");
			// ���饹̾�˻Ȥ��ʤ�ʸ������
			o = o.replaceAll("\\-", "");
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
