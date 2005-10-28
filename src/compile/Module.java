/*
 * ������: 2004/01/10
 *
 */
package compile;

import java.io.*;
import java.util.*;

//import runtime.Functor;
import runtime.Env;
import compile.parser.LMNParser;
import compile.structure.*;

/**
 * �⥸�塼�륷���ƥ��¸����륯�饹��<br><br>
 * 
 * ����
 * 
 * <ul>
 * <li> �⥸�塼��������ˡ   module_name : { ... }  (���̾����Ĥ���)
 * <li> �⥸�塼��λȤ���     module_name.name
 * <li> �⥸�塼��μ��֤�̾���Ĥ�����Ǥ��롣
 * <li> �֥⥸�塼���Ȥ��פȤϡ����ߤ�����˥⥸�塼��Υ롼����ɤ߹���ǡ������ȿ�������뤳�ȤǤ��롣
 * 		�����Ƥ����⥸�塼��Υ롼��κ��դ˴ޤޤ�륢�ȥ��񤯤��Ȥ�ȿ�������뤳�Ȥˤʤ롣
 * </ul><br>
 * 
 * @author hara
 *
 */
public class Module {
	public static List libPath = new ArrayList();
	public static Map memNameTable = new HashMap();
	public static Map loaded = new HashMap();
	public static Object EXIST = new Object();
	
	static {
		//libPath.add("hoge");
		//libPath.add("FOO");
		libPath.add(new File("./lib/src"));
		libPath.add(new File("../lib/src"));
		libPath.add(new File("./lib/public"));
		libPath.add(new File("../lib/public"));
		libPath.add(new File("."));
	}
	
	/**
	 * ���̾ɽ����Ͽ���롣
	 * @param m
	 */
	public static void regMemName(String name, Membrane m) {
		memNameTable.put(name, m);
	}
	
	/**
	 * ���ꤷ���⥸�塼�����ꤷ������ɤ߹���
	 * @param m         �ɤ߹��ޤ����
	 * @param mod_name  �⥸�塼��̾
	 */
	public static void loadModule(Membrane m, String mod_name) {
		if(loaded.get(mod_name)!=null) return;
		
		Iterator it = libPath.iterator();
		while(it.hasNext()) {
			String thePath = ((File)it.next()).toString();
			File file = new File(thePath+"/"+mod_name+".lmn");
			StringBuffer sb = new StringBuffer("Loading Module "+mod_name+" from "+file+" ...");
			try {
				LMNParser lp = new LMNParser(new BufferedReader(new InputStreamReader(new FileInputStream(file))));
				runtime.Ruleset rs = RulesetCompiler.compileMembrane(lp.parse(), file.toString());
				//Env.p("MOD compiled "+rs);
				//memNameTable ���⥸�塼����ؤλ��Ȥ��ݻ����Ƥ���Τǡ�GC����ʤ���
				m.rulesets.add(rs);
				sb.append(" [ OK ] ");
				Env.d(sb.toString());
				loaded.put(mod_name, EXIST);
				return;
			} catch (Exception e) {
				sb.append(" [ FAILED ] ");
				Env.d(sb.toString());
				//Env.e("!! catch !! "+e+"\n"+Env.parray(Arrays.asList(e.getStackTrace()), "\n"));
			}
		}
	}
	
	/**
	 * ���ꤷ����ˤĤ��ơ�̤����⥸�塼����褹�롣
	 * @param m
	 */
	public static void resolveModules(Membrane m) {
		List need = new ArrayList();
		getNeedModules(m, need);
		Iterator i = need.iterator();
		while(i.hasNext()) {
			loadModule(m, (String)i.next());
		}
	}
	
	/**
	 * ���ꤷ����ˤĤ��ơ�̤���⥸�塼�������Ĥ��롣
	 * @param m
	 * @param need ���ϰ������⥸�塼����������롣
	 */
	static void getNeedModules(Membrane m, List need) {
		Iterator i;
		i = m.atoms.listIterator();
		while(i.hasNext()) {
			Atom a = (Atom)i.next();
			String path = a.getPath();
			if(path == null) continue;
			if(path.equals(m.name)) continue;
			if(!memNameTable.containsKey(path)) {
				need.add(path);
			}
		}
		i = m.rules.listIterator();
		while(i.hasNext()) {
			RuleStructure rs = (RuleStructure)i.next();
			getNeedModules(rs.leftMem, need);
			getNeedModules(rs.rightMem, need);
		}
		i = m.mems.listIterator();
		while(i.hasNext()) {
			getNeedModules((Membrane)i.next(), need);
		}
	}
}
