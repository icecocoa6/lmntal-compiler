package compile.structure;

import java.util.*;
import runtime.InterpretedRuleset;

import runtime.Env;

/** 
 * �����������������ι�¤��ɽ�����饹<br>
 * memo:����1�Ĥ�������������ˡ�⤢�롣<br>
 * �����Ǥ�List�Ȥ����ݻ�����롣
 */
public final class Membrane {
	/** ���� <p> todo ������mem��parent��̾���ѹ����� */
	public Membrane mem = null;
	
	/** ���ȥ�(compile.structure.Atom)�Υꥹ�� */
	public List atoms = new ArrayList();

	/** ����(compile.structure.Membrane)�Υꥹ�� */
	public List mems = new ArrayList();
	
	/** �롼��(compile.structure.RuleStructure)�Υꥹ�� */
	public List rules = new ArrayList();

	////////////////////////////////////////////////////////////////

	/** ���ȥླྀ��(compile.structure.Atom)�Υꥹ�� */
	public List aggregates = new ArrayList();

	/** �ץ���ʸ̮�и�(compile.structure.ProcessContext)�Υꥹ�� */
	public List processContexts = new ArrayList();
	
	/** �롼��ʸ̮�и�(compile.struct.RuleContext)�Υꥹ�� */
	public List ruleContexts = new ArrayList();
	
	/** ���դ��ץ���ʸ̮�и�(compile.struct.ProcessContext)�Υꥹ�� */
	public List typedProcessContexts = new ArrayList();
	
	////////////////////////////////////////////////////////////////
	
	/** ��μ�ͳ���̾(String)���餽�Υ�󥯽и�(compile.struct.LinkOccurrence)�ؤμ��� */
	public HashMap freeLinks = new HashMap();
	
	/** �롼�륻�åȡ��������줿�롼�륪�֥������Ȥ��༡�������ɲä���Ƥ�����*/
//	public runtime.Ruleset ruleset = new InterpretedRuleset();
	public List rulesets = new ArrayList();
	 	
	public String name;
	
	////////////////////////////////////////////////////////////////

	/**
	 * ���󥹥ȥ饯��
	 * @param mem ����
	 */
	public Membrane(Membrane mem) {
		this.mem = mem;
	}
	
	/**
	 * {} �ʤ��ǽ��Ϥ��롣
	 * 
	 * �롼��ν��Ϥκݡ�{} �������
	 * (a:-b) �� ({a}:-{b}) �ˤʤä��㤦���顣
	 *  
	 * @return String 
	 */
	public String toStringWithoutBrace() {
		return 
		(atoms.isEmpty() ? "" : ""+Env.parray(atoms))+
		(mems.isEmpty() ? "" : " "+Env.parray(mems))+
		(rules.isEmpty() ? "" : " "+Env.parray(rules))+
		(processContexts.isEmpty() ? "" : " "+Env.parray(processContexts))+
		(ruleContexts.isEmpty() ? "" : " "+Env.parray(ruleContexts))+
		(typedProcessContexts.isEmpty() ? "" : " "+Env.parray(typedProcessContexts))+
		"";
		
	}
	public String toString() {
		return "{ " + toStringWithoutBrace() + " }";
	}
	
	/**
	 * ������˴ޤޤ�����ƤΥ롼�륻�åȤ�ɽ�����롣
	 */
	public void showAllRuleset() {
		Iterator it = rulesets.iterator();
		while (it.hasNext()) {
			Env.d( ((InterpretedRuleset)it.next()) );
		}
		
		Iterator l;
		
		// ľ°�Υ롼�뤽�줾��ˤĤ��ơ����κ�����ȱ�����Υ롼�륻�åȤ�ɽ��
		l = rules.listIterator();
		while(l.hasNext()) {
			RuleStructure rs = (RuleStructure)l.next();
			rs.leftMem.showAllRuleset();
			rs.rightMem.showAllRuleset();
		} 
		// ���줽�줾��
		l = mems.listIterator();
		while(l.hasNext()) ((Membrane)l.next()).showAllRuleset();
	}
	
	/**
	 * ���������ˤ���롼�������ɽ�����롣
	 * 
	 * <pre>
	 * �֤��������ˤ���롼��פȤϡ��ʲ��Σ����ࡣ
	 * ��[1]������Υ롼�륻�åȤ˴ޤޤ�����ƤΥ롼��
	 * ��[2] [1]�κ��������ˤ���롼��
	 * ��[3] [1]�α��������ˤ���롼��
	 * </pre>
	 */
	public void showAllRule() {
		Env.c("Membrane.showAllRule mem="+this);
		Iterator it = rulesets.iterator();
		while (it.hasNext()) {
			((InterpretedRuleset)it.next()).showDetail();
		}
		
		Iterator l;
		
		// ľ°�Υ롼�뤽�줾��ˤĤ��ơ����κ�����ȱ�����Υ롼�륻�åȤ�ɽ��
		l = rules.listIterator();
		while(l.hasNext()) {
			RuleStructure rs = (RuleStructure)l.next();
			//Env.p("");
			//Env.p("About rule structure (LEFT): "+rs.leftMem+" of "+rs);
			rs.leftMem.showAllRule();
			//Env.p("About rule structure (LEFT): "+rs.rightMem+" of "+rs);
			rs.rightMem.showAllRule();
		}
		// ���줽�줾��
		l = mems.listIterator();
		while(l.hasNext()) ((Membrane)l.next()).showAllRule();
	}
}
