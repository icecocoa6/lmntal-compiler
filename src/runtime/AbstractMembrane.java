package runtime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import util.QueuedEntity;
import util.Stack;

/**
 * �������β��ˡ
 * [1] �Ʒ׻��Ρ��ɤ�����ͳ��󥯽��ϴ������ȥ�Υ����Х�ID���դ�����ˡ
 *  - newFreeLink̿�������ˡ
 *      ̿��μ��ब������
 *  - newFreeLink̿�����ʤ����
 *      newAtom�������functor�򸡺����٤���
 * [2] �Ʒ׻��Ρ��ɤ�����ͳ��󥯽��ϴ������ȥ�Υ����Х�ID���դ��ʤ���ˡ
 *  - ���ߥåȻ��˿������ȥ�β�ID���˴�������
 *    ����������ͳ��󥯽��ϴ������ȥ�μ��̤��Ǥ��ʤ��ʤ뢪NG
 *  - ���ߥåȻ��˿������ȥ�β�ID���˴����ʤ����
 *    ����Υ���å��幹�����˲�ID�������������Х�ID���ѹ������å��������������뢪�٤�����������
 * 
 * ������newFreeLink̿����ѻߡ�
 */


/**
 * ����쥯�饹���������쥯�饹����ӥ�⡼���쥯�饹�οƥ��饹
 * @author Mizuno
 */
abstract class AbstractMembrane extends QueuedEntity {
	/** �������������륿���� */
	protected AbstractTask task;
	/** ���졣��⡼�Ȥˤ���ʤ��RemoteMembrane���֥������Ȥޤ���null�򻲾Ȥ��� */
	protected AbstractMembrane parent;
	/** ���ȥ�ν��� */
	protected AtomSet atoms = new AtomSet();
	/** ����ν��� */
	protected Set mems = new HashSet();
//	/** ������ˤ���proxy�ʳ��Υ��ȥ�ο��� */
//	protected int atomCount = 0;
//	/** ���Υ���μ�ͳ��󥯤ο� */
//	protected int freeLinkCount = 0;
	/** �롼�륻�åȤν��硣 */
	protected List rulesets = new ArrayList();
	/** true�ʤ�Ф�����ʲ���Ŭ�ѤǤ���롼�뤬̵�� */
	protected boolean stable = false;
	/** ��å�����Ƥ������true */
	protected boolean locked = false;
//	/** �Ǹ�˥�å������������ */
//	protected AbstractMembrane lastLockedMem;

	private static int nextId = 0;
	private int id;
	
	///////////////////////////////
	// ���󥹥ȥ饯��

	/**
	 * ���ꤵ�줿�������˽�°�������������롣
	 */
	protected AbstractMembrane(AbstractTask task, AbstractMembrane parent) {
		this.task = task;
		this.parent = parent;
		id = nextId++;
	}

	///////////////////////////////
	// ����μ���

	/**
	 * �ǥե���Ȥμ������Ƚ����Ϥ��������֤��Ѥ����Ѥ�äƤ��ޤ��Τǡ�
	 * ���󥹥��󥹤��Ȥ˥�ˡ�����id���Ѱդ��ƥϥå��女���ɤȤ������Ѥ��롣
	 */
	public int hashCode() {
		return id;
	}
	/** ������Υ�����ID��������� */
	String getLocalID() {
		return Integer.toString(id);
	}
	/** �����줬��°����׻��Ρ��ɤˤ����롢�������ID��������� */
	abstract String getMemID();
	/** �����줬��°����׻��Ρ��ɤˤ����롢������λ��ꤵ�줿���ȥ��ID��������� */
	abstract String getAtomID(Atom atom);
	
	//
	
	/** �������������륿�����μ��� */
	AbstractTask getTask() {
		return task;
	}
	/** ����μ��� */
	AbstractMembrane getParent() {
		return parent;
	}
	int getMemCount() {
		return mems.size();
	}
	/** proxy�ʳ��Υ��ȥ�ο������ */
	int getAtomCount() {
		return atoms.getNormalAtomCount();
	}
	/** ���Υ���μ�ͳ��󥯤ο������ */
	int getFreeLinkCount() {
		return atoms.getAtomCountOfFunctor(Functor.INSIDE_PROXY);
	}
	/** ������Ȥ��λ�¹��Ŭ�ѤǤ���롼�뤬�ʤ�����true */
	boolean isStable() {
		return stable;
	}
	/** stable�ե饰��ON�ˤ��� 10/26���� Task#exec()��ǻȤ�*/
	void toStable(){
		stable = true;
	}
	/** ������˥롼�뤬�����true */
	boolean hasRule() {
		return rulesets.size() > 0;
	}
	boolean isRoot() {
		return task.getRoot() == this;
	}
	
