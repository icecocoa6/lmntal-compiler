package compile;

import java.util.List;

import runtime.Functor;

/** ��������������Υ��ȥ�ι�¤��ɽ�����饹 */
final class Atom {
	/** ���� */
	Membrane mem;
	Functor functor;
	LinkOccurrence[] args;
	Atom(Membrane mem, String name, int arity) {
		this.mem = mem;
		functor = new Functor(name, arity);
		args = new LinkOccurrence[arity];
	}
}

/** �����������������ι�¤��ɽ�����饹 */
final class Membrane {
	/** ���� */
	Membrane mem;
	
	//memo:����1�Ĥ�������������ˡ�⤢�롣
	List atoms;
	/** ���� */
	List mems;
	List rules;
	List processContexts;
	List ruleContexts;
	List typedProcessContexts;
	
	Membrane(Membrane mem) {
		this.mem = mem;
	}
}

/** ��������������Υ�󥯤γƽи���ɽ�����饹 */
final class LinkOccurrence {
	String name;
	Atom atom;
	int pos;
	/**
	 * �롼����Τɤξ��˽и����Ƥ��뤫�򸽤���
	 * ���HEAD��BODY�Τ����줫���ͤ����롣
	 * �롼�볰�νи��ξ��ϡ����ˣ��٤����¹Ԥ����롼��α��դȤߤʤ���
	 */
	int place;
	/** 2�󤷤��и����ʤ����ˡ��⤦�����νи����ݻ����� */
	LinkOccurrence buddy;
	
	/**
	 * ��󥯽и����������롣
	 * @param place 
	 *         ���HEAD��BODY�Τ����줫���ͤ����롣
	 *         �롼�볰�νи��ξ��ϡ����ˣ��٤����¹Ԥ����롼��α��դȤߤʤ���
	 */
	LinkOccurrence(String name, Atom atom, int pos, int place) {
		this.name = name;
		this.atom = atom;
		this.pos = pos;
		this.place = place;
	}
	static final int HEAD = 0;
	static final int BODY = 1;
	

	/** ���νи����ǽ���󥯤ξ���true���֤� */
	boolean isFunctorRef() { 
		return atom.functor.getArity() == pos;
	}
	/** ��ͳ��󥯤��Ĥ��� */
	void terminate(Membrane mem) {
		atom = new Atom(null, "*", 1); //todo:�롼����
		pos = 1;
		atom.args[0] = this;
	}
}
/** ��������������Υ롼��ι�¤��ɽ�����饹 */
final class RuleStructure {
	Membrane leftMem, rightMem;
}

/** ProcessContext��RuleContext�οƤȤʤ���ݥ��饹 */
abstract class Context {
	protected String name;
	protected Context(String name) {
		this.name = name;
	}
	String getName() {
		return name;
	}
	/** ���դǤν�°�� */
	Membrane lhsMem;
	/** ���դǤν�°������� */
	List rhsMems;
	/** ���ߤξ��֡�ST_�ǻϤޤ�����Τ����줫���ͤ�Ȥ� */
	int status = ST_FRESH;
	/** ������� */
	static final int ST_FRESH = 0;
	/** ���դ˰��ٽи��������� */
	static final int ST_LHSOK = 1;
	/** ���ա�����ξ���˽и��������� */
	static final int ST_READY = 2;
	static final int ST_ERROR = 3;
}
/**
 * ��������������Υץ���ʸ̮�ι�¤��ɽ�����饹
 * <br>TODO ���դ��ץ���ʸ̮�ΰ����ϡ�
 */
final class ProcessContext extends Context{
	/**
	 * �����Υ��«
	 * <br>TODO ���󥹥ȥ饯�������ꤹ��Τ����᥽�åɤ���Τ�</p>
	 */
	private LinkOccurrence[] arg;
	/**
	 * �����Υ��«
	 * <bf>
	 * TODO ���󥹥ȥ饯�������ꤹ��Τ����᥽�åɤ���Τ�<br>
	 * TODO ���ѤΥ��饹���롩
	 */
	private LinkOccurrence bundle;
	ProcessContext(String name) {
		super(name);
	}
}
/** ��������������Υ롼��ʸ̮�ι�¤��ɽ�����饹 */
final class RuleContext extends Context{
	RuleContext(String name) {
		super(name);
	}
}

/** �ʤ������줬̵����javadoc������Ǥ��ʤ� */
class DataStructure {}
