package runtime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import util.QueuedEntity;
import util.Stack;
import util.Util;


/**
 * ����쥯�饹���������쥯�饹���쥭��å����̤�����ˤοƥ��饹
 * @author Mizuno
 */
abstract class AbstractMembrane extends QueuedEntity {
	/** ��������������ޥ��� */
	protected AbstractMachine machine;
	/** ���� */
	protected AbstractMembrane mem;
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
	protected AbstractMembrane(AbstractMachine machine, AbstractMembrane mem) {
		this.machine = machine;
		this.mem = mem;
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
	AbstractMembrane getMem() {
		return mem;
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
		return atoms.getAtomCountOfFunctor(Functor.OUTSIDE_PROXY);
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
		mem.removeMem(this);
		dstMem.addMem(this);
		mem = dstMem;
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

	// TODO $p�ΰ�ư�Τ���Υ᥽�åɤλ��ͤ�����
	// ��1��remove/pour�Ǽ�ͳ��󥯴������ȥ���ɲá����
	// ��2����ͳ��󥯴������ȥ��ɲá�����Τ�������ѥ᥽�åɤ��ɲ�
	
	///////////////////
	// ��1
	/**
	 * ���������줫�����롣
	 * ���κݡ�������μ�ͳ��󥯤�������Ƥ��뼫ͳ��󥯴������ȥ��
	 * baseMem���outside_proxy�ޤǺ�����롣
	 * @throws RuntimeException ������ˤ���inside_proxy�ȡ��б�����baseMem��outside_proxy�δ֤�
	 * 					��ͳ��󥯴������ȥ�ʳ��Υ��ȥब��³���Ƥ������
	 */
	void remove(AbstractMembrane baseMem) {
		remove();
		Iterator it = atomIteratorOfFunctor(Functor.INSIDE_PROXY);
		while (it.hasNext()) {
			removeProxyAtoms((Atom)it.next(), baseMem);
		}
	}
	/**
	 * inside0���������Ƥ����󥯤�Ʊ����󥯤�������뼫ͳ��󥯴������ȥ��
	 * baseMem���outside_proxy�ޤǺ�����롣
	 * @throws RuntimeException inside0��baseMem��outside_proxy�δ֤�
	 * 					��ͳ��󥯴������ȥ�ʳ��Υ��ȥब��³���Ƥ������
	 */
	protected void removeProxyAtoms(Atom inside0, AbstractMembrane baseMem) {
		Atom inside;
		Atom outside = inside0.args[0].getAtom();
		outside.remove();
		AbstractMembrane current = mem;
		try {
			while (current != baseMem) {
				if (current == null) {
					//TODO SystemError�Ѥ��㳰���饹���ꤲ��
					throw new RuntimeException("SYSTEM ERROR: baseMem is not ancester");
				}
				//curent�μ�ͳ��󥯤��������proxy�����
				inside = outside.args[1].getAtom();
				outside = inside.args[0].getAtom();
				inside.remove();
				outside.remove();
				current = current.mem;
			}
		} catch (IndexOutOfBoundsException e) {
			//����Ǽ�ͳ��󥯴������ȥ�ʳ��Υ��ȥ����³���Ƥ�������ȯ�������ǽ��������
			//TODO SystemError�Ѥ��㳰���饹���ꤲ��
			throw new RuntimeException("SYSTEM ERROR: inconsistent proxy");
		}
		if (outside.getMem() != current) {
			//TODO SystemError�Ѥ��㳰���饹���ꤲ��
			throw new RuntimeException("SYSTEM ERROR: inconsistent proxy");
		}
		//TODO relinkAtomArgs�ΰ��������ǧ
		current.relinkAtomArgs(inside0, 0, outside, 1);
	}
	
	/**
	 * srcMem�����Ƥ����ư�ư���롣
	 * ���κݡ�baseMem�ޤǤ���˼�ͳ��󥯴������ȥ���ɲä��롣
	 */
	void pour(AbstractMembrane srcMem, AbstractMembrane baseMem) {
		pour(srcMem);
		Iterator it = atomIteratorOfFunctor(Functor.INSIDE_PROXY);
		while (it.hasNext()) {
			addProxyAtoms((Atom)it.next(), baseMem);
		}
	}

	protected void addProxyAtoms(Atom inside0, AbstractMembrane baseMem) {
		AbstractMembrane m = mem;
		Atom inside;
		Atom outside = m.newAtom(Functor.OUTSIDE_PROXY);
		m.newLink(inside0, 0, outside, 0);
		while (m != baseMem) {
			inside = m.newAtom(Functor.INSIDE_PROXY);
			m.newLink(outside, 1, inside, 1);
			m = m.mem;
			outside = m.newAtom(Functor.OUTSIDE_PROXY);
			m.newLink(inside, 0, outside, 0);
		}
		m.relinkAtomArgs(outside, 1, inside0, 0);
	}
	// ��1�����ޤ�
	//////////////////////
    
	/**
	 * srcMem�����Ƥ����ư�ư���롣
	 * ��1�ǤϤ����餯����
	 */
	void pour(AbstractMembrane srcMem) {
		atoms.addAll(srcMem.atoms);
		mems.addAll(srcMem.mems);
	}

	
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
		mem.removeMem(this);
		mem = null;
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
	 *     <li>�������proxy_out�ȿ����proxy_in
	 *     <li>�������proxy_in�Ȼ����proxy_out
	 * </ol>
	 * ��3�̤�ξ�礬���롣
	 */
	void newLink(Atom atom1, int pos1, Atom atom2, int pos2) {
		atom1.args[pos1] = new Link(atom2, pos2);
		atom2.args[pos2] = new Link(atom1, pos1);
	}
	/**
	 * atom1����pos1�����ȡ�atom2����pos2�����Υ�������³���롣
	 */
	void relinkAtomArgs(Atom atom1, int pos1, Atom atom2, int pos2) {
		atom1.args[pos1].set(atom2.args[pos2]);
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
	private Membrane(AbstractMachine machine, AbstractMembrane mem) {
		super(machine, mem);
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
			((Membrane)mem).activate();
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