	// ȿ����
	
	/** ������ˤ��륢�ȥ��ȿ���Ҥ�������� */
	Iterator atomIterator() {
		return atoms.iterator();
	}
	/** ������ˤ�������ȿ���Ҥ�������� */
	Iterator memIterator() {
		return mems.iterator();
	}
	/** ̾��func����ĥ��ȥ��ȿ���Ҥ�������� */
	Iterator atomIteratorOfFunctor(Functor functor) {
		return atoms.iteratorOfFunctor(functor);
	}
	/** ������ˤ���롼�륻�åȤ�ȿ���Ҥ��֤� */
	Iterator rulesetIterator() {
		return rulesets.iterator();
	}

	///////////////////////////////
	// ����RemoteMembrane�Ǥϥ����С��饤�ɤ�����

	// ���1 - �롼������
	
	/** �롼������ƾõ�� */
	void clearRules() {
		rulesets.clear();
	}
	/** srcMem�ˤ���롼��򤳤���˥��ԡ����롣 */
	void inheritRules(AbstractMembrane srcMem) {
		rulesets.addAll(srcMem.rulesets);
	}
	/** �롼�륻�åȤ��ɲ� */
	void loadRuleset(Ruleset srcRuleset) {
		rulesets.add(srcRuleset);
	}

	// ���2 - ���ȥ�����

	/** ���������ȥ�����������������ɲä��롣
	 * TODO �¹ԥ����å��˼�ưŪ�ˤϵͤޤ�ʤ��褦�˻����ѹ����Ƥ�褤���⤷��ʤ���
	 * ���������ξ��ϡ�newatom̿��μ���enqueueatom�ܥǥ�̿�᤬ɬ�פˤʤ롣
	 */
	Atom newAtom(Functor functor) {
		Atom a = new Atom(this, functor);
// #if (VERSION != 1.16_mizuno)
		addAtom(a);
		atoms.add(a);
// #else
//	atoms.add(atom);
//	if (functor.isActive()) {
//		enqueueAtom(a);
//	}
//	atomCount++;
// #endif
		return a;
	}
	/** 1������newAtom��ƤӽФ��ޥ��� */
	final Atom newAtom(String name, int arity) {
		return newAtom(new Functor(name, arity));
	}	
	/** ������˥��ȥ���ɲä��뤿�������̿�ᡣ
	 * �����ƥ��֥��ȥ�ξ��ˤϼ¹ԥ����å����ɲä��롣
	 * pour�Ǥ���Ѥ����ͽ�ꡣ */
	protected final void addAtom(Atom atom) {
		atoms.add(atom);
		if (atom.getFunctor().isActive()) {
			enqueueAtom(atom);
		} 
//		atomCount++;
	}

	/** ���ꤵ�줿����˿�����inside_proxy���ȥ���ɲä��� */
	Atom newFreeLink(AbstractMembrane mem) {
		return mem.newAtom(Functor.INSIDE_PROXY);
	}
	/** ���ꤵ�줿���ȥ��̾�����Ѥ��� */
	void alterAtomFunctor(Atom atom, Functor func) {
		atoms.remove(atom);
		atom.changeFunctor(func);
		atoms.add(atom);
	}

	/** ���ꤵ�줿���ȥ�򤳤���μ¹ԥ����å����Ѥ� */
	abstract protected void enqueueAtom(Atom atom);
	/** �����줬��ư���줿�塢�����ƥ��֥��ȥ��¹ԥ����å�������뤿��˸ƤӽФ���롣
	 * <p>Ruby�ǤǤ�movedTo(task,dstMem)��Ƶ��ƤӽФ����Ƥ�������
	 * ���塼��ľ���٤����ɤ�����Ƚ�Ǥμ�֤��ݤ��ꤹ���뤿���¹������Ф���������ѻߤ��줿�� */
	abstract protected void enqueueAllAtoms();

