package runtime;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import util.QueuedEntity;
import util.RandomIterator;
import util.Stack;
import util.Util;

/**
 * �������쥯�饹���¹Ի��Ρ����׻��Ρ�����ˤ������ɽ����
 * todo �ʸ�Ψ������ ��Ŭ���Ѥˡ���¹�˥롼�������Ĥ��Ȥ��Ǥ��ʤ��ʼ¹Ի����顼��Ф��ˡ֥ǡ�����ץ��饹���롣
 * <p><b>��¾����</b></p>
 * <p>lockThread �ե�����ɤؤ������ʤ��ʤ����å��μ����������ˤȡ���å��������Ԥ���碌�Τ����
 * ���Υ��饹�Υ��󥹥��󥹤˴ؤ��� synchronized ������Ѥ��롣
 * �ե�����ɤؤ������򤹤���ϡ��ä˵��Ҥ��ʤ��¤ꤳ����Υ�å���������Ƥ���ɬ�פ����롣
 * @author Mizuno, n-kato
 */
public final class Membrane extends QueuedEntity {
	/** �������������륿��������������Ȥ��ϡ�����Υ�å���������Ƥ���ɬ�פ����롣 */
	protected Task task;
	/** ���졣��⡼�Ȥˤ���ʤ��RemoteMembrane���֥������Ȥ򻲾Ȥ��롣GlobalRoot�ʤ��null��
	 * ��������Ȥ��ϡ�����Υ�å���������Ƥ���ɬ�פ����롣
	 * null �����������=�����������˻��ϡ�������ȿ����ξ���Υ�å���������Ƥ���ɬ�פ����롣 */
	protected Membrane parent;
	/** ���ȥ�ν��� */
	protected AtomSet atoms = new AtomSet();
	/** ����ν��� */
	protected Set<Membrane> mems = null;
//	/** ���Υ���μ�ͳ��󥯤ο� */
//	protected int freeLinkCount = 0;
	/** �롼�륻�åȤν��硣 */
	protected List<Ruleset> rulesets = new ArrayList<Ruleset>();
	/** ��Υ����� */
	protected int kind = 0;
	public static final int KIND_ND = 2;
	/** true�ʤ�Ф�����ʲ���Ŭ�ѤǤ���롼�뤬̵�� */
	protected boolean stable = false;
	/** ��³�ե饰��true�ʤ�Х롼��Ŭ�ѤǤ��ʤ��Ƥ�stable�ˤʤ�ʤ���*/
	public boolean perpetual = false;
	/** ��������å����Ƥ��륹��åɡ���å�����Ƥ��ʤ��Ȥ���null�����äƤ��롣*/
	protected Thread lockThread = null;

	private static int nextId = 0;
	private int id;
	
	/** ����->(���ԡ����Υ��ȥ�in����->���ԡ���Υ��ȥ�in���ԡ����줿����)
	 * TODO ��Υ����ѿ��Ǥ����Τ��ɤ��� */
	HashMap memToCopyMap = null; 
	
	/** �������̾����intern���줿ʸ����ޤ���null�� */
	String name = null;
	public boolean equalName(String s){
		if(name == null && s == null)return true;
		else if(name != null && name != null)
			return name.equals(s);
		else return false;
	}
	public String getName() { return name; }
	public void setName(String name) { this.name = name; } // ���ͤ��Ǥޤä��饳�󥹥ȥ饯�����Ϥ��褦�ˤ��٤�����
	
	/** �¹ԥ��ȥॹ���å���
	 * ����ݤˤ�����Υ�å���������Ƥ���ɬ�פϤʤ���
	 * ��¾����ˤϡ�Stack ���󥹥��󥹤˴ؤ��� synchronized ������Ѥ��Ƥ��롣 */
	private Stack ready = null;
	
//	/** ��⡼�ȥۥ��ȤȤ��̿��Ǥ�����Υ��ȥ��Ʊ�ꤹ��Ȥ��˻��Ѥ����atomid��ɽ��
//	 * <p>atomid (String) -> Atom
//	 * <p>������Υ���å��������塢�������Ϣ³�����å�������Τ�ͭ����
//	 * ����å����������˽�������졢����³����⡼�ȥۥ��Ȥ�����׵���᤹�뤿��˻��Ѥ���롣
//	 * ��⡼�ȥۥ��Ȥ�����׵�ǿ��������ȥब���������ȡ���������NEW_�򥭡��Ȥ��륨��ȥ꤬�ɲä���롣
//	 * $inside_proxy���ȥ�ξ�硢̿��֥�å��������Υ����ߥ󥰤ǥ�����ID�Ǿ�񤭤���롣
//	 * $inside_proxy�ʳ��Υ��ȥ�ξ�硢��å�����ޤ�NEW_�Τޤ����֤���롣
//	 * @see Atom.remoteid */
//	protected HashMap atomTable = null;

	///////////////////////////////
	// ���󥹥ȥ饯��

	/** ���ꤵ�줿�������˽�°�������������롣newMem/newRoot ����ƤФ�롣*/
	private Membrane(Task task, Membrane parent) {
		if (Env.shuffle >= Env.SHUFFLE_MEMS)
			mems = new RandomSet();
		else
			mems = new HashSet<Membrane>();
		
		this.task = task;
		this.parent = parent;
		id = nextId++;
	}
	/** ���������ʤ����������롣Task.createFreeMembrane ����ƤФ�롣*/
	protected Membrane(Task task) {
		this(task, null);
	}
	public Membrane() {
		this(null, null);
	}

	/** ������Υ����Х�ID��������� */
	public String getMemID() { return Integer.toString(id); }
//	/** �����줬��°����׻��Ρ��ɤˤ����롢������λ��ꤵ�줿���ȥ��ID��������� */
//	public String getAtomID(Atom atom) { return atom.getLocalID(); }

	///////////////////////////////
	// ����μ���

	/**
	 * �ǥե���Ȥμ������Ƚ����Ϥ��������֤��Ѥ����Ѥ�äƤ��ޤ��Τǡ�
	 * ���󥹥��󥹤��Ȥ˥�ˡ�����id���Ѱդ��ƥϥå��女���ɤȤ������Ѥ��롣
	 */
	public int hashCode() {
		return id;
	}
	// 061028 okabe ��󥿥����ͣ��ĤʤΤ�Global/Local �ζ��̤�ɬ�פʤ�
//	/** ������Υ�����ID��������� */
//	public String getLocalID() {  //public�ʤΤ�LMNtalDaemon����Ƥ�Ǥ��뤫�颪�ƤФʤ��ʤä��Τ�protected�Ǥ褤
//		return Integer.toString(id);
//	}
	
	/** �������������륿�����μ��� */
	public Task getTask() {
		return task;
	}
	/** ����μ��� */
	public Membrane getParent() {
		return parent;
	}
	
	/** AtomSet����������Ǽ��� */
	// �ȤäƤ��꤬�ʤ��Τǥ����ȥ�����
//	public Atom[] getAtomSet() {
//		return (Atom[])atoms.toArray();
//	}
	/** 060727 */
	/** �롼�륻�åȿ������ */
	public int getRulesetCount() {
		return rulesets.size();
	}
	public int getMemCount() {
		return mems.size();
	}
	/** proxy�ʳ��Υ��ȥ�ο������
	 * todo ����̾���Ǥ����Τ��ɤ��� */
	public int getAtomCount() {
		return atoms.getNormalAtomCount();
	}
	/** ���ꤵ�줿�ե��󥯥����ĥ��ȥ�ο������*/
	public int getAtomCountOfFunctor(Functor functor) {
		return atoms.getAtomCountOfFunctor(functor);
	}
	/** ���Υ���μ�ͳ��󥯤ο������ */
	public int getFreeLinkCount() {
		return atoms.getAtomCountOfFunctor(Functor.INSIDE_PROXY);
	}
	/** ��Υ����פ���� */
	public int getKind() {
		return kind;
	}
	/** ��Υ����פ��ѹ� */
	public void changeKind(int k) {
		kind = k;
		if (kind == KIND_ND) {
			//�����Ū�¹���ϡ����̤���ˡ�Ǥϼ¹Ԥ��ʤ�
			dequeue();
			toStable();
		}
	}
	/** ������Ȥ��λ�¹��Ŭ�ѤǤ���롼�뤬�ʤ�����true */
	public boolean isStable() {
		return stable;
	}
	/** stable�ե饰��ON�ˤ��� 10/26���� Task#exec()��ǻȤ�*/
	void toStable(){
		stable = true;
	}
	/** ��³�ե饰��ON�ˤ��� */
	public void makePerpetual(boolean f) {
		perpetual = f;
	}
//	/** ��³�ե饰��OFF�ˤ��� */
//	public void makeNotPerpetual() {
//		AbstractLMNtalRuntime machine = getTask().getMachine();
//		synchronized(machine) {
//			perpetual = false;
//			machine.notify();
//		}
//	}
	/** ������˥롼�뤬�����true */
	public boolean hasRules() {
		return !rulesets.isEmpty();
	}
	public boolean isRoot() {
		return task.getRoot() == this;
	}
	public boolean isNondeterministic() {
		return kind == KIND_ND;
	}
	
	// ȿ����
	
	public Object[] getAtomArray() {
		return atoms.toArray();
	}
	public Object[] getMemArray() {
		return mems.toArray();
	}
	/** 06/07/27 */
	/** �롼�륻�åȤΥ��ԡ������ */
	public ArrayList<Ruleset> getRuleset() {
		ArrayList<Ruleset> al = new ArrayList<Ruleset>(rulesets);
		return al;
	}
	/** ����Υ��ԡ������ */
	public HashSet getMemCopy() {
		return new HashSet(mems);
		//RandomSet s = new RandomSet();
		//s.addAll(mems);
		//return s;
	}
	/** ������ˤ��륢�ȥ��ȿ���Ҥ�������� */
	public Iterator<Atom> atomIterator() {
		return atoms.iterator();
	}
	/** ������ˤ�������ȿ���Ҥ�������� */
	public Iterator<Membrane> memIterator() {
		return mems.iterator();
	}
	/** ̾��func����ĥ��ȥ��ȿ���Ҥ�������� */
	public Iterator atomIteratorOfFunctor(Functor functor) {
		return atoms.iteratorOfFunctor(functor);
	}
	/** ������ˤ���롼�륻�åȤ�ȿ���Ҥ��֤� */
	public Iterator rulesetIterator() {
		if (Env.shuffle >= Env.SHUFFLE_RULES) {
			return new RandomIterator(rulesets);
		} else {
			return rulesets.iterator();
		}
	}

	///////////////////////////////
	// �ܥǥ�����RemoteMembrane�Ǥϥ����С��饤�ɤ�����

	// �ܥǥ����1 - �롼������
	
	/** �롼������ƾõ�� */
	public void clearRules() {
		if(Env.profile == Env.PROFILE_ALL)
			Env.p(Dumper.dump(this));
		rulesets.clear();
	}
	/** srcMem�ˤ���롼��򤳤���˥��ԡ����롣 */
	public void copyRulesFrom(Membrane srcMem) {
		rulesets.addAll(srcMem.rulesets);
	}
	/** �롼�륻�åȤ��ɲ� */
	public void loadRuleset(Ruleset srcRuleset) {
		if(rulesets.contains(srcRuleset)) return;
		rulesets.add(srcRuleset);
	}

	// �ܥǥ����2 - ���ȥ�����

	/** ���������ȥ�����������������ɲä��롣*/
	public Atom newAtom(Functor functor) {
		Atom atom = new Atom(this, functor);
		onAddAtom(atom);
		if(Env.fUNYO){
			unyo.Mediator.addAddedAtom(atom);
		}
		return atom;
	}
	/** �ʽ�°�������ʤ��˥��ȥ�򤳤�����ɲä��롣*/
	public void addAtom(Atom atom) {
		atom.mem = this;
		onAddAtom(atom);
	}
	/** ���ꤵ�줿���ȥ��̾�����Ѥ��� */
	public void alterAtomFunctor(Atom atom, Functor func) {
		
		atoms.remove(atom);
		atom.setFunctor(func);
		atoms.add(atom);
	}

	/** ���ꤵ�줿���ȥ�򤳤��줫�����롣
	 * <strike>�¹ԥ��ȥॹ���å������äƤ����硢�����å������������</strike>*/
	public void removeAtom(Atom atom) {
		if(Env.fUNYO){
			unyo.Mediator.addRemovedAtom(atom, getMemID());
		}
		atoms.remove(atom);
		atom.mem = null;
	}

