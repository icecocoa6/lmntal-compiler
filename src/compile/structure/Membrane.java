package compile.structure;

import java.util.*;

import runtime.Functor;
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
	/** ��λ�ե饰�����åȤ���Ƥ��뤫�ɤ�����ɽ�� */
	public boolean stable = false;
	
	/** �����ƥ�롼�륻�åȤȤ��ƻȤ��ʤ鿿 */
	public boolean is_system_ruleset = false;

	/** ���ȥ�(compile.structure.Atom)�Υꥹ�� */
	public List atoms = new LinkedList();

	/** ����(compile.structure.Membrane)�Υꥹ�� */
	public List mems = new LinkedList();
	
	/** �롼��(compile.structure.RuleStructure)�Υꥹ�� */
	public List rules = new LinkedList();

	////////////////////////////////////////////////////////////////

	/** ���ȥླྀ��(compile.structure.Atom)�Υꥹ�� */
	public List aggregates = new LinkedList();

	/** �ץ���ʸ̮�и�(compile.structure.ProcessContext)�Υꥹ�� */
	public List processContexts = new LinkedList();
	
	/** �롼��ʸ̮�и�(compile.structure.RuleContext)�Υꥹ�� */
	public List ruleContexts = new LinkedList();
	
	/** ���դ��ץ���ʸ̮�и�(compile.structure.ProcessContext)�Υꥹ�� */
	public List typedProcessContexts = new LinkedList();
	
	////////////////////////////////////////////////////////////////
	
	/** ��μ�ͳ���̾(String)���餽�Υ�󥯽и�(compile.structure.LinkOccurrence)�ؤμ��� */
	public HashMap freeLinks = new HashMap();
	
	/** �롼�륻�åȡ��������줿�롼�륪�֥������Ȥ��༡�������ɲä���Ƥ�����*/
//	public runtime.Ruleset ruleset = new InterpretedRuleset();
	public List rulesets = new LinkedList();
	 	
	public String name;
	
	////////////////////////////////////////////////////////////////

	/**
	 * ���󥹥ȥ饯��
	 * @param mem ����
	 */
	public Membrane(Membrane mem) {
		this.mem = mem;
	}
	
	public int getNormalAtomCount() {
		Iterator it = atoms.iterator();
		int c=0;
		while(it.hasNext()) {
			Atom a = (Atom)it.next();
			if(!(a.functor.equals(Functor.INSIDE_PROXY) || a.functor.equals(Functor.OUTSIDE_PROXY))) c++;
		}
		return c;
	}
	/** ������ˤ���inside_proxy���ȥ�θĿ���������� */
	public int getFreeLinkAtomCount() {
		Iterator it = atoms.iterator();
		int c=0;
		while(it.hasNext()) {
			Atom a = (Atom)it.next();
			if(a.functor.equals(Functor.INSIDE_PROXY)) c++;
		}
		return c;
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
		LinkedList list = new LinkedList();
		list.addAll(atoms);
		list.addAll(mems);
		list.addAll(rules);
		list.addAll(processContexts);
		list.addAll(ruleContexts);
		list.addAll(typedProcessContexts);
		
		//return list.toString().replaceAll("^.|.$","");
		return Env.parray(list).toString();
	}
	public String toString() {
		return "{ " + toStringWithoutBrace() + " }" + (stable ? "/" : "");
	}
	public String toStringAsGuard() {
		Iterator it = atoms.iterator();
		String text = "";
		while (it.hasNext()) {
			text += " " + ((Atom)it.next()).toStringAsTypeConstraint();
		}
		return text.substring(1);
	}
	/**
	 * ������˴ޤޤ�����ƤΥ롼�륻�åȤ�ɽ�����롣
	 */
	public void showAllRulesets() {
		Iterator it = rulesets.iterator();
		while (it.hasNext()) {
			Env.d( ((InterpretedRuleset)it.next()) );
		}
		
		Iterator l;
		
		// ľ°�Υ롼�뤽�줾��ˤĤ��ơ����κ�����ȱ�����Υ롼�륻�åȤ�ɽ��
		l = rules.listIterator();
		while(l.hasNext()) {
			RuleStructure rs = (RuleStructure)l.next();
			rs.leftMem.showAllRulesets();
			rs.rightMem.showAllRulesets();
		} 
		// ���줽�줾��
		l = mems.listIterator();
		while(l.hasNext()) ((Membrane)l.next()).showAllRulesets();
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
	public void showAllRules() {
		Env.c("Membrane.showAllRules " + this);
		Iterator it = rulesets.iterator();
		while (it.hasNext()) {
			((InterpretedRuleset)it.next()).showDetail();
		}
		
		// ľ°�Υ롼�뤽�줾��ˤĤ��ơ����κ�����ȱ�����Υ롼�륻�åȤ�ɽ��
		it = rules.iterator();
		while (it.hasNext()) {
			RuleStructure rs = (RuleStructure)it.next();
			//Env.p("");
			//Env.p("About rule structure (LEFT): "+rs.leftMem+" of "+rs);
			rs.leftMem.showAllRules();
			//Env.p("About rule structure (LEFT): "+rs.rightMem+" of "+rs);
			rs.rightMem.showAllRules();
		}
		// ���줽�줾��
		it = mems.iterator();
		while(it.hasNext()) ((Membrane)it.next()).showAllRules();
	}
	
	/**
	 * Ϳ����줿�����Ȥ򤳤�����ɲä��롣
	 * @param m
	 */
	public void add(Membrane m) {
		atoms.addAll(m.atoms);
		mems.addAll(m.mems);
		rules.addAll(m.rules);
		aggregates.addAll(m.aggregates);
		processContexts.addAll(m.processContexts);
		ruleContexts.addAll(m.ruleContexts);
		typedProcessContexts.addAll(m.typedProcessContexts);
		freeLinks.putAll(m.freeLinks);
		rulesets.addAll(m.rulesets);
	}
	
}