	/** ���ꤵ�줿���ȥ�򤳤��줫�����롣
	 * �¹ԥ����å������äƤ����硢�¹ԥ����å������������
	 * TODO enqueueatomƱ��dequeueatom�ܥǥ�̿�����Ω��������ˡ�⤢�롣
	 * AbstractMembrane#dequeueAtom�Ϥ��ξ��Τ�abstract�᥽�åɤˤ����̣�����롣
	 * �դˡ���Ω�����ʤ��ʤ�dequeueAtom�ϥޥ����private final�ˤǤ褤�� */
	void removeAtom(Atom atom) {
		atoms.remove(atom);
		//if (atom.isQueued()) { // dequeueAtom��˰�ư���ޤ���������ϳ�ǧ�女���Ȥ�ä��Ʋ�����
			dequeueAtom(atom);
		//}
	}
	/** removeAtom��ƤӽФ��ޥ��� */
	final void removeAtoms(ArrayList atomlist) {
		// atoms.removeAll(atomlist);
		Iterator it = atomlist.iterator();
		while (it.hasNext()) {
			removeAtom((Atom)it.next());
		}
	}

	/** 
	 * ������ˤ��륢�ȥ�atom�����η׻��Ρ��ɤ��¹Ԥ��륿�����ˤ�����μ¹ԥ����å���ˤ���С�����롣
	 * ¾�η׻��Ρ��ɤ��¹Ԥ��륿�����ˤ�����μ¹ԥ����å���ΤȤ��ʥ����ƥॳ����ˤϡ��������
	 * ��å�����Ƥ��ʤ��Τǲ��⤷�ʤ��Ǥ褤�������ξ��ϼ¹ԥ����å���ˤʤ��ΤǴ����б��Ǥ��Ƥ��롣
	 * <p><strike>�֤�����μ¹ԥ����å������äƤ��륢�ȥ�atom��¹ԥ����å����������</strike>
	 * �����ߤΥǡ�����¤�Ǥϡ��ɤ���μ¹ԥ����å������äƤ��뤫Ĵ�٤뤳�Ȥ��Ǥ��ʤ�����Ѳ����줿��
	 */
	protected final void dequeueAtom(Atom atom) {
		if (atom.isQueued()) {
			atom.dequeue();
		}
	}

	// ���3 - ��������
	
	/** ������������������ */
	abstract AbstractMembrane newMem();
	/** ���ꤵ�줿��򤳤���λ���Ȥ����ɲä��뤿�������̿�� */
	protected final void addMem(AbstractMembrane mem) {
		mems.add(mem);
	}
	/** ���ꤵ�줿��򤳤��줫������ */
	void removeMem(AbstractMembrane mem) {
		mems.remove(mem);
	}	
	/** ���ꤵ�줿�׻��Ρ��ɤǼ¹Ԥ����롼������������������λ���ˤ��롣
	 * ���Υ᥽�åɤϻȤ�ʤ����⤷��ʤ����������äƤ�����
	 * @return �������줿�롼����
	 */
	abstract AbstractMembrane newRoot(AbstractMachine runtime);

	// ���4 - ��󥯤����
	
	/**
	 * atom1����pos1�����ȡ�atom2����pos2��������³���롣
	 * ��³���륢�ȥ�ϡ�
	 * <ol><li>������Υ��ȥ�Ʊ��
	 *     <li>�������outside_proxy�Ȼ����inside_proxy
	 * </ol>
	 * ��2�̤�˸¤��롣
	 * <p>
	 * <b>���</b>��
	 * newLink��Ruby�ǤǤ����������ĥ�󥯤���������̿��Ǥ��ä�����
	 * Java�ǤǤ�ξ��������٤���������褦�˻��ͤ��ѹ�����Ƥ��롣
	 */
	void newLink(Atom atom1, int pos1, Atom atom2, int pos2) {
		atom1.args[pos1] = new Link(atom2, pos2);
		atom2.args[pos2] = new Link(atom1, pos1);
	}
	/** atom1����pos1�����ȡ�atom2����pos2�����Υ�������³���롣
	 * �¹Ը塢atom2����pos2�������Ѵ����ʤ���Фʤ�ʤ���
	 */
	void relinkAtomArgs(Atom atom1, int pos1, Atom atom2, int pos2) {
		// TODO clone�ϻȤ�ʤ����Ƥ褤�Ϥ������������̤ϥǥХå����ưפˤ��뤿�ᤳ�ΤޤޤǤ褤��
		atom1.args[pos1] = (Link)atom2.args[pos2].clone();
		atom2.args[pos2].getBuddy().set(atom1, pos1);
	}
	/** atom1����pos1�����Υ����ȡ�atom2����pos2�����Υ�������³���롣
	 */
	void unifyAtomArgs(Atom atom1, int pos1, Atom atom2, int pos2) {
		atom1.args[pos1].getBuddy().set(atom2.args[pos2]);
		atom2.args[pos2].getBuddy().set(atom1.args[pos1]);
	}