	/** 
	 * �����ץ������ɤ����򸡺����롥(Stack��Ȥ��褦�˽��� 2005/07/26)
	 * ( 2006/09/12 Membrane�˰�ư��2�����ʾ��ground�򰷤���褦�˳�ĥ )
	 * (�����ȼ�������˼�����äƤ���Set���ѻ�)
	 * �����ץ����������륢�ȥ�ο����֤���
	 * // �����ˤϡ�(���սи����ȥ���)�����ץ����˴ޤޤ�ƤϤ����ʤ����ȥ��Set����ꤹ�롥
	 * �����ץ����˴ޤޤ�ƤϤ����ʤ����ȥ�ڤӤ��ΰ���( �Ĥޤ��� )��Set����ꤹ�롥
	 * ����������ͳ��󥯴������ȥ�˽в�ä����ϡ�-1���֤���
	 * 
	 * �����ץ�����Ϣ��Ǥ���ɬ�פ�����١��������Ϥ���1��������ΤߤǤ褤��
	 * ���������1�����ʳ��ΰ�������ã�����餽�������������������
	 * �����Ѥߤΰ���Ĥ��Ƥ�����
	 * ������λ�塤̤��ã�ΰ����������-1���֤���
	 * 
	 * @param avoList �����ץ����˽ФƤ��ƤϤ����ʤ���󥯤�List
	 * @return �����ץ����������륢�ȥ��
	 */
	public static int isGround(List<Link> links, Set<Atom> avoSet){
		Set<Atom> srcSet = new HashSet<Atom>(); // �����Ѥߥ��ȥ�
		java.util.Stack<Link> s = new java.util.Stack<Link>(); //��󥯤��Ѥॹ���å�
		s.push(links.get(0)); // ��1����������������
		int c=0;
		boolean[] exists = new boolean[links.size()]; // �����ˤĤ�����ã�������ɤ���
		exists[0] = true; // ��1��������é��
		while(!s.isEmpty()){
			Link l = (Link)s.pop();
			Atom a = l.getAtom();
			if(srcSet.contains(a))continue; //����é�ä����ȥ�
			if(avoSet.contains(l.getBuddy()))return -1; //�и����ƤϤ����ʤ����
			int argi = links.indexOf(l.getBuddy());
			if(argi >= 0){ // �����ץ����ΰ�������ã
				exists[argi] = true;
				continue; // ����ʾ��������ʤ�
			}
			if(a.getFunctor().equals(Functor.INSIDE_PROXY)||
				a.getFunctor().isOutsideProxy()) // ��ͳ��󥯴������ȥ����ã
				return -1; // ����
			c++;
			srcSet.add(a);
			for(int i=0;i<a.getArity();i++){
				if(i==l.getPos())continue;
				s.push(a.getArg(i));
			}
		}
		for(int i=0;i<links.size();i++)
			if(exists[i])continue;
			else return -1; // ̤��ã�κ�������м���
		return c;
	}
	
	/**
	 * Ʊ����¤����ä������ץ������ɤ�����������
	 * ( Stack��Ȥ��褦�˽��� 2005/07/27 )
	 * ( �����ȼ�������˼�����äƤ���Map���ѻ�)
	 * (��˰�ư�� 2�����ʾ���б� 2006/09/13 )
	 * �ɤ��餫�����ˤĤ���ground���ɤ����θ����ϺѤ�Ǥ����ΤȤ���
	 * 
	 * �ץ���ʸ̮�Ȥ��Ƥΰ������֤���פ��ʤ���Фʤ�ʤ�
	 * 
	 * @param srcLink ����оݤΥ��
	 * @return
	 */
	public static boolean eqGround(List<Link> srclinks, List<Link> dstlinks){
		Map<Atom,Atom> map = new HashMap<Atom,Atom>(); //��Ӹ����ȥफ������襢�ȥ�ؤΥޥå�
		java.util.Stack<Link> s1 = new java.util.Stack<Link>();  //��Ӹ���󥯤�����륹���å�
		java.util.Stack<Link> s2 = new java.util.Stack<Link>();  //������󥯤�����륹���å�
		s1.push(srclinks.get(0));
		s2.push(dstlinks.get(0));
		while(!s1.isEmpty()){
			Link l1 = s1.pop();
			Link l2 = s2.pop();
			int srci = srclinks.indexOf(l1.getBuddy());
			int dsti = dstlinks.indexOf(l2.getBuddy());
			if(srci != dsti)return false; // �ץ���ʸ̮�ΰ������ä����(>=)�ΰ��֤ΰ��ס������Ǥʤ����(-1)�γ�ǧ
			if(srci >= 0) continue; // �ץ���ʸ̮�ΰ������ä��餽��ʾ��������ʤ�
			if(l1.getPos() != l2.getPos()) return false; //�������֤ΰ��פ򸡺�
			if(!l1.getAtom().getFunctor().equals(l2.getAtom().getFunctor()))return false; //�ե��󥯥��ΰ��פ򸡺�
			if(!map.containsKey(l1.getAtom()))map.put(l1.getAtom(),l2.getAtom()); //̤��
			else if(map.get(l1.getAtom()) != l2.getAtom())return false;         //���Фʤ���԰���
			else continue;
			for(int i=0;i<l1.getAtom().getArity();i++){
				if(i==l1.getPos())continue;
				s1.push(l1.getAtom().getArg(i));
				s2.push(l2.getAtom().getArg(i));
			}
		}
		return true;
	}

	/**
	 * �����ץ�����ʣ������(�����ϺѤ�Ǥ���)
	 * ( java.util.Stack��Ȥ��褦���ѹ����������ȼ��������Map���ѻ� 2005/07/28)
	 * ( 2�����ʾ���б� 2006/09/13 )
	 * 
	 * �ʤ���2�����ξ��ˤϹ������ȥ����0��(�Ĥޤ���)�ξ�礬���뤬��
	 * ���ξ��ϻ�����insertconnectors̿��ˤ�ä�=/2����������Ƥ����ΤȤ��롥
	 * �Ĥޤꡤ���Υ᥽�åɤǤϹ�θ����ɬ�פϤʤ���
	 * 
	 * @param srcGround ���ԡ����δ����ץ��� �κ��Υꥹ��
	 * @return 2���ǤΥꥹ�� ( ������� : ���ԡ���δ����ץ��� �κ��Υꥹ��, �������� : ���ԡ����Υ��ȥफ�饳�ԡ���Υ��ȥ�ؤΥޥå�)
	 */
	public List<Link> copyGroundFrom(List<Link> srclinks){
		java.util.Stack<Link> s = new java.util.Stack<Link>();
		Map<Atom,Atom> map = new HashMap<Atom,Atom>();
		Link first = (Link)srclinks.get(0);
		// �ǽ�Υ��ȥ�����ޤ�ʣ�����Ƥ��ޤ� ( �ʤ������������ʤΤ��������Υ롼�פ˻��äƤ����뵤������)
		Atom cpAtom = newAtom(first.getAtom().getFunctor());
		map.put(first.getAtom(),cpAtom);
		// �ǽ�Υ��ȥ�ΰ��������ƥ����å����Ѥ�
		for(int i=0;i<cpAtom.getArity();i++){
			if(first.getPos()==i)continue;
			s.push(first.getAtom().getArg(i));
		}
		while(!s.isEmpty()){
			Link l = s.pop();
			int srci = srclinks.indexOf(l.getBuddy());
			if(srci >= 0)continue; // �ץ���ʸ̮�ΰ�������ã�����餽��ʾ�é��ʤ�
			if(!map.containsKey(l.getAtom())){
				cpAtom = newAtom(l.getAtom().getFunctor());
				map.put(l.getAtom(),cpAtom);
				Atom a = ((Atom)map.get(l.getAtom().getArg(l.getPos()).getAtom())); //��󥯤κ�
				a.args[l.getAtom().getArg(l.getPos()).getPos()]=new Link(cpAtom,l.getPos());
				for(int i=0;i<cpAtom.getArity();i++){
					s.push(l.getAtom().getArg(i));
				}
			}
			else{
				cpAtom = map.get(l.getAtom());
				Atom a = map.get(l.getAtom().getArg(l.getPos()).getAtom());
				a.args[l.getAtom().getArg(l.getPos()).getPos()]=new Link(cpAtom,l.getPos());
			}
		}
		List<Link> dstlinks = new ArrayList<Link>(srclinks.size());
		for(int i=0;i<srclinks.size();i++){
			Link srclink = srclinks.get(i);
			dstlinks.add( new Link(map.get(srclink.getAtom()),srclink.getPos()));
		}
		List ret_list = new ArrayList();
		ret_list.add(dstlinks);
		ret_list.add(map);
		return ret_list;//		return new Link(((Atom)map.get(srcGround.getAtom())),srcGround.getPos());
	}
	
	/** 1�����δ����ץ�����ʣ������ */
	public Link copyGroundFrom(Link srcGround){
		List<Link> srclinks = new ArrayList<Link>();
		srclinks.add(srcGround);
		List dstlinks = copyGroundFrom(srclinks);
		return (Link)dstlinks.get(0);
	}
	
	/** ���ꤵ�줿�����ץ����򤳤��줫�����롣 by kudo
	 * ( java.util.Stack��Ȥ��褦�˽�������ȼ�äư������� 2005/08/01 )
	 * ( 2�����ʾ���б� 2006/09/13 )
	 * ground�Ǥ��뤳�Ȥθ����ϺѤ�Ǥ����ΤȤ���
	 * 
	 * @param srcGround
	 * @return
	 */
	public void removeGround(List<Link> srclinks){
		java.util.Stack<Link> s = new java.util.Stack<Link>();
		Link first = srclinks.get(0);
		s.push(first);
		Set<Atom> srcSet = new HashSet<Atom>();
		while(!s.isEmpty()){
			Link l = s.pop();
			int srci = srclinks.indexOf(l.getBuddy());
			if( srci >= 0 ) continue; // �ץ���ʸ̮�ΰ�������ã�����餽��ʾ�é��ʤ�
			if(srcSet.contains(l.getAtom()))continue;
			srcSet.add(l.getAtom());
			for(int i=0;i<l.getAtom().getArity();i++){
				if(i==l.getPos())continue;
				s.push(l.getAtom().getArg(i));
			}

			atoms.remove(l.getAtom());
			l.getAtom().mem = null;
			l.getAtom().dequeue();
		}
	}
	
	/** 1�����δ����ץ����򤳤��줫������ */
	public void removeGround(Link srcGround){
		List<Link> srclinks = new ArrayList<Link>();
		srclinks.add(srcGround);
		removeGround(srclinks);
	}
	
	
	/** [final] 1������newAtom��ƤӽФ��ޥ��� */
	final Atom newAtom(String name, int arity) {
		return newAtom(new SymbolFunctor(name, arity));
	}	
	/** [final] ������˥��ȥ���ɲä��뤿�������̿�� */
	protected final void onAddAtom(Atom atom) {
		atoms.add(atom);
//		if (atom.getFunctor().isActive()) {
//			enqueueAtom(atom);
//		}
	}
	/** [final] removeAtom��ƤӽФ��ޥ��� */
	final void removeAtoms(List atomlist) {
		// atoms.removeAll(atomlist);
		Iterator it = atomlist.iterator();
		while (it.hasNext()) {
			removeAtom((Atom)it.next());
		}
	}

//	/** 
//	 * ������ˤ��륢�ȥ�atom�����η׻��Ρ��ɤ��¹Ԥ��륿�����ˤ�����μ¹ԥ��ȥॹ���å���ˤ���С�����롣
//	 * ¾�η׻��Ρ��ɤ��¹Ԥ��륿�����ˤ�����μ¹ԥ��ȥॹ���å���ΤȤ��ʥ����ƥॳ����ˤϡ��������
//	 * ��å�����Ƥ��ʤ��Τǲ��⤷�ʤ��Ǥ褤�������ξ��ϼ¹ԥ��ȥॹ���å���ˤʤ��ΤǴ����б��Ǥ��Ƥ��롣*/
//	public final void dequeueAtom(Atom atom) {
//		if (atom.isQueued()) {
//			atom.dequeue();
//		}
//	}

