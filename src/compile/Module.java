/*
 * ������: 2004/01/10
 *
 */
package compile;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
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
 * <li> �⥸�塼��������ˡ   module_name : { ... }
 * <li> �⥸�塼��λȤ��� [1] module_name.name
 * <li> �⥸�塼��λȤ��� [2] use(module_name), name<br>
 * 		�������Ȥ����˥⥸�塼��̾���ά�Ǥ��롣<br>
 * 		������ʣ���Υ⥸�塼�뤬Ʊ̾�Υե��󥯥���������Ƥ���Ϥɤ줬ȿ�����뤫����ʤΤǡ����λȤ����Ͽ侩����ʤ���
 * <li> �⥸�塼��μ��֤�̾���Ĥ�����Ǥ��롣
 * <li> �֥⥸�塼���Ȥ��פȤϡ����ߤ�����˥⥸�塼��Υ롼����ɤ߹���ǡ������ȿ�������뤳�ȤǤ��롣
 * 		�����Ƥ����⥸�塼��Υ롼��κ��դ˴ޤޤ�륢�ȥ��񤯤��Ȥ�ȿ�������뤳�Ȥˤʤ롣
 * <li> module_name : { ... } �ˤ�ꡢ���̾����Ĥ��롣���줬�⥸�塼��̾�Ȥʤ롣
 * </ul><br>
 * 
 * �����λ��Ȥ�
 * 
 * <ul>
 * <li> �⥸�塼�뤬��������Τϡ����Υ⥸�塼��˴ޤޤ��롼�������
 * <li> �ե��󥯥�̾��ɽ���� [��°��̾.]̾�� �Ȥ��롣
 * <li> �ʲ��Υե�����ɤ��ߤ������˻��ꤷ����ˡ�ǥ���ѥ�����˽�������롣
 * 		<dl>
 * 		<dt>String  Functor.path</dt>
 * 		<dd>�ե��󥯥�ɽ����ν�°��̾��
 * 			�����������ɤ�����Ū�˻��ꤵ�줿�餽�졣
 * 			����ꤵ��ʤ��ä��顢�ǥե���ȤȤ��Ƥ��Υե��󥯥�����°������䡣
 *          �ڲ��򻲾ȡۡڥ롼��ǥե���ȤϽ�°��̵���ˤ��ʤ��ȡ��⥸�塼������ǥꥹ�ȤʤɤΥǡ����������Ǥ��ޤ����(n-kato)</dd>
 * 		<dt>boolean Functor.pathFree</dt>
 * 		<dd>��°�줬����Ū�˻��ꤵ��ʤ��ä����˿���</dd>
 *          �ڲ��򻲾ȡ�'ModuleName$AtomName' �ʤɤȤ���̾���ǥե��󥯥�����������в�褹��Ȼפ��ΤǤ���������Ǥ������丶�� (n-kato)
 * 		</dl>
 * <li> ����ѥ���ϥ���ѥ���塢���ƤΡ֥⥸�塼��̾.̾���פˤĤ��Ƥ��줾�줽�Υ⥸�塼����褷��LOADMODULE̿����������롣
 * 		������ϡ����ߤΥ���ѥ���Ѥ߹�¤���饤�֥��ѥ�����֥⥸�塼��̾.lmn�פ�õ���ƥ���ѥ��뤷����¤��
 * <li> LOADMODULE̿��Ȥϡ����ꤷ���⥸�塼��Υ롼�륻�åȤ���ꤷ������ɤ߹������̿�����
 * <li> FINDATOM ̿���ư����ѹ����롣��AtomSet ���Ѥ��뤳�Ȥˤʤ뤫�ʡ�
 * 		func ��õ���Ȥ����ޥå�����ե��󥯥�����ϰʲ��Τ�Ρ�
 * 		<ul>
 * 		<li>!func.pathFree �ξ��ϡ�name, arity, path �����������
 * 		<li> func.pathFree �ξ��ϡ�name, arity ����������Ρʽ���ɤ����
 * 		</ul>
 *      �ڲ��򻲾ȡۤ��ζ��̤ϥ롼�륳��ѥ��餬�Ԥ��ΤǤ��뤿�ᡢ����ѥ�����ǡ�����¤�Τߤ����̤���ɬ�פ����ꡢ�¹Ի��ˤ����ס�(n-kato)
 * </ul>
 * 
 * �ڸ����ϡ�������ǧ���Ƥ���������
 * 
 * �� ̾�� m.p ���Ф��ơ��⥸�塼��̾��磻��ɥ����ɤˤ��ƥޥå��󥰤�����褦�ˤ�����
 * 
 * runtime.Functor��p�Τߤ�ɽ���褦�ˤ��ơ�
 * runtime.Atom¦�˥⥸�塼��̾��ɽ���ե�����ɤ��ɲä���Τ��褤�Ȼפ��ޤ���
 * 
 * [1] �롼��ΥإåɤǤ� m.p �νи��ϡ�m.p �Τߤ˥ޥå����롣��m.p��'m,p'�Ȥ���������̾���Ȥ��Ƥλ��ѡ�
 *     ����ϡ�findatom dstatom,srcmem,p �μ��ˡ�
 *     �㤨�� path dstatom,m �Ȥ���̿���ȯ�Ԥ���褦�˥롼�륳��ѥ��餬����ѥ��뤹�롣
 * [2] �롼��ΥإåɤǤ� p �νи��ϡ����Ƥ� p �˥ޥå����롣(*A)
 *     findatom dstatom,srcmem,p
 * [3] �롼��Υܥǥ��Ǥ� m.p �νи��ϡ�m.p ���������롣
 *     newatom dstmem,p,m
 * [4] �롼��Υܥǥ��Ǥ� p �νи��ϡ�m.p ���������롣
 *     newatom dstmem,p,m
 * [5] �⥸�塼�������Υ롼�볰�Ǥ� m.p �νи��ϡ�m.p ���������롣
 * [6] �⥸�塼�������Υ롼�볰�Ǥ� p �νи��ϡ�m.p ���������롣
 * 
 * �� ̾�� m.p ���Ф��ơ��⥸�塼��̾��磻��ɥ����ɤˤ��ƥޥå��󥰤����ʤ��褦�ˤ�����
 * 
 * m.p�ϡ�ñ�˿�����̾��'m,p'��ɽ�����Ȥˤʤ롣
 * 
 * [1] �롼��ΥإåɤǤ� m.p �νи��ϡ�'m.p' �ʤΤǡ�m.p �Τߤ˥ޥå����롣
 *     findatom dstatom,srcmem,'m,p'
 * [2] �롼��ΥإåɤǤ� p �νи��ϡ�p �Τߤ˥ޥå����롣
 *     findatom dstatom,srcmem,p 
 * [3] �롼��Υܥǥ��Ǥ� m.p �νи��ϡ�m.p ���������롣
 *     newatom dstmem,mem,'m,p'
 * [4] �롼��Υܥǥ��Ǥ� p �νи��ϡ�p ���������롣
 *     newatom dstmem,mem,p
 * [5] �⥸�塼�������Υ롼�볰�Ǥ� m.p �νи��ϡ�m.p ���������롣
 * [6] �⥸�塼�������Υ롼�볰�Ǥ� p �νи��ϡ�p ���������롣
 * 
 * �� 'm.p' �����Ӥǻ��Ѥ��륢�ȥ�� p ��Ȥäƥޥå��󥰤��뤳��(*A)�Ϥ���ΤǤ�����
 *    ����Ȥ���С��ɤ�ʤȤ������Ω�ĤΤǤ�����
 * 
 *    ̵���ΤǤ���С��ޥå��󥰤ϼ��ʤ��褦�ˤ��ơ�
 *    ���Ĥ⿷����̾��'m.p'�Ȳ�᤹�����������Ȼפ��ޤ���
 * 
 * �� �ä��Ѥ��ޤ������⥸�塼�������Ū�ʥǡ����ΰ������Ϥɤ��ʤ�ޤ�������
 *    �롼��򥳥ԡ���������Ǥ���Ū�ʥǡ����ϼ¸��Ǥ��ޤ���Τǡ�������ˡ��ͤ��Ƥ���������
 *    �����ͤ��뤳�Ȥ�´���Υᥤ��ơ��ޤ��ä��Ϥ��Ǥ����������٤���Ĥ�Ϥ����κ�ȤǤ���
 * 
 *    inline��ȤäƤ��ޤ����Τϡ��ɤ����Ƥ�Ǥ��ʤ��ä��������ˤ������ΤǤ�����
 *