	/** atom2����pos2�����˳�Ǽ���줿��󥯥��֥������Ȥؤλ��Ȥ�������롣
	 */
	Link getAtomArg(Atom atom2, int pos2) {
		return atom2.args[pos2];
	}
	/** atom1����pos1�����ȡ����link2�Υ�������³���롣
	 * <p>link2�Ϻ����Ѥ���뤿�ᡢ�¹Ը�link2�λ��Ȥ���Ѥ��ƤϤʤ�ʤ���
	 */
	void inheritAtomArg(Atom atom1, int pos1, Link link2) {
		link2.getBuddy().set(atom1, pos1);
		atom1.args[pos1] = link2;
	}
	
	// TODO relinkLocalAtomArgs��unifyLocalAtomArgs��Local�Ǥʤ��᥽�åɤ�Ʊ���ʤΤǲ��Ȥ�����

	/** relinkAtomArgs��Ʊ������̿�ᡣ������������Υǡ�����¤�Τ߹������롣
	 */
	protected final void relinkLocalAtomArgs(Atom atom1, int pos1, Atom atom2, int pos2) {
		atom1.args[pos1] = (Link)atom2.args[pos2].clone();
		atom2.args[pos2].getBuddy().set(atom1, pos1);
	}
	/** unifyAtomArgs��Ʊ������̿�ᡣ������������Υǡ�����¤�Τ߹������롣
	 */
	protected final void unifyLocalAtomArgs(Atom atom1, int pos1, Atom atom2, int pos2) {
		atom1.args[pos1].getBuddy().set(atom2.args[pos2]);
		atom2.args[pos2].getBuddy().set(atom1.args[pos1]);
	}

	// ���5 - �켫�Ȥ��ư�˴ؤ������
	
	/** ������ */
	abstract void activate();
	/** ���������줫������ */
	void remove() {
		parent.removeMem(this);
		parent = null;
		removeProxies();
	}

	/** ����줿��srcMem�ˤ������ƤΥ��ȥप�����򤳤���˰�ư���롣
	 * ��srcMem�λ�¹�Τ����롼����μ����ޤǤ����Ƥ���򡢤������Ʊ���������δ����ˤ��롣
	 * srcMem�Ϥ��Υ᥽�åɼ¹Ը塢���Τޤ��Ѵ����ʤ���Фʤ�ʤ���
	 */
	void pour(AbstractMembrane srcMem) {
		if (srcMem.task.getMachine() != task.getMachine()) {
			throw new RuntimeException("cross-site process fusion not implemented");
		}
		atoms.addAll(srcMem.atoms);
		mems.addAll(srcMem.mems);
		Iterator it = srcMem.memIterator();
		while (it.hasNext()) {
			((AbstractMembrane)it.next()).parent = this;
		}
		if (srcMem.task != task) {
			srcMem.setTask(task);
		}
	}
	
//	/** �������ʣ������������ */
//	Membrane copy() {
//		
//	}
	
	/** �������dstMem�˰�ư���롣parent!=null���ꤹ�롣 */
	void moveTo(AbstractMembrane dstMem) {
		if (dstMem.task.getMachine() != task.getMachine()) {
			parent = dstMem;
			//((RemoteMembrane)dstMem).send("ADDROOT",getMemID());
			throw new RuntimeException("cross-site process migration not implemented");
		}
		parent.removeMem(this);
		dstMem.addMem(this);
		parent = dstMem;
		if (dstMem.task != task) {
			setTask(dstMem.task);
		}
		enqueueAllAtoms();
	}
	/** ������Ȥ��λ�¹��������륿�����򹹿����뤿��˸ƤФ������̿�� */
	private void setTask(AbstractTask newTask) {
		if (isRoot()) return;
		this.task = newTask;
		Iterator it = memIterator();
		while (it.hasNext()) {
			((AbstractMembrane)it.next()).setTask(newTask);
		}
	}
	/** ������ʥ롼����ˤο�����ѹ����롣
	 * <p>�����졢
	 * AbstractMembrane#newRoot�����AbstractMachine#newTask�ΰ����˿�����Ϥ��褦�ˤ���
	 * AbstractMembrane#moveTo��Ȥäƿ�����ѹ����뤳�Ȥˤ�ꡢ
	 * TODO ��������Τ���᥽�åɤ��ѻߤ��ʤ���Фʤ�ʤ� */
	void setParent(AbstractMembrane mem) {
		if (!isRoot()) {
			throw new RuntimeException("setParent requires this be a root membrane");
		}
		parent = mem;
	}
	// ���6 - ��å��˴ؤ������
	