	// �ܥǥ����3 - ��������
	/** [final] ���ꤵ�줿�ʿ����̵������򤳤���λ���Ȥ����ɲä��롣
	 * �¹��쥹���å������ʤ�������Υ������ˤĤ��Ƥϲ��⤷�ʤ���*/
	public final void addMem(Membrane mem) {
		mems.add(mem);
		mem.parent = this;
		
		if(Env.fUNYO){
			unyo.Mediator.addAddedMembrane(mem);
		}
	}
	/** ���ꤵ�줿����򤳤��줫�����롣
	 * <strike>�¹��쥹���å������ʤ���</strike>
	 * �¹��쥹���å����Ѥޤ�Ƥ���м������� */
	public void removeMem(Membrane mem) {
		if(Env.LMNgraphic != null && !mem.isRoot())
			Env.LMNgraphic.removeGraphicMem(mem);
		if(Env.LMNtool != null && !mem.isRoot())
			Env.LMNtool.addRemovedMem(mem);		
		mems.remove(mem);
		mem.dequeue();
		mem.parent = null;
		
	}
	/** ���ꤵ�줿�Ρ��ɤǼ¹Ԥ�����å����줿kind==k�Υ롼������������������λ���ˤ������������롣
	 * @param nodedesc �Ρ���̾��ɽ��ʸ����
	 * @return �������줿�롼���� */
	public Membrane newRoot(String nodedesc, int k) {
		if(Env.debug > 0)Util.println("AbstractMembrane.newRoot(" + nodedesc + ")");
		if (nodedesc.equals("")) {
			Membrane mem = newMem();
			mem.changeKind(k);
			mem.lock();
			return mem;
		}
		//(nakajima 2004-10-25) ʬ�����Ȥꤢ�������󥹥ȥ饯������Ͽ������ˤ����Τǥ����ȥ����ȡ�
		//daemon.IDConverter.registerGlobalMembrane(this.getGlobalMemID(),this);
		
		// ��todo (��Ψ�����ڽ����ǽ�Ǥ��뤳�Ȥ�Τ�����)connectRuntime�ϥ����ɤǤ��Ǥ˸ƤФ�Ƥ���ΤǾ�Ĺ���⤷��ʤ�
//		AbstractLMNtalRuntime machine = LMNtalRuntimeManager.connectRuntime(nodedesc);
//		AbstractMembrane mem = machine.newTask(this).getRoot();
//		mem.changeKind(k);
		LMNtalRuntime machine = Env.theRuntime;
		Membrane mem = machine.newTask(this).getRoot();
		mem.changeKind(k);
		return mem;
	}
	/** ���ꤵ�줿�Ρ��ɤǼ¹Ԥ�����å����줿kind==0�Υ롼������������������λ���ˤ������������롣
	 * @param nodedesc �Ρ���̾��ɽ��ʸ����
	 * @return �������줿�롼���� */
	public Membrane newRoot(String nodedesc) {
		return newRoot(nodedesc, 0);
	}
	
	// �ܥǥ����4 - ��󥯤����
	
	/**
	 * atom1����pos1�����ȡ�atom2����pos2��������³���롣
	 * ��³���륢�ȥ�ϡ�
	 * <ol><li>������Υ��ȥ�Ʊ��
	 *     <li>�������outside_proxy��atom1�ˤȻ����inside_proxy��atom2��
	 * </ol>
	 * ��2�̤�˸¤��롣
	 */
	public void newLink(Atom atom1, int pos1, Atom atom2, int pos2) {

		atom1.args[pos1] = new Link(atom2, pos2);
		atom2.args[pos2] = new Link(atom1, pos1);

		if(Env.fUNYO){
			unyo.Mediator.addModifiedAtom(atom1);
			unyo.Mediator.addModifiedAtom(atom2);
		}
	}
	/** atom1����pos1�����ȡ�atom2����pos2�����Υ�������³���롣
	 * �¹Ը塢atom2����pos2�������Ѵ����ʤ���Фʤ�ʤ���
	 * <p><font color=red><b>
	 * clone�ϻȤ�ʤ����Ƥ褤�Ϥ������������̤ϥǥХå����ưפˤ��뤿�ᤳ�ΤޤޤǤ褤��
	 * </b></font>
	 */
	public void relinkAtomArgs(Atom atom1, int pos1, Atom atom2, int pos2) {
		atom1.args[pos1] = (Link)atom2.args[pos2].clone();
		atom2.args[pos2].getBuddy().set(atom1, pos1);

		if(Env.fUNYO){
			unyo.Mediator.addModifiedAtom(atom1);
			unyo.Mediator.addModifiedAtom(atom2);
		}
	}
	/** atom1����pos1�����Υ����ȡ�atom2����pos2�����Υ�������³���롣*/
	public void unifyAtomArgs(Atom atom1, int pos1, Atom atom2, int pos2) {
		atom1.args[pos1].getBuddy().set(atom2.args[pos2]);
		atom2.args[pos2].getBuddy().set(atom1.args[pos1]);
	}
	/** atom1����pos1�����ȡ�atom2����pos2������򴹤��롣--ueda */
	public void swapAtomArgs(Atom atom1, int pos1, Atom atom2, int pos2) {
		Link tmp = atom1.args[pos1];
		atom1.args[pos1] = atom2.args[pos2];
		atom2.args[pos2] = tmp;											
		atom1.args[pos1].getBuddy().set(atom1,pos1);
		atom2.args[pos2].getBuddy().set(atom2,pos2);
	}
	
	// ��ĥ
	
	/** deprecated */
	public void relink(Atom atom1, int pos1, Atom atom2, int pos2) {

		relinkAtomArgs(atom1, pos1, atom2, pos2);
	}
	/** link1�λؤ����ȥ������link2�λؤ����ȥ�����δ֤ˡ��������Υ�󥯤�ĥ�롣
	 * <p>�¹Ը�link1�����link2���Ȥ�̵���ʥ�󥯥��֥������Ȥˤʤ뤿�ᡢ���Ȥ���Ѥ��ƤϤʤ�ʤ���*/
	public void unifyLinkBuddies(Link link1, Link link2) {
		//link1.getBuddy().set(link2);
		//link2.getBuddy().set(link1);
		link1.getAtom().args[link1.getPos()] = link2;
		link2.getAtom().args[link2.getPos()] = link1;
	}
	/** atom1����pos1�����ȡ����link2�Υ�������³���롣
	 * <p>link2�Ϻ����Ѥ���뤿�ᡢ�¹Ը�link2�λ��Ȥ���Ѥ��ƤϤʤ�ʤ���*/
	public void inheritLink(Atom atom1, int pos1, Link link2) {
		link2.getBuddy().set(atom1, pos1);
		atom1.args[pos1] = link2;
	}

	// �ʲ��� AbstractMembrane �� final �᥽�å�
	
	/** [final] atom2����pos2�����˳�Ǽ���줿��󥯥��֥������Ȥؤλ��Ȥ�������롣*/
	public final Link getAtomArg(Atom atom2, int pos2) {
		return atom2.args[pos2];
	}	
	/** [final] relinkAtomArgs��Ʊ������̿�ᡣ������������Υǡ�����¤�Τ߹������롣*/
	protected final void relinkLocalAtomArgs(Atom atom1, int pos1, Atom atom2, int pos2) {
		atom1.args[pos1] = (Link)atom2.args[pos2].clone();
		atom2.args[pos2].getBuddy().set(atom1, pos1);
	}
	/** [final] unifyAtomArgs��Ʊ������̿�ᡣ������������Υǡ�����¤�Τ߹������롣*/
	protected final void unifyLocalAtomArgs(Atom atom1, int pos1, Atom atom2, int pos2) {
		atom1.args[pos1].getBuddy().set(atom2.args[pos2]);
		atom2.args[pos2].getBuddy().set(atom1.args[pos1]);
	}

	// �ܥǥ����5 - �켫�Ȥ��ư�˴ؤ������
	
//	/** ���������줫������
//	 * @deprecated */
//	public void remove() {
//		parent.removeMem(this);
//	}

	/** �ʿ��������ʤ�����srcMem�ˤ������ƤΥ��ȥ�Ȼ���ʥ�å���������Ƥ��ʤ��ˤ򤳤���˰�ư���롣
	 * ����ϥ롼�����ľ������ޤǺƵ�Ū�˰�ư����롣�ۥ��ȴְ�ư������ϳ���������롣
	 * ���Υ᥽�åɼ¹Ը塢srcMem�Ϥ��Τޤ��Ѵ�����ʤ���Фʤ�ʤ���
	 */
	public void moveCellsFrom(Membrane srcMem) {
		if (this == srcMem) return;
		// ���ȥ�ΰ�ư
		Iterator it = srcMem.atomIterator();
		while (it.hasNext()) {
			addAtom((Atom)it.next());
		}
		
		// ����ΰ�ư
		if (srcMem.task.getMachine() instanceof LMNtalRuntime) {
			// �������줫��ΰ�ư
			mems.addAll(srcMem.mems);
		}
		else {
			// ��⡼���줫��ʥ�������ءˤΰ�ư
			it = srcMem.memIterator();
			while (it.hasNext()) {
				Membrane subSrcMem = (Membrane)it.next();
				if (subSrcMem.isRoot()) {
					subSrcMem.moveTo(this);
				}
				else {
					Membrane subMem = newMem();
					if (!subSrcMem.blockingLock()) {
						throw new RuntimeException("Membrane.moveCellsFrom: blockingLock failure");
					}
					subMem.setName(subSrcMem.getName());
					removeMem(subMem);
					subMem.moveCellsFrom(subSrcMem);
					subSrcMem.unlock();
				}
			}
		}
		it = srcMem.memIterator();
		while (it.hasNext()) {
			Membrane subSrcMem = (Membrane)it.next();
			subSrcMem.parent = this;
			if (subSrcMem.task != task) subSrcMem.setTask(task);
		}
	}

	/** ��å����줿�ʿ����̵���ˤ������ʳ��������줿����dstMem�˰�ư���롣
	 * ����Υ�å��ϼ������Ƥ��ʤ���ΤȤ��롣
	 * ����ϥ롼�����ľ������ޤǺƵ�Ū�˰�ư����롣�ۥ��ȴְ�ư������ϳ���������롣
	 * <p>�᥽�åɽ�λ�塢this��̵�������ؤ��Ƥ����ǽ�������롣
	 * @return ������ؤλ��ȡʥ�⡼�ȡ�������֤ǰ�ư������硢�ѹ����Ƥ����ǽ���������
	 */
	public Membrane moveTo(Membrane dstMem) {
		if (parent != null) {
			Util.errPrintln("Warning: membrane with parent was moved");
			parent.removeMem(this);
		} 
		if (dstMem instanceof Membrane) {
			// ��������Υ�������ؤΰ�ư
			dstMem.addMem(this);
			if (dstMem.task != task) {
				setTask(dstMem.task);
			}
			return this;
		}
		else {
			// ��������Υ�⡼����ؤΰ�ư
			Membrane mem = dstMem.newMem();
			mem.moveCellsFrom(this);
			// this���Ѵ����٤��Ǥ��롣���� TO-DO (A) �β��ˡ�Ϥ��Τ褦�ʿ��������moveCellsFrom�餷��
			return mem;
		}
//		activate();
		//enqueueAllAtoms();
	}
	
	/** ������Ȥ��λ�¹��������륿�����򹹿����뤿��˸ƤФ������̿�ᡣ
	 * �������롼����ʲ��Υ��������ѹ����ʤ����Ĥޤ�롼������Ф��ƸƤФ줿���ϲ��⤷�ʤ���*/
	protected void setTask(Task newTask) {
		if (isRoot()) return;
		boolean locked = false;
		if (lockThread != Thread.currentThread()) {
			blockingLock();
			locked = true;
		}
		boolean queued = false;
		if (isQueued()) {
			//��å����Ƥ���Τǡ����δ֤�dequeue����Ƥ�����Ϥʤ���
			dequeue();
			queued = true;
		}
		task = newTask;
		if (queued)
			activate();
		Iterator it = memIterator();
		while (it.hasNext()) {
			((Membrane)it.next()).setTask(newTask);
		}
		if (locked)
			unlock();
		// TODO (A) �ۥ��ȴְ�ư����GlobalMembraneID���ѹ����ʤ�������פ�Ĵ�٤�
	}
//	/** ������ʥ롼����ˤο�����ѹ����롣LocalLMNtalRuntime�ʷ׻��Ρ��ɡˤΤߤ��Ƥ֤��Ȥ��Ǥ��롣
//	 * <p>�����졢
//	 * AbstractMembrane#newRoot�����AbstractMachine#newTask�ΰ����˿�����Ϥ��褦�ˤ���
//	 * AbstractMembrane#moveTo��Ȥäƿ�����ѹ����뤳�Ȥˤ�ꡢ
//	 * todo ��������Τ���᥽�åɤ��ѻߤ��ʤ���Фʤ�ʤ� */
//	void setParent(AbstractMembrane mem) {
//		if (!isRoot()) {
//			throw new RuntimeException("setParent requires this be a root membrane");
//		}
//		parent = mem;
//	}

	//////////////////////////////////////////////////////////////
	// kudo
	
