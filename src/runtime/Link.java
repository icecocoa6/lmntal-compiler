package runtime;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Set;
import java.util.Map;
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
public final class Link implements Cloneable, Serializable {
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
	public int getPos() {
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
	
	/** 
	 * by kudo
	 * �Ƶ�Ū�˴����ץ������ɤ����򸡺����롣
	 * ������é�äƽв�ä���srcSet��̤��Ͽ�Υ��ȥ�ο����֤���
	 * ����������ͳ��󥯴������ȥ�˽в�ä����ϡ�-1���֤���
	 * @param srcSet �����ץ����������륢�ȥ��Set
	 * @param avoSet �����ץ����˽ФƤ��ƤϤ����ʤ����ȥ��Set
	 * @return �����ץ����������륢�ȥ��
	 */
	public int isGround(Set srcSet,Set avoSet){
		if(srcSet.contains(atom))return 0; //�����������륢�ȥ��é��Ĥ�����
		if(avoSet.contains(atom))return -1; //�򤱤�٤����ȥ��é���夤����
		if(atom.getFunctor().equals(Functor.INSIDE_PROXY)||
			atom.getFunctor().equals(Functor.OUTSIDE_PROXY))
			return -1; //��ͳ��󥯴������ȥ�˽в�ä��鼺��
		srcSet.add(atom); // ��ʬ���ɲ�
		int ac=1; //�������륢�ȥ��:�ޤ���ʬ��ޤ�
		for(int i=0;i<atom.getArity();i++){
			if(i==pos)continue; // �褷����é��ʤ�
			int gr=atom.getArg(i).isGround(srcSet,avoSet);
			if(gr == -1)return -1; //�ɤä���$in,$out�˽в�ä��鼺��
			else ac+=gr; //�ƥ����˿��������Ф��줿���ȥ�����פ���
		}
		return ac;
	}
	
	/**
	 * by kudo
	 * �Ƶ�Ū��Ʊ����¤����ä������ץ������ɤ�����������
	 * �ɤ��餫�����ˤĤ���ground���ɤ����θ����ϺѤ�Ǥ����ΤȤ���
	 * @param srcLink ����оݤΥ��
	 * @param srcMap ��Ӹ����ȥफ������襢�ȥ�ؤ�map
	 * @return
	 */
	public boolean eqGround(Link srcLink,Map srcMap){
		if(srcLink.getPos() != pos)return false; // ��������פ�?
		if(!srcLink.getAtom().getFunctor().equals(atom.getFunctor()))return false;
		if(!srcMap.containsKey(atom))srcMap.put(atom,srcLink.getAtom());
		else if(srcMap.get(atom) != srcLink.getAtom())return false;
		else return true;
		boolean flgequal = true;
		for(int i=0;i<atom.getArity();i++){
			if(i==pos)continue;
			flgequal &= atom.getArg(i).eqGround(srcLink.getAtom().getArg(i),srcMap);
			if(!flgequal)return false;
		}
		return flgequal;
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

	/**
	 * ľ���������˸ƤФ�롣
	 * @param in �ɤ߹��ॹ�ȥ꡼��
	 * @throws IOException �����ϥ��顼��ȯ���������
	 * @throws ClassNotFoundException ľ�󲽤��줿���֥������ȤΥ��饹�����Ĥ���ʤ��ä����
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		id = lastId++;
	}
}
