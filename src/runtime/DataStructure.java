package runtime;

import java.util.*;

import util.*;
import runtime.stack.Stack;
import runtime.stack.QueuedEntity;

/**
 * ���[�J���A�g���N���X�B���s���́A���v�Z�m�[�h���ɂ���A�g����\���B
 * @author Mizuno
 */
final class Atom extends AbstractAtom {
	/**
	 * �w�肳�ꂽ���O�ƃ����N�������A�g�����쐬����B
	 * �e���̏�Ԃ͖���B���ۂɎg�p����O�ɐe����mem�ϐ��ɖ����I�ɑ������K�v������B
	 * @param name �A�g���̖��O
	 * @param arity �����N��
	 */
	Atom(String name, int arity) {
		super(name, arity);
	}
//	/**
//	 * �w�肳�ꂽ���O�ƃ����N�������A�g�����쐬���A�w�肳�ꂽ�e����ݒ肷��B
//	 * @param mem ������
//	 * @param name �A�g���̖��O
//	 * @param arity �����N��
//	 */
//	Atom(Membrane mem, String name, int arity) {
//		super((AbstractMembrane)mem, name, arity);
//	}
}

/**
 * ���ۃA�g���N���X�B���[�J���A�g���N���X�ƃA�g���L���b�V���i�������j�̐e�N���X
 * @author Mizuno
 */
class AbstractAtom extends QueuedEntity {
	/** ������ */
	AbstractMembrane mem;
	/** ���O */
	private Functor functor;
	/** �����N */
	Link[] args;
	///////////////////////////////
	// �R���X�g���N�^

	/**
	 * �w�肳�ꂽ���O�ƃ����N�������A�g�����쐬����B
	 * �e���̏�Ԃ͖���B���ۂɎg�p����O�ɐe����mem�ϐ��ɖ����I�ɑ������K�v������B
	 * @param name �A�g���̖��O
	 * @param arity �����N��
	 */
	AbstractAtom(String name, int arity) {
		functor = new Functor(name, arity);
		mem = null;
	}
//	/**
//	 * �w�肳�ꂽ���O�ƃ����N�������A�g�����쐬���A�w�肳�ꂽ�e����ݒ肷��B
//	 * @param mem ������
//	 * @param name �A�g���̖��O
//	 * @param arity �����N��
//	 */
//	AbstractAtom(AbstractMembrane mem, String name, int arity) {
//		functor = new Functor(name, arity);
//		this.mem = mem;
//	}

	///////////////////////////////
	// ���̎擾

	/** ���O�̎擾 */
	Functor getFunctor(){
		return functor;
	}
	/** �����N�����擾 */
	int getArity() {
		return functor.getArity();
	}
}

/**
 * ���[�J�����N���X�B���s���́A���v�Z�m�[�h���ɂ��閌��\���B
 * @author Mizuno
 */
final class Membrane extends AbstractMembrane {
	/**
	 * �w�肳�ꂽ�}�V���ɏ������閌���쐬����B
	 */
	Membrane(AbstractMachine machine) {
		super(machine);
	}
}
/**
 * ���ۖ��N���X�B���[�J�����N���X�Ɩ��L���b�V���i�������j�̐e�N���X
 * @author Mizuno
 */
class AbstractMembrane extends QueuedEntity {
	/** ���̖����Ǘ�����}�V�� */
	private AbstractMachine machine;
	/** �e�� */
	private AbstractMembrane mem;
	/** �A�g���̏W�� */
	private AtomSet atoms;
	/** �q���̏W�� */
	private Set mems;
	/** ���̖��ɂ���proxy�ȊO�̃A�g���̐��B */
	private int natom;
	/** ���s�X�^�b�N */
	private Stack ready;
	/** ���[���Z�b�g�̏W���B */
	private List rulesets;
	/** ���̖��ȉ��ɓK�p�ł��郋�[���������Ƃ���true */
	boolean stable;
	/** ���b�N����Ă��鎞��true */
	private boolean locked = false;
//	/** �Ō�Ƀ��b�N�����v�Z�m�[�h */
//	private CalcNode lastLockNode;

