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
	/** ��°�졣AbstractMembrane�Ȥ��Υ��֥��饹���ѹ����Ƥ褤��
	 * �������ͤ��ѹ�����Ȥ���index��Ʊ���˹������뤳�ȡ�(null,-1)�Ͻ�°��ʤ���ɽ����*/
	AbstractMembrane mem;
	/** ��°���AtomSet��ǤΥ���ǥå��� */
	int index = -1;

	/** �ե��󥯥���̾���ȥ�󥯿��� */
	private Functor functor;
	/** ��� */
	Link[] args;

	private static int lastId = 0;
	/** ���Υ��ȥ�Υ�����ID */
	private int id;
	
	static void gc() {
		lastId = 0;
	}
	
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
	public void setFunctor(Functor newFunctor) {
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

	/** ��°��μ��� */
	public AbstractMembrane getMem() {
		return mem;
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
	/** �� n �����ˤĤʤ��äƤ륢�ȥ��̾����������� */
	public String nth(int n) {
		return nthAtom(n).getFunctor().getName();
	}
	/** �� n �����ˤĤʤ��äƤ륢�ȥ��������� */
	public Atom nthAtom(int n) {
		return args[n].getAtom();
	}
	/** �ե��󥯥�̾���Ѥ��롣��todo �Ѥ��ʤ����⤷��ʤ��Τ�setName����������
	 * ��°�줬��⡼�Ȥξ��⤢�ꡢ������AtomSet��ɬ���������ʤ���Фʤ�ʤ��Τǡ�
	 * ���alterAtomFunctor�᥽�åɤ�Ƥ֡�*/
	public void changeName(String name) {
		mem.alterAtomFunctor(this, new Functor(name, getFunctor().getArity()));
	}
	/** ���� TODO ��󥯤⤱���ʤ��ξ�硢�᥽�å�̾���Ѥ��Ʋ�������
	 * ����쥯�饹�˥᥽�åɤ��äƸƤ֤褦�ˤ��뤫���ޤ��ϡ�
	 * ���Υ᥽�åɤ�������쥯�饹�Υ᥽�åɤ�Ƥ֡��Ȥꤢ���������̤�θ�ԤǤ褤��*/
	public void remove() {
		mem.removeAtom(this);
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