	/** �������ʣ������������ <strike>����Ū�Ǥʤ���ͳ��󥯤�̵����ΤȲ���</strile>
	 * */
//	public HashMap copyFrom(AbstractMembrane srcMem) {
//		int atomsize = srcMem.atoms.size(); //���ȥ�ο�
//		int memsize = srcMem.mems.size(); //����ο�
//		List linkatom[] = new LinkedList[atomsize]; //�����Υ��ȥ�Υꥹ��
//		List linkpos[] = new LinkedList[atomsize]; //�����Υݥ������Υꥹ��
//		//�����
//		for (int i = 0; i < linkatom.length; i++) {
//			linkatom[i] = new LinkedList();
//			linkpos[i] = new LinkedList();
//		}
//
//		//����˷Ҥ����󥯤˴ؤ��ơ�$out�Τߤ���Yes (n-kato 2004-10-24)�ˡ����ǿ��ä��ֹ�ǻȤ���褦�ˤ��Ƥ�����
//		//�ɤλ���Ρ��ɤΥ��ȥ�˷Ҥ��äƤ���Τ��򼨤���id�ֹ档
//		int  glmemid[] = new int[atomsize];
//		int glatomid[] = new int[atomsize];
//		
//		//���ȥ�ˤȤꤢ�����ֹ�򿶤롣Atom.id�Ȥ��̡�̾�����褯�ʤ��ʡ�
//		Map atomId = new HashMap(); //Atom -> int
//		Atom idAtom[] = new Atom[atomsize]; //int -> Atom
//		int varcount = 0;
//		
//		//����ˤ��ֹ�򿶤�
//		Map memId = new HashMap(); // Mem -> int
//		AbstractMembrane idMem[] = new AbstractMembrane[memsize]; //int -> Mem
//		int memvarcount = 0;
//		
//		//��󥯾�������
//		Iterator it = srcMem.atomIterator();
//		while (it.hasNext()) {
//			//��󥯸����ȥ�
//			Atom atomo = (Atom) it.next();//��󥯸�
//			if (!atomId.containsKey(atomo)) {
//				atomId.put(atomo, new Integer(varcount));
//				idAtom[varcount++] = atomo;
//			}
//			int o = ((Integer) atomId.get(atomo)).intValue();
//			//��󥯤�é��
//			for (int i = 0; i < atomo.args.length; i++) {
//				Atom atoml = atomo.nthAtom(i);
//				if (atoml.mem == srcMem) { //�ɽ���
//					if (!atomId.containsKey(atoml)) {
//						atomId.put(atoml, new Integer(varcount));
//						idAtom[varcount++] = atoml;
//					}
//					linkatom[o].add(i, atomId.get(atoml));
//					linkpos[o].add(i, new Integer(atomo.getArg(i).getPos()));
//				}else if(atoml.mem != null && atoml.mem.parent == srcMem){//����ؤΥ��
//					if(!memId.containsKey(atoml.mem)){
//						memId.put(atoml.mem,new Integer(memvarcount));
//						idMem[memvarcount++] = atoml.mem;
//					}
//					glmemid[o] = ((Integer)memId.get(atoml.mem)).intValue();
//					glatomid[o] = atoml.id; 
//					linkatom[o].add(i,null);
//					linkpos[o].add(i,new Integer(atomo.getArg(i).getPos()));
//				}else{//����ʤ����ɤ��ˤ�Ҥ��äƤ��ʤ�
//					linkatom[o].add(i,null);
//					linkpos[o].add(i,null);
//				}
//			}
//		}
//
//		//�����map���������
//		it = srcMem.memIterator();
//		while(it.hasNext()){
//			AbstractMembrane itm = (AbstractMembrane)it.next();
//			if(!memId.containsKey(itm)){
//				memId.put(itm,new Integer(memvarcount));
//				idMem[memvarcount++] = itm;
//			}
//		}
//
//
//		//�����Ƶ�Ū�˥��ԡ�(���Ʊ���ʹԤˤ����Ʊ��֥��ԡ����Ǥ��ʤ�)
//		Map[] oldIdToNewAtom = new Map[memsize];
//		for(int i=0;i<memvarcount;i++){
//			oldIdToNewAtom[i] = newMem().copyFrom(idMem[i]);
//		}
//
//		HashMap retHashMap = new HashMap();//���ԡ�����$in��id -> ���ԡ����$in���ȥ�
//
//		//���ȥ�Υ��ԡ������
//		Atom[] idAtomCopied = new Atom[varcount];
//		for (int i = 0; i < varcount; i++) {
//			idAtomCopied[i] = newAtom(idAtom[i].getFunctor());
//		}
//
//		//��󥯤�Ž��ʤ���
//		for (int i = 0; i < varcount; i++) {
//			for (int j = 0; j < linkatom[i].size();j++) {
//				if(linkatom[i].get(j) != null){
//					int l = ((Integer) linkatom[i].get(j)).intValue();
//					int lp = ((Integer) linkpos[i].get(j)).intValue();
//					newLink(idAtomCopied[i], j, idAtomCopied[l], lp);
//				}else{//����褬Ʊ�����̵�����
//					if(idAtom[i].nthAtom(j).mem != null && idAtom[i].nthAtom(j).mem.parent == srcMem){//����˷Ҥ��äƤ������
//						Atom na = (Atom)oldIdToNewAtom[glmemid[i]].get(new Integer(glatomid[i]));
//						int lp = ((Integer) linkpos[i].get(j)).intValue();
//						newLink(idAtomCopied[i],j,na,lp);
//					}else{//����ʤ����ɤ��ˤ�Ҥ��äƤ��ʤ���map���ɲ�
//						retHashMap.put(new Integer(idAtom[i].id),idAtomCopied[i]);
//					}
//				}
//			}
//		}
//		return retHashMap;
//	}
	
	
	/**
	 * ���ꤵ�줿����,���λ���ڤӥ��ȥ�򤳤���˥��ԡ�����.
	 * ����Υ��ԡ���������ͤΥޥåפ�,memToCopyMap����Ͽ����
	 * ����Υ��ԡ�������,memToCopiedMem����Ͽ�����.
	 * ������⥭���ϥ��ԡ�������.
	 * ���ȥ�Υ��ԡ��ˤ�,copyAtoms�᥽�åɤ�Ƶ�Ū�˸Ƥ�.
	 * ����ͤ�,���ԡ����Υ��ȥ��ꥳ�ԡ���Υ��ȥ�ؤ�Map.
	 * �����copyFrom�᥽�åɤǤϥ�����Atom.id��Integer���ä���,
	 * ������Atom���֥������Ȥ��ѹ��ˤʤä�.
	 * @param srcmem ���ԡ�������
	 * @return ���ԡ����Υ��ȥ�->���ԡ���Υ��ȥ�Ȥ���Map
	 */
	public Map copyCellsFrom(Membrane srcmem){
		memToCopyMap = new HashMap();
		Iterator it = srcmem.memIterator();
		while(it.hasNext()){
			Membrane omem = (Membrane)it.next();
			Membrane nmem = newMem();
			nmem.setName(omem.getName());
			memToCopyMap.put(omem,nmem.copyCellsFrom(omem));
			nmem.copyRulesFrom(omem);
		}
		it = srcmem.atomIterator();
		Map oldAtomToNewAtom = new HashMap();
		while(it.hasNext()){
			Atom oatom = (Atom)it.next();
			if(oldAtomToNewAtom.containsKey(oatom))continue;
			oldAtomToNewAtom.put(oatom,newAtom(oatom.getFunctor()));
			//0�������ȥ�ʤ�а��������ʤ���(2006/05/26 kudo)
			if(oatom.getArity()>0)oldAtomToNewAtom = copyAtoms(oatom,oldAtomToNewAtom);
		}
		return oldAtomToNewAtom;
	}
	
	public Map copyCellsFrom2(Membrane srcmem){
		memToCopyMap = new HashMap();
		Iterator it = srcmem.memIterator();
		while(it.hasNext()){
			Membrane omem = (Membrane)it.next();
			Membrane nmem = newMem();
			nmem.setName(omem.getName());
			memToCopyMap.put(omem,nmem.copyCellsFrom(omem));
			nmem.copyRulesFrom(omem);
		}
		it = srcmem.atomIterator();
		Map oldAtomToNewAtom = new HashMap();
		while(it.hasNext()){
			Atom oatom = (Atom)it.next();
			if(oldAtomToNewAtom.containsKey(oatom))continue;
			if(oatom.getFunctor().getName()!="+" && !oatom.getFunctor().isInsideProxy()){
			    oldAtomToNewAtom.put(oatom,newAtom(oatom.getFunctor()));
				//0�������ȥ�ʤ�а��������ʤ���(2006/05/26 kudo)
				if(oatom.getArity()>0)
					oldAtomToNewAtom = copyAtoms(oatom,oldAtomToNewAtom);
			}
		}
		return oldAtomToNewAtom;
	}
	
	/**
	 * ���ꤵ�줿���ȥ���,é��륢�ȥ�����Ƥ�����˥��ԡ�����.
	 * ��󥯹�¤��Ƹ�����.
	 * atom��,���˥��ԡ�����Ƥ��뤳�ȤˤʤäƤ���.
	 * �ޤ�,����atom�ν�°������λ����,���˥��ԡ�����Ƥ���.
	 * ��󥯤�����˷Ҥ��äƤ������,memToCopyMap��memToCopiedMem���Ȥ��ƥ�󥯤�Ҥ�.
	 * ����˷Ҥ��äƤ������ɤ���ˤ��°���Ƥ��ʤ����ȥ�˷Ҥ��äƤ�����,̵�뤹��.
	 * map��,���ԡ����Υ��ȥफ�饳�ԡ����줿���ȥ�ؤλ��Ȥ���Ͽ����Ƥ���,
	 * ���ԡ��ѤߤΥ��ȥफ�ɤ����θ����ˤ����.
	 * @param atom
	 * @param map
	 * @return �������줿�ʤ��⤷��ʤ���Map
	 */
	public Map copyAtoms(Atom atom,Map map){
		for(int i=0;i<atom.args.length;i++){
			if(map.containsKey(atom.args[i].getAtom())){
				newLink(((Atom)map.get(atom)),i,
				((Atom)map.get(atom.args[i].getAtom())),atom.args[i].getPos());
			}
			else{
				if(atom.args[i].getAtom().mem != atom.mem){//����褬������Ǥʤ�
					if(atom.args[i].getAtom().mem != null &&
					atom.args[i].getAtom().mem.parent == atom.mem){//����طҤ��äƤ���
						//AbstractMembrane copiedmem = (AbstractMembrane)memToCopiedMem.get(atom.args[i].getAtom().mem);
						Map copymap = (Map)memToCopyMap.get(atom.args[i].getAtom().mem);
						Atom copiedatom = (Atom)copymap.get(atom.args[i].getAtom());
						newLink(((Atom)map.get(atom)),i,
						copiedatom,atom.args[i].getPos());
					}
					else continue;
				}
				else{
					Atom natom = newAtom(atom.args[i].getAtom().getFunctor());
					map.put(atom.args[i].getAtom(),natom);
					newLink(((Atom)map.get(atom)),i,
					natom,atom.args[i].getPos());
					map = copyAtoms(atom.args[i].getAtom(),map);
				}
			}
		}
		return map;
	}
	
	public void drop(){
		if (isRoot()) {
			// TODO kill this task
		}
		Iterator it = atomIterator();
		while(it.hasNext()){
			Atom atom = (Atom)it.next();
			atom.dequeue();
			// atom.free();
			// it.remove();
		}
		atoms.clear();
		it = memIterator();
		while(it.hasNext()){
			Membrane mem = (Membrane)it.next();
			mem.drop();
			mem.free();
		}
	}

//	/**
//	 * by kudo
//	 * �����ץ������˴�����(�����ϺѤ�Ǥ���)
//	 * @param srcGround �˴���������ץ���
//	 */
//	public void dropGround(Link srcGround, Set srcSet){
//		if(srcSet.contains(srcGround.getAtom()))return;
//		srcSet.add(srcGround.getAtom());
//		for(int i=0;i<srcGround.getAtom().getArity();i++){
//			if(i==srcGround.getPos())continue;
//			dropGround(srcGround.getAtom().getArg(i),srcSet);
//		}
//		srcGround.getAtom().dequeue();
//	}
	
	////////////////////////////////////////////////////////////////
	// ��å��˴ؤ������ - ������̿��ϴ�������task��ľ��ž�������
	
	/**
	 * ���ߤ�������å����Ƥ��륹��åɤ�������롣
	 */
	public Thread getLockThread() {
		return lockThread;
	}
	
	// - ������̿��
	
	// - �ܥǥ�̿��


	///////////////////////////////
	// ��ͳ��󥯴������ȥ��ĥ���ؤ��򤹤뤿������
	// todo �ʸ�Ψ������star�򥭥塼�Ǵ������뤳�Ȥˤ�ꡢalterAtomFunctor�β���򸺤餹�Ȥ褤�����Τ�ʤ���
	// ���塼��LinkedList���֥������ȤȤ���react�����¸���֤Ȥ���star��Ϣ�Υ᥽�åɤΰ������Ϥ���롣
	// $p��ޤ����Ƥ�������줫������дط����롼��Ŭ�Ѥ����Ѥʾ�硢
	// $p�����Ĥ����Ƥ���򤦤ޤ������Ѥ��뤳�Ȥˤ�äơ�star��Ϣ�ν����������Ƥ�ɬ�פ��ʤ��ʤ롣
	
	// todo �ʸ�Ψ������LinkedList���֥������Ȥ��Ф���contains��Ƥ�Ǥ���Τ򲿤Ȥ�����
	