>>>1
�����ƥ�롼�륻�åȡʤ��٤Ƥ���ˤ��뤳�Ȥˤ����

��ˤ��������롼�롣��˹Ԥ���ˡ�ϣ��̤�ʤΤǡ���ư�Ǥ�����
({{up,$p,@q},$s,@t}:-{$s,@t},{up,$p,@q})
#, {{up,1},2,3}

��������ˡ��ʣ������Τ�down�򥳥ԡ����ʤ��Ȥ����ʤ���
�⤦{down}�����ä��Ȥ��Ϥʤˤ⤷�ʤ���
({down,$p,@q},{$r,@s}:- ! ($r match {down, $t}) | {down,$p,@q}, {{down,$p,@q},$r,@s})
#,  {down,1},{2,3},{4,5}

>>>2
( { find_and_link($Name1, X) }, $Name2, decl :- $Name1 eq $Name2 | $name2(X), decl )

## { find_and_link(m, X) }, m, decl :- m(X), decl    for all {m | String(m)}
## decl �ϡ��⥸�塼�������������ˤ���Ȥ��롣�ʤ���ǡ����ѼԤΥ��ȥ�ȶ��̤�����

>>>3
( $a(X) :- static($a) |  { up, down, find_and_link($a, X) } )

1, 2, 3 ������ȡ������ƥ��å��ʤ�ΤؤΥ��������ϼ��Τ褦�˵��ҤǤ��롣

