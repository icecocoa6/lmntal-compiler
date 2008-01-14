package compile.structure;

import java.util.*;

import runtime.Functor;
import runtime.InterpretedRuleset;
import runtime.Ruleset;

import runtime.Env;

/** 
 * �����������������ι�¤��ɽ�����饹<br>
 * memo:����1�Ĥ�������������ˡ�⤢�롣<br>
 * �����Ǥ�List�Ȥ����ݻ�����롣
 */
public final class Membrane {
	/** ���� */
	public Membrane parent = null;
	/** ��λ�ե饰�����åȤ���Ƥ��뤫�ɤ�����ɽ�� */
	public boolean stable = false;
	/** ��Υ����� */
	public int kind = 0;
	/** ������ޤ���null
	 * <p><b>������</b>
	 * �ۥ��Ȼ����ɽ��ʸ�������뷿�դ��ץ���ʸ̮̾����ä�
	 * ��°�������ʤ��ץ�������ƥ����Ȥ���������롣
	 * <br>[�����]�㳰Ū�ˡ�������Ĺ�������bundle��0�˥��åȤ���롣
	 * @see ContextDef.lhsMem */
	public ProcessContext pragmaAtHost = null;
//	/** �����ƥ�롼�륻�åȤȤ��ƻȤ��ʤ鿿 */
//	public boolean is_system_ruleset = false;
	
	/** ��̾ */
	public String name;

	/** ���ȥ�(compile.structure.Atom)�Υꥹ�� */
	public List<Atom> atoms = new LinkedList<Atom>();

	/** ����(compile.structure.Membrane)�Υꥹ�� */
	public List<Membrane> mems = new LinkedList<Membrane>();
	
	/** �롼��(compile.structure.RuleStructure)�Υꥹ�� */
	public List<RuleStructure> rules = new LinkedList<RuleStructure>();

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
	public List<Ruleset> rulesets = new LinkedList<Ruleset>();
	
	////////////////////////////////////////////////////////////////

	/** ����ѥ����ѥǡ�����¤��atom��mem��GROUP�������٤���ΤˤĤ��ƶ᤯�����֤��롣 **/
	public RependenceGraph rg = new RependenceGraph();
	////////////////////////////////////////////////////////////////

	/**
	 * ���󥹥ȥ饯��
	 * @param mem ����
	 */
	public Membrane(Membrane mem) {
		this.parent = mem;
	}
	
	public int getNormalAtomCount() {
		Iterator it = atoms.iterator();
		int c=0;
		while(it.hasNext()) {
			Atom a = (Atom)it.next();
			if(!a.functor.isInsideProxy() && !a.functor.isOutsideProxy()) c++;
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
		return Env.parray(list, ", ").toString();
	}

	public String toString() {
		String ret = "{ " + toStringWithoutBrace() + " }" + (kind==1 ? "_" : "") + (stable ? "/" : "");
		if (pragmaAtHost != null) {
			ret += "@" + ((ProcessContext)pragmaAtHost).getQualifiedName();
		}
		return ret;
	}
	public String toStringAsGuardTypeConstraints() {
		if (atoms.isEmpty()) return "";
		String text = "";
		Iterator it = atoms.iterator();
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
			//Env.p("About rule structure (RIGHT): "+rs.rightMem+" of "+rs);
			rs.rightMem.showAllRules();
		}
		// ���줽�줾��
		it = mems.iterator();
		while(it.hasNext()) ((Membrane)it.next()).showAllRules();
	}
	
	/*
	 * okabe
	 * ������˴ޤޤ�륢�ȥ�Τ������ֺǽ�Υ��ȥ�Υ��ȥ�̾���֤�
	 * �롼��κ��դ���ξ��ϺƵ��ƤӽФ�
	 * �ȥ졼���⡼�ɤǻ���
	 */
	public String getFirstAtomName(){
		Iterator atomIt = atoms.iterator();
		Iterator memIt= mems.iterator();
		if (atomIt.hasNext()) {
			return ((Atom) atomIt.next()).getName();
		} else if (memIt.hasNext()) {
			return ((Membrane) memIt.next()).getFirstAtomName();
		} else {
			// �ץ����Ȥ��ץ���ʸ̮�ΤȤ��ϤȤꤢ��������
			return "null";
		}
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
	
	public void connect(Object x, Object y){
//		System.out.println("connect "+x + "   " + y);
		rg.connect(x, y);
	}
	
	public void printfRG(){
		System.out.println(rg.toString());
	}
	public void createRG() {
		rg.addAll(atoms);
		rg.addAll(mems);
	}
	
	public Collection<LinkedList> allKnownElements() {
		return rg.allKnownElements();
	}
}

class RependenceGraph {
	public List<Membrane> mems;
	public List<Atomic> atoms;
	public List atomandmems;
	UnionFind uf;

	RependenceGraph(){
		atoms = new LinkedList();
		mems = new LinkedList();
		uf = new UnionFind();
	}
	RependenceGraph(List<Atomic> atoms, List<Membrane> mems){
		this.atoms = atoms;
		this.mems = mems;
		uf = new UnionFind();
		uf.addAll(atoms);
		uf.addAll(mems);
	}
	public void addAll(List x){
		uf.addAll(x);
	}
	public void connect(Object x, Object y){
		uf.union(x,y);
	}
	
	public void reachable(Object x, Object y){
		uf.areUnified(x, y);
	}
	
	public String toString(){
		return uf.allKnownElements().toString();
	}

	public Collection<LinkedList> allKnownElements() {
		return uf.allKnownElements();
	}
}

class UnionFind {
	private HashMap<Object, Object> lnk = new HashMap();
	private HashMap<Object, Integer> lnkSiz = new HashMap();
	private HashMap<Object, LinkedList> lists = new HashMap();

	public	void union( Object x, Object y )
		{
			if(!lnkSiz.containsKey(x))
				add(x);
			if(!lnkSiz.containsKey(y))
				add(y);
			Object tx = find(x);
			Object ty = find(y);
			Object temp = link_repr(tx, ty);
			LinkedList listx = lists.get(tx);
			LinkedList listy = lists.get(ty);
			if(temp == tx){
				listx.addAll(listy);
				lists.remove(ty);
			} else if(temp == ty) {
				listy.addAll(listx);
				lists.remove(tx);
			}
		}
	
	public String toString(){
		return lists.toString();
	}

	public	void add(Object x)
		{
			if(!lnkSiz.containsKey(x))
			{
				lnkSiz.put(x, 1);
				LinkedList<Object> list = new LinkedList();
				list.add(x);
				lists.put(x, list);
			}
		}
	public void addAll(Collection c){
		Iterator it = c.iterator();
		while(it.hasNext())
			add(it.next());
	}
	
	public boolean areUnified(Object x, Object y){
		return find(x) == find(y);
	}
	
	public Collection<LinkedList> allKnownElements(){
		return lists.values();
	}

	private	Object find( Object x )
		{
			// ������Path���̤���ȷ׻��̤� nlog(n) ���� n ack^-1(n) ��
			while(lnk.containsKey(x))
				x = lnk.get(x);
			return x;
		}
	
	private	Object link_repr( Object x, Object y )
		{
			if( x == y )
				return -1;
	
			// ���롼�ײ�
			if( lnkSiz.get(x) < lnkSiz.get(y) ) {
				lnk.put(x, y);
				lnkSiz.put(y, lnkSiz.get(y)+lnkSiz.get(x));
				return y;
			} else {
				lnk.put(y,x);
				lnkSiz.put(x, lnkSiz.get(x)+lnkSiz.get(y));
				return x;
			}
		}
}