	/** �����줬remove���줿ľ��˸ƤФ�롣
	 * �ʤ�removeProxies�ϡ��롼�뺸�դ˽񤫤줿���ȥ�������塢
	 * �롼�뺸�դ˽񤫤줿��Τ���$p����Ĥ�Τ��Ф�����¦���줫��ƤФ�롣
	 * <p>��������Ф���
	 * <ol>
	 * <li>������μ�ͳ/�ɽ��󥯤Ǥʤ��ˤ⤫����餺����������̲ᤷ�Ƥ����󥯤�������:V=A��
	 * <li>������μ�ͳ��󥯤��и����륢�ȥ�ʤ�����$in,$out�Τ����줫�ˤ�̾����star���Ѥ��롣
	 * </ol>
	 * <p>���٤Ƥ�removeProxies�θƤӽФ�����λ�����
	 * <ul>
	 * <li>$p�˥ޥå������ץ����μ�ͳ��󥯤�$p���񤫤줿���star���ȥ�˽и�����褦�ˤʤꡢ
	 * <li>star���ȥ�Υ����ϡ�star���ȥ�ޤ��������outside_proxy����1�����ˤʤäƤ��롣
	 * </ul>
	 * <pre>
	 * ( {{$p},$q},{$r} :- ... )
	 *     {{a(i(A),i(X))},  b(i(B),o(X)),i(V)=o(A)}, {d(i(W))}, c(o(V)),o(B)=o(W)
	 * --> *{a(s(A),s(X))}; {b(i(B),o(X)),i(V)=o(A)}, {d(i(W))}, c(o(V)),o(B)=o(W)
	 * -->  {a(s(A),s(X))};*{b(s(B),s(X))          }; {d(i(W))}, c(o(A)),o(B)=o(W)
	 * -->  {a(s(A),s(X))}; {b(s(B),s(X))          };*{d(s(W))}; c(o(A)),o(B)=o(W)
	 * 
	 * ( {$p} :- ... )
	 *      {a(i(X))}, b(o(X))
	 * --> *{a(s(X))}; b(o(X))
	 * 
	 * 
	 * ( {$p[i(A)|*V]},{$q[i(B)|*W]},E=o(A),F=o(B) :- ... )
	 *                                      {a(i(A)),b(i(B))}, {c(i(A')),d(i(B'))}, o(A)=o(A'),o(B)=o(B')
	 * --> AA=i(A),BB=i(B'),E=o(A),F=o(B'); {a( AA ),b(i(B))}, {c(i(A')),d( BB  )}, E=o(A'),F=o(B)
	 * -->>AA=i(A),BB=i(B'),E=o(A),F=o(B');*{a( AA ),b(s(B))};*{c(s(A')),d( BB  )}; E=o(A'),F=o(B)
	 * </pre>
	 */
	public void removeProxies() {
		// NOTE atoms�ؤ���ɬ�פˤʤ�Τǡ�Set�Υ������������Ƥ���ȿ���Ҥ�Ȥä�����
		//      �ɤߤ䤹������Ψ���ɤ����⤷��ʤ� ����⡼�Ȥξ���Ŭ�Ѥ���Τ��񤷤������Τ�ʤ�
		LinkedList changeList = new LinkedList();	// star�����륢�ȥ�Υꥹ��
		LinkedList removeList = new LinkedList();
		Iterator it = atoms.iteratorOfOUTSIDE_PROXY();
		while (it.hasNext()) {
			Atom outside = (Atom)it.next();
			Atom a0 = outside.args[0].getAtom();
			// outside�Υ���褬����Ǥʤ����ڤ��θ����Τ����remove�� parent=null; ��ɬ�ס�			
			if (a0.getMem().getParent() != this) {
				Atom a1 = outside.args[1].getAtom();
				// ��������̲ᤷ�ƿ���˽ФƤ�����󥯤����
				if (a1.getFunctor().equals(Functor.INSIDE_PROXY)) {
					unifyLocalAtomArgs(outside, 0, a1, 0);
					removeList.add(outside);
					removeList.add(a1);
				}
				else {
					// ��������̲ᤷ��̵�ط���������äƤ�����󥯤����
					if (a1.getFunctor().isOutsideProxy()
					 && a1.args[0].getAtom().getMem().getParent() != this) {
						if (!removeList.contains(outside)) {
							unifyLocalAtomArgs(outside, 0, a1, 0);
							removeList.add(outside);
							removeList.add(a1);
						}
					}
					// ����ʳ��Υ�󥯤ϡ�������μ�ͳ��󥯤ʤΤ�̾����star���Ѥ���
					else {
						changeList.add(outside);
					}
				}
			}
		}
		removeAtoms(removeList);
		// �������inside_proxy���ȥ��̾����star���Ѥ���
		it = atoms.iteratorOfFunctor(Functor.INSIDE_PROXY);
		while (it.hasNext()) {
			changeList.add(it.next());
		}
		// star����¹Ԥ���
		it = changeList.iterator();
		while (it.hasNext()) {
			alterAtomFunctor((Atom)it.next(), Functor.STAR);
		}
	}
	/** ���դ�$p��2�İʾ夢��롼��ǡ����դ����Ƥ�removeProxies���ƤФ줿���
	 * ������Ф��ƸƤ֤��Ȥ��Ǥ���ʸƤФʤ��Ƥ�褤�ˡ�
	 * <p>��������2�Ĥ�outside��ͳ�ǡ��̲ᤷ��̵�ط���������äƤ�����󥯤����롣
	 * <pre>
	 *      {a(s(A),s(X))}; {b(s(B),s(X))          }; {d(s(W))}; c(o(A)),o(B)=o(W)
	 * -->  {a(s(A),s(X))}; {b(s(B),s(X))          }; {d(s(B))}; c(o(A))
	 * 
	 *      {a(s(X))}; b(o(X))
	 * -->  {a(s(X))}; b(o(X))
	 * 
	 *      AA=i(A),BB=i(B'),E=o(A),F=o(B'); {a( AA ),b(s(B))}; {c(s(A')),d( BB  )}; E=o(A'),F=o(B)
	 * -->  �Ѳ��ʤ�
	 * </pre>
	 */
	public void removeToplevelProxies() {
		// (*) �� ( {$p[i(A)]},{$q[|*V]},E=o(A) :- ... ) ��E��*V�˴ޤޤ����ʤɤؤ��к��ʰ���¦�˶����
		ArrayList removeList = new ArrayList();
		Iterator it = atoms.iteratorOfOUTSIDE_PROXY();
		while (it.hasNext()) {
			Atom outside = (Atom)it.next();
			// outside����1�����Υ���褬����Ǥʤ����			
			if (outside.args[0].getAtom().getMem() != null // �ɲ� n-kato 2004-10-30 (*)
			 && outside.args[0].getAtom().getMem().getParent() != this) {
				// outside����2�����Υ���褬outside�ξ��
				Atom a1 = outside.args[1].getAtom();
				if (a1.getFunctor().isOutsideProxy()) {
					// 2�Ĥ��outside����1�����Υ���褬����Ǥʤ����
					if (a1.args[0].getAtom().getMem() != null // �ɲ� n-kato 2004-10-30 (*)
					 && a1.args[0].getAtom().getMem().getParent() != this) {
						if (!removeList.contains(outside)) {
							unifyLocalAtomArgs(outside, 0, a1, 0);
							removeList.add(outside);
							removeList.add(a1);
						}
					}
				}
			}
		}
		removeAtoms(removeList);
	}
	/** ���դΡʰ����˻��ꤷ�����쥻��Ρ��칽¤�����$p�����Ƥ����֤�����ǡ�
	 * �롼�뱦�դ˽񤫤줿���������Ф�����¦���줫��ƤФ�롣
	 * <p>����childMemWithStar�ˤ���star���ȥ�ʻ���μ�ͳ��󥯤��Ĥʤ��äƤ���ˤ��Ф���
	 * <ol>
	 * <li>̾����inside_proxy���Ѥ�
	 * <li>��ͳ��󥯤�ȿ��¦�Υ��ȥ�νи�������ΰ��֤ˤ������äƼ��Τ褦�˾��ʬ���򤹤롧
	 *   <ul>
	 *   <li>�������star���ȥ�ʤ�С�
	 *       ��Ԥ�̾����outside_proxy���Ѥ���ʺǽ��star���ȥ���б�������ˡ�
	 *       �ޤ���ʬ���¹ԤΤ���ˡ����Υ�󥯤�ĥ��ʤ���������:X,Y��
	 *   <li>�����������ˤ˻Ĥä�outside_proxy���ȥ�ʤ�С����⤷�ʤ���
	 *       ����ϥ�󥯤��롼��κ��դ˥ޥå������ץ����μ�ͳ��󥯤ΤȤ��˵����롣����:V��
	 *   <li>Ʊ������ˤ����star�˥��ȥ�ʤ�С�2�Ĥ�star�������롣����:B,C�ˡ��ɲ� 2004/1/18��
	 *   <li>����ʳ�����ˤ��륢�ȥ�ʤ�С�
	 *       ��ͳ��󥯤���������̲᤹��褦�ˤ��롣����:A->V,E->W��
	 *       ���ΤȤ���������˺�������outside_proxy�Ǥʤ����Υ��ȥ��̾����star�ˤ��롣
	 *   </ul>
	 * </ol>
	 * @param childMemWithStar �ʼ�ͳ��󥯤���ġ˻���
	 * <pre>
	 * ( ... :- {{$p},$q,$r} )
	 *      {a(s(A),s(X))}; {b(s(B),s(X)),         }; {d(s(B))}; c(o(A))
	 * -->  {a(s(A),s(X))}; {b(s(B),s(X)),             d(s(B))}, c(o(A))
	 * -->{*{a(i(A),i(X))},  b(s(B),o(X)),s(V)=o(A),   d(s(B))}, c(o(V))
	 * -->*{{a(i(A),i(X))},  b(  B ,o(X)),i(V)=o(A),   d(  B )}, c(o(V))
	 * 
	 * ( ... :- {{$p},$q},{$r} )
	 *      {a(s(A),s(X))}; {b(s(Y),s(X)),         }; {d(s(E))}; c(o(A))
	 * -->  {a(s(A),s(X))}; {b(s(Y),s(X)),         },*{d(i(W))}, c(o(A)),s(E)=o(W)
	 * -->{*{a(i(A),i(X))},  b(s(Y),o(X)),s(V)=o(A)}, {d(i(W))}, c(o(V)),s(E)=o(W)
	 * -->*{{a(i(A),i(X))},  b(i(Y),o(X)),i(V)=o(A)}, {d(i(W))}, c(o(V)),o(Y)=o(W)
	 * 
	 * ( ... :- {$p,$q,$r} )
	 *      {a(s(V),s(B))}; {b(s(C),s(B)),         }; {d(s(C))}; c(o(V))
	 * -->  {a(s(V),s(B)),   b(s(C),s(B)),             d(s(C))}, c(o(V))
	 * -->  {a(s(V),  B ),   b(  C ,  B ),             d(  C )}, c(o(V))
	 *
	 * ( ... :- {$p} )
	 *      {a(s(V))}; b(o(V))
	 * -->  {a(i(V))}, b(o(V))
	 * 
	 * ( ... :- {$p[i(A)|*V],$q[i(B)|*W]},E=o(A),F=o(B) )
	 *      AA=i(A),BB=i(B'),E=o(A),F=o(B'); {a( AA ),b(s(B))}; {c(s(A')),d( BB  )}; E=o(A'),F=o(B)
	 * -->  AA=i(A),BB=i(B'),E=o(A),F=o(B');*{a( AA ),b(i(B))},*{c(i(A')),d( BB  )}, E=o(A'),F=o(B)
	 * </pre>
	 */
	public void insertProxies(Membrane childMemWithStar) {
		LinkedList changeList = new LinkedList();	// inside_proxy�����륢�ȥ�Υꥹ��
		LinkedList removeList = new LinkedList();
		Iterator it = childMemWithStar.atomIteratorOfFunctor(Functor.STAR);
		while (it.hasNext()) {
			Atom star = (Atom)it.next(); // n
			Atom oldstar = star.args[0].getAtom();
			if (oldstar.getMem() == childMemWithStar) { // ����ο������ɽ��󥯤ξ��
				if (!removeList.contains(star)) {
					childMemWithStar.unifyAtomArgs(star,1,oldstar,1);
					removeList.add(star);
					removeList.add(oldstar);
				}
			} else {
				changeList.add(star);
				// ��ͳ��󥯤�ȿ��¦�νи���������Υ��ȥ�ʤ�С���Ԥ�̾����outside_proxy���Ѥ��롣
				// ���ΤȤ�star���ä��뤫�⤷��ʤ��Τǡ�star�򥭥塼�Ǽ�������Ȥ��ϥХ�����ա�
				if (oldstar.getMem() == this) {
					//changeList.add(star);
					alterAtomFunctor(oldstar, new SpecialFunctor("$out",2, childMemWithStar.kind));
					newLink(oldstar, 0, star, 0);
				} else {
					Atom outside = newAtom(new SpecialFunctor("$out",2, childMemWithStar.kind)); // o
					Atom newstar = newAtom(Functor.STAR); // m
					newLink(newstar, 1, outside, 1);
					relinkAtomArgs(newstar, 0, star, 0); // ����ˤ��star[0]��̵���ˤʤ�
					newLink(star, 0, outside, 0);
				}
			}
		}
		it = changeList.iterator();
		while (it.hasNext()) {
			childMemWithStar.alterAtomFunctor((Atom)it.next(), Functor.INSIDE_PROXY);
		}
		childMemWithStar.removeAtoms(removeList);		
	}
	/** ���դΥȥåץ�٥��$p������롼��μ¹Ի����Ǹ������˻Ĥä�star��������뤿��˸ƤФ�롣
	 * <p>������ˤ���star���Ф��ơ�
	 * ȿ��¦�νи��Ǥ���outside_proxy�ޤ���star�ȤȤ�˽����2����Ʊ�Τ�ľ�뤹�롣
	 * ���Τ������Ԥϡ���󥯤��롼��κ��դ˥ޥå������ץ����μ�ͳ��󥯤ΤȤ��˵����롣����:V��
	 * <pre>
	 * ( ... :-  $p,$q,$r  )
	 *      {a(s(V),s(B))}; {b(s(C),s(B)),         }; {d(s(C))}; c(o(V))
	 * -->   a(s(V),s(B)),   b(s(C),s(B)),             d(s(C)),  c(o(V))
	 * -->   a(  V ,  B ),   b(  C ,  B ),             d(  C ),  c(  V )
	 * 
	 * ( ... :- $p )
	 *      {a(s(V))}; b(o(V))
	 * -->   a(s(V)),  b(o(V))
	 * -->   a(  V ),  b(  V )
	 * </pre>
	 */
	public void removeTemporaryProxies() {
		LinkedList removeList = new LinkedList();
		Iterator it = atomIteratorOfFunctor(Functor.STAR);
		while (it.hasNext()) {
			Atom star = (Atom)it.next();
			Atom outside = star.args[0].getAtom();
			if (!removeList.contains(star)) {
				unifyLocalAtomArgs(star,1,outside,1);
				removeList.add(star);
				removeList.add(outside);
			}
		}
		removeAtoms(removeList);
	}
	