	/**
	 * ��������å����롣
	 * @param mem ��å����׵ᤷ�Ƥ���롼�뤬������
	 * @return ��å���������������true
	 */
	synchronized boolean lock(AbstractMembrane mem) {
		if (locked) {
			//todo:���塼�˵�Ͽ
			return false;
		} else {
			//todo:�׻��Ρ��ɤε�Ͽ������å���ι���
			locked = true;
			return true;
		}
	}
	/**
	 * ������Ȥ��λ�¹��Ƶ�Ū�˥�å�����
	 * @param mem �롼��Τ�����
	 * @return ��å���������������true
	 */
	boolean recursiveLock(AbstractMembrane mem) {
		// ��������
		return false;
	}
	/** ��å���������� */
	void unlock() {
		locked = false;
		// TODO ��������
	}
	void recursive() {
		// ��������
	}
	
	///////////////////////////////
	// ��ͳ��󥯴������ȥ��ĥ���ؤ��򤹤뤿�������RemoteMembrane�ϥ����С��饤�ɤ��ʤ���
	//
	// TODO star�򥭥塼�Ǵ������뤳�Ȥˤ�ꡢalterAtomFunctor�β���򸺤餹�Ȥ褤�����Τ�ʤ���
	// ���塼��LinkedList���֥������ȤȤ���react�����¸���֤Ȥ���star��Ϣ�Υ᥽�åɤΰ������Ϥ���롣
	// $p��ޤ����Ƥ�������줫������дط����롼��Ŭ�Ѥ����Ѥʾ�硢
	// $p�����Ĥ����Ƥ���򤦤ޤ������Ѥ��뤳�Ȥˤ�äơ�star��Ϣ�ν����������Ƥ�ɬ�פ��ʤ��ʤ롣
	// �����������Ԥ���硢removeProxies��remove����ʬΥ����ñ�ȤΥܥǥ�̿��ˤ���ɬ�פ����롣
	
