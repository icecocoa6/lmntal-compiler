/*
 * ������: 2004/01/10
 *
 */
package compile;

import java.util.*;

import runtime.Functor;
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
 * <li> �ʲ��Υե�����ɤ��ߤ������˻��ꤷ����ˡ�ǥ���ѥ�����˽�������롣
 * 		<dl>
 * 		<dt>String  Functor.path</dt>
 * 		<dd>�ե��󥯥�ɽ����ν�°��̾��
 * 			�����������ɤ�����Ū�˻��ꤵ�줿�餽�졣
 * 			����ꤵ��ʤ��ä��顢�ǥե���ȤȤ��Ƥ��Υե��󥯥�����°������䡣
 *          TODO �ڥǥե���ȤϽ�°��̵���ˤ��ʤ��ȡ��⥸�塼������ǥꥹ�ȤʤɤΥǡ����������Ǥ��ޤ����(n-kato)</dd>
 * 		<dt>boolean Functor.pathFree</dt>
 * 		<dd>��°�줬����Ū�˻��ꤵ��ʤ��ä����˿���</dd>
 *          TODO 'ModuleName$AtomName' �ʤɤȤ���̾���ǥե��󥯥�����������в�褹��Ȼפ��ΤǤ���������Ǥ������丶�� (n-kato)
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
 *      TODO ���ζ��̤ϥ롼�륳��ѥ��餬�Ԥ��ΤǤ��뤿�ᡢ����ѥ�����ǡ�����¤�Τߤ����̤���ɬ�פ����ꡢ�¹Ի��ˤ����ס�(n-kato)
 * </ul>
 * 
 * @author hara
 *
 */
public class Module {
	public static Map memNameTable = new HashMap();
	
	/**
	 * ���̾ɽ����Ͽ���롣
	 * @param m
	 */
	public static void regMemName(String name, Membrane m) {
		memNameTable.put(name, m);
	}
	
	/**
	 * �⥸�塼��β��򤹤롣ɬ�פ˱����ƥ饤�֥��ե����뤫���ɤ߹��ࡣ
	 * @param m
	 */
	public static void genInstruction(Membrane m) {
		//Env.d("genInstruction");
		
		//Env.p(memNameTable);
		
		Iterator i;
		i = m.atoms.listIterator();
		while(i.hasNext()) {
			Atom a = (Atom)i.next();
			Functor f = a.functor;
			if(f.path==null) continue;
			if(f.path.equals(m.name)) continue;
			Env.p("Check module existence "+f.path);
			if(!memNameTable.containsKey(f.path)) {
				//TODO search lib file
				Env.p("TODO: search lib file : "+f.path);
			}
		}
		i = m.rules.listIterator();
		while(i.hasNext()) {
			RuleStructure rs = (RuleStructure)i.next();
			genInstruction(rs.leftMem);
			genInstruction(rs.rightMem);
		}
		i = m.mems.listIterator();
		while(i.hasNext()) {
			genInstruction((Membrane)i.next());
		}
//		Iterator it0 = m.rulesets.iterator();
//		while (it0.hasNext()){
//			Iterator i = ((InterpretedRuleset)it0.next()).rules.listIterator();
//			while(i.hasNext()) {
//				runtime.Rule rule = (runtime.Rule)i.next();
//				ListIterator ib = rule.body.listIterator();
//				while(ib.hasNext()) {
//					Instruction inst = (Instruction)ib.next();
//					// �����ʤ���
//					// TODO �������ӤǤ� LOADRULESET �� LOADMODULE ��̾���ѹ�����Interpreter�ǥ��ɤ���
//					if(inst.getKind()==Instruction.LOADRULESET && inst.getArg2() instanceof String) {
//						//Env.p("module solved : "+modules.get(inst.getArg2()));
//						ib.remove();
//						Iterator it3 = ((Membrane)memNameTable.get(inst.getArg2())).rulesets.iterator();
//						while (it3.hasNext()) {
//							ib.add(new Instruction(Instruction.LOADRULESET, inst.getIntArg1(),
//								(runtime.Ruleset)it3.next()));
//						}
//					}
//				}
//			}
//		}
//		Iterator i = m.mems.listIterator();
//		while(i.hasNext()) {
//			genInstruction((Membrane)i.next());
//		}
	}
}
