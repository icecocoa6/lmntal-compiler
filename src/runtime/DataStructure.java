package runtime;

import java.util.*;

import util.*;
import runtime.stack.Stack;
import runtime.stack.QueuedEntity;

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
	///////////////////////////////
	// �R���X�g���N�^

	/**
	 * �w�肳�ꂽ���O�ƃ����N�������A�g�����쐬����B
	 * �e���̏�Ԃ͖���B���ۂɎg�p����O�ɐe����mem�ϐ��ɖ����I�ɑ������K�v������B
	 * @param mem �e��
	 * @param name �A�g���̖��O
	 * @param arity �����N��
	 */
	Atom(AbstractMembrane mem, String name, int arity) {
		this.mem = mem;
		functor = new Functor(name, arity);
		args = new Link[arity];
	}

	///////////////////////////////
	// ���̎擾

	/** ���O�̎擾 */
	Functor getFunctor(){
		return functor;
	}
	String getName() {
		return functor.getName();
	}
	public String toString() {
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
	 */
	Membrane(AbstractMachine machine, AbstractMembrane mem) {
		super(machine, mem);
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
	protected void activateAtom(Atom atom) {
		ready.push(atom);
	}
	/** 
	 * �ړ����ꂽ��A�A�N�e�B�u�A�g�������s�X�^�b�N�ɓ���邽�߂ɌĂяo�����B
	 * AbstractMembrane�N���X�Ő錾����Ă��钊�ۃ��\�b�h�̎����ł��B
	 */
//	protected void movedTo(AbstractMachine machine, AbstractMembrane dstMem) {
	protected void activateAllAtoms() {
		Iterator i = atoms.functorIterator();
		while (i.hasNext()) {
			Functor f = (Functor)i.next();
			if (true) { // f ���A�N�e�B�u�̏ꍇ
				ready.pushAll((Set)atoms.getAtomsOfFunctor(f));
			}
		}
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
	protected int natom = 0;
	/** ���[���Z�b�g�̏W���B */
	protected List rulesets = new ArrayList();
	/** ���̖��ȉ��ɓK�p�ł��郋�[���������Ƃ���true */
	boolean stable = false;
	/** ���b�N����Ă��鎞��true */
	protected boolean locked = false;
//	/** �Ō�Ƀ��b�N�����v�Z�m�[�h */
//	protected CalcNode lastLockNode;

	///////////////////////////////
	// �R���X�g���N�^

	/**
	 * �w�肳�ꂽ�}�V���ɏ������閌���쐬����B
	 */
	AbstractMembrane(AbstractMachine machine, AbstractMembrane mem) {
		this.machine = machine;
		this.mem = mem;
	}

	///////////////////////////////
	// ���̎擾

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
		return natom;
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
	/** �A�g���̒ǉ��B�A�N�e�B�u�A�g���̏ꍇ�ɂ͎��s�X�^�b�N�ɒǉ�����B */
	void addAtom(Atom atom) {
		atoms.add(atom);
		activateAtom(atom);
	}
	abstract protected void activateAtom(Atom atom);
	
	/** ���̒ǉ� */
	void addMem(AbstractMembrane mem) {
		mems.add(mem);
	}
	/** dstMem�Ɉړ� */
	void moveTo(AbstractMembrane dstMem) {
		mem.removeMem(this);
		dstMem.addMem(this);
		mem = dstMem;
//		movedTo(machine, dstMem);
		activateAllAtoms();
	}
	/** �ړ����ꂽ��A�A�N�e�B�u�A�g�������s�X�^�b�N�ɓ���邽�߂ɌĂяo����� */
//	protected void movedTo(AbstractMachine machine, AbstractMembrane dstMem) {
	abstract protected void activateAllAtoms();
	
	/** srcMem�̓��e��S�Ĉړ����� */
	void pour(AbstractMembrane srcMem) {
		atoms.addAll(srcMem.atoms);
		mems.addAll(srcMem.mems);
		//���[���̈ړ��H
	}
	
	/** �w�肳�ꂽ�A�g�������̖����珜������B */
	void removeAtom(Atom atom) {
		atoms.remove(atom);
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
		root = new Membrane(this, null);
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