	/**
	 * {} �ʤ��ǽ��Ϥ��롣
	 * 
	 * // �롼��ν��Ϥκݡ�{} �������
	 * // (a:-b) �� ({a}:-{b}) �ˤʤä��㤦���顣
	 * 
	 * @return String 
	 * @deprecated
	 */
	public String toStringWithoutBrace() {
		return Dumper.dump(this);		
	}
	
	public String toString() {
		return "{ " + toStringWithoutBrace() + " }";
	}

	/**
	 * ��򥨥󥳡��ɤ��롥
	 * @return String ��Υ���ѥ����ǽ��ʸ����ɽ��
	 */
	public String encode() {
		return "{" + Dumper.encode(this, true, 0) + "}";
	}
	/**
	 * ��򥨥󥳡��ɤ��롥�������롼�륻�åȤΤߡ�
	 * @return String ��Υ���ѥ����ǽ��ʸ����ɽ��
	 */
	public String encodeRulesets() {
		return "{" + Dumper.encode(this, true, 1) + "}";
	}
	/**
	 * ��򥨥󥳡��ɤ��롥�������롼�륻�åȤϽ�����
	 * @return String ��Υ���ѥ����ǽ��ʸ����ɽ��
	 */
	public String encodeProcess() {
		return "{" + Dumper.encode(this, true, 2) + "}";
	}
		
	////////////////////////////////////////
	// non deterministic LMNtal
	AtomSet getAtoms() {
		return atoms;
	}
	///////////////////////////////
	// �ܥǥ����

	// �ܥǥ����1 - �롼������
	
//	/** �롼������ƾõ�� */
//	public void clearRules() {
//		if (remote == null) super.clearRules();
//		else remote.send("CLEARRULES",this);
//	}
//	/** srcMem�ˤ���롼��򤳤���˥��ԡ����롣 */
//	public void copyRulesFrom(AbstractMembrane srcMem) {
//		if (remote == null) super.copyRulesFrom(srcMem);
//		else remote.send("COPYRULESFROM",this,srcMem.getGlobalMemID());
//		// todo RemoteMembrane�Τ�����ʺǽ���ʳ���LOADRULESET��Ÿ�����������ˤ˹�碌��
//	}
//	/** �롼�륻�åȤ��ɲ� */
//	public void loadRuleset(Ruleset srcRuleset) {
//		if (remote == null) super.loadRuleset(srcRuleset);
//		else remote.send("LOADRULESET",this,srcRuleset.getGlobalRulesetID());
//	}

	// �ܥǥ����2 - ���ȥ�����

//	/** ���������ȥ�����������������ɲä��롣*/
//	public Atom newAtom(Functor functor) {
//		if (remote == null) return super.newAtom(functor);
//		else remote.send("NEWATOM",this,functor.toString());
//		return null;	// todo �ʤ�Ȥ������local-remote-local �����
//	}
//	/** �ʽ�°�������ʤ��˥��ȥ�򤳤�����ɲä��롣*/
//	public void addAtom(Atom atom) {
//		if (remote == null) super.addAtom(atom);
//		else remote.send("ADDATOM", this);
//	}
//	/** ���ꤵ�줿���ȥ��̾�����Ѥ��� */
//	public void alterAtomFunctor(Atom atom, Functor func) {
//		if (remote == null) super.alterAtomFunctor(atom,func);
//		else remote.send("ALTERATOMFUNCTOR", this, atom + " " + func.serialize());
//	}

	/** 
	 * ���ꤵ�줿���ȥ�򤳤���μ¹ԥ��ȥॹ���å����ɲä��롣
	 * ���Ǥ˥����å����Ѥޤ�Ƥ������ư���̤����Ȥ��롣
	 * @param atom �¹ԥ��ȥॹ���å����ɲä��륢�ȥ�
	 */
	public void enqueueAtom(Atom atom) {
		if(null == ready){ ready = new Stack(); }
		ready.push(atom);
	}
	/** �����줬��ư���줿�塢�����ƥ��֥��ȥ��¹ԥ��ȥॹ���å�������뤿��˸ƤӽФ���롣
	 * <p>Ruby�ǤǤ�movedTo(task,dstMem)��Ƶ��ƤӽФ����Ƥ�������
	 * ���塼��ľ���٤����ɤ�����Ƚ�Ǥμ�֤��ݤ��ꤹ���뤿���¹������Ф���������ѻߤ��줿�� 
	 * <p>
	 * ���ߤ��Υ᥽�åɤ�ȤäƤ�����Ϥʤ���(2005/11/30 mizuno)
	 * <p>
	 * ��ư���줿�塢������Υ����ƥ��֥��ȥ��¹ԥ��ȥॹ���å�������뤿��˸ƤӽФ���롣
	 * <p><b>���</b>��Ruby�Ǥ�movedto�Ȱۤʤꡢ��¹����ˤ��륢�ȥ���Ф��Ƥϲ��⤷�ʤ���*/
	public void enqueueAllAtoms() {
		Iterator i = atoms.activeFunctorIterator();
		while (i.hasNext()) {
			Functor f = (Functor)i.next();
			if (f.isActive()) {
				Iterator i2 = atoms.iteratorOfFunctor(f);
				while (i2.hasNext()) {
					if(null == ready){ ready = new Stack(); }
					Atom a = (Atom)i2.next();
					a.dequeue();
					ready.push(a);
				}
			}
		}
	}
//
//	/** ���ꤵ�줿���ȥ�򤳤��줫�����롣
//	 * <strike>�¹ԥ��ȥॹ���å������äƤ����硢�����å������������</strike>*/
//	public void removeAtom(Atom atom) {
//		if(Env.fGUI) {
//			Env.gui.lmnPanel.getGraphLayout().removedAtomPos.add(atom.getPosition());
//		}
//		atoms.remove(atom);
//		atom.mem = null;
//	}

	// �ܥǥ����3 - ��������

	/** �����������פ�k�λ��������������������� */
	public Membrane newMem(int k){
	//		if (remote != null) {
	//		remote.send("NEWMEM",this);
	//		return null; // todo
	//	}
		Membrane m = new Membrane(task, this);
		m.changeKind(k);
		mems.add(m);
		// �����Ʊ���¹��쥹���å����Ѥ�
		if (k != KIND_ND)
			stack.push(m);

		if(Env.fUNYO){
			unyo.Mediator.addAddedMembrane(m);
		}
		return m;
	}
	/** �������ǥե���ȥ����פλ��������������������� */
	public Membrane newMem() {
		return newMem(0);
	}
	
//	/** ���ꤵ�줿����򤳤��줫�����롣
//	 * <strike>�¹��쥹���å������ʤ���</strike>
//	 * �¹��쥹���å����Ѥޤ�Ƥ���м������� */
//	public void removeMem(AbstractMembrane mem) {
//		if (remote == null) super.removeMem(mem);
//		else remote.send("REMOVEMEM", this, mem.getGlobalMemID());
//	}
//	/** ���ꤵ�줿�Ρ��ɤǼ¹Ԥ�����å����줿�롼������������������λ���ˤ������������롣
//	 * @param node �Ρ���̾��ɽ��ʸ����
//	 * @return �������줿�롼����
//	 */
//	public AbstractMembrane newRoot(String node) {
//		if (remote == null) return super.newRoot(node);
//		else remote.send("NEWROOT", this, node);
//		return null;	// todo �ʤ�Ȥ������local-remote-local �����
//	}
	
	// �ܥǥ����5 - �켫�Ȥ��ư�˴ؤ������

	/** ���������롣
	 * <p>
	 * <dl>
	 * <dt><b>�롼����ξ��</b>:<dd>
	 * ���Υ������β��μ¹��쥹���å���ͣ������ǤȤ����Ѥࡣ
	 * <dt><b>�롼����Ǥʤ����</b>:<dd>
	 * ���������������塢�����Ʊ�������å�������롣
	 * <strike>���ʤ�������μ¹��쥹���å������Ǥʤ���в��μ¹��쥹���å����Ѥߡ�
	 * ���ʤ�м¹��쥹���å����Ѥࡣ</strike>
	 * �����������줬�¹��쥹���å����Ѥޤ�Ƥ�����硢���Ǥ�
	 * �ʼ¹��쥹���å��ˡ��Ѥޤ�Ƥ����鲿�⤷�ʤ���
	 * </dl>
	 */
	public void activate() {
		if (isNondeterministic()) return;
		stable = false;
		Task t = (Task)task;
		if (isRoot()) {
			dequeue();
			t.bufferedStack.push(this);
		} else {
			Stack s = ((Membrane)parent).activate2();
			if (s == t.bufferedStack) {
				dequeue();
				s.push(this);
			} else {
				//��ˡ7�Ǥϡ���������å����Ƥ������Ʊ����push����Ƥ�����Ϥʤ���
				//��ˡ8�Ǥϡ���2�Ԥ�synchr`onized(memStack)�������롣
				if (!isQueued())
					s.push(this);
			}
		}
	}
	/** 
	 * activate ��ǿ�������������Ȥ������Ѥ��롣
	 * ���Ǥ˥����å����Ѥޤ�Ƥ���Ȥ��ϲ����ʤ���
	 * @return �������Ǥ��Ѥޤ�Ƥ��륹���å���task.memStack �� task.bufferedStack �ΰ�����
	 */
	private Stack activate2() {
		Task t = (Task)task;
		//��ˡ7�Ǥϡ���������å����Ƥ������Ʊ����push����Ƥ�����Ϥʤ���
		//��ˡ8�Ǥϡ����β�������synchronized(memStack)�������롣
		if (isQueued())
			return stack;
		if (isRoot()) {
			// ASSERT(t.bufferedStack.isEmpty());
			t.bufferedStack.push(this);
			return t.bufferedStack;
		} else {
			Stack s = ((Membrane)parent).activate2();
			s.push(this);
			return s;
		}
	}	
	
	// ��å��˴ؤ������ - ������̿��ϴ�������task��ľ��ž�������
	
	// - ������̿��
	
