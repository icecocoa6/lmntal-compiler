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
 * �A�g���N���X�B���[�J���E�����[�g�Ɋւ�炸���̃N���X�̃C���X�^���X���g�p����B
 * @author Mizuno
 */
class Atom extends QueuedEntity {
	/** �e���BMembrane�N���X��addAtom���\�b�h���ōX�V����B */
	private AbstractMembrane mem;
	/** ���O */
	private Functor functor;
	/** �����N */
	Link[] args;
	
	private static int lastId = 0;
	private int id;
	
	///////////////////////////////
	// �R���X�g���N�^

	/**
	 * �w�肳�ꂽ���O�ƃ����N�������A�g�����쐬����B
	 * AbstractMembrane��newAtom���\�b�h���ŌĂ΂��B
	 * @param mem �e��
	 * @param name �A�g���̖��O
	 * @param arity �����N��
	 */
	Atom(AbstractMembrane mem, String name, int arity) {
		this.mem = mem;
		functor = new Functor(name, arity);
		args = new Link[arity];
		id = lastId++;
	}

	///////////////////////////////
	// ���̎擾

	public String toString() {
		return functor.getName();
	}
	/**
	 * �f�t�H���g�̎������Ə����n�̓�����Ԃ��ς��ƕς���Ă��܂��̂ŁA
	 * �C���X�^���X���ƂɃ��j�[�N��id��p�ӂ��ăn�b�V���R�[�h�Ƃ��ė��p����B
	 */
	public int hashCode() {
		return id;
	}
	/** ���O�̎擾 */
	Functor getFunctor(){
		return functor;
	}
	String getName() {
		return functor.getName();
	}
	/** �����N�����擾 */
	int getArity() {
		return functor.getArity();
	}
	Link getLastArg() {
		return args[getArity() - 1];
	}
	AbstractMembrane getMem() {
		return mem;
	}
}

/**
 * ���[�J�����N���X�B���s���́A���v�Z�m�[�h���ɂ��閌��\���B
 * @author Mizuno
 */
final class Membrane extends AbstractMembrane {
	/** ���s�X�^�b�N */
	private Stack ready = new Stack();
	/**
	 * �w�肳�ꂽ�}�V���ɏ������閌���쐬����B
	 * newMem���\�b�h���ŌĂ΂��B
	 */
	private Membrane(AbstractMachine machine, AbstractMembrane mem) {
		super(machine, mem);
	}
	/**
	 * �w�肳�ꂽ�}�V���̃��[�g�����쐬����B
	 */
	Membrane(Machine machine) {
		super(machine, null);
	}

	///////////////////////////////
	// ����

	/** ���s�X�^�b�N�̐擪�̃A�g�����擾���A���s�X�^�b�N���珜�� */
	Atom popReadyAtom() {
		return (Atom)ready.pop();
	}
	/** ���̊����� */
	void activate() {
		if (this.isQueued()) {
			return;
		}
		if (!isRoot()) {
			((Membrane)mem).activate();
		}
		((Machine)machine).memStack.push(this);
	}
	protected void enqueueAtom(Atom atom) {
		ready.push(atom);
	}
	/** 
	 * �ړ����ꂽ��A�A�N�e�B�u�A�g�������s�X�^�b�N�ɓ���邽�߂ɌĂяo�����B
	 */
//	protected void movedTo(AbstractMachine machine, AbstractMembrane dstMem) {
	protected void enqueueAllAtoms() {
		Iterator i = atoms.functorIterator();
		while (i.hasNext()) {
			Functor f = (Functor)i.next();
			if (true) { // f ���A�N�e�B�u�̏ꍇ
				Iterator i2 = atoms.iteratorOfFunctor(f);
				while (i2.hasNext()) {
					ready.push((Atom)i2.next());
				}
			}
		}
	}
	/**
	 * �q���𐶐�����B
	 */
	AbstractMembrane newMem() {
		Membrane m = new Membrane(machine, this);
		mems.add(m);
		return m;
	}
}
/**
 * ���ۖ��N���X�B���[�J�����N���X�Ɩ��L���b�V���i�������j�̐e�N���X
 * @author Mizuno
 */
abstract class AbstractMembrane extends QueuedEntity {
	/** ���̖����Ǘ�����}�V�� */
	protected AbstractMachine machine;
	/** �e�� */
	protected AbstractMembrane mem;
	/** �A�g���̏W�� */
	protected AtomSet atoms = new AtomSet();;
	/** �q���̏W�� */
	protected Set mems = new HashSet();
	/** ���̖��ɂ���proxy�ȊO�̃A�g���̐��B */
	protected int atomCount = 0;
	/** ���̃Z���̎��R�����N�̐� */
	protected int freeLinkCount = 0;
	/** ���[���Z�b�g�̏W���B */
	protected List rulesets = new ArrayList();
	/** ���̖��ȉ��ɓK�p�ł��郋�[���������Ƃ���true */
	protected boolean stable = false;
	/** ���b�N����Ă��鎞��true */
	protected boolean locked = false;
//	/** �Ō�Ƀ��b�N�����v�Z�m�[�h */
//	protected CalcNode lastLockNode;

	private static int lastId = 0;
	private int id;
	
	///////////////////////////////
	// �R���X�g���N�^

	/**
	 * �w�肳�ꂽ�}�V���ɏ������閌���쐬����B
	 */
	protected AbstractMembrane(AbstractMachine machine, AbstractMembrane mem) {
		this.machine = machine;
		this.mem = mem;
		id = lastId++;
	}

	///////////////////////////////
	// ���̎擾

	/**
	 * �f�t�H���g�̎������Ə����n�̓�����Ԃ��ς��ƕς���Ă��܂��̂ŁA
	 * �C���X�^���X���ƂɃ��j�[�N��id��p�ӂ��ăn�b�V���R�[�h�Ƃ��ė��p����B
	 */
	public int hashCode() {
		return id;
	}

	/** ���̖����Ǘ�����}�V���̎擾 */
	AbstractMachine getMachine() {
		return machine;
	}
	/** �e���̎擾 */
	AbstractMembrane getMem() {
		return mem;
	}
	int getMemCount() {
		return mems.size();
	}
	/** proxy�ȊO�̃A�g���̐����擾 */
	int getAtomCount() {
		return atomCount;
	}
	/** ���̃Z���̎��R�����N�̐����擾 */
	int getFreeLinkCount() {
		return freeLinkCount;
	}
	/** ���̖��Ƃ��̎q���ɓK�p�ł��郋�[�����Ȃ��ꍇ��true */
	boolean isStable() {
		return stable;
	}
	/** ���̖��Ƀ��[���������true */
	boolean hasRule() {
		return rulesets.size() > 0;
	}
	boolean isRoot() {
		return machine.getRoot() == this;
	}
	/** ���̖��ɂ���A�g���̔����q���擾���� */
	Iterator atomIterator() {
		return atoms.iterator();
	}
	/** ���̖��ɂ���q���̔����q���擾���� */
	Iterator memIterator() {
		return mems.iterator();
	}
	/** ���Ofunc�����A�g���̔����q���擾���� */
	Iterator atomIteratorOfFuncor(Functor functor) {
		return atoms.iteratorOfFunctor(functor);
	}
	/** ���̖��ɂ��郋�[���Z�b�g�̔����q��Ԃ� */
	Iterator rulesetIterator() {
		return rulesets.iterator();
	}


	///////////////////////////////
	// ����

	/** ���[����S�ăN���A���� */
	void clearRules() {
		rulesets.clear();
	}

	/** srcMem�ɂ��郋�[�������̖��ɃR�s�[����B */
	void inheritRules(AbstractMembrane srcMem) {
		rulesets.addAll(srcMem.rulesets);
	}
	/** ���[���Z�b�g��ǉ� */
	void loadRuleset(Ruleset srcRuleset) {
		rulesets.add(srcRuleset);
	}
	/** �A�g���̒ǉ� */
	Atom newAtom(String name, int arity) {
		Atom a = new Atom(this, name, arity);
		atoms.add(a);
		enqueueAtom(a);
		atomCount++;
		return a;
	}
	/** �w�肳�ꂽ�A�g�������s�X�^�b�N�ɐς� */
	abstract protected void enqueueAtom(Atom atom);
//	/** ���̒ǉ� */
	abstract AbstractMembrane newMem();

//	�p�~�BnewAtom/newMem���g�p����B
// 	/** �A�g���̒ǉ��B�A�N�e�B�u�A�g���̏ꍇ�ɂ͎��s�X�^�b�N�ɒǉ�����B */
//	void addAtom(Atom atom) {
//		atoms.add(atom);
//		activateAtom(atom);
//	}
	/** ���̒ǉ� */
	private void addMem(AbstractMembrane mem) {
		mems.add(mem);
	}
	/** dstMem�Ɉړ� */
	void moveTo(AbstractMembrane dstMem) {
		mem.removeMem(this);
		dstMem.addMem(this);
		mem = dstMem;
//		movedTo(machine, dstMem);
		enqueueAllAtoms();
	}
	/** �ړ����ꂽ��A�A�N�e�B�u�A�g�������s�X�^�b�N�ɓ���邽�߂ɌĂяo����� */
//	protected void movedTo(AbstractMachine machine, AbstractMembrane dstMem) {
	abstract protected void enqueueAllAtoms();
	
	/** srcMem�̓��e��S�Ĉړ����� */
	void pour(AbstractMembrane srcMem) {
		atoms.addAll(srcMem.atoms);
		mems.addAll(srcMem.mems);
		//���[���̈ړ��H
	}
	
	/** �w�肳�ꂽ�A�g�������̖����珜������B */
	void removeAtom(Atom atom) {
		atoms.remove(atom);
		atomCount--;
		if (atomCount < 0) {
			Util.systemError("Membrane.atomCount is pisitive value");
		}
	}
	/** �w�肳�ꂽ�������̖����珜������ */
	void removeMem(AbstractMembrane mem) {
		mems.remove(mem);
	}
	
	/**
	 * ���̖������b�N����
	 * @param mem ���[���̂��閌
	 * @return ���b�N�ɐ��������ꍇ��true
	 */
	boolean lock(AbstractMembrane mem) {
		if (locked) {
			//todo:�L���[�ɋL�^
			return false;
		} else {
			//todo:�v�Z�m�[�h�̋L�^�A�L���b�V���̍X�V
			locked = true;
			return true;
		}
	}
	/**
	 * ���̖��Ƃ��̎q�����ċA�I�Ƀ��b�N����
	 * @param mem ���[���̂��閌
	 * @return ���b�N�ɐ��������ꍇ��true
	 */
	boolean recursiveLock(AbstractMembrane mem) {
		return false;
	}
	
//	/** ���̖��̕����𐶐����� */
//	Membrane copy() {
//		
//	}
	
	/** ���b�N���������� */
	void unlock() {
		
	}
	void recursiveUnlock() {
	}
	
	///////////////////////
	// �����N�̑���
	/**
	 * atom1�̑�pos1�����ƁAatom2�̑�2������ڑ�����B
	 */
	void newLink(Atom atom1, int pos1, Atom atom2, int pos2) {
		atom1.args[pos1] = new Link(atom2, pos2);
		atom2.args[pos2] = new Link(atom1, pos1);
	}
	/**
	 * atom1�̑�pos1�����ƁAatom2�̑�2�����̃����N���ڑ�����B
	 */
	void relinkAtomArg(Atom atom1, int pos1, Atom atom2, int pos2) {
		atom1.args[pos1].set(atom2.args[pos2]);
		atom2.args[pos2].set(atom1, pos1);
	}
	/**
	 * atom1�̑�pos1�����̃����N��ƁAatom2�̑�2�����̃����N���ڑ�����B
	 */
	void unifyLink(Atom atom1, int pos1, Atom atom2, int pos2) {
		atom1.args[pos1].set(atom2.args[pos2]);
		atom2.args[pos2].set(atom1.args[pos1]);
	}
}


/**
 * �����N�̐ڑ�����A�A�g���ƈ����ԍ��̑g�Ƃ��ĕ\���BLMNtal�̃����N�ɂ͕����������̂ŁA
 * �P�̃����N�ɑ΂��Ă��̃N���X�̃C���X�^���X���Q�g�p����B
 */
final class Link {
	/** �����N��̃A�g�� */
	private Atom atom;
	/** �����N�悪�扽������ */
	private int pos;

	private static int lastId = 0;
	private int id;
	///////////////////////////////
	// �R���X�g���N�^
	
	Link(Atom atom, int pos) {
		set(atom, pos);
		id = lastId++;
	}

	///////////////////////////////
	// ���̎擾

	/** �΂ɂȂ�Q�̃����N��id�̂����A�Ⴂ���������N�̔ԍ��Ƃ��Ďg�p����B */
	public String toString() {
		int i;
		if (this.id < atom.args[pos].id) {
			i = this.id;
		} else {
			i = atom.args[pos].id;
		}
		return "_" + i;
	}
				
	/** �����N��̃A�g�����擾���� */
	Atom getAtom() {
		return atom;
	}
	/** �����N��̈����ԍ����擾���� */
	int getPos() {
		return pos;
	}
	/** ���̃����N�Ƒ΂��Ȃ��t�����̃����N���擾���� */
	Link getBuddy() {
		return atom.args[pos];
	}
	/** �����N�悪�ŏI�����N�̏ꍇ��true��Ԃ� */
	boolean isFuncRef() {
		return atom.getArity() - 1 == pos;
	}

	///////////////////////////////
	// ����
	/**
	 * �ڑ����ݒ肷��B
	 * ���N���X�̃����N����p���\�b�h���ł̂݌Ăяo�����B
	 */
	void set(Atom atom, int pos) {
		this.atom = atom;
		this.pos = pos;
	}
	/**
	 * ���̃����N�̐ڑ�����A�^����ꂽ�����N�̐ڑ���Ɠ����ɂ���B
	 * ���N���X�̃����N����p���\�b�h���ł̂݌Ăяo�����B
	 */
	void set(Link link) {
		this.atom = link.atom;
		this.pos = link.pos;
	}
}

final class Machine extends AbstractMachine {
	/** ���s���X�^�b�N */
	Stack memStack = new Stack();
	Machine() {
		root = new Membrane(this);
		memStack.push(root);
	}
	
	void exec() {
		
	}
}
abstract class AbstractMachine {
	/** ���[�g�� */
	protected AbstractMembrane root;
	/** ���[�g���̎擾 */
	AbstractMembrane getRoot() {
		return root;
	}
}

/** �v�Z�m�[�h */
final class LMNtalRuntime {
	List machines = new ArrayList();
	void exec() {
	}
	Machine newMachine() {
		return new Machine();
	}
}

/** �Ȃ������ꂪ������javadoc���쐬�ł��Ȃ� */
class DataStructure {}
