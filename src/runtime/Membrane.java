package runtime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import util.QueuedEntity;
import util.Stack;

/**
 * ����쥯�饹���������쥯�饹���쥭��å����̤�����ˤοƥ��饹
 * @author Mizuno
 */
abstract class AbstractMembrane extends QueuedEntity {
	/** ��������������ޥ��� */
	protected AbstractMachine machine;
	/** ���� */
	protected AbstractMembrane parent;
	/** ���ȥ�ν��� */
	protected AtomSet atoms = new AtomSet();;
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

	private static int lastId = 0;
	private int id;
	
	///////////////////////////////
	// ���󥹥ȥ饯��

	/**
	 * ���ꤵ�줿�ޥ���˽�°�������������롣
	 */
	protected AbstractMembrane(AbstractMachine machine, AbstractMembrane parent) {
		this.machine = machine;
		this.parent = parent;
		id = lastId++;
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
	// ���

	abstract void activate();
	
	/** �롼������ƥ��ꥢ���� */
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
		enqueueAtom(a);
//		atomCount++;
		return a;
	}
	Atom newAtom(String name, int arity) {
		return newAtom(new Functor(name, arity));
	}
	/** ���ꤵ�줿���ȥ��¹ԥ����å����Ѥ� */
	abstract protected void enqueueAtom(Atom atom);
//	/** ����ɲ� */
	abstract AbstractMembrane newMem();

//	�ѻߡ�newAtom/newMem����Ѥ��롣
//	/** ���ȥ���ɲá������ƥ��֥��ȥ�ξ��ˤϼ¹ԥ����å����ɲä��롣 */
//	void addAtom(Atom atom) {
//		atoms.add(atom);
//		activateAtom(atom);
//	}
	/** dstMem�˰�ư */
	void moveTo(AbstractMembrane dstMem) {
		parent.removeMem(this);
		dstMem.addMem(this);
		parent = dstMem;
//		movedTo(machine, dstMem);
		enqueueAllAtoms();
	}
	/** ����ɲ� */
	protected void addMem(AbstractMembrane mem) {
		mems.add(mem);
	}
	/** ��ư���줿�塢�����ƥ��֥��ȥ��¹ԥ����å�������뤿��˸ƤӽФ���� */
//	protected void movedTo(AbstractMachine machine, AbstractMembrane dstMem) {
	abstract protected void enqueueAllAtoms();

//	// $p�ΰ�ư�Τ���Υ᥽�åɤλ��ͤ����֢���3�˷���
//	// ��1��remove/pour�Ǽ�ͳ��󥯴������ȥ���ɲá����
//	// ��2����ͳ��󥯴������ȥ��ɲá�����Τ�������ѥ᥽�åɤ��ɲ�
//	// ��3����removemem�Ǻ��������/pour�ǰ�ư�Τ�/afterpour���ɲáפ�ʬ���ƹԤ���by n-kato��
//	//		afterpour m,n �ܥǥ�̿�����¦����Ƶ�Ū�˸ƤФ졢���ʤ����ɲä���

	/** ���ꤵ�줿���ȥ�򤳤��줫�����롣 */
	void removeAtom(Atom atom) {
		atoms.remove(atom);
//		atomCount--;
//		if (atomCount < 0) {
//			Util.systemError("Membrane.atomCount is pisitive value");
//		}
	}
	/** ���ꤵ�줿��򤳤��줫������ */
	void removeMem(AbstractMembrane mem) {
		mems.remove(mem);
	}
	void remove() {
		parent.removeMem(this);
		parent = null;
		//��3
		removeProxies();
	}
	protected void removeProxies() {
		// TODO atoms�ؤ���ɬ�פˤʤ�Τǡ�Set�Υ������������Ƥ���ȿ���Ҥ�Ȥä�����
		//      �ɤߤ䤹������Ψ���ɤ����⤷��ʤ�
		Iterator it = atoms.iteratorOfFunctor(Functor.OUTSIDE_PROXY);
		ArrayList removeList = new ArrayList();
		ArrayList changeList = new ArrayList();
		while (it.hasNext()) {
			Atom outside = (Atom)it.next();
			//������ˤ��ä���2����Ʊ�Τ�ľ�ܤĤʤ��äƤ���outside_proxy���ȥ��inside_proxy���ȥ�����
			Atom a = outside.args[1].getAtom();
			if (a.getFunctor().equals(Functor.INSIDE_PROXY)) {
				unifyAtomArgs(outside, 0, a, 0);
				removeList.add(outside);
				removeList.add(a);
			} else {
				//������ˤ��뤽��ʳ���outside_proxy���ȥ�Τ���
				//�����inside_proxy����³���Ƥ��ʤ���Τ�̾����star���Ѥ���
				a = outside.args[0].getAtom();
				if (!a.getFunctor().equals(Functor.INSIDE_PROXY)) { 
//					//TODO ���ȥ�������
//					Atom star = newAtom(Functor.STAR); //atoms���ɲä���뤬it�ˤϱƶ��ʤ�
//					// ��󥯤�ĥ�ؤ���ˡ�Ϥ��������ס����������ޤ��� (n-kato)
//					relinkAtomArgs(star, 0, outside, 0);
//					relinkAtomArgs(star, 1, outside, 1);
//					removeList.add(outside);
					// it��ȤäƤ���Τ�outside�Ϥޤ�����Ǥ��ʤ�
					changeList.add(outside);
				}
			}
		}
		atoms.removeAll(removeList);
		
		//�������inside_proxy���ȥ��̾����star���Ѥ���
//		removeList.clear();
		it = atoms.iteratorOfFunctor(Functor.INSIDE_PROXY);
		while (it.hasNext()) {
//			Atom inside = (Atom)it.next();
//			Atom star = newAtom(Functor.STAR);
//			relinkAtomArgs(star, 0, inside, 0);
//			relinkAtomArgs(star, 1, inside, 1);
//			removeList.add(inside);
			changeList.add(it.next());
		}
//		atoms.removeAll(removeList);

		//̾�����ѹ�
		it = changeList.iterator();
		while (it.hasNext()) {
			Atom a = (Atom)it.next();
			atoms.remove(a);
			a.changeFunctor(Functor.STAR);
			atoms.add(a);
		}
	}
	/**
	 * srcMem�����Ƥ����ư�ư���롣
	 */
	void pour(AbstractMembrane srcMem) {
		atoms.addAll(srcMem.atoms);
		mems.addAll(srcMem.mems);
	}
	void insertProxies(AbstractMembrane childMemWithStar/* N */) {
		Iterator it = childMemWithStar.atomIteratorOfFunctor(Functor.STAR);
//		ArrayList removeList = new ArrayList();
		ArrayList changeList = new ArrayList();
		while (it.hasNext()) {
//			//TODO ̾���ѹ��Υ᥽�åɤ���
//			Atom star = (Atom)it.next(); // n
//			Atom inside = newAtom(Functor.INSIDE_PROXY); //�����n��̾���ѹ����
//			relinkAtomArgs(inside, 0, star, 0);
//			relinkAtomArgs(inside, 1, star, 1);
//			removeList.add(star);
			Atom inside = (Atom)it.next(); //n ̾���Ϻ���star
			changeList.add(inside);
//			if (star.args[0].getAtom().getMem() != this /* M */) {
			if (inside.args[0].getAtom().getMem() != this /* M */) {
				Atom outside = newAtom(Functor.OUTSIDE_PROXY); // o
				Atom newstar = newAtom(Functor.STAR); //m
				newLink(outside, 1, newstar, 1);
//				newLink(newstar, 1, outside, 1);
				relinkAtomArgs(newstar, 0, inside, 0);
				newLink(inside,  0, outside, 0);
//				newLink(outside, 0, inside,  0);
			}
		}
//		atoms.removeAll(removeList);
		it = changeList.iterator();
		while (it.hasNext()) {
			Atom a = (Atom)it.next();
			atoms.remove(a);
			a.changeFunctor(Functor.INSIDE_PROXY);
			atoms.add(a);
		}
	}
	
	/**
	 * ��������å�����
	 * @param mem �롼��Τ�����
	 * @return ��å���������������true
	 */
	boolean lock(AbstractMembrane mem) {
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
		return false;
	}
	
//	/** �������ʣ������������ */
//	Membrane copy() {
//		
//	}
	
	/** ��å��������� */
	void unlock() {
		
	}
	void recursiveUnlock() {
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
		atom1.args[pos1] = atom2.args[pos2];
		atom2.args[pos2].set(atom1, pos1);
	}
	/**
	 * atom1����pos1�����Υ����ȡ�atom2����pos2�����Υ�������³���롣
	 */
	void unifyAtomArgs(Atom atom1, int pos1, Atom atom2, int pos2) {
		atom1.args[pos1].set(atom2.args[pos2]);
		atom2.args[pos2].set(atom1.args[pos1]);
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
		if (this.isQueued()) {
			return;
		}
		if (!isRoot()) {
			((Membrane)parent).activate();
		}
		((Machine)machine).memStack.push(this);
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
