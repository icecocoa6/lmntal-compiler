package runtime;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.QueuedEntity;
import util.RandomIterator;

/**
 * �� �ۥ��Ȥ�ޤ����뼫ͳ��󥯤�Ʊ�����β��ˡ
 * [1] �ƥۥ��Ȥ�����ͳ��󥯽��ϴ������ȥ�Υ����Х�ID���դ�����ˡ
 *  - newFreeLink̿�������ˡ
 *      ̿��μ��ब������
 *  - newFreeLink̿�����ʤ����
 *      newAtom�������functor�򸡺����٤���
 * [2] �ƥۥ��Ȥ�����ͳ��󥯽��ϴ������ȥ�Υ����Х�ID���դ��ʤ���ˡ
 *  - ���ߥåȻ��˿������ȥ�β�ID���˴�������
 *    ����������ͳ��󥯽��ϴ������ȥ�μ��̤��Ǥ��ʤ��ʤ뢪NG
 *  - ���ߥåȻ��˿������ȥ�β�ID���˴����ʤ����
 *    <strike>����Υ���å��幹������</strike>
 *      <ins>̿��֥�å����Ф��������Ȥ���</ins>
 *      ��ID�������������Х�ID���ѹ������å��������������뢪�٤�����������
 *      * ���ꡧ���Υ�å�������ɤ���äƽ�������Τ���
 *        NEW_���饢�ȥ�ؤΥޥåפ��ݻ����ʤ���Фʤ�ʤ��ʺ��Ϥʤ��ˢ����
 *        �������ºݤˤ�$in������Ͽ���Ƥ����н�ʬ��
 * 
 * �ǽ�η�����newFreeLink̿����ѻߡ�
 *
 * ����������ä����⡢Ʊ���褦��̿��֥�å����Ф��������Ȥ����ֿ����롣
 *
 * ����ϡ�newmem�Ͽ��줬ȯ�Ԥ���Τ�������������뤬��
 * newatom $in �ξ�硢���줬ȯ�Ԥ���ΤǼ������ʤ��Ȥ�������
 *  - 3�ۥ��Ȥˤޤ�����������꤬ȯ������
 *   �� newFreeLink̿�᤬�ߤ�����
 *
 * �� �ۥ��Ȥ�ޤ�������컲�Ȥ�Ʊ�����β��ˡ
 * [1] �ƥۥ��Ȥ�������Υ����Х�ID���դ�����ˡ
 *  - NEWMEM/NEWROOTʬ��̿��ΰ������Ϥ�
 *  - ����¦��ID�δ�����;�פʼ�֤������뢪���ޤ�褯�ʤ�
 * 
 * [2] �ƥۥ��Ȥ�������Υ����Х�ID���դ��ʤ���ˡ
 *  - ���ߥåȻ��˿�������β�ID���Ѵ��������NG
 *  - ���ߥåȻ��˿�������β�ID���Ѵ����ʤ���硢
 *    ̿��֥�å����Ф��������Ȥ���
 *    ��ID�������������Х�ID���ѹ������å���������������
 */


/**
 * ����쥯�饹���������쥯�饹����ӥ�⡼���쥯�饹�οƥ��饹��
 * �ե�����ɤؤ������򤹤���ϡ��ä˵��Ҥ��ʤ��¤ꤳ����Υ�å���������Ƥ���ɬ�פ����롣
 * @author Mizuno, n-kato
 */