	///////////////////////////////
	// �R���X�g���N�^

	/**
	 * �w�肳�ꂽ�}�V���ɏ������閌���쐬����B
	 */
	AbstractMembrane(AbstractMachine machine) {
		this.machine = machine;
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

	/** ���s�X�^�b�N�̐擪�̃A�g�����擾���A���s�X�^�b�N���珜�� */
	AbstractAtom popReadyAtom() {
		return (AbstractAtom)ready.pop();
	}

	/** ���̊����� */
	void activate() {
		if (this.isQueued()) {
			return;
		}
		mem.activate();
		machine.memStack.push(this);
	}
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
	void addAtom(AbstractAtom atom) {
		atoms.add(atom);
		if (true) { //�A�N�e�B�u�̏ꍇ
			ready.push(atom);
		}
	}
	/** ���̒ǉ� */
	void addMem(AbstractMembrane mem) {
		mems.add(mem);
	}
	/** dstMem�Ɉړ� */
	void moveTo(AbstractMembrane dstMem) {
		mem.removeMem(this);
		dstMem.addMem(this);
		mem = dstMem;
		movedTo(machine, dstMem); //?
	}
	/** �ړ����ꂽ��A�A�N�e�B�u�A�g�������s�X�^�b�N�ɓ���邽�߂ɌĂяo����� */
	private void movedTo(Machine machine, AbstractMembrane dstMem) {
		Iterator i = atoms.functorIterator();
		while (i.hasNext()) {
			Functor f = (Functor)i.next();
			if (true) { // f ���A�N�e�B�u�̏ꍇ
				ready.pushAll((Set)atoms.getAtomsOfFunctor(f));
			}
		}
	}
	/** srcMem�̓��e��S�Ĉړ����� */
	void pour(AbstractMembrane srcMem) {
		atoms.addAll(srcMem.atoms);
		mems.addAll(srcMem.mems);
		//���[���̈ړ��H
	}
	
	/** �w�肳�ꂽ�A�g�������̖����珜������B */
	void removeAtom(AbstractAtom atom) {
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
	
}


/**
 * �����N<br>
 * �����N�̐ڑ�����A�A�g���ƈ����ԍ��̑g�Ƃ��ĕ\���BLMNtal�̃����N�ɂ͕������������߁A
 * �P�̃����N�ɑ΂��Ă��̃N���X�̃C���X�^���X���Q�g���邱�ƂɂȂ�B
 */
final class Link {
	/** �����N��̃A�g�� */
	private AbstractAtom atom;
	/** �����N�悪�扽������ */
	private int pos;
	
	///////////////////////////////
	// �R���X�g���N�^
	
	Link(AbstractAtom atom, int pos) {
		set(atom, pos);
	}

	///////////////////////////////
	// ���̎擾
		
	/** �����N��̃A�g�����擾���� */
	AbstractAtom getAtom() {
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
		return atom.getArity() == pos;
	}

	///////////////////////////////
	// ����
	
	/** �ڑ����ݒ肷�� */
	void set(AbstractAtom atom, int pos) {
		this.atom = atom;
		this.pos = pos;
	}
	/** ���̃����N�̃����N����Alink�̃����N��ɐݒ肷�� */
	void relink(Link link) {
		this.atom = link.atom;
		this.pos = link.pos;
	}
}

final class Machine extends AbstractMachine {
	/** ���s���X�^�b�N */
	Stack memStack;
	/** ���[�g�� */
	private Membrane root;
	void exec() {
		
	}
}
abstract class AbstractMachine {
}

/** �v�Z�m�[�h */
final class LMNtalRuntime {
	List machines;
	void exec() {
		Atom a = new Atom("a", 1);
		a.dequeue();
	}
	Machine newMachine() {
		return new Machine();
	}
}

/** �Ȃ������ꂪ������javadoc���쐬�ł��Ȃ� */
class DataStructure {}
