package runtime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import util.QueuedEntity;
import util.Stack;

// TODO AbstractMachine AbstractMembrane.machine; ���ѻߤ��ơ�
// Machine Membrane.machine ����� RemoteMachine RemoteMembrane.machine �ˤ��롩

/**
 * ����쥯�饹���������쥯�饹�ȥ�⡼���쥯�饹�ʵ졧�쥭��å��奯�饹��̤�����ˤοƥ��饹
 * @author Mizuno
 */
abstract class AbstractMembrane extends QueuedEntity {
	/** ��������������ޥ��� */
	protected AbstractMachine machine;
	/** ���졣�롼����ʤ��null */
	protected AbstractMembrane parent;
	/** ���ȥ�ν��� */
	protected AtomSet atoms = new AtomSet();
	/** ����ν��� */
	protected Set mems = new HashSet();
	/** ������ˤ���proxy�ʳ��Υ��ȥ�ο��� */
//	protected int atomCount = 0;
	/** ���Υ���μ�ͳ��󥯤ο� */
//	protected int freeLinkCount = 0;
	/** �롼�륻�åȤν��硣 */
	protected List rulesets = new ArrayList();
	/** true�ʤ�Ф�����ʲ���Ŭ�ѤǤ���롼�뤬̵�� */
	protected boolean stable = false;
	/** ��å�����Ƥ������true */
	protected boolean locked = false;
//	/** �Ǹ�˥�å������׻��Ρ��� */
//	protected CalcNode lastLockNode;

	private static int nextId = 0;
	private int id;
	
	///////////////////////////////
	// ���󥹥ȥ饯��

	/**
	 * ���ꤵ�줿�ޥ���˽�°�������������롣
	 */
	protected AbstractMembrane(AbstractMachine machine, AbstractMembrane parent) {
		this.machine = machine;
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
	/** ��������������ޥ���μ��� */
	AbstractMachine getMachine() {
		return machine;
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
	/** stable�ե饰��ON�ˤ��� 10/26���� machine.exec()��ǻȤ�*/
	void toStable(){
		stable = true;
	}
	/** ������˥롼�뤬�����true */
	boolean hasRule() {
		return rulesets.size() > 0;
	}
	boolean isRoot() {
		return machine.getRoot() == this;
	}
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

	abstract void activate();
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
	/** ���ȥ���ɲ� */
	Atom newAtom(Functor functor) {
		Atom a = new Atom(this, functor);
		atoms.add(a);
		if (a.isActive()) {
			enqueueAtom(a);
		}
//		atomCount++;
		return a;
	}
	Atom newAtom(String name, int arity) {
		return newAtom(new Functor(name, arity));
	}
	/** ���ꤵ�줿���ȥ��¹ԥ����å����Ѥ� */
	abstract protected void enqueueAtom(Atom atom);
	/** ����ɲ� */
	abstract AbstractMembrane newMem();

//	�ѻߡ�newAtom/newMem����Ѥ��롣
//	/** ���ȥ���ɲá������ƥ��֥��ȥ�ξ��ˤϼ¹ԥ����å����ɲä��롣 */
//	void addAtom(Atom atom) {
//		atoms.add(atom);
//		activateAtom(atom);
//	}
	/** dstMem�˰�ư���� */
	void moveTo(AbstractMembrane dstMem) {
		parent.removeMem(this);
		dstMem.addMem(this);
		parent = dstMem;
//		movedTo(machine, dstMem);
		enqueueAllAtoms();
	}
	/** ��ư���줿�塢�����ƥ��֥��ȥ��¹ԥ����å�������뤿��˸ƤӽФ���� */
//	protected void movedTo(AbstractMachine machine, AbstractMembrane dstMem) {
	abstract protected void enqueueAllAtoms();

//	// $p�ΰ�ư�Τ���Υ᥽�åɤλ��ͤ����֢���3�˷���
//	// ��1��remove/pour�Ǽ�ͳ��󥯴������ȥ���ɲá����
//	// ��2����ͳ��󥯴������ȥ��ɲá�����Τ�������ѥ᥽�åɤ��ɲ�
//	// ��3����removemem�Ǻ��������/pour�ǰ�ư�Τ�/afterpour���ɲáפ�ʬ���ƹԤ���by n-kato��
//	//		afterpour m,n �ܥǥ�̿�����¦����Ƶ�Ū�˸ƤФ졢���ʤ����ɲä���

	/** ���ȥ��̾�����Ѥ��� */
	void alterAtomFunctor(Atom atom, Functor func) {
		atoms.remove(atom);
		atom.changeFunctor(func);
		atoms.add(atom);
	}
	/** ���ꤵ�줿���ȥ�򤳤��줫�����롣 */
	void removeAtom(Atom atom) {
		atoms.remove(atom);
		if (atom.isQueued()) {
			dequeueAtom(atom);
		}
	}
	abstract protected void dequeueAtom(Atom atom);

	void removeAtoms(ArrayList atomlist) {
		// atoms.removeAll(atomlist);
		Iterator it = atomlist.iterator();
		while (it.hasNext()) {
			removeAtom((Atom)it.next());
		}
	}
	/** ���ꤵ�줿��򤳤��줫������ */
	void removeMem(AbstractMembrane mem) {
		mems.remove(mem);
	}
	/** ���������줫���ڤ�Υ����detach�Ȥ���̾�����������������⤷��ʤ��� */
	void remove() {
		parent.removeMem(this);
		parent = null;
		//��3
		removeProxies();
	}
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
	final void removeProxies() {
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
					unifyAtomArgs(outside, 0, a1, 0);
					removeList.add(outside);
					removeList.add(a1);
				}
				else {
					// ��������̲ᤷ��̵�ط���������äƤ�����󥯤����
					if (a1.getFunctor().equals(Functor.OUTSIDE_PROXY)
					 && a1.args[0].getAtom().getMem().getParent() != this) {
						if (!removeList.contains(outside)) {
							unifyAtomArgs(outside, 0, a1, 0);
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
	final void removeToplevelProxies() {
		ArrayList removeList = new ArrayList();
		Iterator it = atoms.iteratorOfFunctor(Functor.OUTSIDE_PROXY);
		while (it.hasNext()) {
			Atom outside = (Atom)it.next();
			// outside�Υ���褬����Ǥʤ����			
			if (outside.args[0].getAtom().getMem().getParent() != this) {
				if (!removeList.contains(outside)) {
					Atom a1 = outside.args[1].getAtom();
					unifyAtomArgs(outside, 0, a1, 0);
					removeList.add(outside);
					removeList.add(a1);
				}
			}
		}
		atoms.removeAll(removeList);
	}
	/**
	 * srcMem�����Ƥ����Ƥ򤳤���˰�ư���롣
	 * TODO ���Τޤޤ��ȥ�⡼����Υ����륭��å�����Ф�������ΤȤ��ˡ�
	 *       �ԡ֥��ߥåȡ׻��˥�⡼�Ȥ˥����륭��å�������Ƥ򤹤٤�ž�����ʤ��¤��
	 *       ������ư��ʤ��������addAll��Ȥ��ˤ����äƤ�����Ū������Ǥ⤢�롣
	 */
	void pour(AbstractMembrane srcMem) {
		atoms.addAll(srcMem.atoms);
		mems.addAll(srcMem.mems);
	}
	/** ���դ��칽¤�����$p�����Ƥ����֤�����ǡ�
	 * �롼�뱦�դ˽񤫤줿���������Ф�����¦���줫��ƤФ�롣
	 * <p>����childMemWithStar�ˤ���star���ȥ�ʻ���μ�ͳ��󥯤��Ĥʤ��äƤ���ˤ��Ф���
	 * <ol>
	 * <li>̾����inside_proxy���Ѥ�
	 * <li>��ͳ��󥯤�ȿ��¦�νи����������star���ȥ�ʤ�С�
	 *     ��Ԥ�̾����outside_proxy���Ѥ��롣
	 * <li>��ͳ��󥯤�ȿ��¦�νи��������������ˤ˻Ĥä�outside_proxy���ȥ�ʤ�С����⤷�ʤ���
	 * <li>��ͳ��󥯤�ȿ��¦�νи���������ʳ��ˤ��륢�ȥ�ʤ�С�
	 *     ��ͳ��󥯤���������̲᤹��褦�ˤ��롣
	 *     ���ΤȤ���������˺�������outside_proxy�Ǥʤ����Υ��ȥ��̾����star�ˤ��롣
	 * </ol>
	 * @param childMemWithStar �ʼ�ͳ��󥯤���ġ˻���
	 */
	final void insertProxies(AbstractMembrane childMemWithStar) {
		ArrayList changeList = new ArrayList();	// inside_proxy�����륢�ȥ�Υꥹ��
		Iterator it = childMemWithStar.atomIteratorOfFunctor(Functor.STAR);
		while (it.hasNext()) {
			Atom inside = (Atom)it.next(); // n
			changeList.add(inside);
			if (inside.args[0].getAtom().getMem() == this) {
				alterAtomFunctor(inside.args[0].getAtom(), Functor.OUTSIDE_PROXY);
			} else {
				Atom outside = newAtom(Functor.OUTSIDE_PROXY); // o
				Atom newstar = newAtom(Functor.STAR); // m
				newLink(newstar, 1, outside, 1);
				relinkAtomArgs(newstar, 0, inside, 0);	// inside[0]��̵���ˤʤ�
				newLink(inside, 0, outside, 0);
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
	final void removeTemporaryProxies() {
		ArrayList removeList = new ArrayList();
		Iterator it = atomIteratorOfFunctor(Functor.STAR);
		while (it.hasNext()) {
			Atom star = (Atom)it.next();
			Atom outside = star.args[0].getAtom();
			unifyAtomArgs(star,1,outside,1);
			removeList.add(star);
			removeList.add(outside);
		}
		removeAtoms(removeList);
	}
	//////////////////////
	
	/** ����ɲ� */
	protected void addMem(AbstractMembrane mem) {
		mems.add(mem);
	}
	
	////////////////////////////////
	// ��å�
	
	/**
	 * ��������å�����
	 * @param mem �롼��Τ�����
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
		// TODO ��������
		return false;
	}
	
//	/** �������ʣ������������ */
//	Membrane copy() {
//		
//	}
	
	/** ��å���������� */
	void unlock() {
		locked = false;
		// TODO ��������
	}
	void recursiveUnlock() {
		// TODO ��������
	}
	
	///////////////////////
	// ��󥯤����
	/**
	 * atom1����pos1�����ȡ�atom2����pos2��������³���롣
	 * ��³���륢�ȥ�ϡ�
	 * <ol><li>������Υ��ȥ�Ʊ��
	 *     <li>�������inside_proxy�ȿ����outside_proxy
	 *     <li>�������outside_proxy�Ȼ����inside_proxy
	 * </ol>
	 * ��3�̤�ξ�礬���롣
	 * <br>
	 * newLink��Ruby�ǤǤ����������ĹԤ������������������ä��Τ�
	 * ����������ʬ���Ƥ��ꡢ�����⤽��˹�碌�ƽ������Ƥ����ޤ��� (n-kato)
	 * newLink�λ��ͤ�ξ�������٤���������褦���ѹ����Ƥ⤤���Ȼפ��ޤ���
	 * ���ξ�硢�ܥǥ�̿��λ��ͤ�Ʊ�����ѹ�����ɬ�פ�����ޤ��Τǡ�
	 * ���������緯��Ϣ���Ƥ�������������
	 * <br>
	 * ����˴�Ϣ���ơ���ˡ4��ʸ���insertproxies����ʬ��newlink����󥯤ˤĤ�
	 * 1�󤷤��ƤФ�Ƥ��ޤ��󤬡�����ϸ����λ��ͤǤ�2��Ǥʤ���Фʤ�ޤ���
	 * �����ѹ����ʤ����ϵո�����񤤤ƽ������Ƥ����Ƥ�������������
	 */
	void newLink(Atom atom1, int pos1, Atom atom2, int pos2) {
		atom1.args[pos1] = new Link(atom2, pos2);
		atom2.args[pos2] = new Link(atom1, pos1);
	}
	/**
	 * atom1����pos1�����ȡ�atom2����pos2�����Υ�������³���롣
	 */
	void relinkAtomArgs(Atom atom1, int pos1, Atom atom2, int pos2) {
		atom1.args[pos1] = (Link)atom2.args[pos2].clone();
		atom2.args[pos2].getBuddy().set(atom1, pos1);
	}
	/**
	 * atom1����pos1�����Υ����ȡ�atom2����pos2�����Υ�������³���롣
	 */
	void unifyAtomArgs(Atom atom1, int pos1, Atom atom2, int pos2) {
		atom1.args[pos1].getBuddy().set(atom2.args[pos2]);
		atom2.args[pos2].getBuddy().set(atom1.args[pos1]);
	}
}

/**
 * �������쥯�饹���¹Ի��Ρ����׻��Ρ�����ˤ������ɽ����
 * @author Mizuno
 */
final class Membrane extends AbstractMembrane {
	/** �¹ԥ����å� */
	private Stack ready = new Stack();
	/**
	 * ���ꤵ�줿�ޥ���˽�°�������������롣
	 * newMem�᥽�å���ǸƤФ�롣
	 */
	private Membrane(AbstractMachine machine, AbstractMembrane parent) {
		super(machine, parent);
	}
	/**
	 * ���ꤵ�줿�ޥ���Υ롼�����������롣
	 */
	Membrane(Machine machine) {
		super(machine, null);
	}

	///////////////////////////////
	// ���

	/** �¹ԥ����å�����Ƭ�Υ��ȥ����������¹ԥ����å�������� */
	Atom popReadyAtom() {
		return (Atom)ready.pop();
	}
	/** ��γ����� */
	void activate() {
		if (!isQueued()) {
			return;
		}
		if (!isRoot()) {
			((Membrane)parent).activate();
		}
		((Machine)machine).memStack.push(this);
	}
	protected void dequeueAtom(Atom atom) {
		atom.dequeue();
	}
	/** 
	 * ���ꤵ�줿���ȥ��¹ԥ����å����ɲä��롣
	 * @param atom �¹ԥ����å����ɲä��륢�ȥࡣ�����ƥ��֥��ȥ�Ǥʤ���Фʤ�ʤ���
	 */
	protected void enqueueAtom(Atom atom) {
		ready.push(atom);
	}
	/** 
	 * ��ư���줿�塢�����ƥ��֥��ȥ��¹ԥ����å�������뤿��˸ƤӽФ���롣
	 */
//	protected void movedTo(AbstractMachine machine, AbstractMembrane dstMem) {
	protected void enqueueAllAtoms() {
		Iterator i = atoms.functorIterator();
		while (i.hasNext()) {
			Functor f = (Functor)i.next();
			if (true) { // f �������ƥ��֤ξ��
				Iterator i2 = atoms.iteratorOfFunctor(f);
				while (i2.hasNext()) {
					ready.push((Atom)i2.next());
				}
			}
		}
	}
	/**
	 * ������������롣
	 */
	AbstractMembrane newMem() {
		Membrane m = new Membrane(machine, this);
		mems.add(m);
		return m;
	}
}