abstract public class AbstractMembrane extends QueuedEntity {
	/** �������������륿��������������Ȥ��ϡ�����Υ�å���������Ƥ���ɬ�פ����롣 */
	protected AbstractTask task;
	/** ���졣��⡼�Ȥˤ���ʤ��RemoteMembrane���֥������Ȥ򻲾Ȥ��롣GlobalRoot�ʤ��null��
	 * ��������Ȥ��ϡ�����Υ�å���������Ƥ���ɬ�פ����롣
	 * null �����������=�����������˻��ϡ�������ȿ����ξ���Υ�å���������Ƥ���ɬ�פ����롣 */
	protected AbstractMembrane parent;
	/** ���ȥ�ν��� */
	protected AtomSet atoms = new AtomSet();
	/** ����ν��� */
	protected Set mems;
//	/** ���Υ���μ�ͳ��󥯤ο� */
//	protected int freeLinkCount = 0;
	/** �롼�륻�åȤν��硣 */
	protected List rulesets = new ArrayList();
	/** ��Υ����� */
	protected int kind = 0;
	/** true�ʤ�Ф�����ʲ���Ŭ�ѤǤ���롼�뤬̵�� */
	protected boolean stable = false;
	/** ��³�ե饰��true�ʤ�Х롼��Ŭ�ѤǤ��ʤ��Ƥ�stable�ˤʤ�ʤ���*/
	public boolean perpetual = false;
	/** ��������å����Ƥ��륹��åɡ���å�����Ƥ��ʤ��Ȥ���null�����äƤ��롣*/
	protected Thread lockThread = null;
	/** ��⡼�ȡʥ�⡼����ΤȤ��˥ܥǥ�̿��᥽�åɸƤӽФ���ž����Ȥʤ��⡼�ȥ������ޤ���null��
	 * <p>��å�������Τ�ͭ������å������������ꤵ�졢��å���������null�����ꤵ��롣
	 * ��å���ɬ�פȤ����󥿥���Τߤ����Ѥ���ž�������󥿥���ϻ��Ѥ��ʤ���*/
	public RemoteTask remote = null; // public�ϲ�
	/** �������̾����intern���줿ʸ����ޤ���null�� */
	String name;

	private static int nextId = 0;
	private int id;

	public String getName() { return name; }
	void setName(String name) { this.name = name; } // ���ͤ��Ǥޤä��饳�󥹥ȥ饯�����Ϥ��褦�ˤ��٤�����

	///////////////////////////////
	// ���󥹥ȥ饯��

	/**
	 * ���ꤵ�줿�������˽�°�������������롣
	 */
	protected AbstractMembrane(AbstractTask task, AbstractMembrane parent) {
		if (Env.shuffle >= Env.SHUFFLE_MEMS)
			mems = new RandomSet();
		else
			mems = new HashSet();
		
		this.task = task;
		this.parent = parent;
		id = nextId++;
		
		if(Env.gui!=null) {
			Rectangle r = Env.gui.lmnPanel.getGraphLayout().getAtomsBound();
			rect = new Rectangle2D.Double(Math.random()*r.width + r.x, Math.random()*r.height + r.y, 10.0, 10.0);
//			System.out.println(rect);
		}
		
		//(nakajima 2004-10-25) �ʲ���ʬ���Ѥ���Ͽ���롣��Ψ�������ʥ�������Ф��Ƥϡ����������׺�Ŭ����
		daemon.IDConverter.registerGlobalMembrane(this.getGlobalMemID(),this);
	}
//	/**
//	 * ���������ʤ����������롣Task.createFreeMembrane ����ƤФ�롣
//	 */
//	Membrane(Task task) {
//		super(task, null);
//	}

	///////////////////////////////
	// ����μ���

	/**
	 * �ǥե���Ȥμ������Ƚ����Ϥ��������֤��Ѥ����Ѥ�äƤ��ޤ��Τǡ�
	 * ���󥹥��󥹤��Ȥ˥�ˡ�����id���Ѱդ��ƥϥå��女���ɤȤ������Ѥ��롣
	 */
	public int hashCode() {
		if (Env.fNonDeterministic) {
			return atoms.hashCode() + rulesets.hashCode();
		}
		return id;
	}
	/** ������Υ�����ID��������� */
	public String getLocalID() {  //public�ʤΤ�LMNtalDaemon����Ƥ�Ǥ��뤫�颪�ƤФʤ��ʤä��Τ�protected�Ǥ褤
		return Integer.toString(id);
	}
	/** ������Υ����Х�ID��������� */
	public abstract String getGlobalMemID();
	/** �����줬��°����׻��Ρ��ɤˤ����롢������λ��ꤵ�줿���ȥ��ID��������� */
	public abstract String getAtomID(Atom atom);
	
	//
	
