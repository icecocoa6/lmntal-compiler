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
 * ���ȥ९�饹�������롦��⡼�Ȥ˴ؤ�餺���Υ��饹�Υ��󥹥��󥹤���Ѥ��롣
 * @author Mizuno
 */
class Atom extends QueuedEntity {
	/** ���졣Membrane���饹��addAtom�᥽�å���ǹ������롣 */
	private AbstractMembrane mem;
	/** ̾�� */
	private Functor functor;
	/** ��� */
	Link[] args;
	
	private static int lastId = 0;
	private int id;
	
	///////////////////////////////
	// ���󥹥ȥ饯��

	/**
	 * ���ꤵ�줿̾���ȥ�󥯿�����ĥ��ȥ��������롣
	 * AbstractMembrane��newAtom�᥽�å���ǸƤФ�롣
	 * @param mem ����
	 */
	Atom(AbstractMembrane mem, Functor functor) {
		this.mem = mem;
		this.functor = functor;
		args = new Link[functor.getArity()];
		id = lastId++;
	}

	///////////////////////////////
	// ����μ���

	public String toString() {
		return functor.getName();
	}
	/**
	 * �ǥե���Ȥμ������Ƚ����Ϥ��������֤��Ѥ����Ѥ�äƤ��ޤ��Τǡ�
	 * ���󥹥��󥹤��Ȥ˥�ˡ�����id���Ѱդ��ƥϥå��女���ɤȤ������Ѥ��롣
	 */
	public int hashCode() {
		return id;
	}
	/** ̾���μ��� */
	Functor getFunctor(){
		return functor;
	}
	String getName() {
		return functor.getName();
	}
	/** ��󥯿������ */
	int getArity() {
		return functor.getArity();
	}
	Link getLastArg() {
		return args[getArity() - 1];
	}
	AbstractMembrane getMem() {
		return mem;
	}
	
