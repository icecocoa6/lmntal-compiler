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
 * ���ͥ�⡧
 * 
 * <ul>
 * <li> �⥸�塼��μ��֤�̾���Ĥ�����Ǥ��롣
 * <li> �֥⥸�塼���Ȥ��פȤϡ����ߤ�����˥⥸�塼��Υ롼����ɤ߹���ǡ������ȿ�������뤳�ȤǤ��롣
 * 		�����Ƥ����⥸�塼��Υ롼��κ��դ˴ޤޤ�륢�ȥ��񤯤��Ȥ�ȿ�������뤳�Ȥˤʤ롣
 * <li> module_name : { ... } �ˤ�ꡢ���̾����Ĥ��롣���줬�⥸�塼��̾�Ȥʤ롣
 * 		�������������� name(X) ���ޤޤ������Ф��� X �ˤĤʤ��륢�ȥ�Υե��󥯥�̾��Ʊ��̾����Ĥ��롣 
 * <li> �⥸�塼�뤬��������Τϡ����Υ⥸�塼��˴ޤޤ��롼�������
 * <li> ���ȥ�̾ ::= [�ѥå�����̾.]̾�� �Ȥ������Ȥˤ��롣
 * <li> Functor �� package (��°���̾��)�ե�����ɤ��ߤ������åȤ��롣
 * 		set:{count} �� count.package eq "set"
 * <li> �⥸�塼��λȤ��� [1] module_name.name
 * <li> �⥸�塼��λȤ��� [2] use(module_name), name
 * 		�������Ȥ����˥⥸�塼��̾���ά�Ǥ��롣���ͤ����Ȥ��Ϥɤ줬ȿ�����뤫���ꡣ
 * <li> ����ѥ���ϥ���ѥ���塢���ƤΡ֥⥸�塼��̾.̾���פˤĤ��Ƥ��줾�줽�Υ⥸�塼����褷��
 * 		��褵�줿�⥸�塼��Υ롼�륻�åȤ�֥⥸�塼��.̾���פν�°����ɤ߹���̿����������롣
 * 		������ϡ����ߤΥ���ѥ���Ѥ߹�¤���饤�֥��ѥ�����֥ѥå�����̾.lmn�פ�õ���ƥ���ѥ��뤷����¤
 * <li> FINDATOM ̿���ư����ѹ����롣��AtomSet ���Ѥ��뤳�Ȥˤʤ뤫�ʡ�
 * 		func ��õ���Ȥ����ޥå�����ե��󥯥�����ϰʲ��Τ�Ρ�
 * 		<ul>
 * 		<li>func.package ne "" �ξ��ϡ�name, arity, package �����������
 * 		<li>func.package eq "" �ξ��ϡ�name, arity �����������
 * 		</ul>
 * <li> 
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