	/**
	 * ������Υ�å��������ߤ롣
	 * <p>�롼�륹��åɤޤ���dumper��������Υ�å����������Ȥ��˻��Ѥ��롣
	 * <p>�롼�륹��åɤϡ���å������ˤ�unlock()����Ѥ��뤳�ȡ�
	 * <p>dumper�ϡ���å������ˤ�quietUnlock()����Ѥ��뤳�ȡ�
	 * <p>�������������Υ�⡼�Ȥ�Ѿ����롣
	 * �������äƥ롼�륹��åɤϡ�������å�������礿�����˥�⡼�Ȥ�null�����ꤹ�뤳�ȡ�
	 * @return ��å��μ����������������ɤ��� */
	synchronized public boolean lock() {
		if (lockThread != null) {
			return false;
		} else {
			lockThread = Thread.currentThread();
			//����줿��ϥ�å�����Ƥ���Τǡ�parent==null �ˤʤ�Τϥ����Х�롼�ȤΤ�
//			if (parent != null) remote = parent.remote;
			return true;
		}
	}
	/**
	 * ������Υ�å��������ߤ롣
	 * ���Ԥ�����硢�������������륿�����Υ롼�륹��åɤ�����׵�����롣���θ塢
	 * ���Υ������������ʥ��ȯ�Ԥ���Τ��ԤäƤ��顢�Ƥӥ�å��������ߤ뤳�Ȥ򷫤��֤���
	 * <p>�롼�륹��åɰʳ��Υ���åɤ�������Υ�å���2���ܰʹߤΥ�å��Ȥ��Ƽ�������Ȥ��˻��Ѥ��롣
	 * <p>��å������ˤ�unlock()����Ѥ��뤳�ȡ�
	 * ����Υ�å��Ϥ��Ǥ˼������Ƥ���ɬ�פ����롣
	 * @return ���true */
	public boolean blockingLock() {
		//����Υ�å���������Ƥ���Τǡ����������Ѳ������ꤳ���줬����줿�ꤹ����Ϥʤ���
		Task t = (Task)task;
		boolean stopped = false;
		while (true) {
			if (lockThread == t.thread) {
				//�����������Υ롼�륹��åɤ���å����Ƥ��뤫�⤷��ʤ���������׵������
				//suspend ��Ƥ���Ȥ��Ϥ��Ǥ˲����Ѥߤ��⤷��ʤ���������Ϥʤ���
				//�������˴ؤ��� synchronized ������Ѥ���Τǡ����� synchronized ����������ϤǤ��ʤ���
				t.suspend();
				stopped = true;
			}
			synchronized(this) {
				if (lock()) {
					break;
				} else {
					//��롼�륹��åɤ�ƥ������ʤ��������Τ��Ԥ�
					try {
						wait();
					} catch (InterruptedException e) {}
				}
			}
		}
		if (stopped)
			t.resume();
		return true;
	}
	/**
	 * �����줫�餳�����������륿�����Υ롼����ޤǤ����Ƥ���Υ�å���֥�å��󥰤Ǽ��������¹��쥹���å��������롣
     * �롼����ʤ��blockingLock()��Ʊ���ˤʤ�
	 * <p>�롼�륹��åɰʳ��Υ���åɤ�������Υ�å���ǽ�Υ�å��Ȥ��Ƽ�������Ȥ��˻��Ѥ��롣
	 * <p>�����������⡼�Ȥ�null�����ꤹ�롣
	 * <p>��å������ˤ�asyncUnlock()����Ѥ��뤳�ȡ�
	 * @return ���������� true�����Ԥ���Τϡ������줬���Ǥ�remove����Ƥ�����Τߡ� */
	public boolean asyncLock() {
		Task t = (Task)task;
		Membrane root = t.getRoot();;
		//blockingLock �Ȱ㤤����ߤ���ޤ��Ԥ�ɬ�פ����롣
		t.suspend();
		synchronized(this) {
			while (true) {
				boolean ret = root.lock();
				if (parent == null || t != task) {
					//�������Ѳ����Ƥ������Υ���󥻥����
					if (ret) {
						root.activate();
						root.unlock();
					}
					t.resume();
	
					if (parent == null) {
						//�����줬����줿
						return false;
					} else {
						//��°���������Ѳ����Ƥ���
						t = (Task)task;
						root = t.getRoot();
						t.suspend();
					}
				} else if (ret) {
					//�롼����Υ�å��������������줫��롼����ޤǤδ֤����ƥ�å����롣
					for (Membrane mem = this; mem != root; mem = mem.parent) {
						ret = mem.lock();
						if (!ret)
							throw new RuntimeException("SYSTEM ERROR : failed to asyncLock" + mem.lockThread + mem.task);
					}
					t.resume();
					return true;
				} else {
					//�롼����Υ�å������������Τ��Ԥġ�
					//��°���������Ѳ����Ƥ��ʤ���������������뤿��ˡ������ॢ���Ȥ����ꤷ�Ƥ��롣
					try {
						wait(1);
					} catch (InterruptedException e) {}
				}
			}
		}
	}

	/** ���Υ�å�����������Ƥλ�¹����Υ�å���Ƶ�Ū�˥֥�å��󥰤Ǽ������롣
	 * <p>�ץ���ʸ̮�Υ��ԡ����˴��򤹤�Ȥ��˻Ȥ��롣
	 * <p>��å������ˤ�recursiveUnlock()����Ѥ��뤳�ȡ�
	 * @return ��å��μ����������������ɤ��� */
	public boolean recursiveLock() {
		Iterator it = memIterator();
		LinkedList lockedmems = new LinkedList();
		boolean result = true;
		while (it.hasNext()) {
			Membrane mem = (Membrane)it.next();
			if (!mem.blockingLock()) {
				result = false;
				break;
			}
			if (!mem.recursiveLock()) {
				mem.unlock();
				result = false;
				break;
			}
			lockedmems.add(mem);
		}
		if (result) return true;
		it = lockedmems.iterator();
		while (it.hasNext()) {
			Membrane mem = (Membrane)it.next();
			mem.recursiveUnlock();
			mem.unlock();
		}
		return false;
	}

	// - �ܥǥ�̿��

	/**
	 * ��������������Υ�å���������롣
	 * �¹��쥹���å������ʤ���
	 * �롼����ξ�硢�������������륿�������Ф��ƥ����ʥ��notify�᥽�åɡˤ�ȯ�Ԥ��롣
	 */
	public void quietUnlock() {
		Task task = (Task)getTask();
		synchronized(this) {
			lockThread = null;
			//blockingLock �ǥ֥�å����Ƥ��륹��åɤ򵯤�����
			//��ĵ������н�ʬ��
			notify();
		}
		if (isRoot()) {
			// ���Υ������Υ롼�륹��åɤ�Ƴ����롣
			// ���Υ������ʳ��Υ���åɤ�ɬ���롼���줫���å����Ƥ���Τǡ��롼����Υ�å��������˵������н�ʬ��
			// ��å���������Ƥ��餳�������ޤǤδ֤˽�°���������Ѥ�äƾ�礬���뤬���ä�����Ϥʤ���
			synchronized(task) {
				// �������ʳ��ˤ⡢suspend �᥽�å���� wait ���Ƥ��륹��åɤ����뤫�⤷��ʤ��Τǡ����Ƶ�������
				task.notifyAll();
			}
		}
	}
	
	/**
	 * ��������������Υ�å���������롣�롼����ξ�硢
	 * ���μ¹��쥹���å������Ƥ�¹��쥹���å������ž������
	 * �������������륿�������Ф��ƥ����ʥ��notify�᥽�åɡˤ�ȯ�Ԥ��롣
	 * <p>lock�����blockingLock�θƤӽФ����б����롣asyncLock�ˤ�asyncUnlock���б����롣
	 * <p><strike>todo unlock �� weakUnlock ��̾���ѹ�����</strike>
	 */
	public void unlock() {
		unlock(false);
	}
	
	public void unlock(boolean changed) {
		Task task = (Task)getTask();
		if (isRoot()) {
			task.memStack.moveFrom(task.bufferedStack);
		}
		quietUnlock();
		if(changed & Env.LMNgraphic != null)
			Env.LMNgraphic.setMem(this);
		if(changed & Env.LMNtool != null)
			Env.LMNtool.setMem(this);
	}

	/** ������Υ�å�����Ū�˲������롣
	 * unlock()��Ʊ����
	 * TODO unlock�����ѹ礹��
	 */
	public void forceUnlock() {
		unlock();
	}
	/** �����줫�餳�����������륿�����Υ롼����ޤǤ����Ƥ���μ���������å�������������������������롣
	 * ���μ¹��쥹���å������Ƥ�¹��쥹���å���ž�����롣�롼����ξ���unlock()��Ʊ���ˤʤ롣
	 * <p>�롼�륹��åɰʳ��Υ���åɤ��ǽ�˼���������Υ�å����������Ȥ��˻��Ѥ��롣*/
	public void asyncUnlock() {
		asyncUnlock(true);
	}
	public void asyncUnlock(boolean asyncFlag) {
		activate();
		Membrane mem = this;
		while (!mem.isRoot()) {
			mem.lockThread = null;
			mem = mem.parent;
		}
		task.asyncFlag = asyncFlag;
		mem.unlock();
	}
	/** ������������������Ƥλ�¹����Υ�å���Ƶ�Ū�˲������롣*/
	public void recursiveUnlock() {
		Iterator it = memIterator();
		while (it.hasNext()) {
			Membrane mem = (Membrane)it.next();
			mem.recursiveUnlock();
			mem.unlock();
		}
	}

	///////////////////////////////	
	// Membrane ����������᥽�å�
	
//	// ���줫�ɤ���
//	boolean isCurrent() { return getTask().memStack.peek() == this; }
	
	/** �ǥХå��� */
	String getReadyStackStatus() { return ready.toString(); }

	/** �¹ԥ��ȥॹ���å�����Ƭ�Υ��ȥ����������¹ԥ��ȥॹ���å�������� */
	Atom popReadyAtom() {
		if(null == ready){ return null; }
		Atom atom = (Atom)ready.pop();
		if(ready.isEmpty()){ ready = null; }
		return atom;
	}

//	/** ��γ�������������������ϥ롼����ǤϤʤ��������å����Ѥޤ�Ƥ��餺��
//	 * ���������ϲ��Ǥʤ��¹��쥹���å����Ѥޤ�Ƥ��롣
//	 * �� newMem / newLocalMembrane �˰�ư���ޤ��� */
//	public void activateThis() {
//		((Task)task).memStack.push(this);
//	}

//	/** ������Υ���å����ɽ���Х������������롣
//	 * @see RemoteMembrane#updateCache(byte[]) */
//	public byte[] cache() {
//		if(null == atomTable){ atomTable = new HashMap(); }
//		
//		// atomTable�򹹿����� // ����μ�ͳ��󥯤ˤĤ��Ƥ��׸�Ƥ
//		atomTable.clear();
//		Iterator it = atomIterator();
//		while (it.hasNext()) {
//			Atom atom = (Atom)it.next();
//			atomTable.put(atom.getLocalID(), atom);
//		}
//		if(atomTable.isEmpty()){atomTable = null; }
//
//		try {
//			ByteArrayOutputStream bout = new ByteArrayOutputStream();
//			ObjectOutputStream out = new ObjectOutputStream(bout);
//
//			//����
//			out.writeInt(mems.size());
//			it = memIterator();
//			while (it.hasNext()) {
//				Membrane m = (Membrane)it.next();
//				out.writeObject(m.getTask().getMachine().hostname);
//				out.writeObject(m.getLocalID());
//				out.writeObject(new Boolean(m.isRoot()));
//			}
//			//���ȥ�
//			out.writeInt(atoms.size());
//			it = atomIterator();
//			while (it.hasNext()) {
//				Atom a = (Atom)it.next();
//				out.writeObject(a);
//			}
//			//�롼�륻�å�
//			out.writeInt(rulesets.size());
//			it = rulesetIterator();
//			while (it.hasNext()) {
//				Ruleset r = (Ruleset)it.next();
//				out.writeObject(r.getGlobalRulesetID());
//			}
//			//todo name�����ס�
//			//out.writeObject(name);
//			out.writeObject(new Boolean(stable));
//
//			out.close();
//			return bout.toByteArray();
//		} catch (IOException e) {
//			//ByteArrayOutputStream�ʤΤǡ�ȯ������Ϥ����ʤ�
//			throw new RuntimeException("Unexpected Exception", e);
//		}
//	}

//	/** ���ȥ�ID���б����륢�ȥ��������� */
//	public Atom lookupAtom(String atomid) {
//		return (Atom)atomTable.get(atomid);
//	}
//	/** ���ȥ�ID���б����륢�ȥ����Ͽ���� */
//	public void registerAtom(String atomid, Atom atom) {
//		if(null == atomTable){ atomTable = new HashMap(); }
//		atomTable.put(atomid, atom);
//	}
	
	/** �������������(���줬̵�����ˤΤ߸Ƥ���ɤ�) 
	 * �ǡ���������������ΤϤʤ�
	 */
	public void free() {
//		IDConverter.unregisterGlobalMembrane(getGlobalMemID());
	}
	
	
	/** ����饤���ѥޥ��� Any=_old/1 :- Any=_new/1 */
	public void replace1by1(Atom _old, Atom _new) {
		relink(_old, 0, _new, 0);
		removeAtom(_old);
	}
	
	// XML <-> Mem ��������
	
	// Membrane -> XML
	private static int lastLinkId;

	private static int lastFreeLinkId;

	private static HashMap<Link, Integer> links = null;

	private static ArrayList<Link> freeLinks = null;

	/**
	 * xmlSerialize() ���XMLɽ���ȼ�ͳ��󥯤�������֤���
	 * 
	 * @return Object[] { String xml, ArrayList<Link> freeLinks }
	 */
	public Object[] xmlSerialize() {
		lastLinkId = 0;
		lastFreeLinkId = 0;
		links = new HashMap<Link, Integer>();
		freeLinks = new ArrayList<Link>();

		String xml = this.xmlSerialize(new StringBuffer(""));
		return new Object[] { xml, freeLinks };
	}