	void remove() {
		mem.removeAtom(this);
		mem = null;
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
	protected int atomCount = 0;
	/** ���Υ���μ�ͳ��󥯤ο� */
	protected int freeLinkCount = 0;
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
		return atomCount;
	}
	/** ���Υ���μ�ͳ��󥯤ο������ */
	int getFreeLinkCount() {
		return freeLinkCount;
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
		atomCount++;
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
// 	/** ���ȥ���ɲá������ƥ��֥��ȥ�ξ��ˤϼ¹ԥ����å����ɲä��롣 */
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
	 * ���κݡ�baseMem�ޤǤ���ˤ��뼫ͳ��󥯴������ȥ�����롣
	 */
	void remove(Membrane baseMem) {
		remove();
		Iterator it = atomIteratorOfFunctor(Functor.INSIDE_PROXY);
		while (it.hasNext()) {
			Atom outside = ((Atom)it.next()).args[0].getAtom();
			Atom inside;
			while (outside.getMem() != baseMem) {
				inside = outside.args[1].getAtom();
				outside = inside.args[0].getAtom();
				inside.remove();
				outside.remove();
			}
		}
	}
	
	/**
	 * srcMem�����Ƥ����ư�ư���롣
	 * ���κݡ�baseMem�ޤǤ���˼�ͳ��󥯴������ȥ���ɲä��롣
	 */
	void pour(AbstractMembrane srcMem, Membrane baseMem) {
		pour(srcMem);
		Iterator it = atomIteratorOfFunctor(Functor.INSIDE_PROXY);
		while (it.hasNext()) {
			AbstractMembrane m = mem;
			Atom inside = (Atom)it.next();
			Atom outside = m.newAtom(Functor.OUTSIDE_PROXY);
			m.newLink(inside, 0, outside, 0);
			while (m != baseMem) {
				inside = m.newAtom(Functor.INSIDE_PROXY);
				m.newLink(outside, 1, inside, 1);
				m = m.mem;
				outside = m.newAtom(Functor.OUTSIDE_PROXY);
				m.newLink(inside, 0, outside, 0);
			}
		}
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
		atomCount--;
		if (atomCount < 0) {
			Util.systemError("Membrane.atomCount is pisitive value");
		}
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
 * ��󥯤���³��򡢥��ȥ�Ȱ����ֹ���ȤȤ���ɽ����LMNtal�Υ�󥯤ˤ�������̵���Τǡ�
 * ���ĤΥ�󥯤��Ф��Ƥ��Υ��饹�Υ��󥹥��󥹤򣲤Ļ��Ѥ��롣
 */
final class Link {
	/** �����Υ��ȥ� */
	private Atom atom;
	/** ����褬�貿������ */
	private int pos;

	private static int lastId = 0;
	private int id;
	///////////////////////////////
	// ���󥹥ȥ饯��
	
	Link(Atom atom, int pos) {
		set(atom, pos);
		id = lastId++;
	}

	///////////////////////////////
	// ����μ���

	/** �Фˤʤ룲�ĤΥ�󥯤�id�Τ������㤤�����󥯤��ֹ�Ȥ��ƻ��Ѥ��롣 */
	public String toString() {
		int i;
		if (this.id < atom.args[pos].id) {
			i = this.id;
		} else {
			i = atom.args[pos].id;
		}
		return "_" + i;
	}
				
	/** �����Υ��ȥ��������� */
	Atom getAtom() {
		return atom;
	}
	/** �����ΰ����ֹ��������� */
	int getPos() {
		return pos;
	}
	/** ���Υ�󥯤��Ф�ʤ��ո����Υ�󥯤�������� */
	Link getBuddy() {
		return atom.args[pos];
	}
	/** ����褬�ǽ���󥯤ξ���true���֤� */
	boolean isFuncRef() {
		return atom.getArity() - 1 == pos;
	}

	///////////////////////////////
	// ���
	/**
	 * ��³������ꤹ�롣
	 * �쥯�饹�Υ������ѥ᥽�å���ǤΤ߸ƤӽФ���롣
	 */
	void set(Atom atom, int pos) {
		this.atom = atom;
		this.pos = pos;
	}
	/**
	 * ���Υ�󥯤���³���Ϳ����줿��󥯤���³���Ʊ���ˤ��롣
	 * �쥯�饹�Υ������ѥ᥽�å���ǤΤ߸ƤӽФ���롣
	 */
	void set(Link link) {
		this.atom = link.atom;
		this.pos = link.pos;
	}
}

final class Machine extends AbstractMachine {
	/** �¹��쥹���å� */
	Stack memStack = new Stack();
	boolean idle;
	static final int maxLoop = 10;
	
	Machine() {
		root = new Membrane(this);
		memStack.push(root);
		idle = false;
	}
	
	boolean isIdle(){
		return idle;
	}
	void exec() {
		if(memStack.isEmpty()){ // ���ʤ�idle�ˤ��롣
			idle = true;
			return;
		}
		// �¹��쥹���å������Ǥʤ�
		Membrane mem = (Membrane)memStack.peek();
		if(!mem.lock(mem)) return; // ��å�����
		
		Atom a;
		for(int i=0; i < maxLoop && mem == memStack.peek(); i++){
			// ���줬�Ѥ��ʤ��� & �롼�ײ����ۤ��ʤ���
			
			a = mem.popReadyAtom();
			Iterator it = mem.rulesetIterator();
			boolean flag;
			if(a != null){ // �¹��쥹���å������Ǥʤ��Ȥ�
				flag = false;
				while(it.hasNext()){ // ����Τ�ĥ롼���a��Ŭ��
					if(((Ruleset)it.next()).react(mem, a)) flag = true;
				}
				if(flag == false){ // �롼�뤬Ŭ�ѤǤ��ʤ��ä���
					if(!mem.isRoot()) mem.getMem().enqueueAtom(a);
				}
				else {}// �����ƥॳ���륢�ȥ�ʤ����ˤĤߡ�����������
			}else{ // �¹��쥹���å������λ�
				flag = false;
				while(it.hasNext()){ // ���Ƴ�ƥ��Ȥ�Ԥ�
					if(((Ruleset)it.next()).react(mem)) flag = true;
				}
				if(flag == false){ // �롼�뤬Ŭ�ѤǤ��ʤ��ä���
					memStack.pop(); // �����pop
					// ���줬root�줫�Ŀ������Ĥʤ顢����������
					if(mem.isRoot() && mem.getMem() != null)
						((Membrane)mem.getMem()).activate();
					it = mem.memIterator();
					// ���줬����stable�ʤ顢�������stable�ˤ��롣
					flag = false;
					while(it.hasNext()){
						if(((Membrane)it.next()).isStable() == false)
								flag = true;
					}
					if(flag == false) mem.toStable();
				}
			}
		}
		// ���줬�Ѥ�ä�or�����������֤����顢��å���������ƽ�λ
		mem.unlock();
	}
}
abstract class AbstractMachine {
	/** �롼���� */
	protected AbstractMembrane root;
	
	/** �롼����μ��� */
	AbstractMembrane getRoot() {
		return root;
	}
}

/** �׻��Ρ��� */
final class LMNtalRuntime {
	List machines = new ArrayList();
	
	/** �׻��Ρ��ɤ����ĥޥ������Ƥ�idle�ˤʤ�ޤǼ¹ԡ�<br>
	 *  machines���Ѥޤ줿��˼¹Ԥ��롣�ƥޥ���ͥ��ˤ��뤿��ˤ�
	 *  �ޥ����ڹ�¤�ˤʤäƤ��ʤ��Ƚ���ʤ���ͥ���٤Ϥ��Ф餯̤������
	 */
	void exec() {
		boolean allIdle;
		Iterator it;
		Machine m;
		do{
			allIdle = true; // idle�Ǥʤ��ޥ��󤬸��Ĥ��ä���false�ˤʤ롣
			it = machines.iterator();
			while(it.hasNext()){
				m = (Machine)it.next();
				if(!m.isIdle()){ // idle�Ǥʤ��ޥ��󤬤��ä���
					m.exec(); // �ҤȤ�����¹�
					allIdle = false; // idle�Ǥʤ��ޥ��󤬤���
					break;
				}
			}
		}while(!allIdle);
	}
	
	Machine newMachine() {
		Machine m = new Machine();
		machines.add(m);
		return m;
	}
}

/** �ʤ������줬̵����javadoc������Ǥ��ʤ� */
class DataStructure {}