	/** �������������륿�����μ��� */
	public AbstractTask getTask() {
		return task;
	}
	/** ����μ��� */
	public AbstractMembrane getParent() {
		return parent;
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
	public void makePerpetual() {
		perpetual = true;
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
	
	// ȿ����
	
	public Object[] getAtomArray() {
		return atoms.toArray();
	}
	public Object[] getMemArray() {
		return mems.toArray();
	}
	/** ������ˤ��륢�ȥ��ȿ���Ҥ�������� */
	public Iterator atomIterator() {
		return atoms.iterator();
	}
	/** ������ˤ�������ȿ���Ҥ�������� */
	public Iterator memIterator() {
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
		rulesets.clear();
	}
	/** srcMem�ˤ���롼��򤳤���˥��ԡ����롣 */
	public void copyRulesFrom(AbstractMembrane srcMem) {
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

	/** ���ꤵ�줿���ȥ�򤳤���μ¹ԥ��ȥॹ���å����Ѥࡣ
	 * ���Ǥ˥����å����Ѥޤ�Ƥ������ư���̤����Ȥ��롣*/
	public abstract void enqueueAtom(Atom atom);
	/** �����줬��ư���줿�塢�����ƥ��֥��ȥ��¹ԥ��ȥॹ���å�������뤿��˸ƤӽФ���롣
	 * <p>Ruby�ǤǤ�movedTo(task,dstMem)��Ƶ��ƤӽФ����Ƥ�������
	 * ���塼��ľ���٤����ɤ�����Ƚ�Ǥμ�֤��ݤ��ꤹ���뤿���¹������Ф���������ѻߤ��줿�� */
	public abstract void enqueueAllAtoms();

	/** ���ꤵ�줿���ȥ�򤳤��줫�����롣
	 * <strike>�¹ԥ��ȥॹ���å������äƤ����硢�����å������������</strike>*/
	public void removeAtom(Atom atom) {
		if(Env.fGUI) {
			Env.gui.lmnPanel.getGraphLayout().removedAtomPos.add(atom.getPosition());
		}
		atoms.remove(atom);
		atom.mem = null;
	}
	
	/** ���ꤵ�줿�����ץ����򤳤��줫�����롣by kudo
	 * ( java.util.Stack��Ȥ��褦�˽�������ȼ�äư������� 2005/08/01 )
	 * @param srcGround
	 * @return
	 */
	public void removeGround(Link srcGround){//,Set srcSet){
		java.util.Stack s = new java.util.Stack();
		s.push(srcGround);
		Set srcSet = new HashSet();
		while(!s.isEmpty()){
			Link l = (Link)s.pop();
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
	
	// �ʲ��� AbstractMembrane �� final �᥽�å�
	
	/** [final] 1������newAtom��ƤӽФ��ޥ��� */
	final Atom newAtom(String name, int arity) {
		return newAtom(new Functor(name, arity));
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
	
	/** ���������������������������� */
	public abstract AbstractMembrane newMem();
	public abstract AbstractMembrane newMem(int k);
	/** [final] ���ꤵ�줿�ʿ����̵������򤳤���λ���Ȥ����ɲä��롣
	 * �¹��쥹���å������ʤ�������Υ������ˤĤ��Ƥϲ��⤷�ʤ���*/
	public final void addMem(AbstractMembrane mem) {
		mems.add(mem);
		mem.parent = this;
	}
	/** ���ꤵ�줿����򤳤��줫�����롣
	 * <strike>�¹��쥹���å������ʤ���</strike>
	 * �¹��쥹���å����Ѥޤ�Ƥ���м������� */
	public void removeMem(AbstractMembrane mem) {
		mems.remove(mem);
		mem.dequeue();
		mem.parent = null;
	}
	/** ���ꤵ�줿�Ρ��ɤǼ¹Ԥ�����å����줿kind==k�Υ롼������������������λ���ˤ������������롣
	 * @param nodedesc �Ρ���̾��ɽ��ʸ����
	 * @return �������줿�롼���� */
	public AbstractMembrane newRoot(String nodedesc, int k) {
		if(Env.debug > 0)System.out.println("AbstraceMembrane.newRoot(" + nodedesc + ")");
		
		if (nodedesc.equals("")) {
			AbstractMembrane mem = newMem();
			mem.changeKind(k);
			mem.lock();
			return mem;
		}
		//(nakajima 2004-10-25) ʬ�����Ȥꤢ�������󥹥ȥ饯������Ͽ������ˤ����Τǥ����ȥ����ȡ�
		//daemon.IDConverter.registerGlobalMembrane(this.getGlobalMemID(),this);
		
		// ��TODO (��Ψ�����ڽ����ǽ�Ǥ��뤳�Ȥ�Τ�����)connectRuntime�ϥ����ɤǤ��Ǥ˸ƤФ�Ƥ���ΤǾ�Ĺ���⤷��ʤ�
		AbstractLMNtalRuntime machine = LMNtalRuntimeManager.connectRuntime(nodedesc);
		AbstractMembrane mem = machine.newTask(this).getRoot();
		mem.changeKind(k);
		return mem;
	}
	/** ���ꤵ�줿�Ρ��ɤǼ¹Ԥ�����å����줿kind==0�Υ롼������������������λ���ˤ������������롣
	 * @param nodedesc �Ρ���̾��ɽ��ʸ����
	 * @return �������줿�롼���� */
	public AbstractMembrane newRoot(String nodedesc) {
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
	
	/** ���������롣
	 * <p>
	 * <strike>���Ǥ˥����å����Ѥޤ�Ƥ���в��⤷�ʤ���</strike>
	 * �����å����Ѥޤ�Ƥ��ʤ��ʤ�С�
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
	public abstract void activate();

//	/** ���������줫������
//	 * @deprecated */
//	public void remove() {
//		parent.removeMem(this);
//	}

	/** �ʿ��������ʤ�����srcMem�ˤ������ƤΥ��ȥ�Ȼ���ʥ�å���������Ƥ��ʤ��ˤ򤳤���˰�ư���롣
	 * ����ϥ롼�����ľ������ޤǺƵ�Ū�˰�ư����롣�ۥ��ȴְ�ư������ϳ���������롣
	 * ���Υ᥽�åɼ¹Ը塢srcMem�Ϥ��Τޤ��Ѵ�����ʤ���Фʤ�ʤ���
	 */
	public void moveCellsFrom(AbstractMembrane srcMem) {
		if (this == srcMem) return;
		// ���ȥ�ΰ�ư
		Iterator it = srcMem.atomIterator();
		while (it.hasNext()) {
			addAtom((Atom)it.next());
		}
		
		// ����ΰ�ư
		if (srcMem.task.getMachine() instanceof LocalLMNtalRuntime) {
			// �������줫��ΰ�ư
			mems.addAll(srcMem.mems);
		}
		else {
			// ��⡼���줫��ʥ�������ءˤΰ�ư
			it = srcMem.memIterator();
			while (it.hasNext()) {
				AbstractMembrane subSrcMem = (AbstractMembrane)it.next();
				if (subSrcMem.isRoot()) {
					subSrcMem.moveTo(this);
				}
				else {
					AbstractMembrane subMem = newMem();
					if (!subSrcMem.blockingLock()) {
						throw new RuntimeException("AbstractMembrane.moveCellsFrom: blockingLock failure");
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
			AbstractMembrane subSrcMem = (AbstractMembrane)it.next();
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
	public AbstractMembrane moveTo(AbstractMembrane dstMem) {
		if (parent != null) {
			System.err.println("Warning: membrane with parent was moved");
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
			AbstractMembrane mem = dstMem.newMem();
			mem.moveCellsFrom(this);
			// this���Ѵ����٤��Ǥ��롣���� TO-DO (A) �β��ˡ�Ϥ��Τ褦�ʿ��������moveCellsFrom�餷��
			return mem;
		}
//		activate();
		//enqueueAllAtoms();
	}
	
	/** ������Ȥ��λ�¹��������륿�����򹹿����뤿��˸ƤФ������̿�ᡣ
	 * �������롼����ʲ��Υ��������ѹ����ʤ����Ĥޤ�롼������Ф��ƸƤФ줿���ϲ��⤷�ʤ���*/
	protected void setTask(AbstractTask newTask) {
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
			((AbstractMembrane)it.next()).setTask(newTask);
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
	
	HashMap memToCopyMap; //����->(���ԡ����Υ��ȥ�in����->���ԡ���Υ��ȥ�in���ԡ����줿����)
	HashMap memToCopiedMem; //����->���ԡ���λ���
	
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
	public Map copyCellsFrom(AbstractMembrane srcmem){
		memToCopyMap = new HashMap();
		memToCopiedMem = new HashMap();
		Iterator it = srcmem.memIterator();
		while(it.hasNext()){
			AbstractMembrane omem = (AbstractMembrane)it.next();
			AbstractMembrane nmem = newMem();
			memToCopiedMem.put(omem,nmem);
			memToCopyMap.put(omem,nmem.copyCellsFrom(omem));
			nmem.copyRulesFrom(omem);
		}
		it = srcmem.atomIterator();
		Map oldAtomToNewAtom = new HashMap();
		while(it.hasNext()){
			Atom oatom = (Atom)it.next();
			if(oldAtomToNewAtom.containsKey(oatom))continue;
			oldAtomToNewAtom.put(oatom,newAtom(oatom.getFunctor()));
			oldAtomToNewAtom = copyAtoms(oatom,oldAtomToNewAtom);
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
						AbstractMembrane copiedmem = (AbstractMembrane)memToCopiedMem.get(atom.args[i].getAtom().mem);
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
		it = memIterator();
		while(it.hasNext()){
			AbstractMembrane mem = (AbstractMembrane)it.next();
			mem.drop();
			mem.free();
		}
	}
	
	/**
	 * by kudo
	 * �����ץ�����ʣ������(�����ϺѤ�Ǥ���)
	 * ( java.util.Stack��Ȥ��褦���ѹ����������ȼ��������Map���ѻ� 2005/07/28)
	 * @param srcGround ���ԡ����δ����ץ���
	 * @return ���ԡ���Υ��
	 */
	public Link copyGroundFrom(Link srcGround){//,Map srcMap){
		java.util.Stack s = new java.util.Stack();
		Map map = new HashMap();
		Atom cpAtom = newAtom(srcGround.getAtom().getFunctor());
		map.put(srcGround.getAtom(),cpAtom);
		for(int i=0;i<cpAtom.getArity();i++){
			if(srcGround.getPos()==i)continue;
			s.push(srcGround.getAtom().getArg(i));
		}
		while(!s.isEmpty()){
			Link l = (Link)s.pop();
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
				cpAtom = (Atom)map.get(l.getAtom());
				Atom a = ((Atom)map.get(l.getAtom().getArg(l.getPos()).getAtom()));
				a.args[l.getAtom().getArg(l.getPos()).getPos()]=new Link(cpAtom,l.getPos());
			}
		}
		return new Link(((Atom)map.get(srcGround.getAtom())),srcGround.getPos());
//		
//		if(!srcMap.containsKey(srcGround.getAtom())){
//			Atom cpAtom = newAtom(srcGround.getAtom().getFunctor());
//			srcMap.put(srcGround.getAtom(),cpAtom);
//			for(int i=0;i<cpAtom.getArity();i++){
//				if(i==srcGround.getPos())continue;
//				cpAtom.args[i] = copyGroundFrom(srcGround.getAtom().getArg(i),srcMap);
//				cpAtom.getArg(i).getAtom().args[srcGround.getAtom().getArg(i).getPos()] = new Link(cpAtom,i);
//			}
//		}
//		return new Link(((Atom)srcMap.get(srcGround.getAtom())),srcGround.getPos());
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
	
	/**
	 * ������Υ�å��������ߤ롣
	 * <p>�롼�륹��åɤޤ���dumper��������Υ�å����������Ȥ��˻��Ѥ��롣
	 * <p>�롼�륹��åɤϡ���å������ˤ�unlock()����Ѥ��롣
	 * <p>dumper�ϡ���å������ˤ�quietUnlock()����Ѥ��롣
	 * @return ��å��μ����������������ɤ��� */
	public abstract boolean lock();

	/**
	 * ������Υ�å��������ߤ롣
	 * ���Ԥ�����硢�������������륿�����Υ롼�륹��åɤ�����׵�����롣���θ塢
	 * ���Υ������������ʥ��ȯ�Ԥ���Τ��ԤäƤ��顢�Ƥӥ�å��������ߤ뤳�Ȥ򷫤��֤���
	 * <p>�롼�륹��åɰʳ��Υ���åɤ�������Υ�å����������Ȥ��˻��Ѥ��롣
	 * <p>��å������ˤ�unlock()����Ѥ��롣
	 * <p>��������ξ�硢��å���������������ޤ����ʤ���
	 * @return ��å��μ����������������ɤ��� */
	public abstract boolean blockingLock();
	/**
	 * �����줫�餳�����������륿�����Υ롼����ޤǤ����Ƥ���Υ�å���֥�å��󥰤Ǽ�������
	 * �¹��쥹���å��������롣�롼����ʤ��blockingLock()��Ʊ���ˤʤ롣
	 * <p>�롼�륹��åɰʳ��Υ���åɤ��ǽ�Υ�å��Ȥ��Ƥ�����Υ�å����������Ȥ��˻��Ѥ��롣
	 * <p>��å������ˤ�asyncUnlock()����Ѥ��롣
	 * <p>��������ξ�硢��å���������������ޤ����ʤ���
	 * @return ��å��μ����������������ɤ��� */
	public abstract boolean asyncLock();

	/** ���Υ�å�����������Ƥλ�¹����Υ�å���Ƶ�Ū�˥֥�å��󥰤Ǽ������롣
	 * ����å���Ϲ������ʤ���
	 * <p>��å������ˤ�recursiveUnlock()����Ѥ��롣
	 * @return ��å��μ����������������ɤ��� */
	public abstract boolean recursiveLock();
	
	// - �ܥǥ�̿��
	
	/**
	 * ��������������Υ�å���������롣
	 * �¹��쥹���å������ʤ���
	 * �롼����ξ�硢�������������륿�������Ф��ƥ����ʥ��notify�᥽�åɡˤ�ȯ�Ԥ��롣
	 */
	public abstract void quietUnlock();

	/**
	 * ��������������Υ�å���������롣�롼����ξ�硢
	 * ���μ¹��쥹���å������Ƥ�¹��쥹���å������ž������
	 * �������������륿�������Ф��ƥ����ʥ��notify�᥽�åɡˤ�ȯ�Ԥ��롣
	 * <p>�����������줬��⡼����ξ��ϲ�������������ǲ��⤷�ʤ������Τ�ʤ���
	 * <p><strike>todo unlock �� weakUnlock ��̾���ѹ�����</strike> */
	public abstract void unlock();
	public abstract void unlock(boolean changed);
	
	/** ������Υ�å�����Ū�˲������롣��⡼����ξ��Ⲿ�������ʤ���
	 * ��������ξ���unlock()��Ʊ����*/
	public abstract void forceUnlock();

	/** �����줫�餳�����������륿�����Υ롼����ޤǤ����Ƥ���μ���������å�������������������������롣
	 * ���μ¹��쥹���å������Ƥ�¹��쥹���å���ž�����롣�롼����ξ���unlock()��Ʊ���ˤʤ롣
	 * <p>�롼�륹��åɰʳ��Υ���åɤ��ǽ�˼���������Υ�å����������Ȥ��˻��Ѥ��롣*/
	public abstract void asyncUnlock();

	/** ������������������Ƥλ�¹����Υ�å���Ƶ�Ū�˲������롣*/
	public abstract void recursiveUnlock();

	///////////////////////////////
	// ��ͳ��󥯴������ȥ��ĥ���ؤ��򤹤뤿�������RemoteMembrane�ϥ����С��饤�ɤ��ʤ���
	//
	// TODO �ʸ�Ψ������star�򥭥塼�Ǵ������뤳�Ȥˤ�ꡢalterAtomFunctor�β���򸺤餹�Ȥ褤�����Τ�ʤ���
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
					if (a1.getFunctor().isOUTSIDE_PROXY()
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
				if (a1.getFunctor().isOUTSIDE_PROXY()) {
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
	public void insertProxies(AbstractMembrane childMemWithStar) {
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
	
	/** ������������� (���줬̵�����ˤΤ߸Ƥ���ɤ�)*/
	public void free() {}
	
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
	
	/* *** *** *** *** *** BEGIN GUI *** *** *** *** *** */
	public java.awt.geom.Rectangle2D.Double rect;
	
	
	
	/* *** *** *** *** *** END GUI *** *** *** *** *** */
	
	////////////////////////////////////////
	// non deterministic LMNtal
	public boolean equals(Object o) {
		if (Env.fNonDeterministic) {
			AbstractMembrane m = (AbstractMembrane)o;
			return atoms.equals(m.atoms) && rulesets.equals(m.rulesets);
			//�롼�륻�åȤ򽸹�Ȥ�����Ӥ��Ƥ��ʤ��Τǡ������ˤ�Ʊ���Ǥ��������Ƚ�Ǥ����������롣
			//���⤽��롼�륻�åȤ�ưŪ���Ѳ�������褦����ϤۤȤ�ɤʤ��Τǡ��Ȥꤢ�������ɤ����ˤ��롣
		} else {
			return super.equals(o);
		}
	}
	
}