	/** �����줬remove���줿ľ��˸ƤФ�롣
	 * �ʤ�remove�ϡ��롼�뺸�դ˽񤫤줿���ȥ�������塢
	 * �롼�뺸�դ˽񤫤줿��Τ���$p����Ĥ�Τ��Ф�����¦���줫��ƤФ�롣
	 * <p>��������Ф���
	 * <ol>
	 * <li>������μ�ͳ/�ɽ��󥯤Ǥʤ��ˤ⤫����餺����������̲ᤷ�Ƥ����󥯤���
	 * <li>������μ�ͳ��󥯤��и����륢�ȥ��̾����star���Ѥ��롣
	 * </ol>
	 * <p>���٤Ƥ�removeProxies�θƤӽФ�����λ�����
	 * <ul>
	 * <li>$p�˥ޥå������ץ����μ�ͳ��󥯤�$p���񤫤줿���star���ȥ�˽и�����褦�ˤʤꡢ
	 * <li>star���ȥ�Υ����ϡ�star���ȥ�ޤ��������outside_proxy����1�����ˤʤäƤ��롣
	 *     ���Τ�����Ԥϡ�removeToplevelProxies�ǽ����롣
	 * </ul>
	 */
	void removeProxies() {
		// TODO atoms�ؤ���ɬ�פˤʤ�Τǡ�Set�Υ������������Ƥ���ȿ���Ҥ�Ȥä�����
		//      �ɤߤ䤹������Ψ���ɤ����⤷��ʤ�
		ArrayList changeList = new ArrayList();	// star�����륢�ȥ�Υꥹ��
		ArrayList removeList = new ArrayList();
		Iterator it = atoms.iteratorOfFunctor(Functor.OUTSIDE_PROXY);
		while (it.hasNext()) {
			Atom outside = (Atom)it.next();
			Atom a0 = outside.args[0].getAtom();
			// outside�Υ���褬����Ǥʤ����			
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
					if (a1.getFunctor().equals(Functor.OUTSIDE_PROXY)
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
	 * <p>��������̲ᤷ��̵�ط���������äƤ�����󥯤����롣
	 */
	void removeToplevelProxies() {
		ArrayList removeList = new ArrayList();
		Iterator it = atoms.iteratorOfFunctor(Functor.OUTSIDE_PROXY);
		while (it.hasNext()) {
			Atom outside = (Atom)it.next();
			// outside�Υ���褬����Ǥʤ����			
			if (outside.args[0].getAtom().getMem().getParent() != this) {
				if (!removeList.contains(outside)) {
					Atom a1 = outside.args[1].getAtom();
					unifyLocalAtomArgs(outside, 0, a1, 0);
					removeList.add(outside);
					removeList.add(a1);
				}
			}
		}
		atoms.removeAll(removeList);
	}
	/** ���դ��칽¤�����$p�����Ƥ����֤�����ǡ�
	 * �롼�뱦�դ˽񤫤줿���������Ф�����¦���줫��ƤФ�롣
	 * <p>����childMemWithStar�ˤ���star���ȥ�ʻ���μ�ͳ��󥯤��Ĥʤ��äƤ���ˤ��Ф���
	 * <ol>
	 * <li>̾����inside_proxy���Ѥ�
	 * <li>��ͳ��󥯤�ȿ��¦�νи����������star���ȥ�ʤ�С�
	 *     ��Ԥ�̾����outside_proxy���Ѥ��롣
	 *     �ޤ���ʬ���¹ԤΤ���ˡ����Υ�󥯤�ĥ��ʤ�����
	 * <li>��ͳ��󥯤�ȿ��¦�νи��������������ˤ˻Ĥä�outside_proxy���ȥ�ʤ�С����⤷�ʤ���
	 * <li>��ͳ��󥯤�ȿ��¦�νи���������ʳ��ˤ��륢�ȥ�ʤ�С�
	 *     ��ͳ��󥯤���������̲᤹��褦�ˤ��롣
	 *     ���ΤȤ���������˺�������outside_proxy�Ǥʤ����Υ��ȥ��̾����star�ˤ��롣
	 * </ol>
	 * @param childMemWithStar �ʼ�ͳ��󥯤���ġ˻���
	 */
	void insertProxies(AbstractMembrane childMemWithStar) {
		ArrayList changeList = new ArrayList();	// inside_proxy�����륢�ȥ�Υꥹ��
		Iterator it = childMemWithStar.atomIteratorOfFunctor(Functor.STAR);
		while (it.hasNext()) {
			Atom star = (Atom)it.next(); // n
			changeList.add(star);
			// ��ͳ��󥯤�ȿ��¦�νи���������Υ��ȥ�ʤ�С���Ԥ�̾����outside_proxy���Ѥ��롣
			// ���ΤȤ�star���ä��뤫�⤷��ʤ��Τǡ�star�򥭥塼�Ǽ�������Ȥ��ϥХ�����ա�
			if (star.args[0].getAtom().getMem() == this) {
				alterAtomFunctor(star.args[0].getAtom(), Functor.OUTSIDE_PROXY);
			} else {
				Atom outside = newAtom(Functor.OUTSIDE_PROXY); // o
				Atom newstar = newAtom(Functor.STAR); // m
				newLink(newstar, 1, outside, 1);
				relinkLocalAtomArgs(newstar, 0, star, 0); // ����ˤ��star[0]��̵���ˤʤ�
				newLink(star, 0, outside, 0);
			}
		}
		it = changeList.iterator();
		while (it.hasNext()) {
			alterAtomFunctor((Atom)it.next(), Functor.INSIDE_PROXY);
		}
	}
	/** ���դΥȥåץ�٥��$p������롼��μ¹Ի����Ǹ������˻Ĥä�star��������뤿��˸ƤФ�롣
	 * <p>������ˤ���star���Ф��ơ�
	 * ȿ��¦�νи��Ǥ���outside_proxy�ȤȤ�˽����2����Ʊ�Τ�ľ�뤹�롣
	 */
	void removeTemporaryProxies() {
		ArrayList removeList = new ArrayList();
		Iterator it = atomIteratorOfFunctor(Functor.STAR);
		while (it.hasNext()) {
			Atom star = (Atom)it.next();
			Atom outside = star.args[0].getAtom();
			unifyLocalAtomArgs(star,1,outside,1);
			removeList.add(star);
			removeList.add(outside);
		}
		removeAtoms(removeList);
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
		(rulesets.isEmpty() ? "" : " "+Env.parray(rulesets))+
		//(processContexts.isEmpty() ? "" : " "+Env.parray(processContexts))+
		//(ruleContexts.isEmpty() ? "" : " "+Env.parray(ruleContexts))+
		//(typedProcessContexts.isEmpty() ? "" : " "+Env.parray(typedProcessContexts))+
		"";
		
	}
	
	public String toString() {
		return "{ " + toStringWithoutBrace() + " }";
	}
}
