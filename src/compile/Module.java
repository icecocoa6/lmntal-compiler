/*
 * ������: 2004/01/10
 *
 */
package compile;

import java.util.*;
import runtime.Instruction;
import runtime.InterpretedRuleset;
import runtime.Env;
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
 * 		�������������� name(X) ���ޤޤ������Ф��� X �ˤĤʤ��륢�ȥ�Υե��󥯥�̾��Ʊ��̾����Ĥ��롣 
 * </ul><br>
 * 
 * �����λ��Ȥ�
 * 
 * <ul>
 * <li> �⥸�塼�뤬��������Τϡ����Υ⥸�塼��˴ޤޤ��롼�������
 * <li> �ե��󥯥�̾��ɽ���� [��°��̾.]̾�� �Ȥ��롣
 * 		TODO flex ������ѹ�
 * <li> �ʲ��Υե�����ɤ��ߤ������˻��ꤷ����ˡ�ǥ���ѥ�����˽�������롣
 * 		<dl>
 * 		<dt>String  Functor.path</dt>
 * 		<dd>�ե��󥯥�ɽ����ν�°��̾��
 * 			�����������ɤ�����Ū�˻��ꤵ�줿�餽�졣
 * 			���ꤵ��ʤ��ä��顢�ǥե���ȤȤ��Ƥ��Υե��󥯥�����°�����졣</dd>
 * 		<dt>boolean Functor.pathFree</dt>
 * 		<dd>��°�줬����Ū�˻��ꤵ��ʤ��ä����˿���</dd>
 * 		</dl>
 * <li> ����ѥ���ϥ���ѥ���塢���ƤΡ֥⥸�塼��̾.̾���פˤĤ��Ƥ��줾�줽�Υ⥸�塼����褷��LOADMODULE̿����������롣
 * 		������ϡ����ߤΥ���ѥ���Ѥ߹�¤���饤�֥��ѥ�����֥⥸�塼��̾.lmn�פ�õ���ƥ���ѥ��뤷����¤��
 * 		(TODO)
 * <li> LOADMODULE̿��Ȥϡ����ꤷ���⥸�塼��Υ롼�륻�åȤ���ꤷ������ɤ߹������̿�����
 * <li> FINDATOM ̿���ư����ѹ����롣��TODO AtomSet ���Ѥ��뤳�Ȥˤʤ뤫�ʡ�
 * 		func ��õ���Ȥ����ޥå�����ե��󥯥�����ϰʲ��Τ�Ρ�
 * 		<ul>
 * 		<li>!func.pathFree �ξ��ϡ�name, arity, package �����������
 * 		<li> func.pathFree �ξ��ϡ�name, arity ����������Ρʽ���ɤ����
 * 		</ul>
 * </ul>
 * 
 * @author hara
 *
 */
public class Module {
	public static Map modules = new HashMap();
	public static void listupModules(Membrane m) {
		//Env.d("listupModules");
		runtime.Functor f = new runtime.Functor("name", 1);
		
		Iterator i;
		i = m.atoms.listIterator();
		while(i.hasNext()) {
			Atom a = (Atom)i.next();
			if(a.functor.equals(f)) {
				Env.d("Module found : "+a.args[0].atom);
				modules.put(a.args[0].buddy.atom.functor.getName(), a.args[0].atom.mem);
			}
		}
		i = m.rules.listIterator();
		while(i.hasNext()) {
			RuleStructure rs = (RuleStructure)i.next();
			//Env.d("");
			//Env.d("About rule structure (LEFT): "+rs.leftMem+" of "+rs);
			listupModules(rs.leftMem);
			//Env.d("About rule structure (LEFT): "+rs.rightMem+" of "+rs);
			listupModules(rs.rightMem);
		}
		i = m.mems.listIterator();
		while(i.hasNext()) {
			listupModules((Membrane)i.next());
		}
	}
	public static void fixupLoadRuleset(Membrane m) {
		//Env.d("fixupLoadRuleset");
		
		Iterator it0 = m.rulesets.iterator();
		while (it0.hasNext()){
			Iterator i = ((InterpretedRuleset)it0.next()).rules.listIterator();
			while(i.hasNext()) {
				runtime.Rule rule = (runtime.Rule)i.next();
				ListIterator ib = rule.body.listIterator();
				while(ib.hasNext()) {
					Instruction inst = (Instruction)ib.next();
					// �����ʤ���
					// TODO �������ӤǤ� LOADRULESET �� LOADMODULE ��̾���ѹ�����Interpreter�ǥ��ɤ���
					if(inst.getKind()==Instruction.LOADRULESET && inst.getArg2() instanceof String) {
						//Env.p("module solved : "+modules.get(inst.getArg2()));
						ib.remove();
						Iterator it3 = ((Membrane)modules.get(inst.getArg2())).rulesets.iterator();
						while (it3.hasNext()) {
							ib.add(new Instruction(Instruction.LOADRULESET, inst.getIntArg1(),
								(runtime.Ruleset)it3.next()));
						}
						
	//					ib.set(new Instruction(Instruction.LOADRULESET, inst.getIntArg1(), 
	//						((Membrane)modules.get(inst.getArg2())).ruleset ));
						
					}
				}
			}
		}
		Iterator i = m.mems.listIterator();
		while(i.hasNext()) {
			fixupLoadRuleset((Membrane)i.next());
		}
	}
}