	private String xmlSerialize(StringBuffer sb) {
		sb.append("<mem>");
		// ���ȥ�
		Iterator<Atom> atomIt = this.atomIterator();
		while (atomIt.hasNext()) {
			Atom nowAtom = atomIt.next();
			sb.append("<atom class=\"" + nowAtom.getFunctor().getClass().getSimpleName()
					+ "\" name=\"" + nowAtom.getName() + "\" arity=\""
					+ nowAtom.getArity() + "\">");
			// ���
			for (int i = 0; i < nowAtom.getArity(); i++) {
				Link nowLink = nowAtom.getArg(i);
				if (links.containsKey(nowLink)) {
					sb.append("<link id=\"" + links.remove(nowLink).toString()
							+ "\" pos=\"" + i + "\"/>");
				} else {
					if (nowAtom.getFunctor().equals(Functor.INSIDE_PROXY)
							&& i == 0) {
						// ��ͳ��󥯤ν���
						// ������¸�ߤ��ʤ��褦��InsideProxy���Ф��ƹԤ�
						sb.append("<free_link id=\"" + lastFreeLinkId++
								+ "\"/>");
						freeLinks.add(nowLink.getAtom().getArg(1).getBuddy());
						links.remove(nowLink);
					} else {
						links.put(nowLink.getBuddy(), lastLinkId);
						sb.append("<link id=\"" + lastLinkId++ + "\" pos=\""
								+ i + "\"/>");
					}
				}
			}
			sb.append("</atom>");
		}

		// ����
		Iterator<Membrane> memIt = this.memIterator();
		while (memIt.hasNext()) {
			sb.append(memIt.next().xmlSerialize(new StringBuffer("")));
		}

		sb.append("</mem>");
		return sb.toString();
	}
	
	
	// XML -> Membrane
	public void xmlDeserialize(String src) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(new InputSource(new StringReader(src)), new LMNtalSax(
					this));
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class LMNtalSax extends DefaultHandler {
		Membrane rootMem = null;

		Membrane tmpMem = null;

		Atom tmpAtom = null;

		HashMap<Integer, Object[]> linkMap = null; // <id, (atom, pos)>

		ArrayList<Atom> insides = null;

		LMNtalSax(Membrane mem) {
			this.rootMem = this.tmpMem = mem;
			this.linkMap = new HashMap<Integer, Object[]>();
			this.insides = new ArrayList<Atom>();
		}

		public void startDocument() {
		}

		public void startElement(String uri, String localName, String qName,
				Attributes attributes) {
			if (qName.equals("mem")) {
				Membrane newMem = tmpMem.newMem();
				this.tmpMem = newMem;
			} else if (qName.equals("atom")) {
				String funcClass = attributes.getValue(0).intern();
				String funcName = attributes.getValue(1).intern();

				if (funcClass == "SpecialFunctor") {
					if (funcName == "$in") {
						this.tmpAtom = tmpMem.newAtom(Functor.INSIDE_PROXY);
					} else if (funcName == "$out") {
						this.tmpAtom = tmpMem.newAtom(Functor.OUTSIDE_PROXY);
					}
				} else if (funcClass == "IntegerFunctor") {
					this.tmpAtom = tmpMem.newAtom(new IntegerFunctor(Integer
							.parseInt(funcName)));
				} else if (funcClass == "FloatingFunctor") {
					this.tmpAtom = tmpMem.newAtom(new FloatingFunctor(Double
							.parseDouble(funcName)));
				} else { // SymbolFunctor
					this.tmpAtom = tmpMem.newAtom(new SymbolFunctor(funcName,
							Integer.parseInt(attributes.getValue(2))));
				}
			} else if (qName.equals("link")) {
				int linkId = Integer.parseInt(attributes.getValue(0));
				int linkPos = Integer.parseInt(attributes.getValue(1));
				if (linkMap.containsKey(linkId)) {
					Object[] dst = linkMap.remove(linkId);
					Atom dstAtom = (Atom) dst[0];
					int dstPos = (Integer) dst[1];
					tmpMem.newLink(this.tmpAtom, linkPos, dstAtom, dstPos);
				} else {
					linkMap.put(linkId, new Object[] { this.tmpAtom, linkPos });
				}
			} else { // ��ͳ���
				insides.add(tmpAtom);
			}
		}

		public void characters(char[] ch, int offset, int length) {
		}

		public void endElement(String uri, String localName, String qName) {
			if (qName.equals("mem")) {
				this.tmpMem = this.tmpMem.getParent();
			}
		}

		public void endDocument() {
			// ��ͳ��󥯤Υꥹ�Ȥ��������
			if (!insides.isEmpty()) {
				boolean first = true;
				Atom parent = tmpMem.newAtom(new SymbolFunctor("fls", 1));
				for (Atom in : insides) {
					Atom c = tmpMem.newAtom(new SymbolFunctor(".", 3));
					Atom out = tmpMem.newAtom(Functor.OUTSIDE_PROXY);
					tmpMem.newLink(c, 0, out, 1);
					in.getMem().newLink(in, 0, out, 0);
					if (first) {
						tmpMem.newLink(c, 2, parent, 0);
					} else {
						tmpMem.newLink(c, 2, parent, 1);
					}
					parent = c;
					first = false;
				}
				Atom nil = tmpMem.newAtom(new SymbolFunctor("[]", 1));
				if (first) {
					tmpMem.newLink(nil, 0, parent, 0);
				} else {
					tmpMem.newLink(nil, 0, parent, 1);
				}
			}
		}
	}
	
	// XML <-> Mem �����ޤ�
	
	// hash ��������
	
	/* ��Υϥå��女���ɤ��֤� */
	static int calculate(Membrane m) {
		return calculate(m, new HashMap<Membrane, Integer>());
	}
	
	/*
	 * ��Υϥå��女���ɤ��֤�
	 * @param m �ϥå��女���ɻ����оݤ���
	 * @param m2hc m�λ��줫��ϥå��女���ɤؤΥޥåס�����Υϥå��女���ɻ��Ф򷫤��֤��ʤ�����˻Ȥ���
	 */
	private static int calculate(Membrane m, Map<Membrane, Integer> m2hc) {
		//System.out.println("membrane:" + m);
		final long MAX_VALUE = Integer.MAX_VALUE;
		/*
		 * add: m���ʬ�ҤΥϥå��女���ɤ��û�����Ƥ����ѿ�
		 * mult: m���ʬ�ҤΥϥå��女���ɤ��軻����Ƥ����ѿ�
		 */
		long add = 3412;        // 3412��Ŭ���ʽ����
		long mult = 3412;
		
		/* ����ѿ� */
		Atom a = null;
		Membrane mm = null;
		QueuedEntity q = null;
		
		/*
		 * contents:��������Υ��ȥ�Ȼ������Τν���
		 * toCalculate:���߷׻����ʬ�����̤�������ȥ�ޤ��ϻ���ν���
		 * calculated:���߷׻����ʬ����ν����ѥ��ȥ�ޤ��ϻ���ν���
		 */
		Set<QueuedEntity> contents = new HashSet<QueuedEntity>(), 
							toCalculate = new HashSet<QueuedEntity>(), 
							calculated = new HashSet<QueuedEntity>();
		
		for (Iterator i = m.atomIterator(); i.hasNext(); ) {
			a = (Atom) i.next();
			if (a.getFunctor().isOutsideProxy() || a.getFunctor().isInsideProxy()) {
				continue;
			}
			contents.add(a);
		}
		
		for (Iterator i = m.memIterator(); i.hasNext(); ) {
			mm = (Membrane) i.next();
			contents.add(mm);
			m2hc.put(mm, calculate(mm, m2hc));
		}
		
		while (!contents.isEmpty()) {
			//System.out.println("uncalculated:" + contents);
			q = contents.iterator().next();
			contents.remove(q);
			
			/*
			 * mol: ����ʬ�ҤΥϥå��女���ɤ��ݻ�����
			 * mol_add: ���ܷ׻�ñ�̤Υϥå��女���ɤ��û�����Ƥ����ѿ�
			 * mol_mult: ���ܷ׻�ñ�̤Υϥå��女���ɤ��軻����Ƥ����ѿ�
			 * temp: ���ܷ׻�ñ�̤Υϥå��女���ɤ��ݻ�����
			 */
			 
			long mol = -1, mol_add = 0, mol_mult = 41, temp = 0;
			
			toCalculate.clear();
			calculated.clear();
			toCalculate.add(q);
				
			// ʬ�ҤΥϥå��女���ɤη׻�
			while (!toCalculate.isEmpty()) {
				q = toCalculate.iterator().next();
				calculated.add(q);
				toCalculate.remove(q);
				
				if (q instanceof Atom) {
					a = (Atom) q;
					temp = a.getFunctor().hashCode();
								
					// ���Υ��ȥ�Υ�󥯤��������
					int arity = a.getFunctor().getArity();
					for (int k = 0; k < arity; k++) {
						temp *= 31;
						Link link = a.getArg(k);
						if (link.getAtom().getFunctor().isInsideProxy()) {
							Atom inside = link.getAtom();
							int pos = link.getPos() + 1;
							temp += (inside.getFunctor().hashCode() * pos);	
						} else if (link.getAtom().getFunctor().isOutsideProxy()) { // ����褬����ξ��
							/*
							 * ����襢�ȥ�˻��ޤǴӤ�������Υϥå��女���ɤ�
							 * �ǽ�Ū�ʥ���襢�ȥ�Υϥå��女���ɤ�
							 * ���Υ��ȥ�ΰ����ֹ���Ȥ�
							 * ���Υ��ȥफ�����ؤΥ�󥯤�ɽ�������Ȥ��롣
							 */
							int t = 0;
							mm = link.getAtom().nthAtom(0).getMem();
							if (!calculated.contains(mm)) {
								toCalculate.add(mm);
							}
							while (link.getAtom().getFunctor().isOutsideProxy()) {
								link = link.getAtom().nthAtom(0).getArg(1);
								mm = link.getAtom().getMem();
								t += m2hc.get(mm);
								t *= 13;
							}
							
							t *= link.getAtom().getFunctor().hashCode();
							t *= link.getPos() + 1;
							temp += t;
						} else { // ����褬�ץ����ʳ��Υ��ȥ�ξ��
							Atom linked = link.getAtom();
							if (!calculated.contains(linked)) {
								toCalculate.add(linked);
							}
							int pos = link.getPos() + 1;
							// ��³��ΰ����ֹ��ϥå��女���ɤ˴�Ϳ������
							temp += (linked.getFunctor().hashCode() * pos);
						}
					}
				} else {
					Membrane mt = (Membrane) q;
					final int thisMembsHC = m2hc.get(mt);
					temp = thisMembsHC;
					
					// �����줫����γ����ؤΥ�󥯤��������
					Link link = null;
					for (Iterator i = mt.atomIteratorOfFunctor(Functor.INSIDE_PROXY); i.hasNext(); ) {
						Atom inside = (Atom) i.next();
						// �����쳰���Ρʥץ����Ǥʤ��˥���襢�ȥ�ޤǥȥ졼��
						int s = 0;
						link = inside.nthAtom(0).getArg(1);
						
						if (link.getAtom().getFunctor().isOutsideProxy()) { // ������Υ���褬��ΤȤ�
							mm = link.getAtom().nthAtom(0).getMem();
							if (!calculated.contains(mm)) {
								toCalculate.add(mm);
							}
						} else { // ������Υ���褬���ȥ�ξ��
							a = link.getAtom();
							if (!calculated.contains(a)) {
								toCalculate.add(a);
							}
						}
							
						while (link.getAtom().getFunctor().isOutsideProxy()) {
							link = link.getAtom().nthAtom(0).getArg(1);
							s += m2hc.get(link.getAtom().getMem());
							s *= 13;
						}
						s += link.getAtom().getFunctor().hashCode();
						s *= link.getPos() + 1;
							
						// �����������Ρʥץ����Ǥʤ��˥�󥯸����ȥ�ޤǥȥ졼��
						int t = 0;
						link = inside.getArg(1);
						while (link.getAtom().getFunctor().isOutsideProxy()) {
							link = link.getAtom().nthAtom(0).getArg(1);
							t += m2hc.get(link.getAtom().getMem());
							t *= 13;
						}
						t *= link.getAtom().getFunctor().hashCode();
						t *= link.getPos() + 1;
						temp += thisMembsHC^t * s;
					}
				}
				
				mol_add += temp;
				mol_add %= MAX_VALUE;
				mol_mult *= temp;
				mol_mult %= MAX_VALUE;
			}
			mol = mol_add^mol_mult;
			//System.out.println("molecule: " + calculated + " = " + mol);
			/* �ϥå��女���ɤ򻻽Ф���ʬ�Ҥ�׻��оݤ�������� */
			contents.removeAll(calculated);
			
			add += mol;
			add %= MAX_VALUE;
			mult *= mol;
			mult %= MAX_VALUE;
		}
		
		//System.out.println("membrane:" + m + " = " + (mult^add) + " (mult=" + mult + ", add=" + add + ")");
		return (int) (mult^add);
	}
}
