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
	/** ��°�졣AbstractMembrane�Ȥ��Υ��֥��饹����ͳ���ѹ����Ƥ褤��null�����뤳�Ȥ⤢�롣*/
	AbstractMembrane mem;
	/** �ե��󥯥���̾���ȥ�󥯿��� */
	private Functor functor;
	/** ��� */
	public Link[] args;
	
	/** ��°���AtomSet��ǤΥ���ǥå��� */
	int index = -1;

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
	void setFunctor(Functor newFunctor) {
		if (newFunctor.getArity() > args.length) {
			// TODO SystemError�Ѥ��㳰���饹���ꤲ��
			throw new RuntimeException("SYSTEM ERROR: insufficient link vector length");
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

	/** �ե��󥯥������ */
	public Functor getFunctor(){
		return functor;
	}
	/** ̾������� */
	public String getName() {
		return functor.getName();
	}
	/** Ŭ�ڤ˾�ά���줿̾������� */
	public String getAbbrName() {
		return functor.getAbbrName();
	}
	/** ��󥯿������ */
	int getArity() {
		return functor.getArity();
	}
	/** �ǽ���������� */
	Link getLastArg() {
		return args[getArity() - 1];
	}
	/** ��pos�����˳�Ǽ���줿��󥯥��֥������Ȥ���� */
	public Link getArg(int pos) {
		return args[pos];
	}
	/** ��°��μ��� */
	public AbstractMembrane getMem() {
		return mem;
	}
	/** �� n �����ˤĤʤ��äƤ륢�ȥ�Υե��󥯥�̾���֤� */
	public String nth(int n) {
		return args[n].getAtom().getFunctor().getName();
	}
	/** �ե��󥯥�̾���Ѥ��� */
	public void changeName(String name) {
		setFunctor( new Functor(name, getFunctor().getArity()) );
	}
//	/** ��°������ꤹ�롣AbstractMembrane�Ȥ��Υ��֥��饹�Τ߸ƤӽФ��Ƥ褤��*/
//	void setMem(AbstractMembrane mem) {
//		this.mem = mem;
//	}
//	/**@deprecated*/	
//	void remove() {
//		mem.removeAtom(this);
//		mem = null;
//	}
//	/** �����å������äƤ���н���� */
//	public void dequeue() {
//		super.dequeue();
//	}
}
