package compile;

import java.util.*;
import runtime.Functor;

/** �\�[�X�R�[�h���̃A�g���̍\����\���N���X */
final class Atom {
	/** �e�� */
	Membrane mem;
	Functor functor;
	LinkOccurrence[] args;
	Atom(Membrane mem, String name, int arity) {
		this.mem = mem;
		functor = new Functor(name, arity);
		args = new LinkOccurrence[arity];
	}
}

final class Membrane {
	/** �e�� */
	Membrane mem;
	
	//memo:�S��1�̔z��ɓ������@������B
	List atoms;
	/** �q�� */
	List mems;
	List rules;
	List processContexts;
	List ruleContexts;
	List typedProcessContexts;
	
	Membrane(Membrane mem) {
		this.mem = mem;
	}
}

final class LinkOccurrence {
	String name;
	Atom atom;
	int pos;
	/**
	 * ���[�����̂ǂ̏ꏊ�ɏo�����Ă��邩�������B
	 * �萔HEAD��BODY�̂����ꂩ�̒l������B
	 * ���[���O�̏o���̏ꍇ�́A���߂ɂP�x�������s����郋�[���̉E�ӂƂ݂Ȃ��B
	 */
	int place;
	/** 2�񂵂��o�����Ȃ��ꍇ�ɁA�����Е��̏o����ێ����� */
	LinkOccurrence buddy;
	
	/**
	 * �����N�o���𐶐�����B
	 * @param place 
	 *         �萔HEAD��BODY�̂����ꂩ�̒l������B
	 *         ���[���O�̏o���̏ꍇ�́A���߂ɂP�x�������s����郋�[���̉E�ӂƂ݂Ȃ��B
	 */
	LinkOccurrence(String name, Atom atom, int pos, int place) {
		this.name = name;
		this.atom = atom;
		this.pos = pos;
		this.place = place;
	}
	static final int HEAD = 0;
	static final int BODY = 1;
	

	/** ���̏o�����ŏI�����N�̏ꍇ��true��Ԃ� */
	boolean isFunctorRef() { 
		return atom.functor.getArity() == pos;
	}
	/** ���R�����N����� */
	void terminate(Membrane mem) {
		atom = new Atom(null, "*", 1); //todo:���[�g��
		pos = 1;
		atom.args[0] = this;
	}
}
final class RuleStructure {
	Membrane leftMem, rightMem;
}

/** ProcessContext��RuleContext�̐e�ƂȂ钊�ۃN���X */
abstract class Context {
	protected String name;
	protected Context(String name) {
		this.name = name;
	}
	String getName() {
		return name;
	}
	/** ���ӂł̏����� */
	Membrane lhsMem;
	/** �E�ӂł̏������̔z�� */
	List rhsMems;
	/** ���݂̏�ԁBST_�Ŏn�܂�萔�̂����ꂩ�̒l���Ƃ� */
	int status = ST_FRESH;
	/** ������� */
	static final int ST_FRESH = 0;
	/** ���ӂɈ�x�o��������� */
	static final int ST_LHSOK = 1;
	/** ���ӁE�E�ӗ����ɏo��������� */
	static final int ST_READY = 2;
	static final int ST_ERROR = 3;
}
final class ProcessContext extends Context{
	ProcessContext(String name) {
		super(name);
	}
}
final class RuleContext extends Context{
	RuleContext(String name) {
		super(name);
	}
}

/** �Ȃ������ꂪ������javadoc���쐬�ł��Ȃ� */
class DataStructure {}
