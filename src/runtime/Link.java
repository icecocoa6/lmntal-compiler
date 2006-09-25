package runtime;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
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
		if (Env.verbose > Env.VERBOSE_SIMPLELINK)
			return "_" + i;
		else
			return "L" + i;
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
	public Link getBuddy() {
		return atom.args[pos];
	}
	/** ����褬�ǽ���󥯤ξ���true���֤� */
	boolean isFuncRef() {
		return atom.getArity() - 1 == pos;
	}
	
	/** 
	 * �����ץ������ɤ����򸡺����롥(Stack��Ȥ��褦�˽��� 2005/07/26)
	 * (�����ȼ�������˼�����äƤ���Set���ѻ�)
	 * �����ץ����������륢�ȥ�ο����֤���
	 * �����ˤϡ�(���սи����ȥ���)�����ץ����˴ޤޤ�ƤϤ����ʤ����ȥ��Set����ꤹ�롥
	 * ����������ͳ��󥯴������ȥ�˽в�ä����ϡ�-1���֤���
	 * @param avoSet �����ץ����˽ФƤ��ƤϤ����ʤ����ȥ��Set
	 * @return �����ץ����������륢�ȥ��
	 */
//	public int isGround(Set avoSet){
//		Set srcSet = new HashSet();
//		Stack s = new Stack(); //��󥯤��Ѥॹ���å�
//		s.push(this);
//		int c=0;
//		while(!s.isEmpty()){
//			Link l = (Link)s.pop();
//			Atom a = l.getAtom();
//			if(srcSet.contains(a))continue; //����é�ä����ȥ�
//			if(avoSet.contains(a))return -1; //�и����ƤϤ����ʤ����ȥ�
//			if(a.getFunctor().equals(Functor.INSIDE_PROXY)||
//				a.getFunctor().isOutsideProxy()) //�ץ����˻�äƤϤ����ʤ�
//				return -1;
//			c++;
//			srcSet.add(a);
//			for(int i=0;i<a.getArity();i++){
//				if(i==l.getPos())continue;
//				s.push(a.getArg(i));
//			}
//		}
//		return c;
//	}
	public int isGround(Set avoSet){
		List srclinks = new ArrayList();
		srclinks.add(this);
		return Membrane.isGround(srclinks,avoSet);
	}

	/**
	 * Ʊ����¤����ä������ץ������ɤ�����������(Stack��Ȥ��褦�˽��� 2005/07/27)
	 * ( �����ȼ�������˼�����äƤ���Map���ѻ�)
	 * �ɤ��餫�����ˤĤ���ground���ɤ����θ����ϺѤ�Ǥ����ΤȤ���
	 * @param srcLink ����оݤΥ��
	 * @return
	 */
//	public boolean eqGround(Link srcLink){//,Map srcMap){
//		Map map = new HashMap(); //��Ӹ����ȥफ������襢�ȥ�ؤΥޥå�
//		Stack s1 = new Stack();  //��Ӹ���󥯤�����륹���å�
//		Stack s2 = new Stack();  //������󥯤�����륹���å�
//		s1.push(this);
//		s2.push(srcLink);
//		while(!s1.isEmpty()){
//			Link l1 = (Link)s1.pop();
//			Link l2 = (Link)s2.pop();
//			if(l1.getPos() != l2.getPos())return false; //�������֤ΰ��פ򸡺�
//			if(!l1.getAtom().getFunctor().equals(l2.getAtom().getFunctor()))return false; //�ե��󥯥��ΰ��פ򸡺�
//			if(!map.containsKey(l1.getAtom()))map.put(l1.getAtom(),l2.getAtom()); //̤��
//			else if(map.get(l1.getAtom()) != l2.getAtom())return false;         //���Фʤ���԰���
//			else continue;
//			for(int i=0;i<l1.getAtom().getArity();i++){
//				if(i==l1.getPos())continue;
//				s1.push(l1.getAtom().getArg(i));
//				s2.push(l2.getAtom().getArg(i));
//			}
//		}
//		return true;
//	}
	public boolean eqround(Link srcLink){
		List srclinks = new ArrayList();
		List dstlinks = new ArrayList();
		srclinks.add(srcLink);
		dstlinks.add(this);
		return Membrane.eqGround(srclinks,dstlinks);
	}
	/**
	 * ground��¤���Ф��ư�դ�ʸ������֤���
	 * ground �����å��ѤߤǤʤ���Фʤ�ʤ���
	 * hara
	 * @return
	 */
	public String groundString(){
		Set srcSet = new HashSet();
		Stack s = new Stack(); //��󥯤��Ѥॹ���å�
		HashMap linkStr = new HashMap();
		StringBuffer sb = new StringBuffer();
		int linkNo=0;
		s.push(this);
		while(!s.isEmpty()){
			Link l = (Link)s.pop();
			Atom a = l.getAtom();
			if(srcSet.contains(a))continue; //����é�ä����ȥ�
			sb.append(a.getFunctor().getName());
			sb.append("(");
			srcSet.add(a);
			for(int i=0;i<a.getArity();i++){
				Link l0 = a.args[i];
				Link l1 = l0.getBuddy();
				if(!linkStr.containsKey(l0)) {
					String ss="L"+(linkNo++);
					linkStr.put(l0, ss);
					linkStr.put(l1, ss);
				}
				sb.append(linkStr.get(l0));
				if(i==l.getPos())continue;
				s.push(a.getArg(i));
			}
			sb.append(")");
		}
		return sb.toString();
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
