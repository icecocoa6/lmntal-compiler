package runtime;

//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;

import util.QueuedEntity;
import java.lang.Integer;
//import util.Stack;

/**
 * ���ȥ९�饹�������롦��⡼�Ȥ˴ؤ�餺���Υ��饹�Υ��󥹥��󥹤���Ѥ��롣
 * @author Mizuno
 */
public final class Atom extends QueuedEntity {
	/** ��°�졣Membrane���饹��addAtom�᥽�å���ǹ������롣 */
	private AbstractMembrane mem;
	/** ̾���ȥ�󥯿� */
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
	 * @param mem ��°��
	 */
	Atom(AbstractMembrane mem, Functor functor) {
		this.mem = mem;
		this.functor = functor;
		args = new Link[functor.getArity()];
		id = lastId++;
	}

	///////////////////////////////
	// ���
	void changeFunctor(Functor newFunctor) {
		if (functor.getArity() != newFunctor.getArity()) {
			// TODO SystemError�Ѥ��㳰���饹���ꤲ��
			throw new RuntimeException("SYSTEM ERROR: cannot change arity");
		}
		functor = newFunctor;
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
	/** ���Υ��ȥ�Υ�����ID��������� */
	String getLocalID() {
		return Integer.toString(id);
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