{{{ io.print(m.incr) }}}, m:{ decl, m.incr, id(0), ( m.incr(X), id(N) :- cp(s(N), Cp1, Cp2), id(Cp1), X=Cp2, m.incr )  }
-> {{{ io.print(X), { up, down, find_and_link(m.incr, X) }, m.incr(X) }}}, m:{...}
-> {{{ io.print(X) }, { up, down, find_and_link(m.incr, X) } }}, m:{...}
-> {{{ io.print(X) }}, { up, down, find_and_link(m.incr, X) } }, m:{...}
-> {{{ io.print(X) }}}, { up, down, find_and_link(m.incr, X) }, m:{...}
-> {{{ io.print(X) }}}, m:{ decl, m.incr, id, { up, down, find_and_link(m.incr, X) }, (... :- ...) }
-> {{{ io.print(X) }}}, m:{ decl, id, m.incr(X), (... :- ...) }
-> {{{ io.print(X) }}}, m:{ decl, id(1), 1(X), m.incr, (... :- ...) }

���礦�Ϥ����Ǥ����ޤ�


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
		libPath.add("./lmntal_lib/");
		libPath.add("../lmntal_lib/");
		libPath.add(".");
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
			String thePath = (String)it.next();
			String filename = thePath+mod_name+".lmn";
			StringBuffer sb = new StringBuffer("Loading Module "+mod_name+" from "+thePath+" ...");
			try {
				LMNParser lp = new LMNParser(new BufferedReader(new InputStreamReader(new FileInputStream(filename))));
				Membrane nm = RulesetCompiler.runStartWithNull(lp.parse());
				//Env.p("MOD compiled "+nm);
				//memNameTable ���⥸�塼����ؤλ��Ȥ��ݻ����Ƥ���Τǡ�GC����ʤ���
				m.rulesets.addAll(nm.rulesets);
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
//			Env.p("Check module existence "+path);
			if(!memNameTable.containsKey(path)) {
//				Env.p("TODO: search lib file : " + path);
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
	
	/**
	 * 
	 * @param m
	 */
	public static void fixupLoadModule(Membrane m) {
		//Env.d("genInstruction");
//		
//		//Env.p(memNameTable);
//		
//		Iterator i;
//		Iterator it0 = m.rulesets.iterator();
//		while (it0.hasNext()){
//			i = ((InterpretedRuleset)it0.next()).rules.listIterator();
//			while(i.hasNext()) {
//				runtime.Rule rule = (runtime.Rule)i.next();
//				ListIterator ib = rule.body.listIterator();
//				while(ib.hasNext()) {
//					Instruction inst = (Instruction)ib.next();
//					// �����ʤ���
//					if(inst.getKind()==Instruction.LOADMODULE) {
//						//Env.p("module solved : "+modules.get(inst.getArg2()));
//						ib.remove();
//						// �⥸�塼����ľ°�Υ롼�륻�åȤ������ɤ߹���
//						Membrane mem = (Membrane)memNameTable.get(inst.getArg2());
//						if(mem==null) {
//							Env.e("Undefined module "+inst.getArg2());
//						} else {
//							Iterator it3 = mem.rulesets.iterator();
//							while (it3.hasNext()) {
//								ib.add(new Instruction(Instruction.LOADMODULE, inst.getIntArg1(),
//									(runtime.Ruleset)it3.next()));
//							}
//						}
//					}
//				}
//			}
//		}
//		i = m.rules.listIterator();
//		while(i.hasNext()) {
//			RuleStructure rs = (RuleStructure)i.next();
//			fixupLoadModule(rs.leftMem);
//			fixupLoadModule(rs.rightMem);
//		}
//		i = m.mems.listIterator();
//		while(i.hasNext()) {
//			fixupLoadModule((Membrane)i.next());
//		}
	}
}
