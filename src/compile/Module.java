/*
 * ������: 2004/01/10
 *
 */
package compile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import runtime.Env;

import compile.parser.LMNParser;
import compile.structure.Atom;
import compile.structure.Membrane;
import compile.structure.RuleStructure;

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
		libPath.add(new File(System.getenv("LMNTAL_HOME") + "/lib/src"));
		libPath.add(new File(System.getenv("LMNTAL_HOME") + "/lib/public"));
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
	
	/** �⥸�塼�뤬���ĥ롼�륻�åȰ�������Ϥ��롣*/
	public static void showModuleList() {
		if (memNameTable.size() == 0) return;
		
		System.out.println("Module");
		Iterator it = memNameTable.keySet().iterator();
		while (it.hasNext()) {
			String name = (String)it.next();
			Membrane mem = (Membrane)memNameTable.get(name);
			name = name.replaceAll("\\\\", "\\\\\\\\");
			name = name.replaceAll("'", "\\\\'");
			name = name.replaceAll("\r", "\\\\r");
			name = name.replaceAll("\n", "\\\\n");
			System.out.print("'" + name + "'");
			System.out.print(" {");
			if (mem.rulesets.size() > 0) {
				Iterator it2 = mem.rulesets.iterator();
				System.out.print(it2.next());
				while (it2.hasNext()) {
					System.out.print(", " + it2.next());
				}
			}
			System.out.println("}");
		}
	}
}
