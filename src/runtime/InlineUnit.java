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
	/** ����̾�ʥե�����̾�� */
	String name;
	
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
			Env.d("Register inlineCode : "+code);
			codes.put(code, new Integer(codeCount++));
			break;
		case DEFINE:
			Env.d("Register inlineDefineCode : "+code);
			defs.add(code);
			break;
		}
	}
	
	/**
	 * �����ɤ��������롣
	 */
	public void makeCode() {
		try {
			if(codes.isEmpty() && defs.isEmpty()) return;
			Iterator i;
			
			String className = Inline.className_of_lmntalFilename(name);
			PrintWriter p = new PrintWriter(new FileOutputStream(className+".java"));
			Env.d("make inline code "+codes);
			
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
			
		} catch (Exception e) {
			Env.d("!!! "+e+Arrays.asList(e.getStackTrace()));
		}
	}
	
	/****** �¹Ի��˻Ȥ� ******/
	
	/**
	 * ����饤��̿���¹Ԥ��롣
	 * @param atom �¹Ԥ��٤����ȥ�̾����ĥ��ȥ�
	 */
	public void callInline(Atom atom, int codeID) {
		//Env.d(atom+" "+codeID);
		if(inlineCode==null) return;
		//Env.d("=> call Inline "+atom.getName());
		inlineCode.run(atom, codeID);
	}
	
}
