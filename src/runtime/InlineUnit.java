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
	
	Date classDate;
	Date srcDate;
	
	/** Hash { ����饤�󥳡���ʸ���� -> ��դ�Ϣ�� } */
	public static Map codes = new HashMap(); 
	
	/** List ����饤�����������ʸ���� */
	public static List defs = new ArrayList(); 
	
	/** ��դ�Ϣ�� */
	static int codeCount = 0;
	
	static final int EXEC   = 0;
	static final int DEFINE = 1;
	
	/****** ����ѥ�����˻Ȥ� ******/
	
	/**
	 * TODO class �ե����뤬�ǿ����ɤ������֤�
	 */
	public boolean isCached() {
		return false;
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
	
	public void register(String code, int type) {
		switch(type) {		case EXEC:
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
			
			String className = Inline.className_of_unitName(name);
			File outputFile = Inline.fileName_of_unitName(name);
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
			Env.d("!!! "+e+Arrays.asList(e.getStackTrace()));
		}
	}
	
	/****** �¹Ի��˻Ȥ� ******/
	
	public void attach() {
		// jar �ǽ����Ϥ�ư����ȡ�����ʥե����뤫�饯�饹����ɤ��뤳�Ȥ��Ǥ��ʤ��ߤ�����
		String cname = Inline.className_of_unitName(name);
		File path = Inline.path_of_unitName(name);
		FileClassLoader cl = new FileClassLoader();
		cl.setClassPath(path.toString());
		Env.d("Try loading "+cl.filename_of_class(cname));
		try {
			Object o = cl.loadClass(cname).newInstance();
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
}
