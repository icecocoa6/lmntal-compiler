package runtime;

//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;

//import util.QueuedEntity;
//import java.lang.Integer;
//import util.Stack;

/**
 * ��󥯤���³��򡢥��ȥ�Ȱ����ֹ���ȤȤ���ɽ����LMNtal�Υ�󥯤ˤ�������̵���Τǡ�
 * ���ĤΥ�󥯤��Ф��Ƥ��Υ��饹�Υ��󥹥��󥹤򣲤Ļ��Ѥ��롣
 */
public final class Link implements Cloneable {
	/** �����Υ��ȥ� */
	private Atom atom;
	/** ����褬�貿������ */
	private int pos;

	private static int lastId = 0;
	private int id;
	
	static void gc() {
		lastId = 0;
	}
	
	///////////////////////////////
	// ���󥹥ȥ饯��
	
	public Link(Atom atom, int pos) {
		set(atom, pos);
		id = lastId++;
	}

	public Object clone() {
		return new Link(atom, pos);
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
	public Atom getAtom() {
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

